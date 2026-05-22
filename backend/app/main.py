from fastapi import FastAPI
from app.api.v1.api import api_router
from app.core.database import Base, engine
from app.core.config import settings

# Automatically create tables in database (suitable for development and simplified deployments)
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Hackathon Hunter API",
    description="Backend API for crawling, filtering, and reporting hackathons.",
    version="1.0.0"
)

app.include_router(api_router, prefix="/api/v1")

@app.get("/")
def read_root():
    return {
        "status": "online",
        "project": "Hackathon Hunter API",
        "docs": "/docs"
    }
