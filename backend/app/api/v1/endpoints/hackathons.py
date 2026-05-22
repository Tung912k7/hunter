from fastapi import APIRouter, Depends, HTTPException, Query, BackgroundTasks
from sqlalchemy.orm import Session
from sqlalchemy import or_
from typing import List, Optional
from app.api import deps
from app.models.hackathon import Hackathon
from app.schemas.hackathon import HackathonResponse
from app.scraper.devpost import DevpostScraper
from app.scraper.devfolio import DevfolioScraper
from app.scraper.hackerearth import HackerEarthScraper
from app.scraper.gitcoin import GitcoinClient
from app.scraper.dorahacks import DoraHacksScraper
from app.scraper.bewater import BeWaterScraper
import logging
from sqlalchemy.exc import IntegrityError

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

router = APIRouter()

@router.get("", response_model=List[HackathonResponse])
def get_hackathons(
    is_vietnam_eligible: Optional[bool] = Query(None),
    is_online: Optional[bool] = Query(None),
    prize_type: Optional[str] = Query(None, description="fiat, crypto, or all"),
    min_prize_value: Optional[float] = Query(None),
    platforms: Optional[List[str]] = Query(None),
    query: Optional[str] = Query(None),
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    db: Session = Depends(deps.get_db),
):
    q = db.query(Hackathon)
    
    if is_vietnam_eligible is not None:
        q = q.filter(Hackathon.is_vietnam_eligible == is_vietnam_eligible)
        
    if is_online is not None:
        q = q.filter(Hackathon.is_online == is_online)
        
    if prize_type and prize_type.lower() != "all":
        q = q.filter(Hackathon.prize_type == prize_type.lower())
        
    if min_prize_value is not None:
        q = q.filter(Hackathon.prize_value >= min_prize_value)
        
    if platforms:
        platforms_lower = [p.lower() for p in platforms]
        q = q.filter(Hackathon.platform.in_(platforms_lower))
        
    if query:
        search_filter = or_(
            Hackathon.title.ilike(f"%{query}%"),
            Hackathon.description.ilike(f"%{query}%")
        )
        q = q.filter(search_filter)
        
    q = q.order_by(Hackathon.created_at.desc())
    
    offset = (page - 1) * size
    results = q.offset(offset).limit(size).all()
    return results

@router.post("/{id}/report", response_model=HackathonResponse)
def report_hackathon(
    id: int,
    db: Session = Depends(deps.get_db)
):
    hackathon = db.query(Hackathon).filter(Hackathon.id == id).first()
    if not hackathon:
        raise HTTPException(status_code=404, detail="Hackathon not found")
        
    hackathon.report_count += 1
    if hackathon.report_count >= 3:
        hackathon.is_vietnam_eligible = False
        
    db.commit()
    db.refresh(hackathon)
    return hackathon

async def run_sync_task():
    from app.core.database import SessionLocal
    db = SessionLocal()
    scrapers = [
        DevpostScraper(),
        DevfolioScraper(),
        HackerEarthScraper(),
        GitcoinClient(),
        DoraHacksScraper(),
        BeWaterScraper()
    ]
    
    try:
        for scraper in scrapers:
            try:
                logger.info(f"Running scraper {scraper.__class__.__name__}...")
                events = await scraper.scrape_events()
                for event in events:
                    existing = db.query(Hackathon).filter(
                        Hackathon.platform == event["platform"],
                        Hackathon.platform_id == event["platform_id"]
                    ).first()
                    if not existing:
                        db_event = Hackathon(**event)
                        db.add(db_event)
                        try:
                            db.commit()
                        except Exception as e:
                            db.rollback()
                            logger.error(f"Error saving hackathon {event.get('title')}: {e}")
            except Exception as e:
                logger.error(f"Error running scraper {scraper.__class__.__name__}: {e}")
    finally:
        db.close()

@router.post("/sync")
async def sync_hackathons(
    background_tasks: BackgroundTasks
):
    background_tasks.add_task(run_sync_task)
    return {"status": "Sync initiated"}
