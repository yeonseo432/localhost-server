from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    inventory_api_url: str = ""
    inventory_api_key: str = ""
    inventory_model: str = "gemini-2.5-flash"

    model_config = {"env_file": ".env", "extra": "ignore"}


settings = Settings()
