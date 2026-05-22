import pytest
from unittest.mock import patch, MagicMock, AsyncMock
from sqlalchemy.exc import IntegrityError
from app.models.hackathon import Hackathon
from app.tasks.crawler import run_scrapers_task, run_scrapers_async
from app.services.hackathon_service import create_hackathon_and_notify

@pytest.mark.asyncio
@patch("app.tasks.crawler.DevpostScraper")
@patch("app.tasks.crawler.DevfolioScraper")
@patch("app.tasks.crawler.HackerEarthScraper")
@patch("app.tasks.crawler.GitcoinClient")
@patch("app.tasks.crawler.DoraHacksScraper")
@patch("app.tasks.crawler.BeWaterScraper")
@patch("app.services.hackathon_service.FCMService")
async def test_crawler_success_and_duplicate_suppression(
    mock_fcm,
    mock_bewater,
    mock_dora,
    mock_gitcoin,
    mock_hackerearth,
    mock_devfolio,
    mock_devpost,
    db_session
):
    # Define a new mock event
    event_new = {
        "platform": "devpost",
        "platform_id": "new-event-1",
        "title": "New Devpost Hack",
        "description": "A newly crawled event",
        "url": "https://devpost.com/new-event-1",
        "rules_url": "https://devpost.com/new-event-1/rules",
        "prize_type": "fiat",
        "prize_currency": "USD",
        "prize_value": 5000.0,
        "is_online": True,
        "start_date": None,
        "end_date": None,
    }
    
    # Define a duplicate event
    event_duplicate = {
        "platform": "devfolio",
        "platform_id": "dup-event-2",
        "title": "Existing Devfolio Hack",
        "description": "An event already in DB",
        "url": "https://devfolio.co/dup-event-2",
        "rules_url": "https://devfolio.co/dup-event-2/rules",
        "prize_type": "crypto",
        "prize_currency": "USDC",
        "prize_value": 10000.0,
        "is_online": True,
        "start_date": None,
        "end_date": None,
    }

    # Pre-populate the duplicate event directly in DB
    h_existing = Hackathon(
        platform="devfolio",
        platform_id="dup-event-2",
        title="Existing Devfolio Hack",
        description="An event already in DB",
        url="https://devfolio.co/dup-event-2",
        rules_url="https://devfolio.co/dup-event-2/rules",
        prize_type="crypto",
        prize_currency="USDC",
        prize_value=10000.0,
        is_online=True,
        is_vietnam_eligible=True,
        report_count=0
    )
    db_session.add(h_existing)
    db_session.commit()

    # Configure scraper mocks to return these lists
    mock_devpost.return_value.scrape_events = AsyncMock(return_value=[event_new])
    mock_devfolio.return_value.scrape_events = AsyncMock(return_value=[event_duplicate])
    mock_hackerearth.return_value.scrape_events = AsyncMock(return_value=[])
    mock_gitcoin.return_value.scrape_events = AsyncMock(return_value=[])
    mock_dora.return_value.scrape_events = AsyncMock(return_value=[])
    mock_bewater.return_value.scrape_events = AsyncMock(return_value=[])

    # Run the crawler with db_session mocked in place of SessionLocal
    with patch("app.tasks.crawler.SessionLocal", return_value=db_session):
        await run_scrapers_async()

    # Assertions:
    # 1. New event should be stored successfully
    new_in_db = db_session.query(Hackathon).filter(Hackathon.platform == "devpost", Hackathon.platform_id == "new-event-1").first()
    assert new_in_db is not None
    assert new_in_db.title == "New Devpost Hack"
    
    # 2. FCM alert should be broadcasted for the new event
    mock_fcm.broadcast_hackathon_alert.assert_called_once()
    topic, payload = mock_fcm.broadcast_hackathon_alert.call_args[0]
    assert topic == "hackathons"
    assert payload["id"] == str(new_in_db.id)
    assert payload["title"] == "New Devpost Hack"
    assert payload["prize_value"] == "5000.00"
    assert payload["is_vietnam_eligible"] == "true"

    # 3. The duplicate event should not trigger another FCM broadcast
    assert mock_fcm.broadcast_hackathon_alert.call_count == 1


@pytest.mark.asyncio
@patch("app.services.hackathon_service.FCMService")
async def test_create_hackathon_integrity_error(mock_fcm, db_session):
    event_data = {
        "platform": "hackerearth",
        "platform_id": "error-event",
        "title": "Hackerearth Integrity Error",
        "description": "Trigger integrity check",
        "url": "https://hackerearth.com/1",
        "rules_url": None,
        "prize_type": "fiat",
        "prize_currency": "USD",
        "prize_value": 5000.0,
        "is_online": True,
        "start_date": None,
        "end_date": None,
    }

    # Simulate IntegrityError on database commit
    with patch.object(db_session, "commit", side_effect=IntegrityError("Mock integrity error", {}, None)):
        res = create_hackathon_and_notify(db_session, event_data)
        assert res is None
        # FCM broadcast should not be triggered due to db transaction rollback
        mock_fcm.broadcast_hackathon_alert.assert_not_called()


@patch("app.tasks.crawler.asyncio.run")
def test_run_scrapers_task(mock_asyncio_run):
    run_scrapers_task()
    mock_asyncio_run.assert_called_once()
