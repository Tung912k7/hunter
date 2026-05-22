# Implementation Plan: Hackathon Hunter - Index

## 📌 User Request (VERBATIM)
### Request 1
> Tạo 1 ứng dụng Android dùng để săn các cuộc thi hackathon trên các trang chuyên host Hackthon. Ứng dụng sẽ lọc các cuộc thi với tiêu chí: Tổ chức online, giải là tiền mặt, cho phép công dân Việt Nam tham gia. Hỏi nếu muốn làm rõ

### Request 2
> Trả lời câu hỏi: 1) Quét từ các trang uy tín và lớn. 2) Phương án A. 3) Thử cách 2. 4) Sử dụng công nghệ có độ tương thích cao với môi trường Android. 5) Có, ngoài ra tôi muốn là khi mở ứng dụng, ứng dụng sẽ hiện list các cuộc thi, có thêm chức năng lọc.

---

## 🎯 Acceptance Criteria (Derived from User Request)
| ID | Criterion | Verification Method |
|----|-----------|---------------------|
| **AC-01** | The backend must crawl hackathon events from six platforms: Devpost, Devfolio, HackerEarth, Gitcoin, DoraHacks, and BeWater. | Execute the scraper and indexer tasks and verify data is successfully extracted and saved to the database. |
| **AC-02** | The backend must filter and identify if the event is conducted online. | Verify the `is_online` attribute is correctly parsed and populated for each event in the database. |
| **AC-03** | The backend must extract prize details including prize type, currency, and value. | Verify the `prize_type` (fiat/crypto), `prize_currency` (USD, ETH, SOL, TON, USDC, USDT, etc.), and `prize_value` (numeric) are correctly populated. |
| **AC-04** | The system must support crowdsourced reporting of hackathons that ban Vietnamese developers. | Send `POST /api/v1/hackathons/{id}/report` and verify it increments `report_count`. Verify `is_vietnam_eligible` changes to False when `report_count >= 3`. |
| **AC-05** | The backend must expose REST API endpoints for the Android app to list, filter, search, and report events. | Query `GET /api/v1/hackathons` and check `POST /api/v1/hackathons/{id}/report` routes. |
| **AC-06** | Real-time push notifications must be sent to the Android app when new matching hackathons are found. | Trigger background scraper run, intercept FCM Data Messages on the Android emulator, and verify notifications are displayed based on user criteria. |
| **AC-07** | The Android application must cache list data locally in Room for offline access. | Turn off network connection on the device, launch the app, and verify previously loaded hackathons are still visible. |
| **AC-08** | The Android application must render a main listings view, a details screen, and a filter sheet. | Launch the app, verify lists display, toggle filters, select a hackathon, and verify UI components. |
| **AC-09** | The Android UI must support dual-pane/foldable layout when screen width exceeds 600dp. | Rotate the emulator or launch on a tablet/foldable emulator and verify dual-pane (List on left, details on right) is active. |
| **AC-10** | Users can report ineligible hackathons locally, which immediately hides them from their UI list. | Tap "Báo cáo cấm VN" button, confirm, and verify the card performs a fade-out animation and disappears from list. |

---

## 📋 Context Summary & Constraints
- **Backend Architecture**: FastAPI REST API, SQLAlchemy ORM, PostgreSQL database, Celery task queue, Redis broker, Playwright browser automation, GraphQL clients (for Gitcoin indexer API). All Google Gemini/AI components are removed.
- **Android Architecture**: Kotlin, Jetpack Compose, Retrofit HTTP Client, Room DB local cache, Hilt Dependency Injection, Firebase Cloud Messaging (FCM) client.
- **Crowdsourced Eligibility Logic**:
  - Hackathons initially default to `is_vietnam_eligible = True` and `report_count = 0`.
  - Reporting increments `report_count`. At `>= 3` reports, `is_vietnam_eligible` shifts to `False` globally.
  - The Android client caches reports locally in a `reported_hackathons` table to immediately hide them from the active list of that specific user device.
- **Rules Constraints**:
  - Respect site crawler policies and rate-limits.
  - Implement anti-blocking practices (random jitter, rotated User-Agents, Docker browser environment for Playwright).
  - Use FCM Data Messages with `"priority": "high"` so background sync happens reliably.
  - Apply compound unique constraints in the DB: `UniqueConstraint('platform', 'platform_id', name='uq_platform_platform_id')` to handle scraping duplicates.

---

## 📅 Phase Breakdown

This implementation plan is split into 4 logical, sequential, and self-contained phases:

1. **[PLAN-hackathon-hunter-phase1.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/plans/PLAN-hackathon-hunter-phase1.md)**
   - **Scope**: Core Backend. PostgreSQL Database Setup with prize metrics, Devpost Scraper, Devfolio Scraper, HackerEarth Scraper, Gitcoin Indexer GraphQL client, DoraHacks and BeWater scrapers, crowdsourced report REST API endpoint `POST /api/v1/hackathons/{id}/report`, and FastAPI query endpoints.
   - **Target Files**: `/backend/app/core/`, `/backend/app/models/`, `/backend/app/schemas/`, `/backend/app/api/`, `/backend/app/scraper/`, `/backend/tests/`

2. **[PLAN-hackathon-hunter-phase2.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/plans/PLAN-hackathon-hunter-phase2.md)**
   - **Scope**: Background Worker & Notifications. Celery Workers, Redis Broker integration, periodic cron scheduler for all six platform crawls, Firebase Cloud Messaging helper service, and push notifications dispatcher.
   - **Target Files**: `/backend/app/tasks/`, `/backend/app/services/`, `/backend/tests/`

3. **[PLAN-hackathon-hunter-phase3.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/plans/PLAN-hackathon-hunter-phase3.md)**
   - **Scope**: Android App Infrastructure. Hilt DI setup, Room Local DB Cache with crypto/fiat fields, `reported_hackathons` local table to track user-submitted reports, Retrofit API Client & DTOs with POST report support, Repository pattern, and FCM Receiver Service.
   - **Target Files**: `/android/app/src/main/java/com/hackathon/hunter/data/`, `.../di/`, `.../notifications/`, `/android/app/src/test/`

4. **[PLAN-hackathon-hunter-phase4.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/plans/PLAN-hackathon-hunter-phase4.md)**
   - **Scope**: Android App UI & Screens. Visual design theme, glassmorphic HackathonCard component with platform visual badges, main list view screen, bottom sheet filters, details screen. Integrates the interactive "Báo cáo cấm VN" button, confirmation dialogs, snackbar confirmations, and animated list fade-outs.
   - **Target Files**: `/android/app/src/main/java/com/hackathon/hunter/ui/`, `/android/app/src/androidTest/`

---

## 🛠️ Global Execution Guidelines
- Prioritize TIER 1 execution: Use parallel/domain-specific specialists (`database-architect`, `backend-engineer`, `mobile-engineer`, `tester`) to perform implementation.
- Run verification tests at the end of each phase before marking complete.
- Follow the PEP 8 convention (snake_case) for Python/backend, and Kotlin standard guidelines (CamelCase/PascalCase) for Android.
