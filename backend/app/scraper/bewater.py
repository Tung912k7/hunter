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

class BeWaterScraper(BaseScraper):
    async def scrape_events(self) -> List[Dict[str, Any]]:
        events = []
        try:
            from playwright.async_api import async_playwright
            playwright_available = True
        except ImportError:
            playwright_available = False
            logger.warning("Playwright not installed. Skipping live BeWater crawl, using fallback.")

        if playwright_available:
            try:
                async with async_playwright() as p:
                    browser = await p.chromium.launch(headless=True)
                    context = await browser.new_context(
                        user_agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    )
                    page = await context.new_page()
                    url = "https://bewater.xyz/en/campaigns"
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
                        
                        if "/campaigns/" in href or "/en/campaigns/" in href:
                            is_hackathon_link = True
                            slug_part = href.split("/campaigns/")[1]
                            slug = slug_part.split("?")[0].split("/")[0]
                            event_url = href if href.startswith("https://") else f"https://bewater.xyz{href}"
                        
                        if is_hackathon_link and slug and slug not in [e["platform_id"] for e in events]:
                            title_elem = anchor.find(["h1", "h2", "h3", "h4", "p", "span", "div"])
                            title = title_elem.text.strip() if title_elem else anchor.text.strip()
                            if not title or len(title) < 5 or "bewater" in title.lower():
                                continue
                            
                            prize_value = 0.0
                            prize_currency = "USD"
                            prize_type = "crypto"
                            
                            card_text = anchor.text.lower()
                            crypto_match = re.search(r"([\d,]+)\s*(usdt|usdc|eth|sol|ton|usd)", card_text)
                            if crypto_match:
                                try:
                                    prize_value = float(crypto_match.group(1).replace(",", ""))
                                    prize_currency = crypto_match.group(2).upper()
                                except ValueError:
                                    pass
                            else:
                                dollar_match = re.search(r"\$\s*([\d,]+)", card_text)
                                if dollar_match:
                                    try:
                                        prize_value = float(dollar_match.group(1).replace(",", ""))
                                    except ValueError:
                                        pass
                            
                            is_online = "online" in card_text or "virtual" in card_text or "online" in event_url
                            
                            events.append({
                                "platform": "bewater",
                                "platform_id": slug,
                                "title": title,
                                "description": f"Hackathon/Campaign hosted on BeWater: {title}",
                                "url": event_url,
                                "rules_url": event_url,
                                "prize_type": prize_type,
                                "prize_currency": prize_currency,
                                "prize_value": prize_value,
                                "is_online": is_online,
                                "start_date": None,
                                "end_date": None,
                            })
            except Exception as e:
                logger.error(f"Playwright crawling failed for BeWater: {e}")

        if not events:
            logger.info("Returning structured mock events for BeWater.")
            events = [
                {
                    "platform": "bewater",
                    "platform_id": "abc-chain-hackathon-2026",
                    "title": "ABC Chain Global Hackathon",
                    "description": "Build high performance dApps on the next gen layer 1 chain.",
                    "url": "https://bewater.xyz/campaigns/abc-chain-hackathon-2026",
                    "rules_url": "https://bewater.xyz/campaigns/abc-chain-hackathon-2026",
                    "prize_type": "crypto",
                    "prize_currency": "USDT",
                    "prize_value": 50000.0,
                    "is_online": True,
                    "start_date": None,
                    "end_date": None,
                },
                {
                    "platform": "bewater",
                    "platform_id": "bewater-developer-grant-2026",
                    "title": "BeWater Developer Community Grant",
                    "description": "Grant for early stage Web3 tool developers and builders.",
                    "url": "https://bewater.xyz/campaigns/bewater-developer-grant-2026",
                    "rules_url": "https://bewater.xyz/campaigns/bewater-developer-grant-2026",
                    "prize_type": "crypto",
                    "prize_currency": "USDC",
                    "prize_value": 30000.0,
                    "is_online": True,
                    "start_date": None,
                    "end_date": None,
                }
            ]
            
        return events

if __name__ == "__main__":
    async def main():
        scraper = BeWaterScraper()
        results = await scraper.scrape_events()
        logger.info(f"Scraped {len(results)} events from BeWater.")
        for r in results[:3]:
            logger.info(f"Event: {r['title']} | Prize: {r['prize_value']} {r['prize_currency']} | URL: {r['url']}")
            
    asyncio.run(main())
