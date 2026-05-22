import asyncio
import logging
from app.tasks.celery_app import celery_app
from app.core.database import SessionLocal
from app.services.hackathon_service import create_hackathon_and_notify

# Import Scrapers
from app.scraper.devpost import DevpostScraper
from app.scraper.devfolio import DevfolioScraper
from app.scraper.hackerearth import HackerEarthScraper
from app.scraper.gitcoin import GitcoinClient
from app.scraper.dorahacks import DoraHacksScraper
from app.scraper.bewater import BeWaterScraper

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

async def run_scrapers_async():
    scrapers = [
        DevpostScraper(),
        DevfolioScraper(),
        HackerEarthScraper(),
        GitcoinClient(),
        DoraHacksScraper(),
        BeWaterScraper()
    ]
    
    db = SessionLocal()
    try:
        for scraper in scrapers:
            try:
                logger.info(f"[Celery] Starting scraper: {scraper.__class__.__name__}")
                events = await scraper.scrape_events()
                logger.info(f"[Celery] Scraper {scraper.__class__.__name__} found {len(events)} events.")
                for event in events:
                    # Leverage coordinator service to handle DB check, insert & FCM push safely
                    create_hackathon_and_notify(db, event)
            except Exception as e:
                logger.error(f"[Celery] Error in scraper {scraper.__class__.__name__}: {e}", exc_info=True)
    finally:
        db.close()

@celery_app.task(name="app.tasks.crawler.run_scrapers_task")
def run_scrapers_task():
    """
    Periodic Celery worker task that executes the scrapers, saves new events,
    and alerts clients of new hackathons.
    """
    logger.info("[Celery] Starting periodic scraper execution...")
    asyncio.run(run_scrapers_async())
    logger.info("[Celery] Periodic scraper execution completed.")
