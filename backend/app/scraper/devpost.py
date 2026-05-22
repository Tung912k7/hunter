import httpx
from datetime import datetime
from typing import List, Dict, Any
import logging
import re
import asyncio
from app.scraper.base import BaseScraper

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class DevpostScraper(BaseScraper):
    async def scrape_events(self) -> List[Dict[str, Any]]:
        url = "https://devpost.com/api/hackathons?challenge_type[]=online&status[]=open&status[]=upcoming"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        
        events = []
        try:
            async with httpx.AsyncClient(timeout=15.0) as client:
                response = await client.get(url, headers=headers)
                if response.status_code != 200:
                    logger.error(f"Devpost scraper failed: HTTP {response.status_code}")
                    return []
                data = response.json()
                hackathons_data = data.get("hackathons", [])
                
                for item in hackathons_data:
                    platform_id = str(item.get("id") or "")
                    if not platform_id:
                        continue
                    
                    title = item.get("title", "Untitled Devpost Hackathon")
                    description = item.get("tagline") or item.get("description")
                    event_url = item.get("url")
                    
                    # Parse prize amount
                    prize_value = 0.0
                    prize_str = item.get("prize_amount")
                    if prize_str:
                        cleaned = re.sub(r"[^\d.]", "", str(prize_str))
                        try:
                            # If multiple dots, only take the first one or clean it up
                            if cleaned.count(".") > 1:
                                parts = cleaned.split(".")
                                cleaned = parts[0] + "." + "".join(parts[1:])
                            prize_value = float(cleaned) if cleaned else 0.0
                        except ValueError:
                            prize_value = 0.0
                    
                    is_online = True  # We query challenge_type[]=online
                    
                    events.append({
                        "platform": "devpost",
                        "platform_id": platform_id,
                        "title": title,
                        "description": description,
                        "url": event_url,
                        "rules_url": f"{event_url}/rules" if event_url else None,
                        "prize_type": "fiat",
                        "prize_currency": "USD",
                        "prize_value": prize_value,
                        "is_online": is_online,
                        "start_date": None,
                        "end_date": None,
                    })
        except Exception as e:
            logger.exception(f"Error scraping Devpost: {e}")
            
        return events

if __name__ == "__main__":
    async def main():
        scraper = DevpostScraper()
        results = await scraper.scrape_events()
        logger.info(f"Scraped {len(results)} events from Devpost.")
        for r in results[:3]:
            logger.info(f"Event: {r['title']} | Prize: {r['prize_value']} {r['prize_currency']} | URL: {r['url']}")
            
    asyncio.run(main())
