import json
import logging

from fastapi import APIRouter, File, Form, HTTPException, UploadFile

from app.schemas import AnalyzeResponse
from app.services import llm

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/analyze", tags=["analyze"])


@router.post("/receipt", response_model=AnalyzeResponse)
async def analyze_receipt(
    image: UploadFile = File(..., description="Receipt image"),
    config: str = Form(..., description='JSON config: {"targetProductKey":"...","confidenceThreshold":0.8}'),
) -> AnalyzeResponse:
    """M3: Analyze a receipt image to check if the target product was purchased."""
    try:
        cfg = json.loads(config)
    except json.JSONDecodeError:
        raise HTTPException(status_code=422, detail="config must be valid JSON")

    target_product_key = cfg.get("targetProductKey")
    confidence_threshold = cfg.get("confidenceThreshold", 0.7)

    if not target_product_key:
        raise HTTPException(status_code=422, detail="targetProductKey is required in config")

    image_bytes = await image.read()
    content_type = image.content_type or "image/jpeg"

    result = llm.analyze_receipt(image_bytes, content_type, target_product_key, confidence_threshold)
    return AnalyzeResponse(**result)


@router.post("/inventory", response_model=AnalyzeResponse)
async def analyze_inventory(
    image: UploadFile = File(..., description="User's photo of the product"),
    config: str = Form(..., description='JSON config: {"answerImageUrl":"...","confidenceThreshold":0.75}'),
) -> AnalyzeResponse:
    """M4: Compare a user's inventory photo with the reference image."""
    try:
        cfg = json.loads(config)
    except json.JSONDecodeError:
        raise HTTPException(status_code=422, detail="config must be valid JSON")

    answer_image_url = cfg.get("answerImageUrl")
    confidence_threshold = cfg.get("confidenceThreshold", 0.7)

    if not answer_image_url:
        raise HTTPException(status_code=422, detail="answerImageUrl is required in config")

    image_bytes = await image.read()
    content_type = image.content_type or "image/jpeg"

    result = llm.compare_inventory(image_bytes, content_type, answer_image_url, confidence_threshold)
    return AnalyzeResponse(**result)
