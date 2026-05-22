# Implementation Plan: Hackathon Hunter - Phase 4: Android Jetpack Compose UI Screens

## 📌 User Request (VERBATIM)
### Request 1
> Tạo 1 ứng dụng Android dùng để săn các cuộc thi hackathon trên các trang chuyên host Hackthon. Ứng dụng sẽ lọc các cuộc thi với tiêu chí: Tổ chức online, giải là tiền mặt, cho phép công dân Việt Nam tham gia. Hỏi nếu muốn làm rõ

### Request 2
> Trả lời câu hỏi: 1) Quét từ các trang uy tín và lớn. 2) Phương án A. 3) Thử cách 2. 4) Sử dụng công nghệ có độ tương thích cao với môi trường Android. 5) Có, ngoài ra tôi muốn là khi mở ứng dụng, ứng dụng sẽ hiện list các cuộc thi, có thêm chức năng lọc.

---

## 🎯 Acceptance Criteria (Derived from User Request)
| ID | Criterion | Verification Method |
|----|-----------|---------------------|
| **AC-08** | Render main listings, a detail view, empty bookmark/search states, and a filter sheet. | Launch application, open filters, toggle options, click items, and verify elements are styled as specified in Design Spec. |
| **AC-08** | Render visual branding badges for Gitcoin, DoraHacks, and BeWater on the list and details view. | Verify that cards belonging to Gitcoin, DoraHacks, and BeWater display platform-specific background/text colors. |
| **AC-08** | Render bottom sheet filter controls for Prize Type (segmented toggle) and Platform (3x2 grid). | Verify that the bottom sheet contains a segmented toggle for Prize Type (All, Cash, Crypto) and a 3x2 grid for platforms. |
| **AC-10** | Integrate crowdsourced "Báo cáo cấm VN" flow with confirmations, toasts, and fade-out animations. | Tap the report button, verify dialog confirms, confirm report, check that snackbar pops up, and watch card perform a fade-out animation and exit list. |
| **AC-09** | Support landscape/foldable screen width dimensions (>600dp). | Rotate emulator to landscape or start tablet emulator, and verify split-pane dual view renders. |

---

## 📋 Context Summary & Constraints
- **Visual Theme**: Dark obsidian background (`#0E1116`), glassmorphic cards (`#1E2230` with `70%` opacity, `1dp` translucent borders), typography fonts (Outfit and Inter).
- **Platform Branding & Badging**:
  - *Devpost*: Background `#00283C` | Text `#6BE0FF` (Electric Teal).
  - *Devfolio*: Background `#273C8F` | Text `#6E88FF` (Neon Indigo).
  - *HackerEarth*: Background `#381F10` | Text `#FFA06B` (Vibrant Amber).
  - *Gitcoin*: Background `#0D2E27` | Text `#00FFC4` (Mint Green).
  - *DoraHacks*: Background `#3C2C00` | Text `#FFD700` (Gold / DoraHacks Yellow).
  - *BeWater*: Background `#0D1B2A` | Text `#4CC9F0` (Electric Sky Blue).
- **Report Interaction Flow**:
  - Main cards or detail view have a "Báo cáo cấm VN" button.
  - Clicking shows a confirmation dialog.
  - Confirming initiates the repository reporting process, highlights a success snackbar "Đã gửi báo cáo thành công.", and triggers an `AnimatedVisibility` fade-out exit animation on the card container, removing it from view.
- **Accessibility**: Support TalkBack via semantic merging (`Modifier.semantics(mergeDescendants = true)`), distinct color-blind icon indicators (Shield-checkmark for Vietnam eligible), and contrast ratios meeting WCAG AA standards.
- **Dual-Pane Logic**: If screen width is larger than 600dp, show the split-pane. Left side (40% width) lists hackathons, right side (60% width) dynamically displays details of the selected item, preventing navigation stack overhead.
- **Prior Deliverables**:
  - Research: [RESEARCH-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/researchers/RESEARCH-hackathon-hunter.md)
  - Scout: [SCOUT-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/scouts/SCOUT-hackathon-hunter.md)
  - Design: [DESIGN-hackathon-hunter.md](file:///C:/Users/LAPTOP/Desktop/hackathon/reports/designs/DESIGN-hackathon-hunter.md)

---

## Overview
This phase implements the frontend user interface of the Android application. It creates the Compose visual theme, the listings view, the detail layout, the filters bottom sheet, and sets up interactive crowdsourced report buttons with animated card transitions.

---

## Prerequisites
- Phase 3 complete.
- Local repository and data layer configured and compiling.

---

## Tasks

### Task 4.1: UI Theme & Glassmorphic Card Component
- **Agent**: `mobile-engineer`
- **File(s)**:
  - `/android/app/src/main/java/com/hackathon/hunter/ui/theme/Theme.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/ui/theme/Color.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/ui/theme/Type.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/ui/components/HackathonCard.kt`
- **Description**:
  - Setup colors and typography in theme package.
  - Implement `HackathonCard` with a glassmorphic look:
    - Custom rounded container with 1dp border opacity.
    - Platform badges mapping colors for Devpost, Devfolio, HackerEarth, Gitcoin, DoraHacks, and BeWater.
    - Include a "Báo cáo cấm VN" text button (or warning triangle icon) inside the card container with minimum touch size bounds of 48x48dp.
    - Render prize values reflecting currency symbols (e.g. "$5,000", "2.5 ETH", "10,000 USDT").
    - Bookmarking interactive button (scale click transitions).
    - Enable TalkBack semantic merging to read the entire card contents at once.
- **Verification**: Preview the component in Android Studio using `@Preview` and assert accessibility bounds.

---

### Task 4.2: List Screen & Viewmodel
- **Agent**: `mobile-engineer`
- **File(s)**:
  - `/android/app/src/main/java/com/hackathon/hunter/ui/screens/list/HackathonListViewModel.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/ui/screens/list/HackathonListScreen.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/ui/components/EmptyStates.kt`
- **Description**:
  - Implement `HackathonListViewModel` storing filtering states and UI State (Loading, Success, Error, Empty).
  - Implement `HackathonListScreen` containing:
    - LazyColumn displaying hackathon card list. Use `AnimatedVisibility` wrapped around card items, bound to `isReportedByUser` state, to perform smooth fade-out exit animations when reported.
    - Floating action button to open filter bottom sheet modal.
    - SnackbarHost to show dynamic notification toasts (such as "Đã gửi báo cáo thành công.").
    - Swipe-to-refresh to pull latest database synchronizations.
    - Muted EmptyState view with vector illustrations when search queries or bookmarks return zero results.
- **Verification**: Run on emulator, verify swipe-to-refresh executes repository fetches, and bookmarks empty state displays when no items are bookmarked.

---

### Task 4.3: Interactive Filter Bottom Sheet Modal
- **Agent**: `mobile-engineer`
- **File(s)**:
  - `/android/app/src/main/java/com/hackathon/hunter/ui/components/FilterBottomSheet.kt`
- **Description**:
  - Implement a modal bottom sheet using Compose `ExperimentalMaterial3Api` ModalBottomSheet.
  - Layout sections:
    - **Header**: Title and "Reset All" button.
    - **Section 1**: Format Selector (Online/In-person/Hybrid chips).
    - **Section 2**: Segmented Toggle control for Prize Type: "All", "Cash" (fiat only), and "Crypto" (crypto only).
    - **Section 3**: Minimum Prize Value Slider (discrete steps with numerical amount display badge).
    - **Section 4**: Platform Source Toggle Filter: Rendered as a 3x2 grid of chips (Devpost, Devfolio, HackerEarth, Gitcoin, DoraHacks, BeWater).
    - **Section 5**: Keyword text field search input.
  - Bind states directly to ListViewModel.
- **Verification**: Toggle options in the sheet, click "Apply", and verify the list updates instantly based on selected filters.

---

### Task 4.4: Detail Screen & Eligibility Moderation Interactivity
- **Agent**: `mobile-engineer`
- **File(s)**:
  - `/android/app/src/main/java/com/hackathon/hunter/ui/screens/detail/HackathonDetailViewModel.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/ui/screens/detail/HackathonDetailScreen.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/ui/components/ReportConfirmationDialog.kt`
- **Description**:
  - Implement `ReportConfirmationDialog` component: Custom AlertDialog showing warning details ("Bạn có chắc chắn muốn báo cáo cuộc thi này cấm nhà phát triển Việt Nam?") with Confirm and Cancel actions.
  - Create `HackathonDetailScreen` featuring:
    - Hero banner header with dynamic Navy/Teal mesh gradient and title overlay.
    - Display of total prizes with type/currency indicators.
    - Warning/Report card showing crowdsourced reports: Displays `reportCount` values, tells the user they can report it if they find Vietnam is banned in the rules text.
    - Clicking the button reveals the dialog, confirming reports updates state, displays the success toast, and navigates back to list (since it is now hidden).
    - Sticky bottom actions bar (bookmark toggle and register CTA).
- **Verification**: Navigate to details, tap "Báo cáo cấm VN", confirm dialog, and check if app shows snackbar toast and returns to list view successfully.

---

### Task 4.5: Navigation & Multi-pane Foldable Layout
- **Agent**: `mobile-engineer`
- **File(s)**:
  - `/android/app/src/main/java/com/hackathon/hunter/ui/navigation/AppNavigation.kt`
  - `/android/app/src/main/java/com/hackathon/hunter/ui/MainActivity.kt`
- **Description**:
  - Define Jetpack Compose NavHost.
  - Update `MainActivity.kt` to check screen dimensions using `WindowWidthSizeClass`.
  - If screen width is Compact (width < 600dp):
    - Single pane layout. Standard NavHost logic navigates between List Screen and Detail Screen.
  - If screen width is Expanded (width >= 600dp):
    - Dual pane layout. Render split screen: Left side (40% width) remains list, Right side (60% width) loads the details view of the selected hackathon dynamically by hoisting state.
- **Verification**: Run on tablet/foldable emulator or rotate to landscape, and verify list and details load side-by-side.

---

### Task 4.6: UI Screen Integration Tests
- **Agent**: `tester`
- **File(s)**:
  - `/android/app/src/androidTest/java/com/hackathon/hunter/ui/`
- **Description**:
  - Write Espresso/Compose E2E testing scenarios.
  - Assert that:
    1. Clicking "Báo cáo cấm VN" shows the confirmation dialog.
    2. Dismissing the dialog retains the card; confirming the dialog triggers the reporting action, displays a Toast/Snackbar, and fades out the card.
    3. Modifying filters updates items in list.
    4. Accessibility TalkBack nodes exist on cards.
- **Verification**: Run `./gradlew connectedAndroidTest` on the emulator and verify all assertions pass.

---

## Exit Criteria
- [ ] Visual look matches Design spec (dark obsidian, glassmorphism, exact hex colors, and platform brandings).
- [ ] Segmented toggle for Prize Type and 3x2 Platform grid filters operate correctly in the bottom sheet.
- [ ] Reporting a card launches the confirmation dialog, triggers a snackbar toast, and performs a fade-out animation.
- [ ] Landscape/foldable dual pane layout displays on screens > 600dp.
- [ ] Compose E2E tests pass.

---

## Risks & Rollback

| Risk | Impact | Probability | Mitigation | Rollback Plan |
|------|--------|-------------|------------|---------------|
| Slow rendering or list lags with high number of card animations | Medium | Medium | Use `remember` keys for list indexes, and disable scale animations for older Android OS versions. | Simplify glassmorphism rendering by replacing translucent gradients with solid background colors. |
| Dual-pane layout state conflicts during rotation | Medium | Low | Ensure the selected hackathon ID state is saved in ViewModel `SavedStateHandle` to preserve selection on configuration changes. | Disable landscape dual-pane layout temporarily and use standard single pane landscape. |

### Rollback Action
If Compose rendering lags or causes UI thread drops:
1. Revert complex animations and glassmorphic card overlays. Replace with standard Material Card containers.
2. Disable dual-pane layout logic, forcing standard single-pane layout on all screens.
