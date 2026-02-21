import logging

from fastapi import FastAPI

from app.routers.analyze import router as analyze_router
from app.schemas import HealthResponse

logging.basicConfig(level=logging.INFO)

app = FastAPI(
    title="AI Judgment Server",
    description="Mission M3 (Receipt Vision) and M4 (Inventory Comparison) AI analysis",
    version="0.1.0",
)

app.include_router(analyze_router)


@app.get("/health", response_model=HealthResponse)
async def health() -> HealthResponse:
    return HealthResponse()
