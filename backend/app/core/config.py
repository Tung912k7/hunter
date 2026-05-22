from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    DATABASE_URL: str = "postgresql://postgres:postgres@localhost:5432/hackathon_hunter"
    ENVIRONMENT: str = "development"
    REDIS_URL: str = "redis://localhost:6379/0"
    FIREBASE_CREDENTIALS_PATH: Optional[str] = "secrets/firebase-credentials.json"

    class Config:
        env_file = ".env"
        case_sensitive = True

settings = Settings()
