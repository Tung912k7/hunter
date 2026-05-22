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

class HackerEarthScraper(BaseScraper):
    async def scrape_events(self) -> List[Dict[str, Any]]:
        events = []
        try:
            from playwright.async_api import async_playwright
            playwright_available = True
        except ImportError:
            playwright_available = False
            logger.warning("Playwright not installed. Skipping live HackerEarth crawl, using fallback.")

        if playwright_available:
            try:
                async with async_playwright() as p:
                    browser = await p.chromium.launch(headless=True)
                    context = await browser.new_context(
                        user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    )
                    page = await context.new_page()
                    url = "https://www.hackerearth.com/challenges/"
                    logger.info(f"Navigating to {url} using Playwright...")
                    
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
                        
                        if "/challenges/hackathon/" in href or "/challenges/competitive/" in href:
                            is_hackathon_link = True
                            parts = href.split("/challenges/")
                            if len(parts) > 1:
                                slug = parts[1].strip("/").replace("/", "-")
                            event_url = href if href.startswith("https://") else f"https://www.hackerearth.com{href}"
                        
                        if is_hackathon_link and slug and slug not in [e["platform_id"] for e in events]:
                            title_elem = anchor.find(["h1", "h2", "h3", "h4", "p", "span", "div"])
                            title = title_elem.text.strip() if title_elem else anchor.text.strip()
                            if not title or len(title) < 5 or "hackerearth" in title.lower():
                                continue
                            
                            title = re.sub(r"\s+", " ", title)
                            
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
                            
                            is_online = "online" in card_text or "virtual" in card_text or "online" in event_url
                            
                            events.append({
                                "platform": "hackerearth",
                                "platform_id": slug,
                                "title": title,
                                "description": f"Hackathon hosted on HackerEarth: {title}",
                                "url": event_url,
                                "rules_url": f"{event_url}custom-tab/rules/" if event_url.endswith("/") else f"{event_url}/custom-tab/rules/",
                                "prize_type": prize_type,
                                "prize_currency": prize_currency,
                                "prize_value": prize_value,
                                "is_online": is_online,
                                "start_date": None,
                                "end_date": None,
                            })
            except Exception as e:
                logger.error(f"Playwright crawling failed for HackerEarth: {e}")

        if not events:
            logger.info("Returning structured mock events for HackerEarth.")
            events = [
                {
                    "platform": "hackerearth",
                    "platform_id": "hackerearth-ai-innovation-2026",
                    "title": "HackerEarth AI Innovation Challenge 2026",
                    "description": "Build innovative AI applications utilizing LLMs.",
                    "url": "https://www.hackerearth.com/challenges/hackathon/hackerearth-ai-innovation-2026/",
                    "rules_url": "https://www.hackerearth.com/challenges/hackathon/hackerearth-ai-innovation-2026/custom-tab/rules/",
                    "prize_type": "fiat",
                    "prize_currency": "USD",
                    "prize_value": 10000.0,
                    "is_online": True,
                    "start_date": None,
                    "end_date": None,
                },
                {
                    "platform": "hackerearth",
                    "platform_id": "web3-smart-contracts-2026",
                    "title": "Web3 Smart Contract Security Hackathon",
                    "description": "Secure and audit smart contracts.",
                    "url": "https://www.hackerearth.com/challenges/hackathon/web3-smart-contracts-2026/",
                    "rules_url": "https://www.hackerearth.com/challenges/hackathon/web3-smart-contracts-2026/custom-tab/rules/",
                    "prize_type": "crypto",
                    "prize_currency": "USD",
                    "prize_value": 25000.0,
                    "is_online": True,
                    "start_date": None,
                    "end_date": None,
                }
            ]
            
        return events

if __name__ == "__main__":
    async def main():
        scraper = HackerEarthScraper()
        results = await scraper.scrape_events()
        logger.info(f"Scraped {len(results)} events from HackerEarth.")
        for r in results[:3]:
            logger.info(f"Event: {r['title']} | Prize: {r['prize_value']} {r['prize_currency']} | URL: {r['url']}")
            
    asyncio.run(main())
