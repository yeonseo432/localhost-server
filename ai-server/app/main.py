import logging
import functools

from fastapi import FastAPI
from starlette.formparsers import MultiPartParser

from app.routers.analyze import router as analyze_router
from app.schemas import HealthResponse

# Monkey-patch MultiPartParser.__init__ to increase max_part_size default to 10MB
_original_init = MultiPartParser.__init__

@functools.wraps(_original_init)
def _patched_init(self, headers, stream, *, max_files=1000, max_fields=1000, max_part_size=10 * 1024 * 1024):
    _original_init(self, headers, stream, max_files=max_files, max_fields=max_fields, max_part_size=max_part_size)

MultiPartParser.__init__ = _patched_init

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
