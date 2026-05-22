# Scout Report: Hackathon Hunter

## Exploration Scope
- **Target**: Workspace analysis, architectural layout design, and multi-platform integration paths (incorporating Devpost, Devfolio, HackerEarth, DoraHacks, and Gitcoin) for the "Hackathon Hunter" project.
- **Boundaries**: The root directory `c:\Users\LAPTOP\Desktop\hackathon`, with specific design layouts for `/backend` and `/android`.

---

## Patterns Discovered
### Pattern: Greenfield Workspace Architecture
- **Location**: Root directory `/` (currently containing only `./reports/researchers` and `./reports/scouts`)
- **Usage**: Since the workspace is currently empty of source code, we establish a clean, standard, and highly decoupled boilerplate layout for both the Python/FastAPI backend and the Android/Kotlin app. This prevents ad-hoc folder creation and enforces modular boundaries early on.
- **Must Follow**: Yes

---

## Proposed Directory Layouts

### 1. `/backend` (FastAPI + Celery + Playwright + GraphQL + PostgreSQL)
```
/backend
├── app/
│   ├── __init__.py
│   ├── main.py                  # FastAPI application initialization and startup events
│   ├── core/
│   │   ├── __init__.py
│   │   ├── config.py            # Pydantic Settings for environment variables validation
│   │   ├── security.py          # Security utilities (e.g., API key validations if needed)
│   │   └── database.py          # SQLAlchemy engine, session maker, and Base model declarations
│   ├── models/
│   │   ├── __init__.py
│   │   └── hackathon.py         # SQLAlchemy ORM models (defining tables, fields, and compound unique indexes)
│   ├── schemas/
│   │   ├── __init__.py
│   │   └── hackathon.py         # Pydantic validation schemas (requests, responses, and validation rules)
│   ├── api/
│   │   ├── __init__.py
│   │   ├── deps.py              # FastAPI dependencies (database session providers, authentication checks)
│   │   └── v1/
│   │       ├── __init__.py
│   │       ├── api.py           # Centralized API router routing all sub-endpoints
│   │       └── endpoints/
│   │           ├── __init__.py
│   │           └── hackathons.py# REST API routes (e.g., GET /hackathons, POST /hackathons/{id}/report)
│   ├── scraper/
│   │   ├── __init__.py
│   │   ├── base.py              # Scraper interface class and base configuration
│   │   ├── devpost.py           # Scraper specifically for Devpost (REST JSON and rules parsing)
│   │   ├── devfolio.py          # Playwright-based scraper for Devfolio
│   │   ├── hackerearth.py       # Playwright-based scraper for HackerEarth
│   │   ├── dorahacks.py         # Frontend scraper / internal REST API client for DoraHacks
│   │   └── gitcoin.py           # Gitcoin indexer client (contains GraphQL query schemas to fetch grants)
│   ├── tasks/
│   │   ├── __init__.py
│   │   ├── celery_app.py        # Celery client initialization and broker configuration
│   │   └── crawler.py           # Celery tasks (cron scrapers execution, messaging dispatcher)
│   └── services/
│       ├── __init__.py
│       ├── fcm.py               # Firebase Cloud Messaging service helper (sends push Data Messages)
│       └── hackathon_service.py # Core business logic (coordinating scraper runs, database saving, and push alerts)
├── tests/
│   ├── __init__.py
│   ├── conftest.py              # Pytest configuration and database fixtures
│   ├── test_api.py              # FastAPI endpoint integration tests
│   └── test_scraper.py          # Unit tests for parsing logic
├── Dockerfile                   # Multistage Docker build file for FastAPI app and Celery worker
├── docker-compose.yml           # Local development stack (FastAPI, Postgres, Redis, Celery worker)
├── requirements.txt             # Python dependency manifest
└── README.md                    # Backend setup and local development instructions
```

### 2. `/android` (Kotlin + Jetpack Compose + Retrofit + Room + FCM)
```
/android
├── app/
│   ├── build.gradle.kts         # Core app-level build configuration and dependency declarations
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           └── java/com/hackathon/hunter/
│               ├── HackathonHunterApp.kt  # Base Application class, responsible for Hilt DI setup
│               ├── data/
│               │   ├── local/
│               │   │   ├── RoomAppDatabase.kt   # Central Room DB definition
│               │   │   ├── dao/
│               │   │   │   └── HackathonDao.kt  # Database DAOs (insert, query, delete)
│               │   │   └── entity/
│               │   │       └── HackathonEntity.kt # Local SQLite database schema representation
│               │   ├── remote/
│               │   │   ├── HackathonApiService.kt # Retrofit interface for communicating with FastAPI
│               │   │   └── dto/
│               │   │       └── HackathonDto.kt  # Raw API response models
│               │   └── repository/
│               │       ├── HackathonRepository.kt # Interface for fetching, caching, and filtering events
│               │       └── HackathonRepositoryImpl.kt # Impl coordinating Room cache and remote API calls
│               ├── di/
│               │   ├── DatabaseModule.kt  # Hilt module for Room components
│               │   ├── NetworkModule.kt   # Hilt module for Retrofit client
│               │   └── RepositoryModule.kt# Hilt module mapping interfaces to repository implementations
│               ├── notifications/
│               │   └── FCMService.kt      # FirebaseMessagingService to intercept incoming Data Messages
│               └── ui/
│                   ├── MainActivity.kt    # Main activity launching the Jetpack Compose app
│                   ├── theme/             # Color, Type, Shape definitions for styling
│                   ├── components/        # Reusable custom UI components (e.g. Cards, chips)
│                   ├── navigation/        # App navigation graph using Jetpack Compose Navigation
│                   └── screens/
│                       ├── list/          # Hackathon listings screen
│                       │   ├── HackathonListScreen.kt
│                       │   └── HackathonListViewModel.kt
│                       ├── detail/        # Hackathon detailed view
│                       │   ├── HackathonDetailScreen.kt
│                       │   └── HackathonDetailViewModel.kt
│                       └── settings/      # User settings (filtering rules, notifications config)
│                           ├── SettingsScreen.kt
│                           └── SettingsViewModel.kt
```

---

## Model & Schema Requirements
To support manual reporting/crowdsourcing and prize tracking, the database schema (SQLAlchemy model in `/backend/app/models/hackathon.py` and Room Entity in `/android/app/src/main/java/com/hackathon/hunter/data/local/entity/HackathonEntity.kt`) must define the following fields:

- **`prize_type`**: String (`VARCHAR(50)`), restricted to `'fiat'` or `'crypto'` to differentiate cash awards from token grants.
- **`prize_currency`**: String (`VARCHAR(20)`), e.g., `'USD'`, `'USDC'`, `'ETH'`, `'SOL'`, `'TON'`, representing the valuation asset.
- **`prize_value`**: Numeric/Float (`NUMERIC(15, 2)` or `Double`), representing the numeric value or denomination amount of the prize pool.
- **`is_vietnam_eligible`**: Boolean, defaults to `True`. Indicates if the hackathon is open to developers in Vietnam.
- **`report_count`**: Integer, defaults to `0`. Accumulates manual reports indicating Vietnam ineligibility.

---

## Integration Points

| Point | File | Function | New Code Location |
| :--- | :--- | :--- | :--- |
| **Database Deduplication** | `/backend/app/models/hackathon.py` | Implement compound unique constraints to filter duplicates. | SQLAlchemy model defining a unique constraint: `UniqueConstraint('platform', 'platform_id', name='uq_platform_platform_id')` |
| **DoraHacks Scraper** | `/backend/app/scraper/dorahacks.py` | Crawl DoraHacks listings utilizing HTML selectors / internal endpoints. | Parses events list and detailed rules from `dorahacks.io`. |
| **Gitcoin GraphQL Query** | `/backend/app/scraper/gitcoin.py` | Query Gitcoin Indexer API programmatically via GraphQL. | Performs POST requests with GraphQL queries to `https://grants-stack-indexer-v2.gitcoin.co/graphql`. |
| **Worker Cron Scrapers** | `/backend/app/tasks/crawler.py` | Asynchronous crawler orchestration running periodically. | Celery tasks executing the unified scraping queue (Devpost, Devfolio, HackerEarth, DoraHacks, Gitcoin). |
| **Crowdsourced Ineligibility Reporting** | `/backend/app/api/v1/endpoints/hackathons.py` | Increments reporting counters to mark ineligibility. | Implements endpoint `POST /api/v1/hackathons/{id}/report` which updates `report_count`. If `report_count >= 3`, sets `is_vietnam_eligible = False`. |
| **Push Notifications Dispatch** | `/backend/app/services/fcm.py` | Dispatch high-priority data payloads via FCM admin SDK. | Service class converting raw models to JSON payload data messages and invoking the Firebase Admin client. |
| **Data Messages Interception** | `/android/app/src/main/java/com/hackathon/hunter/notifications/FCMService.kt` | Intercept incoming FCM data payload and trigger local checks. | Inherits `FirebaseMessagingService.onMessageReceived()`. Checks locally saved interest tags before invoking `NotificationManager`. |
| **Offline Local Caching** | `/android/app/src/main/java/com/hackathon/hunter/data/repository/HackathonRepositoryImpl.kt` | Coordinate Room DB and FastAPI REST responses. | Repository implementation executing local database cache-first retrieval and caching REST API data locally. |

---

## Conventions
- **Naming**:
  - *Backend (Python)*: Follow PEP 8 style. Use `snake_case` for all file names, functions, variables, and modules. Use `PascalCase` for classes, and `UPPER_SNAKE_CASE` for global environment constants.
  - *Android (Kotlin)*: Use Kotlin style. Use `PascalCase` for classes, interface, Compose components, and filenames. Use `camelCase` for variable names and methods. Room entities should end with `Entity` (e.g. `HackathonEntity.kt`), Retrofit DTOs with `Dto` (e.g. `HackathonDto.kt`).
- **File organization**:
  - *Backend*: Highly modular structure separated by layer (models, schemas, scraper, tasks, services). Keep business logic in `services/` and REST definitions in `api/`.
  - *Android*: Standard package structures grouped by purpose. The data layers reside under `data/`, UI code is split into specific sub-packages inside `ui/screens/` following the MVVM architecture patterns.

---

## Warnings
- ⚠️ **Playwright Headless Blocking**: Devfolio, DoraHacks, and HackerEarth have strict anti-scraping protections. The Playwright scripts inside `/backend/app/scraper/` must load randomized user agents, incorporate custom delays, and run headlessly in a stable Docker container containing browser dependencies.
- ⚠️ **FCM Data Delivery Latency**: Make sure to declare `"priority": "high"` in FCM payloads inside `/backend/app/services/fcm.py`. If normal priority is used, Android OS will queue messages and delay them, failing the real-time requirements.
- ⚠️ **Unique Indexes Handling**: The FastAPI service must handle postgres integration errors gracefully. When trying to insert an existing `(platform, platform_id)` entry, catch `IntegrityError` to ignore duplicates instead of raising a server exception.
- ⚠️ **Gitcoin GraphQL Rate Limits**: The Gitcoin GraphQL indexer might rate limit heavy clients; use batch querying, caching, and fetch incremental updates (since last crawled timestamp) to avoid IP bans.
- ⚠️ **Reporting Abuse / Spam Prevention**: The endpoint `/api/v1/hackathons/{id}/report` needs to be secured (e.g., using client device ID checks, user auth, or IP rate limiting) to prevent malicious users from spam-reporting legitimate hackathons as ineligible.
