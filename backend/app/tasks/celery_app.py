from celery import Celery
from app.core.config import settings

# Initialize Celery using settings.REDIS_URL for broker and backend
celery_app = Celery(
    "hackathon_hunter_tasks",
    broker=settings.REDIS_URL,
    backend=settings.REDIS_URL
)

celery_app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="UTC",
    enable_utc=True,
)

# Define tasks to discover
celery_app.autodiscover_tasks(["app.tasks"])

# Set celery beat schedule to run the scrapers task every 6 hours (21600 seconds)
celery_app.conf.beat_schedule = {
    "run-scrapers-every-6-hours": {
        "task": "app.tasks.crawler.run_scrapers_task",
        "schedule": 21600.0,
    },
}
