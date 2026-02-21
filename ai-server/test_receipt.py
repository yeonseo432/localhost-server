"""
CLI script to test receipt LLM Vision product matching.

Usage:
    python test_receipt.py <image_path> <target_product>

Example:
    python test_receipt.py test_images/receipt.jpg "코카콜라"
"""

import sys
import logging

logging.basicConfig(level=logging.INFO, format="%(levelname)s | %(message)s")
logger = logging.getLogger(__name__)


def main():
    if len(sys.argv) < 3:
        print("Usage: python test_receipt.py <image_path> <target_product>")
        print('Example: python test_receipt.py test_images/receipt.jpg "코카콜라"')
        sys.exit(1)

    image_path = sys.argv[1]
    target_product = sys.argv[2]
    confidence_threshold = float(sys.argv[3]) if len(sys.argv) > 3 else 0.8

    # Read image
    try:
        with open(image_path, "rb") as f:
            image_bytes = f.read()
        logger.info("Image loaded: %s (%d bytes)", image_path, len(image_bytes))
    except FileNotFoundError:
        print(f"ERROR: File not found: {image_path}")
        sys.exit(1)

    # LLM Vision analysis
    from app.services.llm import analyze_receipt

    logger.info("Calling LLM Vision (target: '%s', threshold: %.2f)...", target_product, confidence_threshold)
    result = analyze_receipt(image_bytes, target_product, confidence_threshold)

    # Output
    print("\n===== RESULT =====")
    print(f"match: {result['match']}")
    print(f"confidence: {result['confidence']}")
    if result.get("retryHint"):
        print(f"retryHint: {result['retryHint']}")
    print(f"rawJson: {result['rawJson']}")


if __name__ == "__main__":
    main()
