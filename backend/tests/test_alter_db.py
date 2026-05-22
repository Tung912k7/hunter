import pytest
import asyncio
from sqlalchemy import create_engine, text
from app.core.database import SessionLocal
from app.core.config import settings
from app.tasks.crawler import run_scrapers_async
from app.models.hackathon import Hackathon

@pytest.mark.asyncio
async def test_run_live_sync():
    print("Running live scrapers sync directly to Supabase...")
    await run_scrapers_async()
    
    # Verify records in Supabase
    db = SessionLocal()
    try:
        # Check total count
        total = db.query(Hackathon).count()
        print(f"Total hackathons in database after sync: {total}")
        
        # Check DoraHacks hackathons
        dora_events = db.query(Hackathon).filter(Hackathon.platform == "dorahacks").all()
        print(f"Total DoraHacks hackathons in database: {len(dora_events)}")
        
        # Print some titles (handling possible console encoding limitations safely)
        for i, event in enumerate(dora_events[:10]):
            safe_title = event.title.encode('ascii', errors='replace').decode('ascii')
            print(f"[{i+1}] {safe_title} (length={len(event.title)})")
            
        assert len(dora_events) > 0
    finally:
        db.close()
