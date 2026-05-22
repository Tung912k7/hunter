# Implementation Plan: Hackathon Hunter - Phase 1: Backend Scrapers, Crowdsourced Reporting, & REST API

## 📌 User Request (VERBATIM)
### Request 1
> Tạo 1 ứng dụng Android dùng để săn các cuộc thi hackathon trên các trang chuyên host Hackthon. Ứng dụng sẽ lọc các cuộc thi với tiêu chí: Tổ chức online, giải là tiền mặt, cho phép công dân Việt Nam tham gia. Hỏi nếu muốn làm rõ

### Request 2
> Trả lời câu hỏi: 1) Quét từ các trang uy tín và lớn. 2) Phương án A. 3) Thử cách 2. 4) Sử dụng công nghệ có độ tương thích cao với môi trường Android. 5) Có, ngoài ra tôi muốn là khi mở ứng dụng, ứng dụng sẽ hiện list các cuộc thi, có thêm chức năng lọc.

---

## 🎯 Acceptance Criteria (Derived from User Request)
| ID | Criterion | Verification Method |
|----|-----------|---------------------|
| **AC-01** | Scrapers retrieve hackathons from Devpost, Devfolio, HackerEarth, Gitcoin, DoraHacks, and BeWater. | Execute scrapers individually and verify they print/return list of raw hackathon details. |
| **AC-02** | Scrapers extract whether a hackathon is online. | Verify the parsed outputs contain a boolean flag indicating online status. |
| **AC-03** | Scrapers extract structured prize details including prize type, currency, and value. | Verify the parsed output contains: `prize_type` ("fiat" or "crypto"), `prize_currency` (e.g. USD, ETH, SOL, TON, etc.), and `prize_value` (numeric). |
| **AC-04** | Backend supports crowdsourced moderation reporting of hackathons banning Vietnam. | POST to `/api/v1/hackathons/{id}/report`, assert `report_count` increases, and check that `is_vietnam_eligible` turns False when count reaches 3. |
| **AC-05** | API exposes endpoints to filter by Vietnam eligibility, online format, minimum prize value, prize type, platform list, and text queries. | Query `GET /api/v1/hackathons` with various combinations of filters and verify the response status and content. |

---

## 📋 Context Summary & Constraints
- **Scraper Anti-Blocking**: Devfolio, HackerEarth, DoraHacks, and BeWater have strict protections. Scraping needs to use Playwright (headless) inside Docker, rotate User-Agent headers, and add randomized delays (jitter).
- **Gitcoin Indexer**: Connects via GraphQL query directly to Gitcoin's public indexer endpoint (`https://grants-stack-indexer-v2.gitcoin.co/graphql`) to discover active grants/hackathons.
- **Crowdsourced Moderation**: All events default to `is_vietnam_eligible = True`. AI eligibility parsing is completely replaced with crowdsourced reports.
- **Deduplication**: PostgreSQL needs a compound unique index on `(platform, platform_id)` so duplicate scraper runs do not trigger duplicate records.
- **Prior Deliverables**:
  - Research: [RESEARCH-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/researchers/RESEARCH-hackathon-hunter.md)
  - Scout: [SCOUT-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/scouts/SCOUT-hackathon-hunter.md)
  - Design: [DESIGN-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/designs/DESIGN-hackathon-hunter.md)

---

## Overview
This phase sets up the database schema including crowdsourced metrics, implements the crawling scrapers/clients for all six target platforms, implements the moderation APIs, and creates the REST API endpoints.

---

## Prerequisites
- Docker & Docker Compose installed on the development machine.

---

## Tasks

### Task 1.1: Core Configuration and Database Schema Setup
- **Agent**: `database-architect`
- **File(s)**:
  - `/backend/app/core/config.py`
  - `/backend/app/core/database.py`
  - `/backend/app/models/hackathon.py`
- **Description**: 
  - Create Pydantic Settings configuration inside `config.py` to validate environment variables (`DATABASE_URL`, `ENVIRONMENT`).
  - Set up SQLAlchemy engine and SessionLocal in `database.py`.
  - Create the `Hackathon` SQLAlchemy model with the following columns:
    - `id` (Integer, Primary Key)
    - `platform` (String - devpost/devfolio/hackerearth/gitcoin/dorahacks/bewater)
    - `platform_id` (String - unique event ID on that platform)
    - `title` (String)
    - `description` (String, nullable)
    - `url` (String)
    - `rules_url` (String, nullable)
    - `prize_type` (String - "fiat" or "crypto")
    - `prize_currency` (String - USD, ETH, SOL, TON, USDC, USDT, etc.)
    - `prize_value` (Numeric/Decimal, default 0.0)
    - `is_online` (Boolean, default False)
    - `start_date` (DateTime, nullable)
    - `end_date` (DateTime, nullable)
    - `is_vietnam_eligible` (Boolean, default True)
    - `report_count` (Integer, default 0)
    - `created_at` (DateTime, default functional now)
    - `updated_at` (DateTime, default functional now, on update now)
  - Add a compound unique constraint: `UniqueConstraint('platform', 'platform_id', name='uq_platform_platform_id')` to prevent duplicates.
- **Verification**: Run a migration script or database test to verify that the table compiles and has the unique constraint on `(platform, platform_id)` with `is_vietnam_eligible` default to True.

---

### Task 1.2: Devpost Scraper
- **Agent**: `backend-engineer`
- **File(s)**:
  - `/backend/app/scraper/base.py`
  - `/backend/app/scraper/devpost.py`
- **Description**:
  - Define `BaseScraper` abstract interface with async method `scrape_events() -> List[dict]`.
  - Implement `DevpostScraper` that queries the public REST endpoint: `https://devpost.com/api/hackathons?challenge_type[]=online&status[]=open&status[]=upcoming`.
  - Extract basic details. Map prize details: type is "fiat", currency is "USD", extract numeric prize amount to `prize_value`.
- **Verification**: Run a manual python script `python -m app.scraper.devpost` and verify it logs parsed Devpost events with cash prize values.

---

### Task 1.3: Devfolio Scraper
- **Agent**: `backend-engineer`
- **File(s)**:
  - `/backend/app/scraper/devfolio.py`
- **Description**:
  - Implement `DevfolioScraper` using Playwright (headless context).
  - Navigate to `https://devfolio.co/hackathons` and scrape lists.
  - Map prize structures (usually "fiat" / "USD" or native crypto if sponsored).
  - Extract event details and rules page link.
- **Verification**: Run `python -m app.scraper.devfolio` and verify it bypasses blocks and returns the structured events.

---

### Task 1.4: HackerEarth Scraper
- **Agent**: `backend-engineer`
- **File(s)**:
  - `/backend/app/scraper/hackerearth.py`
- **Description**:
  - Implement `HackerEarthScraper` using Playwright.
  - Load `https://www.hackerearth.com/challenges/` page.
  - Filter active hackathons, map fiat cash prizes, and extract page details.
- **Verification**: Run `python -m app.scraper.hackerearth` and verify it outputs HackerEarth events and parsed cash prizes.

---

### Task 1.5: Gitcoin Indexer GraphQL Client
- **Agent**: `backend-engineer`
- **File(s)**:
  - `/backend/app/scraper/gitcoin.py`
- **Description**:
  - Implement `GitcoinClient` queries to connect to the public indexer endpoint `https://grants-stack-indexer-v2.gitcoin.co/graphql`.
  - Execute a GraphQL query fetching active hackathon/grant rounds.
  - Extract details. Map prize details: type is "crypto", currency matches tokens used (ETH, DAI, USDC, etc.), and value represents total pool.
- **Verification**: Run `python -m app.scraper.gitcoin` and verify it outputs Gitcoin events with crypto prize metadata.

---

### Task 1.6: DoraHacks & BeWater Scrapers
- **Agent**: `backend-engineer`
- **File(s)**:
  - `/backend/app/scraper/dorahacks.py`
  - `/backend/app/scraper/bewater.py`
- **Description**:
  - Implement scrapers using Playwright or internal REST endpoints discovered from web traffic.
  - Map DoraHacks and BeWater events. Map prizes (frequently crypto-based: SOL, ETH, TON, USDC, USDT). Determine `prize_type` as "crypto" or "fiat", parse `prize_currency` and `prize_value`.
- **Verification**: Run `python -m app.scraper.dorahacks` and `python -m app.scraper.bewater` and verify events map correctly.

---

### Task 1.7: REST API Core & Endpoints
- **Agent**: `backend-engineer`
- **File(s)**:
  - `/backend/app/schemas/hackathon.py`
  - `/backend/app/api/deps.py`
  - `/backend/app/api/v1/endpoints/hackathons.py`
  - `/backend/app/main.py`
- **Description**:
  - Define Pydantic request/response schemas: `HackathonResponse`, `HackathonCreate`, etc., including new fields: `prize_type`, `prize_currency`, `prize_value`, `is_vietnam_eligible`, and `report_count`.
  - Create database dependency `get_db`.
  - Implement `GET /api/v1/hackathons` supporting filters:
    - `is_vietnam_eligible` (Optional[bool])
    - `is_online` (Optional[bool])
    - `prize_type` (Optional[str] - "fiat", "crypto", "all")
    - `min_prize_value` (Optional[float])
    - `platforms` (Optional[List[str]] - supports a subset of the six platforms)
    - `query` (Optional[str] - searches text in title/description)
    - `page` (int, default 1) and `size` (int, default 20)
  - Implement `POST /api/v1/hackathons/{id}/report` moderation endpoint:
    - Load the hackathon with primary key `{id}`. Raise 404 if not found.
    - Increment the `report_count` field by 1.
    - If `report_count >= 3`, automatically toggle `is_vietnam_eligible` to `False` on the record.
    - Save and commit to DB. Return the updated record representation.
  - Implement manual trigger sync route `POST /api/v1/hackathons/sync`.
- **Verification**: Start the FastAPI server locally, hit Swagger UI at `/docs`, execute `POST /report` on a dummy event, and verify the record's eligibility changes when count reaches 3.

---

### Task 1.8: Docker Setup & Local Configuration
- **Agent**: `devops-engineer`
- **File(s)**:
  - `/backend/requirements.txt`
  - `/backend/Dockerfile`
  - `/backend/docker-compose.yml`
- **Description**:
  - Create `requirements.txt` with dependencies: `fastapi`, `uvicorn`, `sqlalchemy`, `psycopg2-binary`, `playwright`, `beautifulsoup4`, `pydantic-settings`, `httpx`, `pytest`. (Note: Google GenAI SDK is excluded).
  - Write multi-stage `Dockerfile`. Install Playwright dependencies: `playwright install --with-deps`.
  - Write `docker-compose.yml` for PostgreSQL 15, Redis 7, and FastAPI.
- **Verification**: Run `docker-compose up --build -d` and check container logs.

---

### Task 1.9: Unit & Integration Tests
- **Agent**: `tester`
- **File(s)**:
  - `/backend/tests/conftest.py`
  - `/backend/tests/test_api.py`
  - `/backend/tests/test_scraper.py`
- **Description**:
  - Configure `conftest.py` with test database, API Client, and mock scrapers.
  - Write tests for the `GET /api/v1/hackathons` filters (asserting `prize_type` and `platforms` logic).
  - Write tests for the `POST /api/v1/hackathons/{id}/report` endpoint:
    - Test single report increments count to 1.
    - Test three reports increment count to 3 and toggle `is_vietnam_eligible` to False.
- **Verification**: Run `docker-compose exec web pytest` and verify all tests pass.

---

## Exit Criteria
- [ ] PostgreSQL table `hackathons` has unique constraint on `(platform, platform_id)`.
- [ ] Devpost, Devfolio, HackerEarth, Gitcoin, DoraHacks, and BeWater scraper/client classes compile and retrieve data.
- [ ] `POST /api/v1/hackathons/{id}/report` increments count and flips eligibility to False at 3 reports.
- [ ] `GET /api/v1/hackathons` works with filters (Vietnam eligibility, online, prize type, platforms).
- [ ] Pytest suite achieves > 80% code coverage.

---

## Risks & Rollback

| Risk | Impact | Probability | Mitigation | Rollback Plan |
|------|--------|-------------|------------|---------------|
| Playwright blocked by Cloudflare on DoraHacks/BeWater | High | High | Rotate user-agents, inject custom delays, fallback to GraphQL API interception if UI fails. | Disable specific scraper temporarily in configuration; fall back to manual mock import. |
| Double reporting by same client device | Medium | Medium | Handle client-side state logging (Phase 3) and implement IP/Token rate limiting on backend if abused. | Reset affected `report_count` fields using admin script if spam occurs. |

### Rollback Action
If database integrity crashes or Playwright fails to compile in Docker:
1. Revert Dockerfile to basic python image, mock the Playwright tasks to return static JSON profiles.
2. Revert DB migrations to previous state: drop table and re-run standard schema.
