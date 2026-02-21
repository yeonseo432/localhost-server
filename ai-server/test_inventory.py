"""
CLI script to test inventory image comparison via LLM Vision.

Usage:
    python test_inventory.py <test_image> <answer_image> [threshold]

Example:
    python test_inventory.py test_images/inventory/cola_test_1.jpg test_images/inventory/cola_answer.jpg
"""

import base64
import mimetypes
import sys
import logging

logging.basicConfig(level=logging.INFO, format="%(levelname)s | %(message)s")
logger = logging.getLogger(__name__)


def _image_to_data_url(path: str) -> str:
    """Convert a local image file to a data:image/xxx;base64,... URL."""
    mime, _ = mimetypes.guess_type(path)
    if mime is None:
        mime = "image/jpeg"
    with open(path, "rb") as f:
        b64 = base64.b64encode(f.read()).decode("utf-8")
    return f"data:{mime};base64,{b64}"


def main():
    if len(sys.argv) < 3:
        print("Usage: python test_inventory.py <test_image> <answer_image> [threshold]")
        print("Example: python test_inventory.py test_images/inventory/cola_test_1.jpg test_images/inventory/cola_answer.jpg")
        sys.exit(1)

    test_image_path = sys.argv[1]
    answer_image_path = sys.argv[2]
    confidence_threshold = float(sys.argv[3]) if len(sys.argv) > 3 else 0.8

    # Read test image as bytes
    try:
        with open(test_image_path, "rb") as f:
            test_image_bytes = f.read()
        logger.info("Test image loaded: %s (%d bytes)", test_image_path, len(test_image_bytes))
    except FileNotFoundError:
        print(f"ERROR: File not found: {test_image_path}")
        sys.exit(1)

    # Convert answer image to data URL
    try:
        answer_data_url = _image_to_data_url(answer_image_path)
        logger.info("Answer image loaded: %s", answer_image_path)
    except FileNotFoundError:
        print(f"ERROR: File not found: {answer_image_path}")
        sys.exit(1)

    # LLM Vision comparison
    from app.services.llm import compare_inventory

    logger.info(
        "Calling LLM Vision (test: '%s', answer: '%s', threshold: %.2f)...",
        test_image_path, answer_image_path, confidence_threshold,
    )
    result = compare_inventory(test_image_bytes, answer_data_url, confidence_threshold)

    # Output
    print("\n===== RESULT =====")
    print(f"match: {result['match']}")
    print(f"confidence: {result['confidence']}")
    if result.get("retryHint"):
        print(f"retryHint: {result['retryHint']}")
    print(f"rawJson: {result['rawJson']}")


if __name__ == "__main__":
    main()
