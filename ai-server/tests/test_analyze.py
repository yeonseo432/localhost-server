import json
from unittest.mock import patch

import pytest
from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health():
    resp = client.get("/health")
    assert resp.status_code == 200
    assert resp.json() == {"status": "ok"}


def test_receipt_missing_config_field():
    """Should return 422 if targetProductKey is missing from config."""
    resp = client.post(
        "/analyze/receipt",
        files={"image": ("test.jpg", b"fake-image-data", "image/jpeg")},
        data={"config": json.dumps({"confidenceThreshold": 0.8})},
    )
    assert resp.status_code == 422
    assert "targetProductKey" in resp.json()["detail"]


def test_receipt_invalid_config_json():
    """Should return 422 if config is not valid JSON."""
    resp = client.post(
        "/analyze/receipt",
        files={"image": ("test.jpg", b"fake-image-data", "image/jpeg")},
        data={"config": "not-json"},
    )
    assert resp.status_code == 422


def test_inventory_missing_config_field():
    """Should return 422 if answerImageUrl is missing from config."""
    resp = client.post(
        "/analyze/inventory",
        files={"image": ("test.jpg", b"fake-image-data", "image/jpeg")},
        data={"config": json.dumps({"confidenceThreshold": 0.75})},
    )
    assert resp.status_code == 422
    assert "answerImageUrl" in resp.json()["detail"]


def test_inventory_invalid_config_json():
    """Should return 422 if config is not valid JSON."""
    resp = client.post(
        "/analyze/inventory",
        files={"image": ("test.jpg", b"fake-image-data", "image/jpeg")},
        data={"config": "bad-json"},
    )
    assert resp.status_code == 422


@patch("app.routers.analyze.llm.analyze_receipt")
def test_receipt_success(mock_llm):
    """Should return LLM Vision result for receipt analysis."""
    mock_llm.return_value = {
        "match": True,
        "confidence": 0.95,
        "retryHint": None,
        "rawJson": '{"match": true, "confidence": 0.95, "retryHint": null, "reason": "found"}',
    }
    resp = client.post(
        "/analyze/receipt",
        files={"image": ("receipt.jpg", b"fake-image-data", "image/jpeg")},
        data={"config": json.dumps({"targetProductKey": "아메리카노", "confidenceThreshold": 0.8})},
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["match"] is True
    assert body["confidence"] == 0.95
    assert body["retryHint"] is None
    assert "rawJson" in body
    mock_llm.assert_called_once_with(b"fake-image-data", "아메리카노", 0.8)


@patch("app.routers.analyze.llm.analyze_receipt")
def test_receipt_unreadable_image(mock_llm):
    """Should return retryHint when LLM cannot read the receipt image."""
    mock_llm.return_value = {
        "match": False,
        "confidence": 0.0,
        "retryHint": "영수증이 너무 흐릿합니다. 더 선명한 사진을 촬영해주세요.",
        "rawJson": '{"match": false, "confidence": 0.0, "retryHint": "영수증이 너무 흐릿합니다. 더 선명한 사진을 촬영해주세요.", "reason": "image too blurry"}',
    }
    resp = client.post(
        "/analyze/receipt",
        files={"image": ("blurry.jpg", b"blurry-image-data", "image/jpeg")},
        data={"config": json.dumps({"targetProductKey": "돌체 라떼", "confidenceThreshold": 0.8})},
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["match"] is False
    assert body["confidence"] == 0.0
    assert body["retryHint"] is not None


@patch("app.routers.analyze.llm.compare_inventory")
def test_inventory_success(mock_llm):
    """Should return LLM result for inventory comparison."""
    mock_llm.return_value = {
        "match": True,
        "confidence": 0.88,
        "retryHint": None,
        "rawJson": '{"match": true, "confidence": 0.88, "retryHint": null, "reason": "same product"}',
    }
    resp = client.post(
        "/analyze/inventory",
        files={"image": ("photo.jpg", b"fake-image-data", "image/jpeg")},
        data={"config": json.dumps({"answerImageUrl": "https://example.com/ref.jpg", "confidenceThreshold": 0.75})},
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["match"] is True
    assert body["confidence"] == 0.88
