import abc
from typing import List, Dict, Any

class BaseScraper(abc.ABC):
    @abc.abstractmethod
    async def scrape_events(self) -> List[Dict[str, Any]]:
        """
        Scrape events from the platform.
        Returns a list of dicts representing hackathons.
        """
        pass
