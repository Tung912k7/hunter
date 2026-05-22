# Implementation Plan: Hackathon Hunter - Phase 3: Android App Foundation (Hilt, Room, Retrofit, FCM Receiver)

## 📌 User Request (VERBATIM)
### Request 1
> Tạo 1 ứng dụng Android dùng để săn các cuộc thi hackathon trên các trang chuyên host Hackthon. Ứng dụng sẽ lọc các cuộc thi với tiêu chí: Tổ chức online, giải là tiền mặt, cho phép công dân Việt Nam tham gia. Hỏi nếu muốn làm rõ

### Request 2
> Trả lời câu hỏi: 1) Quét từ các trang uy tín và lớn. 2) Phương án A. 3) Thử cách 2. 4) Sử dụng công nghệ có độ tương thích cao với môi trường Android. 5) Có, ngoài ra tôi muốn là khi mở ứng dụng, ứng dụng sẽ hiện list các cuộc thi, có thêm chức năng lọc.

---

## 🎯 Acceptance Criteria (Derived from User Request)
| ID | Criterion | Verification Method |
|----|-----------|---------------------|
| **AC-07** | Android app caches hackathons locally using Room for offline availability. | Inspect Room database contents using Android Studio Database Inspector and verify items persist when device has no internet connection. |
| **AC-05** | App fetches hackathons from FastAPI backend REST API. | Set up a test repository call and verify that Retrofit receives the exact JSON (including crypto/fiat prize details) and maps it to Kotlin DTOs. |
| **AC-06** | Receive and filter push notifications locally before display. | Send a mock FCM Data Message payload, trigger `onMessageReceived`, check user preferences (e.g. minimum prize, Vietnam only, crypto/fiat filters) from SharedPreferences/DataStore, and verify notification triggers only when criteria match. |
| **AC-10** | Users can report ineligible hackathons locally, which immediately hides them from their UI list. | Call `reportHackathon(id)` in repository, verify local Room cache is updated to set `isReportedByUser = true`, and check that the event is excluded from user queries. |

---

## 📋 Context Summary & Constraints
- **FCM Interception**: The app MUST use **FCM Data Messages** so `onMessageReceived` is invoked regardless of whether the app is in the foreground or background. This enables checking filter preferences and storing the event in Room before notifying the user.
- **Offline Caching**: Implement a cache-first strategy. The UI should always subscribe to a Flow of data from the Room database. The remote fetch should populate the DB, triggering automatic UI updates.
- **Local User Reporting Cache**: To hide reported events instantly and prevent double-reporting from the same device, the app implements an `isReportedByUser` local field on Room's `HackathonEntity` and tracks it in a `reported_hackathons` table.
- **Dependency Injection**: Use Hilt for DI to maintain clean architectural separation.
- **Prior Deliverables**:
  - Research: [RESEARCH-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/researchers/RESEARCH-hackathon-hunter.md)
  - Scout: [SCOUT-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/scouts/SCOUT-hackathon-hunter.md)
  - Design: [DESIGN-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/designs/DESIGN-hackathon-hunter.md)

---

## Overview
This phase sets up the base of the Android Kotlin application, implementing Dependency Injection via Hilt, local database cache with Room supporting cryptocurrency metadata and local report status logs, network communications using Retrofit, repository coordination, and the background Firebase push messaging receiver service.

---

## Prerequisites
- Android Studio installed.
- Backend API running and reachable by the emulator.

---

## Tasks

### Task 3.1: Hilt DI & Core Build Configuration
- **Agent**: `mobile-engineer`
- **File(s)**:
  - `/android/app/build.gradle.kts`
  - `/android/app/src/main/java/com/hackathon/hunter/HackathonHunterApp.kt`
- **Description**:
  - Add dependency libraries to `build.gradle.kts` (Hilt, Room, Retrofit, OkHttp, Compose, Firebase Messaging).
  - Create the base application class `HackathonHunterApp` extending `Application` and annotate it with `@HiltAndroidApp`.
  - Declare the App class in the `AndroidManifest.xml`.
- **Verification**: Compile and run the empty application on an emulator, ensuring Hilt dependency graph initializes.

---

### Task 3.2: Room Database Setup
- **Agent**: `mobile-engineer`
- **File(s)**:
  - `/android/app/src/main/java/com/hackathon/hunter/data/local/entity/HackathonEntity.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/data/local/entity/ReportedHackathonEntity.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/data/local/dao/HackathonDao.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/data/local/RoomAppDatabase.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/di/DatabaseModule.kt`
- **Description**:
  - Define `HackathonEntity` schema with columns corresponding to API values: `id`, `platform`, `platformId`, `title`, `description`, `url`, `rulesUrl`, `prizeType` (String - fiat/crypto), `prizeCurrency` (String), `prizeValue` (Double), `isOnline`, `startDate`, `endDate`, `isVietnamEligible` (Boolean, default true), `reportCount` (Integer, default 0), `isReportedByUser` (Boolean, default false), `isBookmarked` (local-only), and `createdAt`.
  - Define `ReportedHackathonEntity` containing `hackathonId` (Primary Key, integer) to track local reports history.
  - Create `HackathonDao` interface defining:
    - `fun getHackathons(): Flow<List<HackathonEntity>>` (Filters out events where `isVietnamEligible = false` or `isReportedByUser = true`)
    - `fun insertAll(hackathons: List<HackathonEntity>)`
    - `fun toggleBookmark(id: Int, isBookmarked: Boolean)`
    - `fun markAsReported(id: Int)`
    - `fun insertReportedLog(log: ReportedHackathonEntity)`
    - `fun getReportedLogs(): List<Int>`
    - `fun clearAll()`
  - Define Hilt `DatabaseModule` providing singleton instances of the Room Database and DAOs.
- **Verification**: Write an instrumentation test to insert a list of entities (containing crypto fields and reported states) and verify the Flow emits only active, non-reported events.

---

### Task 3.3: Retrofit Client, DTOs, & Network Module
- **Agent**: `mobile-engineer`
- **File(s)**:
  - `/android/app/src/main/java/com/hackathon/hunter/data/remote/dto/HackathonDto.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/data/remote/HackathonApiService.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/di/NetworkModule.kt`
- **Description**:
  - Define Kotlin data class `HackathonDto` mirroring backend's JSON schemas (mapping `prize_type`, `prize_currency`, `prize_value`, `is_vietnam_eligible`, and `report_count`).
  - Implement `HackathonApiService` interface containing:
    - `@GET("api/v1/hackathons")` with parameters for filters.
    - `@POST("api/v1/hackathons/{id}/report")` to increment the backend's report count.
  - Setup Hilt `NetworkModule` providing `OkHttpClient` and `Retrofit` builder.
- **Verification**: Run an emulator call hitting the backend API and verify data serializations work.

---

### Task 3.4: Repository Implementation
- **Agent**: `mobile-engineer`
- **File(s)**:
  - `/android/app/src/main/java/com/hackathon/hunter/data/repository/HackathonRepository.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/data/repository/HackathonRepositoryImpl.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/di/RepositoryModule.kt`
- **Description**:
  - Define `HackathonRepository` interface supporting `fetchHackathons()`, `toggleBookmark()`, and `reportHackathon(id: Int)`.
  - Implement `HackathonRepositoryImpl` using cache-first approach.
  - Implement `reportHackathon(id: Int)` logic:
    1. Immediately write to Room local DB: update `isReportedByUser = true` on `HackathonEntity` and insert `ReportedHackathonEntity(id)`. This guarantees the UI list updates instantly.
    2. Fire remote Retrofit API call: `POST /api/v1/hackathons/{id}/report`.
    3. On API success return, parse updated `HackathonDto` and save returned `report_count` and `is_vietnam_eligible` fields to local Room record.
- **Verification**: Mock the API endpoint, verify `reportHackathon` makes immediate local DB update, and subsequently updates backend states.

---

### Task 3.5: FCM Messaging Service with Local Client-Side Filters
- **Agent**: `mobile-engineer`
- **File(s)**:
  - `/android/app/src/main/java/com/hackathon/hunter/notifications/FCMService.kt`
- **Description**:
  - Create `FCMService` extending `FirebaseMessagingService`. Register service in `AndroidManifest.xml`.
  - Override `onMessageReceived(remoteMessage: RemoteMessage)`.
  - Extract the data payload.
  - Fetch user preferences.
  - Query local database `getReportedLogs()` or local memory cache. If the incoming event ID is already in the reported logs, discard it immediately.
  - Evaluate criteria locally. If the received event matches user's criteria:
    1. Instantly save the item into the Room database cache.
    2. Build a local system notification using `NotificationCompat.Builder` (including channels, shield/checkmark icon, and clear messaging detailing eligibility and prizes).
- **Verification**: Trigger a mock FCM service execution using a test payload (fiat and crypto models) and verify the notification is triggered only when preferences are matched.

---

### Task 3.6: Database & Routing Unit Tests
- **Agent**: `tester`
- **File(s)**:
  - `/android/app/src/test/java/com/hackathon/hunter/data/repository/HackathonRepositoryImplTest.kt`
  - `/android/app/src/test/java/com/hackathon/hunter/notifications/FCMServiceTest.kt`
- **Description**:
  - Write unit tests mocking Room DAO and Retrofit API Service to assert Repository logic works during offline, online, and report action states.
  - Write test cases asserting that reporting an event immediately sets `isReportedByUser = true` and excludes it from standard list queries.
- **Verification**: Run `gradlew test` and verify tests execute successfully.

---

## Exit Criteria
- [ ] Dependency Injection compiles with Hilt modules.
- [ ] Room database operates offline and successfully stores `prizeType`, `prizeCurrency`, `prizeValue`, and user report states.
- [ ] API client retrieves lists and successfully sends `POST /report` requests.
- [ ] FCM service executes background filtering checks and notifications, ignoring locally reported items.

---

## Risks & Rollback

| Risk | Impact | Probability | Mitigation | Rollback Plan |
|------|--------|-------------|------------|---------------|
| POST report API fails due to network loss after local UI update | Medium | High | Cache the report task locally using WorkManager to retry synchronization with backend when connectivity resumes. | Keep local `isReportedByUser = true` status active to preserve UI clarity; retry in background. |
| Background Doze mode blocks FCM Service | Medium | Low | Ensure backend dispatcher configures FCM messages with `"priority": "high"` and set request permissions. | Use Android WorkManager to trigger periodic background polling checks as a fallback when FCM drops. |

### Rollback Action
If Hilt generation crashes or FCM notifications are blocked:
1. Revert from Hilt DI to manual dependency container setup inside `HackathonHunterApp` constructor.
2. Replace FCM background push notifications with a periodic sync task using WorkManager scheduler.
