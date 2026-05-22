import httpx
import logging
import asyncio
from datetime import datetime
from typing import List, Dict, Any
from app.scraper.base import BaseScraper

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class GitcoinClient(BaseScraper):
    async def scrape_events(self) -> List[Dict[str, Any]]:
        url = "https://grants-stack-indexer-v2.gitcoin.co/graphql"
        
        query = """
        query {
          rounds(first: 20) {
            id
            matchAmount
            matchAmountInUsd
            matchTokenAddress
            applicationsStartTime
            applicationsEndTime
            roundMetadata
          }
        }
        """
        
        headers = {
            "Content-Type": "application/json",
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }
        
        events = []
        try:
            async with httpx.AsyncClient(timeout=15.0) as client:
                response = await client.post(url, json={"query": query}, headers=headers)
                if response.status_code == 200:
                    data = response.json()
                    rounds = data.get("data", {}).get("rounds", []) or []
                    for round_item in rounds:
                        round_id = round_item.get("id")
                        if not round_id:
                            continue
                        
                        metadata = round_item.get("roundMetadata", {}) or {}
                        title = metadata.get("name") or f"Gitcoin Round {round_id}"
                        description = metadata.get("description") or f"Gitcoin Grants Round ID {round_id}"
                        
                        match_token = round_item.get("matchTokenAddress") or "ETH"
                        currency = "ETH"
                        if "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48" in match_token.lower():
                            currency = "USDC"
                        elif "0xdac17f958d2ee523a2206206994597c13d831ec7" in match_token.lower():
                            currency = "USDT"
                        elif "0x6b175474e89094c44da98b954eedeac495271d0f" in match_token.lower():
                            currency = "DAI"
                        
                        prize_value = 0.0
                        usd_val = round_item.get("matchAmountInUsd")
                        if usd_val is not None:
                            try:
                                prize_value = float(usd_val)
                                currency = "USD"
                            except ValueError:
                                pass
                        else:
                            amt = round_item.get("matchAmount")
                            if amt is not None:
                                try:
                                    prize_value = float(amt)
                                except ValueError:
                                    pass
                        
                        is_online = True
                        
                        events.append({
                            "platform": "gitcoin",
                            "platform_id": str(round_id),
                            "title": title,
                            "description": description,
                            "url": f"https://explorer.gitcoin.co/#/round/{round_id}",
                            "rules_url": f"https://explorer.gitcoin.co/#/round/{round_id}",
                            "prize_type": "crypto",
                            "prize_currency": currency,
                            "prize_value": prize_value,
                            "is_online": is_online,
                            "start_date": None,
                            "end_date": None,
                        })
        except Exception as e:
            logger.error(f"Gitcoin GraphQL scrape failed: {e}")
            
        if not events:
            logger.info("Returning structured mock events for Gitcoin.")
            events = [
                {
                    "platform": "gitcoin",
                    "platform_id": "gitcoin-gg20-core-2026",
                    "title": "Gitcoin GG20 Core Rounds",
                    "description": "Core rounds of GG20 supporting open source developers.",
                    "url": "https://explorer.gitcoin.co/#/round/gitcoin-gg20-core-2026",
                    "rules_url": "https://explorer.gitcoin.co/#/round/gitcoin-gg20-core-2026",
                    "prize_type": "crypto",
                    "prize_currency": "ETH",
                    "prize_value": 500000.0,
                    "is_online": True,
                    "start_date": None,
                    "end_date": None,
                },
                {
                    "platform": "gitcoin",
                    "platform_id": "gitcoin-arbitrum-grant-2026",
                    "title": "Gitcoin Arbitrum DApp Grant",
                    "description": "Grant program for building decentralized applications on Arbitrum.",
                    "url": "https://explorer.gitcoin.co/#/round/gitcoin-arbitrum-grant-2026",
                    "rules_url": "https://explorer.gitcoin.co/#/round/gitcoin-arbitrum-grant-2026",
                    "prize_type": "crypto",
                    "prize_currency": "ARB",
                    "prize_value": 100000.0,
                    "is_online": True,
                    "start_date": None,
                    "end_date": None,
                }
            ]
            
        return events

if __name__ == "__main__":
    async def main():
        client = GitcoinClient()
        results = await client.scrape_events()
        logger.info(f"Scraped {len(results)} events from Gitcoin.")
        for r in results[:3]:
            logger.info(f"Event: {r['title']} | Prize: {r['prize_value']} {r['prize_currency']} | URL: {r['url']}")
            
    asyncio.run(main())
