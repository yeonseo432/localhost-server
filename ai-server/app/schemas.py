from pydantic import BaseModel, Field


class AnalyzeResponse(BaseModel):
    """Response schema matching Spring Boot's AiJudgmentResult (camelCase)."""

    match: bool = Field(description="Whether the target was found/matched")
    confidence: float = Field(description="Confidence score 0.0-1.0")
    retry_hint: str | None = Field(
        default=None,
        alias="retryHint",
        description="Hint for the user if match failed",
    )
    raw_json: str = Field(
        alias="rawJson",
        description="Raw JSON response from the AI model",
    )

    model_config = {"populate_by_name": True, "by_alias": True}


class HealthResponse(BaseModel):
    status: str = "ok"
