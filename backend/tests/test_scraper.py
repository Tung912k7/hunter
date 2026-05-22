import pytest
from app.scraper.devpost import DevpostScraper
from app.scraper.devfolio import DevfolioScraper
from app.scraper.hackerearth import HackerEarthScraper
from app.scraper.gitcoin import GitcoinClient
from app.scraper.dorahacks import DoraHacksScraper
from app.scraper.bewater import BeWaterScraper

@pytest.mark.asyncio
async def test_devpost_scraper():
    scraper = DevpostScraper()
    results = await scraper.scrape_events()
    assert isinstance(results, list)
    for r in results:
        assert r["platform"] == "devpost"
        assert "platform_id" in r
        assert "title" in r
        assert "url" in r
        assert r["prize_type"] in ["fiat", "crypto"]
        assert "prize_value" in r

@pytest.mark.asyncio
async def test_devfolio_scraper():
    scraper = DevfolioScraper()
    results = await scraper.scrape_events()
    assert isinstance(results, list)
    assert len(results) > 0
    for r in results:
        assert r["platform"] == "devfolio"
        assert "platform_id" in r
        assert "title" in r

@pytest.mark.asyncio
async def test_hackerearth_scraper():
    scraper = HackerEarthScraper()
    results = await scraper.scrape_events()
    assert isinstance(results, list)
    assert len(results) > 0
    for r in results:
        assert r["platform"] == "hackerearth"
        assert "platform_id" in r
        assert "title" in r

@pytest.mark.asyncio
async def test_gitcoin_client():
    client = GitcoinClient()
    results = await client.scrape_events()
    assert isinstance(results, list)
    assert len(results) > 0
    for r in results:
        assert r["platform"] == "gitcoin"
        assert "platform_id" in r
        assert "title" in r

@pytest.mark.asyncio
async def test_dorahacks_scraper():
    scraper = DoraHacksScraper()
    results = await scraper.scrape_events()
    assert isinstance(results, list)
    assert len(results) > 0
    for r in results:
        assert r["platform"] == "dorahacks"
        assert "platform_id" in r
        assert "title" in r

@pytest.mark.asyncio
async def test_bewater_scraper():
    scraper = BeWaterScraper()
    results = await scraper.scrape_events()
    assert isinstance(results, list)
    assert len(results) > 0
    for r in results:
        assert r["platform"] == "bewater"
        assert "platform_id" in r
        assert "title" in r
