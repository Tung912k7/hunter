## Research Report: Hackathon Hunter

### Executive Summary
Building the "Hackathon Hunter" application requires a robust backend crawler that aggregates data from Devpost, Devfolio, and HackerEarth, processes this data via the Gemini API to filter by Vietnam eligibility, and pushes updates to the Android app. While Devpost offers a public, undocumented JSON endpoint, Devfolio and HackerEarth lack public listings APIs, necessitating browser automation (Playwright/Puppeteer) for scraping. Processing rules texts using the `gemini-2.5-flash` model with structured JSON schema outputs is highly cost-effective (~$0.002 per event), and FCM Data Messages are recommended to allow the Android client to customize notifications.

### Findings

#### Finding 1: Data Source Integration (Devpost, Devfolio, HackerEarth)
- **Devpost**: Offers a publicly accessible, undocumented REST API endpoint (`https://devpost.com/api/hackathons`) which returns structured JSON. It supports filters like `challenge_type[]=online`, `status[]=open`, `status[]=upcoming`, and sorting via `sort_by=Prize+Amount`. Detailed hackathon rules are not returned in the list endpoint and must be fetched from the HTML at `https://{hackathon-slug}.devpost.com/rules`.
- **Devfolio**: Lacks an official public REST or GraphQL API for discovering hackathons. The website communicates with an internal GraphQL service at `api.devfolio.co`. For reliable data extraction, the crawler must use browser automation (e.g. Playwright) or mimic internal GraphQL payloads. Devfolio has an MCP (Model Context Protocol) server, but it is restricted to personal user/event context using private API keys, not public directory discovery. Rules are retrieved by parsing the markdown contents of the specific event's page.
- **HackerEarth**: Does not provide a public API for listing hackathons; its official API v4 is strictly for asynchronous code compilation and evaluation. The "Events List" API endpoint is for recruiters to manage invite-only tests and requires a partner `client_id` and `client_secret`. Aggregating public hackathons requires browser automation to scrape `https://www.hackerearth.com/challenges/` and follow links to the rules page (typically under a custom tab `/custom-tab/rules/`).
- Source: [Devpost API Scraper](https://apify.com/)
- Source: [Devfolio Platform Documentation](https://devfolio.co)
- Source: [HackerEarth Developer Portal](https://api.hackerearth.com/)
- Confidence: High

#### Finding 2: AI Eligibility Extraction and Cost Estimation
- **AI Extraction Workflow**:
  1. **Clean Text**: Fetch the rules page HTML and extract the raw text, removing headers, footers, scripts, and CSS to minimize token consumption.
  2. **Structured Output**: Query the Gemini API using Structured Outputs (`response_schema`) to enforce a strict JSON structure.
  3. **Analysis Prompt**: Instruct the model to analyze the text for geographic restrictions, citizenship requirements, sanctioned country lists, and specifically "Vietnam".
  4. **Target Schema**:
     ```json
     {
       "type": "object",
       "properties": {
         "is_vietnam_eligible": { "type": "boolean" },
         "eligibility_summary": { "type": "string" },
         "minimum_age": { "type": "integer" },
         "restricted_countries": { "type": "array", "items": { "type": "string" } },
         "supporting_quote": { "type": "string" },
         "confidence_score": { "type": "number" }
       },
       "required": ["is_vietnam_eligible", "eligibility_summary", "confidence_score"]
     }
     ```
- **Gemini Model Choice**:
  - `gemini-2.5-flash` is recommended for high reasoning capabilities and reliable schema compliance. Price: $0.30/1M input tokens, $2.50/1M output tokens.
  - `gemini-2.5-flash-lite` is a budget alternative for basic classifications. Price: $0.10/1M input tokens, $0.40/1M output tokens.
- **Cost Analysis**:
  - Assuming an average rules document of 5,000 input tokens and 200 output tokens:
    - `gemini-2.5-flash` costs $0.0020 per hackathon ($0.0015 input + $0.0005 output).
    - `gemini-2.5-flash-lite` costs $0.00058 per hackathon ($0.0005 input + $0.00008 output).
  - With ~20 new hackathons crawled daily (600/month), monthly Gemini cost is **$1.20** for `gemini-2.5-flash` and **$0.35** for `gemini-2.5-flash-lite`.
- Source: [Google AI for Developers Models & Pricing](https://google.dev/)
- Confidence: High

#### Finding 3: Backend Stack Architecture and Anti-Blocking
- **Backend Stack**: A Python backend using **FastAPI** is highly optimal. FastAPI provides lightweight REST endpoints, while Python has the richest ecosystem for scraping (Playwright-Python, BeautifulSoup) and native support for the official `google-genai` SDK.
- **Task Queue & Cron Crawler**: Use **Celery** with Redis (or APScheduler) to run scraping crons asynchronously and handle AI requests without blocking the API endpoints.
- **Deduplication**: Store hackathons in a **PostgreSQL** database with a compound unique index on `(platform, platform_id)` (e.g. `(devpost, 'ai-hackathon')`). This guarantees that duplicate runs of the cron crawler do not re-insert or re-analyze the same event.
- **Scraper Best Practices**:
  - Respect `robots.txt` crawlers policies.
  - Apply random request delays (jitter) and rotate User-Agent headers.
  - Use residential proxy rotators to bypass Cloudflare protection which secures Devpost and Devfolio.
- Source: [Playwright Python Documentation](https://playwright.dev/python/)
- Source: [Celery Queue Documentation](https://docs.celeryq.dev/)
- Confidence: High

#### Finding 4: Push Notification Infrastructure
- **Infrastructure**: Firebase Cloud Messaging (FCM) is the recommended standard for push notifications on Android.
- **FCM Message Types**:
  - **Notification Messages**: Handled by the system tray when the app is in the background. The app code is not invoked, preventing dynamic local database syncing or metadata filtering.
  - **Data Messages**: Handled entirely by the client application via `FirebaseMessagingService.onMessageReceived()`, whether the app is in the foreground or background.
- **Workflow Recommendation**:
  - The server must send **FCM Data Messages** (with `"priority": "high"` to avoid battery-saver/Doze mode delays).
  - Upon receiving the message, the Android app processes the payload, performs local checks (e.g. matching user-selected tags or custom filters), updates its local database, and displays a customized notification using `NotificationManager`.
- Source: [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- Confidence: High

### Recommendations

1. **Recommended**: Python (FastAPI + Celery + Playwright) with PostgreSQL for the backend stack, because it provides native compatibility with the official Google GenAI SDK and rich scraping libraries, while PostgreSQL's compound indexes guarantee event deduplication.
2. **Recommended**: `gemini-2.5-flash` for the eligibility verification step, because its reasoning capabilities are highly accurate for parsing legal rules documents, and the cost remains extremely low (~$1.20/month for 600 hackathons).
3. **Recommended**: FCM Data Messages with high priority for the Android push notification strategy, because they guarantee that the client app can intercept, parse, filter, and locally cache hackathon updates in the background before rendering a notification.

### Sources
1. [Devpost API Scraper](https://apify.com/) - accessed 2026-05-21
2. [Devfolio Platform Documentation](https://devfolio.co) - accessed 2026-05-21
3. [HackerEarth Developer Portal](https://api.hackerearth.com/) - accessed 2026-05-21
4. [Google AI for Developers Models & Pricing](https://google.dev/) - accessed 2026-05-21
5. [Playwright Python Documentation](https://playwright.dev/python/) - accessed 2026-05-21
6. [Celery Queue Documentation](https://docs.celeryq.dev/) - accessed 2026-05-21
7. [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging) - accessed 2026-05-21
