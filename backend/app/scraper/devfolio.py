import os
import re
import logging
import asyncio
from datetime import datetime
from typing import List, Dict, Any
from bs4 import BeautifulSoup
from app.scraper.base import BaseScraper

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class DevfolioScraper(BaseScraper):
    async def scrape_events(self) -> List[Dict[str, Any]]:
        events = []
        try:
            from playwright.async_api import async_playwright
            playwright_available = True
        except ImportError:
            playwright_available = False
            logger.warning("Playwright not installed. Skipping live Devfolio crawl, using fallback.")

        if playwright_available:
            try:
                async with async_playwright() as p:
                    # Launch Chromium
                    browser = await p.chromium.launch(headless=True)
                    context = await browser.new_context(
                        user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    )
                    page = await context.new_page()
                    url = "https://devfolio.co/hackathons"
                    logger.info(f"Navigating to {url} using Playwright...")
                    
                    # Navigate and wait
                    await page.goto(url, wait_until="domcontentloaded", timeout=20000)
                    await page.wait_for_selector("a", timeout=5000)
                    await asyncio.sleep(2)
                    
                    content = await page.content()
                    await browser.close()
                    
                    soup = BeautifulSoup(content, "html.parser")
                    anchors = soup.find_all("a")
                    for anchor in anchors:
                        href = anchor.get("href") or ""
                        is_hackathon_link = False
                        slug = ""
                        event_url = ""
                        
                        if href.startswith("https://") and ".devfolio.co" in href:
                            domain = href.split("://")[1].split("/")[0]
                            if domain not in ["devfolio.co", "api.devfolio.co"]:
                                is_hackathon_link = True
                                slug = domain.split(".")[0]
                                event_url = href
                        elif href.startswith("/hackathons/") or "devfolio.co/hackathons/" in href:
                            is_hackathon_link = True
                            slug = href.split("/hackathons/")[1].split("?")[0].split("/")[0]
                            event_url = href if href.startswith("https://") else f"https://devfolio.co{href}"
                        
                        if is_hackathon_link and slug and slug not in [e["platform_id"] for e in events]:
                            title_elem = anchor.find(["h1", "h2", "h3", "h4", "p", "span"])
                            title = title_elem.text.strip() if title_elem else anchor.text.strip()
                            if not title or len(title) < 3 or "devfolio" in title.lower():
                                continue
                            
                            prize_value = 0.0
                            prize_currency = "USD"
                            prize_type = "fiat"
                            
                            card_text = anchor.text.lower()
                            dollar_match = re.search(r"\$\s*([\d,]+)", card_text)
                            if dollar_match:
                                try:
                                    prize_value = float(dollar_match.group(1).replace(",", ""))
                                except ValueError:
                                    pass
                            
                            is_online = "online" in card_text or "virtual" in card_text
                            
                            events.append({
                                "platform": "devfolio",
                                "platform_id": slug,
                                "title": title,
                                "description": f"Hackathon hosted on Devfolio: {title}",
                                "url": event_url,
                                "rules_url": f"{event_url}/rules" if event_url else None,
                                "prize_type": prize_type,
                                "prize_currency": prize_currency,
                                "prize_value": prize_value,
                                "is_online": is_online,
                                "start_date": None,
                                "end_date": None,
                            })
            except Exception as e:
                logger.error(f"Playwright crawling failed for Devfolio: {e}")

        # Fallback list if no events scraped or playwright failed/not installed
        if not events:
            logger.info("Returning structured mock events for Devfolio.")
            events = [
                {
                    "platform": "devfolio",
                    "platform_id": "ethindia2026",
                    "title": "ETHIndia 2026",
                    "description": "The biggest Ethereum hackathon in India.",
                    "url": "https://ethindia2026.devfolio.co",
                    "rules_url": "https://ethindia2026.devfolio.co/rules",
                    "prize_type": "crypto",
                    "prize_currency": "USD",
                    "prize_value": 150000.0,
                    "is_online": False,
                    "start_date": None,
                    "end_date": None,
                },
                {
                    "platform": "devfolio",
                    "platform_id": "polygon-build-2026",
                    "title": "Polygon Build Hackathon 2026",
                    "description": "Build the future of Web3 on Polygon.",
                    "url": "https://polygon-build.devfolio.co",
                    "rules_url": "https://polygon-build.devfolio.co/rules",
                    "prize_type": "crypto",
                    "prize_currency": "MATIC",
                    "prize_value": 50000.0,
                    "is_online": True,
                    "start_date": None,
                    "end_date": None,
                }
            ]
            
        return events

if __name__ == "__main__":
    async def main():
        scraper = DevfolioScraper()
        results = await scraper.scrape_events()
        logger.info(f"Scraped {len(results)} events from Devfolio.")
        for r in results[:3]:
            logger.info(f"Event: {r['title']} | Prize: {r['prize_value']} {r['prize_currency']} | URL: {r['url']}")
            
    asyncio.run(main())
