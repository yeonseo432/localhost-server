import io
import logging

import easyocr

logger = logging.getLogger(__name__)

_reader: easyocr.Reader | None = None


def _get_reader() -> easyocr.Reader:
    global _reader
    if _reader is None:
        logger.info("Initializing EasyOCR reader (ko, en)...")
        _reader = easyocr.Reader(["ko", "en"], gpu=False)
        logger.info("EasyOCR reader ready.")
    return _reader


def extract_text(image_bytes: bytes) -> str:
    """Extract Korean/English text from an image using EasyOCR."""
    reader = _get_reader()
    results = reader.readtext(io.BytesIO(image_bytes).read())
    lines = [text for (_, text, _) in results]
    return "\n".join(lines)
