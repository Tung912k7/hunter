## Design: Hackathon Hunter Mobile App

### Overview
The "Hackathon Hunter" Android app is a premium utility designed to aggregate hackathons from major traditional hosting platforms (Devpost, Devfolio, HackerEarth) and decentralized Web3 ecosystems (DoraHacks, Gitcoin, BeWater). The application's core functionality is eligibility tracking, with a focus on identifying events open to developers in Vietnam. Instead of relying purely on automated analysis, the app utilizes a crowdsourced/manual reporting approach where users flag events that restrict Vietnamese citizens. 

The user interface follows a modern, dark-themed, glassmorphic design system developed in Jetpack Compose, emphasizing readability, fluid micro-animations, and strict accessibility standards.

---

### Visual Specifications

#### Component: Hackathon Card (List Screen)
- **Layout**: 
  - Card-based layout with standard margins (`16dp` horizontal, `8dp` vertical spacing between items).
  - Outlined container style with rounded corners (`16dp` corner radius).
  - Internal content padding: `16dp` on all sides.
  - Multi-row layout:
    - **Header Row**: Platform badge (left-aligned), Prize Type indicator tag (left-aligned, next to platform), and bookmark action button (right-aligned, minimum touch target `48x48dp`).
    - **Title & Subtitle Row**: Hackathon title (maximum 2 lines, ellipses overflow) and host/organizer name.
    - **Metadata Grid**: A 2x2 grid representing dynamic info (Prizes & Currencies, Location/Format, Deadline/Countdown, and Vietnamese Eligibility status based on crowdsourced indicators).
    - **Tag Row**: FlowRow displaying relevant tech stack/theme chips (max 3 tags shown on list view).
- **Colors**:
  - **Card Background**: Glassmorphic dark card overlay (`#1E2230` with `70%` opacity / `rgba(30, 34, 48, 0.7)`).
  - **Card Border**: Subtle semi-transparent border (`1dp` width, `#FFFFFF` with `8%` opacity / `rgba(255, 255, 255, 0.08)`).
  - **Platform Badges (Traditional)**:
    - *Devpost*: Background `#00283C` | Text `#6BE0FF` (Electric Teal).
    - *Devfolio*: Background `#273C8F` | Text `#6E88FF` (Neon Indigo).
    - *HackerEarth*: Background `#381F10` | Text `#FFA06B` (Vibrant Amber).
  - **Platform Badges (Web3/Crypto)**:
    - *Gitcoin*: Background `#092E20` | Text `#02E296` (Mint Green).
    - *DoraHacks*: Background `#2E1E09` | Text `#FFA000` (Electric Amber).
    - *BeWater*: Background `#0A1C3C` | Text `#4DA3FF` (Ice Blue).
  - **Prize Type Indicator Tags**:
    - *Fiat*: Border `#45A29E` | Background `rgba(69, 162, 158, 0.15)` | Text `#45A29E` (Slate Cyan).
    - *Crypto*: Border `#7209B7` | Background `rgba(114, 9, 183, 0.15)` | Text `#BB86FC` (Vibrant Purple).
    - *Mixed (Both)*: Border Gradient (`#45A29E` to `#7209B7`) | Background `rgba(255, 255, 255, 0.05)` | Text `#F8F9FA`.
  - **Status Accents & Indicators**:
    - *Vietnam Eligible* (No/low reports): Background `#0C2B1D` | Text/Icon `#00E676` (Emerald Green).
    - *Vietnam Ineligible* (Report count >= 3): Background `#2E1317` | Text/Icon `#FF1744` (Crimson Coral).
    - *Ending Soon* (<3 days): Background `#3B230B` | Text/Icon `#FF9100` (Radiant Orange).
- **Typography**:
  - **Title**: Font Family: Outfit | Size: `18sp` | Weight: SemiBold (`600`) | Line Height: `24sp` | Color: `#F8F9FA`.
  - **Subtitle**: Font Family: Inter | Size: `14sp` | Weight: Regular (`400`) | Line Height: `20sp` | Color: `#B0B3B8`.
  - **Metadata Labels**: Font Family: Inter | Size: `13sp` | Weight: Medium (`500`) | Color: `#D0D3D6`.
  - **Chips / Badges Text**: Font Family: Inter | Size: `11sp` | Weight: Bold (`700`) | Letter Spacing: `0.5sp`.
- **States & Transitions (Reported Ineligible)**:
  - **Default**: Background `rgba(30, 34, 48, 0.7)`, Border `rgba(255, 255, 255, 0.08)`.
  - **Pressed (Active)**: Background scale animation to `97%` scale, overlay opacity increases to `85%`.
  - **Faded/Hidden State (When reported by user)**:
    - Upon reporting, the card's opacity immediately drops dynamically to `15%` (`alpha = 0.15f`) via Compose `animateFloatAsState`.
    - Touch input and clicks are disabled (`enabled = false`).
    - An overlay banner "Đã báo cáo cấm VN" (Reported: Vietnam Ineligible) in Crimson Coral `#FF1744` is displayed across the card.
    - When the list is refreshed or after a `1.5s` delay, the item height shrinks to `0dp` utilizing `animateDpAsState` for a smooth collapse animation.

#### Component: Cryptocurrency and Token Prize Display
- **Layout**:
  - Inline badge element representing the financial structure of the prize.
  - Layout: Horizontal row with token/currency symbol icon (left) and numeric value (right).
  - Symbol representation: Currency-specific icons or labels (e.g. `$` for USD, `Ξ` for ETH, `⟁` for SOL, `₮` for USDT, `USDC`, `TON`).
  - Differentiated color borders and typography to ensure users instantly recognize decentralized token grants vs traditional bank wire cash rewards.
- **Colors**:
  - **USD / Fiat Prize**: Border `#45A29E` (Slate Cyan) | Text `#45A29E` | Cash Icon `#45A29E`.
  - **USDC / USDT Stablecoin**: Border `#2775CA` (USDC Blue) | Text `#6BE0FF` | Circle Dollar Icon.
  - **ETH (Ethereum)**: Border `#627EEA` (Ethereum Purple/Blue) | Text `#8A2BE2` | Diamond Vector Icon.
  - **SOL (Solana)**: Border `#14F195` (Solana Green) | Text `#00E676` | Gradient Icon.
  - **TON (The Open Network)**: Border `#0098EA` (TON Blue) | Text `#0098EA` | Diamond/Gem Vector Icon.
  - **Generic Crypto Token**: Border `#BB86FC` (Vibrant Purple) | Text `#BB86FC` | Hexagonal Icon.
- **Typography**:
  - **Prize Value Text**: Font Family: Outfit | Size: `14sp` | Weight: Bold (`700`) | Color: `#FFFFFF`.
  - **Currency/Asset Denomination**: Font Family: Inter | Size: `11sp` | Weight: SemiBold (`600`) | Color: `#D0D3D6`.
- **States**:
  - **Default**: Outlined style with dynamic asset colors.
  - **Interactive (Clickable detail preview)**: On tapping the prize display, a micro-modal reveals the token distribution breakdown (e.g. "1st place: 3 ETH, 2nd place: 1.5 ETH") with smooth fade-in scaling.

#### Component: Filter Bottom Sheet Modal
- **Layout**:
  - Modal overlay sliding from bottom-to-top covering `75%` of screen height.
  - **Header Row**: "Filters" title (left-aligned) and "Reset All" button (right-aligned, minimum touch target `48x48dp`).
  - **Section 1: Core Target (Vietnamese Eligibility)**: A prominent full-width Switch container with detailed helper text outlining that this hides events reported as ineligible for Vietnam.
  - **Section 2: Prize Type Filter**: Segmented control or horizontal Select Toggles:
    - *All* | *Cash (Fiat) Only* | *Crypto (Tokens) Only*.
  - **Section 3: Platform Selection**: A 3x2 grid of custom Checkbox/Toggle cards representing all 6 sources:
    - Devpost, Devfolio, HackerEarth, DoraHacks, Gitcoin, BeWater.
  - **Section 4: Event Format**: Horizontal Selector Chips ("Online", "In-Person", "Hybrid").
  - **Section 5: Minimum Cash Value (USD Equivalent)**: Interactive horizontal Slider with discrete steps ($0 to $50,000+) and a numerical badge displaying selection.
  - **Section 6: Keywords / Tags**: Scrollable FlowRow of popular tags (AI/ML, Web3, Mobile, Open Source, Social Good) with a text field for custom tag entry.
- **Colors**:
  - **Sheet Background**: Jet Black base (`#0E1116`) with top rounded corners (`24dp` radius).
  - **Core Switch (Vietnam Eligibility)**: Border highlighted in Electric Cyan (`#00F5D4` with `25%` opacity when off, `100%` glow when active).
  - **Segmented Prize Toggles**: Selected option has a background gradient (`#00ADB5` to `#00F5D4`) with dark text (`#0E1116`).
  - **Platform Checkbox Cards**:
    - *Checked*: Outlined border matched to the platform's brand colors (e.g., Mint Green for Gitcoin, Orange for DoraHacks, Deep Navy for Devpost) with a filled background at `12%` opacity.
    - *Unchecked*: Outlined border in `#2A2F3D` with no background fill.
  - **Slider Track**: Active track colored in Gradient (`#00ADB5` to `#00F5D4`), inactive track `#2A2F3D`.
- **Typography**:
  - **Header Title**: Font Family: Outfit | Size: `22sp` | Weight: Bold (`700`) | Color: `#FFFFFF`.
  - **Section Headers**: Font Family: Outfit | Size: `16sp` | Weight: SemiBold (`600`) | Letter Spacing: `0.75sp` | Color: `#B0B3B8`.
  - **Body / Labels**: Font Family: Inter | Size: `14sp` | Weight: Regular (`400`) | Color: `#F8F9FA`.
  - **Platform Chip Names**: Font Family: Inter | Size: `13sp` | Weight: SemiBold (`600`).
  - **Helper/Explanatory Text**: Font Family: Inter | Size: `12sp` | Weight: Regular (`400`) | Line Height: `16sp` | Color: `#8E959E`.
- **States**:
  - **Switch Off**: Slider thumb `#8E959E`, track background `#2A2F3D`.
  - **Switch On**: Slider thumb `#00F5D4`, track background `#002D2B` (Dark Teal).
  - **Platform Selected**: Active brand borders and visual inner checkmark.
  - **Platform Disabled**: Border color `#1A1D24` with text `#6C757D`.

#### Component: Detail Screen Header & Hero
- **Layout**:
  - Non-collapsing banner area displaying a high-contrast generative gradient backdrop representing the contest theme.
  - Height: `220dp`.
  - Overlaid Title: Title sits in the lower `40%` of the hero banner, backed by a semi-transparent gradient mask (Black to Transparent) to guarantee text legibility.
  - Left-aligned back button and right-aligned share button overlaying top header bar (spacing `16dp` from screen edges, touch targets `48x48dp`).
- **Colors**:
  - **Gradient Backdrop**: Dynamic mesh gradient interpolating between Navy Blue (`#0B192C`), Deep Teal (`#1E5F74`), and Dark Purple (`#1D1A39`).
  - **Overlay Mask**: Linear gradient (`rgba(14, 17, 22, 0.0)` top to `rgba(14, 17, 22, 0.95)` bottom).
  - **Back/Share Icons**: Circle container with `#000000` (`40%` opacity) background, white vector icon (`#FFFFFF`).
- **Typography**:
  - **Hero Title**: Font Family: Outfit | Size: `24sp` | Weight: Bold (`700`) | Line Height: `32sp` | Color: `#FFFFFF`.
  - **Countdown Clock**: Font Family: Inter | Size: `14sp` | Weight: Bold (`700`) | Color: `#FF9100` (Amber).
- **States**:
  - **Scrolled Under**: As the detail content scrolls upwards, the header area transitions to a solid dark surface (`#0E1116`) with an Electric Cyan bottom line (`1dp` thickness, `#00F5D4` at `20%` opacity) for visual separation.

#### Component: "Report Ineligible" Dialog & Snackbar Feedback
- **Layout**:
  - **Confirmation Dialog**: Modal AlertDialog overlay with curved corners (`20dp` radius).
    - **Header Row**: Left-aligned warning icon (yellow triangle/flag) next to title text.
    - **Body Content**: Descriptive prompt detailing the consequences of flagging the event as ineligible.
    - **Action Row**: Bottom-right aligned horizontally arranged buttons: "Hủy" (dismiss) and "Báo cáo" (confirm).
  - **Snackbar Message**: Bottom-docked floating panel spanning the width of the screen (margins `16dp` from edges).
    - Layout: Horizontal row with message text (left) and an optional "Hoàn tác" (Undo) action button (right).
- **Colors**:
  - **Dialog Background**: Dark grey slate (`#1A1D24`) with subtle red stroke border (`1dp`, `#FF1744` at `30%` opacity).
  - **Dialog Warning Icon & Confirm Button**: Warning Red (`#FF1744`).
  - **Dialog Dismiss Button**: Muted grey text (`#8E959E`).
  - **Snackbar Background**: Flat Obsidian Black (`#12151B`) with high contrast white text.
  - **Snackbar Action**: Teal accent (`#00F5D4`).
- **Typography**:
  - **Dialog Title**: Font Family: Outfit | Size: `18sp` | Weight: Bold (`700`) | Color: `#FFFFFF`.
  - **Dialog Body**: Font Family: Inter | Size: `14sp` | Weight: Regular (`400`) | Line Height: `20sp` | Color: `#B0B3B8`.
  - **Dialog Button Labels**: Font Family: Outfit | Size: `14sp` | Weight: SemiBold (`600`).
  - **Snackbar Text**: Font Family: Inter | Size: `14sp` | Weight: Medium (`500`) | Color: `#FFFFFF`.
- **States**:
  - **Dialog Open/Closed**: Animates overlay transition (fade-in overlay, scale-up dialog card).
  - **Snackbar Slide**: Animates from the bottom with a slide-and-fade transition (`Modifier.offset` animation). Auto-dismisses after `5` seconds unless "Hoàn tác" is tapped.

#### Component: Sticky Bottom Action Bar (Detail Screen)
- **Layout**:
  - Solid bottom-docked panel containing three core actions:
    1. **Bookmark Toggle**: Outlined square button (left, width `56dp`).
    2. **"Báo cáo cấm VN" (Report Ineligible)**: Outlined secondary warning button with flag icon (middle, width `120dp`).
    3. **"Register/Apply Now"**: Primary CTA button filling the remaining space (right).
  - Layout height: `80dp` including system navigation bar offset.
  - Buttons padding: `12dp` horizontal, `12dp` vertical.
- **Colors**:
  - **Dock Background**: Obsidian black (`#0E1116`) with top border line (`1dp`, `#2A2F3D`).
  - **Main CTA Button**: Gradient background (`#00ADB5` to `#00F5D4`) with dark text (`#0E1116`).
  - **Report Button**: Outlined red border (`1dp` stroke, `#FF1744` with `50%` opacity) with semi-transparent warning background (`rgba(255, 23, 68, 0.05)`) and Red text (`#FF1744`).
  - **Bookmark Icon Button**: Outlined gray (`#2A2F3D`) base.
- **Typography**:
  - **CTA Button Text**: Font Family: Outfit | Size: `15sp` | Weight: Bold (`700`) | Letter Spacing: `1sp` | Color: `#0E1116`.
  - **Report Button Text**: Font Family: Outfit | Size: `13sp` | Weight: SemiBold (`600`) | Color: `#FF1744`.
- **States**:
  - **Default CTA**: Gradient active, scale `100%`.
  - **Pressed CTA**: Scale down to `98%`, color shifts slightly darker (`#008B92`).
  - **Report Button Pressed**: Background opacity increases to `15%` (`rgba(255, 23, 68, 0.15)`), triggers the **Confirmation Dialog**.
  - **Disabled CTA**: Background changes to `#1A1D24` (Slate Grey), text changes to `#6C757D`.

#### Component: Bookmark & Hidden States Empty Views
- **Layout**:
  - Centered vertical layout inside the container.
  - Illustration top (vector asset, maximum size `160x160dp`), followed by a title header, description paragraph, and action shortcut button (e.g., "Browse Active Events").
- **Colors**:
  - **Illustration Tint**: Monochromatic gradients (`#2A2F3D` to `#1E2230`).
  - **Action Button**: Outlined border using Electric Teal (`#00ADB5`).
- **Typography**:
  - **Empty State Header**: Font Family: Outfit | Size: `20sp` | Weight: SemiBold (`600`) | Color: `#F8F9FA`.
  - **Empty State Description**: Font Family: Inter | Size: `14sp` | Weight: Regular (`400`) | Line Height: `20sp` | Color: `#8E959E` | Alignment: Center.
- **States**:
  - **Default**: Static centered view.
  - **Button Interaction**: Highlight border and scale animation on action click.

---

### Responsive / Mobile Specifics

- **Aspect Ratio Adaptability**:
  - The UI uses Jetpack Compose relative sizing, constraints, and dynamic `LazyColumn` heights to fit screens starting from standard `16:9` displays to extra-long `22:9` aspects.
- **Dynamic Font Scale Handling**:
  - All text sizes are declared in Scale-independent Pixels (`sp`). Components like the Hackathon Card use dynamic height configurations to ensure text fits gracefully without overlapping if system font scale is increased up to `150%`.
- **System Bar Integration (Edge-to-Edge)**:
  - The application draws behind the status and navigation bars. System bar status icons use light mode (`darkTheme = true`) to stand out against the dark obsidian backgrounds. Bottom navigation margins automatically adjust utilizing `WindowInsets.navigationBars` padding to prevent overlaying the Android system navigation pill.
- **Foldable & Dual-Pane Layouts**:
  - *Portrait Mode*: Single pane (List view displays, click navigates to full Detail Screen).
  - *Landscape / Foldable Screen (width > 600dp)*: Dual-pane split layout. The list is pinned to the left pane (`40%` width) and clicking an item updates the right pane (`60%` width) representing the Detail Screen dynamically via state hoist, preventing navigation stack overhead.

---

### Accessibility

- **Contrast**:
  - Meets WCAG AA standards.
  - Core body text (`#F8F9FA`) maintains a contrast ratio of `14.5:1` against the main background (`#0E1116`).
  - Metadata labels (`#B0B3B8`) maintains a contrast ratio of `7.2:1` against cards (`#1E2230`).
  - Switch tracks and interactive active indicators guarantee a contrast ratio of at least `4.5:1` relative to their adjacent surface.
- **Focus Indicators**:
  - When utilizing accessibility keyboard navigation (D-pad/Tab):
    - A visible bounding border box (`2dp` stroke width, solid Neon Cyan `#00F5D4`) highlights the active focus target.
    - An inner margin of `2dp` separates the focus border from the content to prevent outline clipping.
- **Touch Targets**:
  - Standardized interactive touch targets meet or exceed Android design guidelines:
    - Main CTA Buttons, bookmark chips, and filter toggles are bounded to a minimum size of `48x48dp`.
    - Dropdown menus, slider thumbs, and close buttons incorporate transparent padding (using Compose `Modifier.padding` or `IconButton` custom bounds) to extend physical touch zones without enlarging visual graphics.
- **Color Blindness Accommodations**:
  - Color is never used as the sole indicator of status or eligibility:
    - *Vietnam Eligible* states display a verified badge icon (SVG Checkmark inside a Shield) alongside green text.
    - *Closed* states show an "Expired" lock icon alongside red text.
    - Platform sources use distinct textual labels inside their chips, not just brand colors.
    - Prize types (fiat vs crypto) display clear typographic labels (`USD`, `ETH`, `USDC`) alongside unique geometric/token icons (Cash vs Hexagonal/Diamond structures).
    - The *Report Ineligible* warning state uses a clear Warning Flag icon alongside red text.
- **Screen Reader Support (TalkBack)**:
  - Every non-text component (e.g., bookmark icon, eligibility icons, platform logos, report warning flags) contains a descriptive, localized `contentDescription` property.
  - Cards utilize semantic merging (`Modifier.semantics(mergeDescendants = true)`) so TalkBack reads the entire card content consecutively rather than forcing the user to swipe through every title, date, and chip manually.
