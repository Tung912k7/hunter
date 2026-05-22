# Implementation Plan: Hackathon Hunter - Phase 2: Background Workers, Scheduling, & Push Notifications

## 📌 User Request (VERBATIM)
### Request 1
> Tạo 1 ứng dụng Android dùng để săn các cuộc thi hackathon trên các trang chuyên host Hackthon. Ứng dụng sẽ lọc các cuộc thi với tiêu chí: Tổ chức online, giải là tiền mặt, cho phép công dân Việt Nam tham gia. Hỏi nếu muốn làm rõ

### Request 2
> Trả lời câu hỏi: 1) Quét từ các trang uy tín và lớn. 2) Phương án A. 3) Thử cách 2. 4) Sử dụng công nghệ có độ tương thích cao với môi trường Android. 5) Có, ngoài ra tôi muốn là khi mở ứng dụng, ứng dụng sẽ hiện list các cuộc thi, có thêm chức năng lọc.

---

## 🎯 Acceptance Criteria (Derived from User Request)
| ID | Criterion | Verification Method |
|----|-----------|---------------------|
| **AC-06** | Real-time push notifications dispatch to the Android client when a new hackathon matches criteria. | Trigger the background task, mock FCM network output, and verify that FCM service logs the message payload sending action. |
| **AC-01** | The scraper runs automatically in the background on a periodic cron schedule. | Verify Celery Beat configurations schedule the scraper task at the correct intervals and that it executes without manual HTTP triggers. |

---

## 📋 Context Summary & Constraints
- **Celery Broker**: Use Redis as the broker and result backend.
- **Background Scaling**: Celery worker runs scrapers for Devpost, Devfolio, HackerEarth, Gitcoin Indexer, DoraHacks, and BeWater. All AI/Gemini processing tasks are removed.
- **FCM Message Format**: Send **FCM Data Messages** (not notifications). Ensure `"priority": "high"` is set so the Android system delivers the message immediately to the client background receiver.
- **Deduplication Handling**: If a scraper runs and discovers an event already in the DB (based on the unique key constraint), it should suppress push dispatch to avoid spamming the user.
- **Prior Deliverables**:
  - Research: [RESEARCH-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/researchers/RESEARCH-hackathon-hunter.md)
  - Scout: [SCOUT-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/scouts/SCOUT-hackathon-hunter.md)
  - Design: [DESIGN-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/designs/DESIGN-hackathon-hunter.md)

---

## Overview
This phase builds the background orchestration layer. It sets up Celery workers to run scrapers periodically, save events to PostgreSQL (defaulting to eligible), and invoke Firebase Cloud Messaging (FCM) to push data alerts to the client app.

---

## Prerequisites
- Phase 1 complete.
- Firebase Console Project setup and `serviceAccountKey.json` downloaded.

---

## Tasks

### Task 2.1: Celery Application Configuration
- **Agent**: `devops-engineer`
- **File(s)**:
  - `/backend/app/tasks/celery_app.py`
  - `/backend/docker-compose.yml` (update)
- **Description**:
  - Initialize the Celery application in `celery_app.py`. Set broker and backend URLs from Pydantic config (`REDIS_URL`).
  - Configure Celery Beat to execute the periodic crawling task. Set default schedule (e.g. every 6 hours).
  - Update `docker-compose.yml` to define a separate `worker` container running `celery -A app.tasks.celery_app worker -B --loglevel=info`.
- **Verification**: Run `docker-compose up --build` and verify the Celery worker logs successful connection to the Redis broker.

---

### Task 2.2: Periodic Scraper Crawler Task
- **Agent**: `backend-engineer`
- **File(s)**:
  - `/backend/app/tasks/crawler.py`
- **Description**:
  - Implement the `run_scrapers_task` Celery task.
  - Sequentially instantiate the scrapers/clients (Devpost, Devfolio, HackerEarth, Gitcoin, DoraHacks, BeWater).
  - For each new hackathon found:
    1. Check the database to see if `(platform, platform_id)` already exists.
    2. If it exists, skip.
    3. If new, save the event in the database (with default columns `is_vietnam_eligible = True`, `report_count = 0`).
    4. Trigger FCM notification dispatch for this event (delegated to `hackathon_service.py`).
- **Verification**: Run `celery -A app.tasks.celery_app call app.tasks.crawler.run_scrapers_task` and verify it scrapes, saves, and skips duplicate events correctly.

---

### Task 2.3: Firebase Admin SDK Integration (FCM Service)
- **Agent**: `backend-engineer`
- **File(s)**:
  - `/backend/app/services/fcm.py`
- **Description**:
  - Install `firebase-admin` dependency.
  - Setup Firebase Admin SDK in `fcm.py` using credentials located at `/backend/secrets/firebase-credentials.json` (load from config path).
  - Create helper class `FCMService` with method `send_hackathon_alert(device_token: str, payload: dict) -> bool` or `broadcast_hackathon_alert(topic: str, payload: dict)`.
  - Ensure the payload is structured inside `data` block only (FCM Data Message) and headers specify priority:
    ```python
    message = messaging.Message(
        data=payload,
        topic=topic,
        android=messaging.AndroidConfig(
            priority="high"
        )
    )
    ```
- **Verification**: Write a test sending a mock payload to a test device token (or a test topic) and check if Firebase API accepts the message.

---

### Task 2.4: Push Notification Data Dispatcher Coordinator
- **Agent**: `backend-engineer`
- **File(s)**:
  - `/backend/app/services/hackathon_service.py`
- **Description**:
  - Implement logic in `hackathon_service` that coordinates database insertion and notification dispatch.
  - The payload sent to FCM should contain:
    - `id`: unique DB ID
    - `title`: Hackathon title
    - `platform`: platform name
    - `is_online`: "true" / "false"
    - `prize_type`: "fiat" / "crypto"
    - `prize_currency`: e.g. "USD", "ETH", "SOL", "TON"
    - `prize_value`: string representation of the numeric amount (e.g. "5000.00")
    - `is_vietnam_eligible`: "true" / "false"
    - `report_count`: "0"
- **Verification**: Verify that the crawler task calls `fcm.send_hackathon_alert` with correct stringified payloads when a database transaction successfully commits a new event.

---

### Task 2.5: Background Tasks & Messaging Tests
- **Agent**: `tester`
- **File(s)**:
  - `/backend/tests/test_tasks.py`
- **Description**:
  - Write test cases for the crawler task using `unittest.mock` to mock Redis, all six scrapers, and the Firebase Admin Client. (All AI/Gemini mock tests are removed).
  - Test that if a DB insertion throws an `IntegrityError` (due to unique index constraints), the crawler handles it, does not send the FCM message, and continues processing other events.
- **Verification**: Run `pytest tests/test_tasks.py` and verify all scenarios pass.

---

## Exit Criteria
- [ ] Celery worker and Celery Beat run correctly in Docker containers.
- [ ] Celery crawler task handles unique constraint errors gracefully without crashing.
- [ ] FCM Service successfully parses model parameters into FCM high-priority Data Message payloads.
- [ ] Pytest integration test suite runs and covers worker task flows.

---

## Risks & Rollback

| Risk | Impact | Probability | Mitigation | Rollback Plan |
|------|--------|-------------|------------|---------------|
| Firebase Admin JSON credentials missing on deploy | High | Medium | Add fallback mock FCM dispatcher that outputs notifications to logs if credentials are not found. | Verify configuration checks credentials file at startup, crash quickly with descriptive error. |
| Redis container crashes or memory leaks | Medium | Low | Configure Redis persistence policies (`appendonly yes`) and set memory limits in Docker. | Rollback to APScheduler running thread-based timers inside the FastAPI web container temporarily. |

### Rollback Action
If Celery queue blocks or Redis memory causes server drop:
1. Stop the celery containers: `docker-compose stop worker`.
2. Fall back to using standard python-background thread executors or FastAPI startup tasks scheduler to run crons sequentially.
