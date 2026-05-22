from fastapi import APIRouter
from app.api.v1.endpoints import hackathons

api_router = APIRouter()
api_router.include_router(hackathons.router, prefix="/hackathons", tags=["hackathons"])
