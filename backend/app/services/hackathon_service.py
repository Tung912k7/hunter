from typing import Optional
from sqlalchemy.orm import Session
from sqlalchemy.exc import IntegrityError
from app.models.hackathon import Hackathon
from app.services.fcm import FCMService
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def create_hackathon_and_notify(db: Session, event_data: dict) -> Optional[Hackathon]:
    """
    Saves a hackathon event to the database if it doesn't already exist.
    If it is newly inserted, triggers an FCM push notification broadcast.
    If it already exists, suppresses insertion and push to avoid duplicate spam.
    """
    # 1. Double check database presence to prevent IntegrityError
    existing = db.query(Hackathon).filter(
        Hackathon.platform == event_data["platform"],
        Hackathon.platform_id == event_data["platform_id"]
    ).first()
    
    if existing:
        logger.info(f"Hackathon {event_data['platform']}/{event_data['platform_id']} already exists. Skipping.")
        return None
        
    # 2. Insert new event
    db_event = Hackathon(**event_data)
    db.add(db_event)
    
    try:
        db.commit()
        db.refresh(db_event)
    except Exception as e:
        db.rollback()
        logger.warning(f"Error saving {event_data['platform']}/{event_data['platform_id']}: {e}. Skipping push.")
        return None
        
    # 3. Formulate and broadcast FCM payload
    topic = "hackathons"
    
    try:
        prize_val_str = f"{float(db_event.prize_value):.2f}"
    except (ValueError, TypeError):
        prize_val_str = "0.00"
        
    payload = {
        "id": str(db_event.id),
        "title": str(db_event.title),
        "platform": str(db_event.platform),
        "is_online": "true" if db_event.is_online else "false",
        "prize_type": str(db_event.prize_type),
        "prize_currency": str(db_event.prize_currency),
        "prize_value": prize_val_str,
        "is_vietnam_eligible": "true" if db_event.is_vietnam_eligible else "false",
        "report_count": str(db_event.report_count)
    }
    
    logger.info(f"Broadcasting new hackathon: {db_event.title} ({db_event.platform})")
    FCMService.broadcast_hackathon_alert(topic, payload)
    
    return db_event
