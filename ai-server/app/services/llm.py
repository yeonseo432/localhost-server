import base64
import json
import logging

import httpx

from app.config import settings

logger = logging.getLogger(__name__)

_RECEIPT_SYSTEM_PROMPT = (
    "You are a receipt image analysis assistant. "
    "You will be given an image of a receipt. Read the receipt directly from the image "
    "and determine whether the target product name appears in it. "
    "IMPORTANT RULES:\n"
    "- Read every line of the receipt image carefully.\n"
    "- Match by CHARACTER SHAPE SIMILARITY ONLY. Do NOT consider semantic meaning or product categories.\n"
    "- For example, '돌체라떼' and '돌채라떼' are similar (printing artifacts), but '바리스타' and '돌체라떼' are NOT similar even though both are coffee-related.\n"
    "- NEVER increase confidence based on semantic similarity (same category, related meaning, etc.).\n"
    "- Only match when the actual characters closely resemble the target product name.\n"
    "- If the image is too blurry or unreadable, set match to false and provide a helpful retryHint.\n"
    "Respond ONLY with a JSON object: "
    '{"match": true/false, "confidence": 0.0-1.0, "retryHint": "string or null", "reason": "brief explanation"}'
)

_INVENTORY_SYSTEM_PROMPT = (
    "You are an inventory verification assistant. "
    "Compare the user's photo (first image) with the reference product image (second image) "
    "and determine if they show the SAME product.\n"
    "IMPORTANT RULES:\n"
    "- Judge by PRODUCT IDENTITY: same brand, same product name, same packaging design.\n"
    "- Different flavors, sizes, or variants of the same brand are DIFFERENT products (e.g., Coca-Cola Original vs Coca-Cola Zero are different).\n"
    "- IGNORE differences caused by shooting angle, lighting, background, or image quality.\n"
    "- If the user's photo is too blurry, too dark, or the product is not clearly visible, set match to false and provide a helpful retryHint IN KOREAN.\n"
    "- The retryHint must always be in Korean (e.g., '제품이 잘 보이도록 다시 촬영해주세요.').\n"
    "Respond ONLY with a JSON object: "
    '{"match": true/false, "confidence": 0.0-1.0, "retryHint": "string or null", "reason": "brief explanation"}'
)


def _call_elice_api(
    api_url: str,
    api_key: str,
    model: str,
    messages: list[dict],
) -> dict:
    """Call the Elice Cloud ML API (OpenAI-compatible)."""
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
    }
    payload = {
        "model": model,
        "messages": messages,
        "max_completion_tokens": 4096,
    }
    with httpx.Client(timeout=60.0) as client:
        resp = client.post(
            f"{api_url}/v1/chat/completions",
            headers=headers,
            json=payload,
        )
        resp.raise_for_status()
        return resp.json()


def _parse_ai_json(raw_content: str) -> dict:
    """Extract JSON from the AI response, stripping markdown fences if present."""
    text = raw_content.strip()
    if text.startswith("```"):
        lines = text.split("\n")
        # Remove first line (```json) and last line (```)
        lines = [l for l in lines[1:] if not l.strip().startswith("```")]
        text = "\n".join(lines)
    return json.loads(text)


def analyze_receipt(image_bytes: bytes, target_product_key: str, confidence_threshold: float) -> dict:
    """Use Gemini 2.5 Pro Vision to determine if the target product appears in a receipt image."""
    b64_image = base64.b64encode(image_bytes).decode("utf-8")

    user_content = [
        {
            "type": "text",
            "text": (
                f"Read the receipt in this image and determine if it contains a purchase of the target product.\n"
                f"Target product: {target_product_key}\n"
                f"Confidence threshold: {confidence_threshold}"
            ),
        },
        {
            "type": "image_url",
            "image_url": {"url": f"data:image/jpeg;base64,{b64_image}"},
        },
    ]

    messages = [
        {"role": "system", "content": _RECEIPT_SYSTEM_PROMPT},
        {"role": "user", "content": user_content},
    ]

    api_response = _call_elice_api(settings.inventory_api_url, settings.inventory_api_key, settings.inventory_model, messages)
    raw_content = api_response["choices"][0]["message"]["content"]

    try:
        parsed = _parse_ai_json(raw_content)
    except (json.JSONDecodeError, KeyError, IndexError):
        logger.warning("Failed to parse AI response: %s", raw_content)
        parsed = {"match": False, "confidence": 0.0, "retryHint": "AI 응답을 파싱할 수 없습니다. 다시 시도해주세요."}

    return {
        "match": parsed.get("match", False),
        "confidence": parsed.get("confidence", 0.0),
        "retryHint": parsed.get("retryHint"),
        "rawJson": json.dumps(parsed, ensure_ascii=False),
    }


def _download_image_as_base64(url: str) -> str:
    """Download an image from a URL and return it as a base64-encoded data URI."""
    with httpx.Client(timeout=30.0) as client:
        resp = client.get(url)
        resp.raise_for_status()
    content_type = resp.headers.get("content-type", "image/jpeg").split(";")[0].strip()
    b64 = base64.b64encode(resp.content).decode("utf-8")
    return f"data:{content_type};base64,{b64}"


def compare_inventory(
    image_bytes: bytes,
    answer_image_url: str,
    confidence_threshold: float,
) -> dict:
    """Use Gemini 2.5 Pro (Vision) to compare user photo with reference image."""
    b64_image = base64.b64encode(image_bytes).decode("utf-8")
    answer_b64_uri = _download_image_as_base64(answer_image_url)

    user_content = [
        {
            "type": "text",
            "text": (
                "Compare these two images. The first is the user's photo, "
                "the second is the reference product image. "
                f"Confidence threshold: {confidence_threshold}\n"
                "Are they showing the same product?"
            ),
        },
        {
            "type": "image_url",
            "image_url": {"url": f"data:image/jpeg;base64,{b64_image}"},
        },
        {
            "type": "image_url",
            "image_url": {"url": answer_b64_uri},
        },
    ]

    messages = [
        {"role": "system", "content": _INVENTORY_SYSTEM_PROMPT},
        {"role": "user", "content": user_content},
    ]

    api_response = _call_elice_api(settings.inventory_api_url, settings.inventory_api_key, settings.inventory_model, messages)
    raw_content = api_response["choices"][0]["message"]["content"]

    try:
        parsed = _parse_ai_json(raw_content)
    except (json.JSONDecodeError, KeyError, IndexError):
        logger.warning("Failed to parse AI response: %s", raw_content)
        parsed = {"match": False, "confidence": 0.0, "retryHint": "AI 응답을 파싱할 수 없습니다. 다시 시도해주세요."}

    return {
        "match": parsed.get("match", False),
        "confidence": parsed.get("confidence", 0.0),
        "retryHint": parsed.get("retryHint"),
        "rawJson": json.dumps(parsed, ensure_ascii=False),
    }
