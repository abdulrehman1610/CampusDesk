# Viva Notes: Campus Desk (Student Complaint & Notice Board)

This file is updated after every milestone. For each new file there is a short explanation and one likely viva question with its answer.

---

## Milestone M0: Setup and design system

**Project check:** Empty Views Activity, **Java** (not Kotlin), **XML** layouts (not Compose), minSdk 24, package `com.example.myapplication`.

**Decisions made (defaults from the implementation plan):**
- App name shown on the phone: **Campus Desk** (only the label changed, the package stays the same).
- Complaint categories use the design names: Hostel & Mess, Transport, Academic, Fee & Dues, Library, General.
- Minimum password length: **6** characters (from the PRD).
- The app uses a **light theme only**, because the Figma design is light.

### `gradle/libs.versions.toml` and `app/build.gradle.kts`
Added two libraries the PRD allows: **RecyclerView** (efficient scrolling lists) and **ViewPager2** (swipeable admin tabs). The version catalog (`libs.versions.toml`) keeps every version number in one place, and `build.gradle.kts` uses them through `libs.recyclerview` and `libs.viewpager2`.
- **Q:** Why add RecyclerView as a separate dependency?
  **A:** RecyclerView is not part of the core Android framework. It lives in the AndroidX library `androidx.recyclerview`, so we must declare it in Gradle before we can use it.

### `res/values/themes.xml`
The app theme. It extends `Theme.Material3.Light.NoActionBar` and sets the brand colors (crimson primary, midnight secondary) and the default font (Plus Jakarta Sans). `NoActionBar` is used because each screen adds its own `Toolbar`. The `values-night` version was removed so the app always uses the light design.
- **Q:** What is a theme and how is it different from a style?
  **A:** A style applies to one view (for example one TextView). A theme applies to the whole app or Activity. Every view reads its default colors and font from the theme.

### `res/values/colors.xml`
Every color from the Figma design gets a name here (`crimson`, `midnight`, `input_bg`, `status_pending`…). Layouts use `@color/name` instead of hex codes.
- **Q:** Why not write `#E02040` directly in the layout?
  **A:** The PRD forbids hardcoded colors. With named colors, a change in one place updates the whole app, and the names make the layouts easier to read.

### `res/values/dimens.xml`
Shared sizes: spacing (8dp, 16dp, 24dp), corner radius, button heights and text sizes.
- **Q:** What is the difference between `dp` and `sp`?
  **A:** `dp` (density-independent pixels) keeps sizes the same on every screen density. `sp` works like dp but also follows the user's font-size setting, so it is used for text.

### `res/values/styles.xml`
Reusable styles for text (`Text.Heading`, `Text.Body`, `Text.Label`…), buttons (`Button.Primary` = crimson pill), inputs (`Input.Pill` = light blue rounded field) and list cards (`Card.List`).
- **Q:** What is the benefit of `style="@style/Button.Primary"`?
  **A:** We write the size, color, font and corner radius once and reuse them on every button. Layouts stay short and every button looks the same.

### `res/values/strings.xml`
All text shown to the user. Screens add their own strings as they are built.
- **Q:** Why keep text in `strings.xml`?
  **A:** It keeps text out of the code, makes translation possible (for example a `values-ur/strings.xml` for Urdu), and the PRD requires it.

### `res/font/` (Plus Jakarta Sans + JetBrains Mono)
The font files used in the Figma design. `plus_jakarta_sans.xml` is a **font family** that groups the five weights (400–800), so `textStyle="bold"` automatically picks the Bold file. JetBrains Mono is used for ticket ids like `#C-0042`.
- **Q:** Why are there both `android:` and `app:` attributes in the font family file?
  **A:** `android:` attributes work on API 26 and above. `app:` attributes are read by the AppCompat library so the fonts also work on API 24–25 (our minSdk is 24).

### `res/drawable/bg_*.xml` and `res/color/*.xml`
Shape drawables made in XML instead of images: the header gradient, the white rounded sheet, pill inputs, round icon buttons, the bottom dock, status badges (orange Pending, green Resolved) and chips. `bg_segment_option.xml` and `segment_text_color.xml` are **selectors**: they change the look when a RadioButton is checked.
- **Q:** What is a selector drawable?
  **A:** An XML file that picks a different drawable or color depending on the view's state (checked, pressed, focused). Android goes through the items top to bottom and uses the first one that matches.

### `utils/Constants.java`
Holds fixed values used in many files: role names (`user`, `admin`), statuses, priorities, categories, Intent extra keys and SharedPreferences keys.
- **Q:** Why use constants instead of typing `"Pending"` everywhere?
  **A:** A typo like `"pending"` would compile but break the logic. With `Constants.STATUS_PENDING` the compiler catches mistakes, and the value can be changed in one place.

### `utils/SessionManager.java`
A wrapper around **SharedPreferences**. It saves the logged-in user's id, name, email, role and the "Remember me" flag (`saveLogin`). It can read them back (`getUserId`, `getRole`, `isLoggedIn`, `isRememberMe`) and clear them (`logout`).
- **Q:** What is SharedPreferences and what do we store in it?
  **A:** A small key-value storage that is private to the app and stays saved after the app closes. We store only session data (user id, name, email, role, remember-me) and the alerts setting. Real records like complaints go in SQLite.

### Package folders
`auth/`, `user/`, `user/fragments/`, `admin/`, `admin/fragments/`, `adapters/`, `database/`, `models/`, `utils/`. Splitting by feature keeps related classes together and makes the project easy to navigate in the viva.


---

## Milestone M1: Database layer (Models + SQLiteOpenHelper)

### `models/User.java`
A plain Java model class (POJO) representing one row in the `users` table (`id`, `name`, `email`, `password`, `role`). It provides constructors, getters, and setters so other classes can pass a user around cleanly instead of raw database cursors.
- **Q:** Why do we create a model class like `User` instead of passing a `Cursor` directly to the UI?
  **A:** A `Cursor` holds an open database resource and must be closed quickly. Copying the row into a simple `User` object lets us close the `Cursor` immediately and work with clean Java methods like `user.getName()`.

### `models/Complaint.java`
Represents one complaint row from the `complaints` table (`id`, `userId`, `title`, `category`, `priority`, `description`, `status`, `reply`, `createdDate`), plus an optional `userName` populated when we `LEFT JOIN` with the `users` table for the Admin view. It also has `getTicketCode()` to format the ID like `#C-0001`.
- **Q:** Why is `userName` stored in the `Complaint` model if there is no `user_name` column in the `complaints` table?
  **A:** In the database we normalize data and only store `user_id` as a foreign key. When the admin views complaints, we run a SQL `LEFT JOIN` with `users` so we can show the student's name on each card without duplicating names in the database.

### `models/Notice.java`
Represents one row in the `notices` table (`id`, `title`, `content`, `createdDate`) and provides `getNoticeCode()` to format the badge like `NTC-0001`.
- **Q:** How does an Adapter use `List<Notice>`?
  **A:** `DatabaseHelper.getAllNotices()` returns a `List<Notice>`. The RecyclerView Adapter takes that list, checks `list.size()` for the item count, and binds each `Notice` object's title, date, and content to a card ViewHolder.

### `database/DatabaseHelper.java`
Extends `SQLiteOpenHelper` to manage `complaint_app.db` (version 1). It enables foreign keys in `onConfigure()`, creates the `users`, `complaints`, and `notices` tables in `onCreate()`, seeds the default admin (`admin@uni.com` / `admin123`) and 2 campus notices, and implements all CRUD and `COUNT(*)` statistics methods using `ContentValues` and `Cursor`.
- **Q:** Why do we extend `SQLiteOpenHelper`, and what do `onCreate()`, `onUpgrade()`, and `onConfigure()` do?
  **A:** `SQLiteOpenHelper` handles opening the database file and tracking its version number. `onConfigure()` runs every time the database opens so we can enable foreign keys (`ON DELETE CASCADE`). `onCreate()` runs only once on first install to create the tables and seed data. `onUpgrade()` runs whenever `DATABASE_VERSION` increases to update the schema.
- **Q:** What does `FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE` do in the `complaints` table?
  **A:** It links every complaint to the user who created it (`users.id`). `ON DELETE CASCADE` tells SQLite that when an admin deletes a student from the `users` table, all complaints belonging to that student are automatically deleted too.


---

## Milestone M2: Authentication (Login, Register, Role Routing, Remember Me, Logout)

### `auth/LoginActivity.java` and `res/layout/activity_login.xml`
Implements the Figma `Login (Crimson Dynamic)` screen using `ScrollView`, `ConstraintLayout`, `LinearLayout`, `RelativeLayout`, and `FrameLayout`. It validates email and password, queries `DatabaseHelper.loginUser()`, saves the session in `SessionManager` (including the "Remember device" `CheckBox`), toggles password visibility via both the "Show password" `CheckBox` and the eye icon, and routes to `UserDashboardActivity` or `AdminDashboardActivity` using an explicit `Intent`.
- **Q:** How do we clear the back stack after login so pressing the Back button on the dashboard does not take the user back to the Login screen?
  **A:** Before calling `startActivity(intent)`, we set `intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)` and then call `finish()` on `LoginActivity`.

### `auth/RegisterActivity.java` and `res/layout/activity_register.xml`
Implements the Figma `Register (Crimson Dynamic)` screen. It validates that no field is empty, checks the email format with `Patterns.EMAIL_ADDRESS`, verifies the password is at least 6 characters, confirms both passwords match, checks `!dbHelper.emailExists(email)`, updates a 4-bar password strength meter with a `TextWatcher`, and inserts the new student with role `"user"`.
- **Q:** How does inline form validation work in Android Views?
  **A:** We call `editText.setError("message")` and `editText.requestFocus()`. Android automatically displays an error icon and popup message right inside that `EditText` without crashing or leaving the screen.

### `MainActivity.java` (Launcher / Session Router)
Runs first when the app icon is tapped (`MAIN` / `LAUNCHER` intent-filter in `AndroidManifest.xml`). It reads `SessionManager`: if `isLoggedIn()` and `isRememberMe()` are both true, it forwards directly to `AdminDashboardActivity` or `UserDashboardActivity`; otherwise it opens `LoginActivity` and calls `finish()`.
- **Q:** Why use `MainActivity` as a router instead of making `LoginActivity` the launcher?
  **A:** Having a dedicated router checks `SharedPreferences` before drawing any UI. When "Remember me" is ticked, the user goes straight to their dashboard without seeing a flash of the Login screen.

### `user/UserDashboardActivity.java`, `admin/AdminDashboardActivity.java`, and `res/menu/menu_toolbar.xml`
Dashboard shells for students and admins. Each dashboard checks `sessionManager.getRole()` inside `onCreate()` (redirecting to `LoginActivity` if the role does not match) and attaches a `MaterialToolbar` that inflates `menu_toolbar.xml` (`onCreateOptionsMenu` / `onOptionsItemSelected`) so tapping **Logout** clears `SharedPreferences` and returns to `LoginActivity`.
- **Q:** How is the Admin section protected from normal student users?
  **A:** At two levels: first, `LoginActivity` checks `user.getRole()` (`"user"` vs `"admin"`) to decide which dashboard to open; second, `AdminDashboardActivity.onCreate()` verifies `Constants.ROLE_ADMIN.equals(sessionManager.getRole())` and immediately redirects back to `LoginActivity` if it does not match.


---

## Milestone M3: Admin Shell + Notices CRUD

### `admin/AdminDashboardActivity.java` and `res/layout/activity_admin_dashboard.xml`
Implements the Admin Portal shell with the curved Midnight-to-Crimson header, live recorded-complaints micro-bar, `MaterialToolbar` Logout menu, and a pill-styled `TabLayout` connected to a `ViewPager2` via `TabLayoutMediator`.
- **Q:** How do `TabLayout` and `ViewPager2` stay synchronized when the user swipes or taps a tab?
  **A:** We use `TabLayoutMediator(tabLayout, viewPager, (tab, position) -> ...)`. Calling `.attach()` sets the title of each tab and automatically scrolls `TabLayout` when `ViewPager2` is swiped (and vice versa).

### `adapters/AdminPagerAdapter.java`
Extends `FragmentStateAdapter` to supply the 4 fragments for `ViewPager2`: `OverviewFragment` (0), `AdminComplaintsFragment` (1), `AdminNoticesFragment` (2), and `UsersFragment` (3).
- **Q:** What does `FragmentStateAdapter` do with `ViewPager2`?
  **A:** It manages the lifecycle of each tab's `Fragment`, creating a fragment only when needed and saving/restoring its state when the user swipes across tabs so memory is used efficiently.

### `adapters/NoticeAdapter.java` and `res/layout/item_notice.xml`
A `RecyclerView.Adapter` with a static `NoticeViewHolder` that binds each `Notice` object (`getNoticeCode()`, `getCreatedDate()`, `getTitle()`, `getContent()`) to a MaterialCardView with a 4dp crimson left accent bar. It accepts an `isAdmin` boolean so the same adapter and card layout can show Edit/Delete icon buttons for the Admin and hide them for Students in Milestone M4.
- **Q:** Why is `RecyclerView` more efficient than an old `ListView` without a ViewHolder?
  **A:** `RecyclerView` enforces the **ViewHolder pattern** (`findViewById` runs only once when a row view is created in `onCreateViewHolder`, not on every scroll frame) and recycles off-screen item views in `onBindViewHolder`.

### `admin/fragments/AdminNoticesFragment.java` and `res/layout/fragment_admin_notices.xml`
Displays all notices from SQLite in a `RecyclerView`, updates the total badge count, filters notices live as the admin types in the search bar, shows an empty-state `TextView` when there are no matches, refreshes automatically in `onResume()`, launches `AddEditNoticeActivity` from the `+ New Notice` `ExtendedFloatingActionButton`, and confirms deletions with an `AlertDialog`.
- **Q:** Why do we call `loadNotices()` inside `onResume()` in `AdminNoticesFragment`?
  **A:** When the admin opens `AddEditNoticeActivity` to add or edit a notice and then finishes that activity, `AdminNoticesFragment` resumes. Reloading in `onResume()` ensures the `RecyclerView` immediately reflects the new or updated notice from SQLite.

### `admin/AddEditNoticeActivity.java` and `res/layout/activity_add_edit_notice.xml`
A single Activity used for both publishing a new notice and editing an existing notice (PRD D7). It reads `Constants.EXTRA_MODE` (`"add"` or `"edit"`) and `Constants.EXTRA_NOTICE_ID` from `getIntent()`. In edit mode it loads the existing notice from `DatabaseHelper.getNoticeById(noticeId)`, pre-fills the `EditText` fields, changes the button label to **Update Notice**, and reveals a **Delete This Notice** button.
- **Q:** How does one Activity handle both "Add Notice" and "Edit Notice"?
  **A:** The caller passes `intent.putExtra(Constants.EXTRA_MODE, Constants.MODE_EDIT)` and `intent.putExtra(Constants.EXTRA_NOTICE_ID, notice.getId())`. In `onCreate()`, `AddEditNoticeActivity` reads those extras with `getStringExtra` and `getIntExtra` to decide whether to call `dbHelper.addNotice(...)` or `dbHelper.updateNotice(...)`.


---

## Milestone M4: User Shell + Notices (BottomNavigationView, Notices Feed, Detail, Share Intent)

### `user/UserDashboardActivity.java`, `res/layout/activity_user_dashboard.xml`, and `res/menu/menu_bottom_user.xml`
Implements the student dashboard shell matching the Figma header and floating midnight bottom dock (`Notices`, `New`, `Complaints`, `Profile`). When the student taps a bottom navigation item, `UserDashboardActivity` updates the Toolbar subtitle and runs a `FragmentManager` transaction (`getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerUser, selectedFragment).commit()`) to swap the active screen.
- **Q:** What is the difference between an `Activity` and a `Fragment`, and why do we use Fragments for the bottom navigation tabs?
  **A:** An `Activity` is a full standalone screen with its own window, while a `Fragment` is a reusable sub-screen that lives inside an Activity. Using Fragments lets the top Toolbar and bottom navigation dock stay in place while only the middle content area swaps, which is faster and smoother than launching 4 separate Activities.

### `user/fragments/NoticesFragment.java` and `res/layout/fragment_notices.xml`
Implements the Figma `Notices (Crimson Dynamic)` screen (`2:467`). It shows the **Urgent Broadcast** banner for the most recent notice, a glass search bar (`TextWatcher`) that filters notices live by title/content/code (PRD U4), an empty-state message when no notices match (PRD U11), and a `RecyclerView` powered by `NoticeAdapter` (`isAdmin = false`). Tapping any notice card opens `NoticeDetailActivity` with `EXTRA_NOTICE_ID`.
- **Q:** How does `NoticesFragment` pass the selected notice to `NoticeDetailActivity`?
  **A:** Using an **explicit Intent** with an extra: `Intent intent = new Intent(requireContext(), NoticeDetailActivity.class); intent.putExtra(Constants.EXTRA_NOTICE_ID, notice.getId()); startActivity(intent);`.

### `user/NoticeDetailActivity.java` and `res/layout/activity_notice_detail.xml`
Reads `Constants.EXTRA_NOTICE_ID` via `getIntent().getIntExtra(...)`, loads the full `Notice` from `DatabaseHelper.getNoticeById(noticeId)`, displays its code, date, full title, and full content, and provides a **Share Notice** button that launches Android's system share sheet using an **implicit Intent** (`Intent.ACTION_SEND`).
- **Q:** What is the difference between an explicit Intent and an implicit Intent? Give an example from this app.
  **A:** An **explicit Intent** names the exact target Activity class inside our app (e.g., `new Intent(requireContext(), NoticeDetailActivity.class)`). An **implicit Intent** declares a general action and data type (e.g., `new Intent(Intent.ACTION_SEND)` with `type = "text/plain"` and `EXTRA_TEXT`) so Android shows a chooser sheet of external apps (WhatsApp, Gmail, Messages) that can handle sharing text.


---

## Milestone M5: User Complaints (New Complaint GridView + RadioGroup, My Complaints, Detail, Edit with ActivityResultLauncher, Delete)

### `adapters/CategoryGridAdapter.java` and `res/layout/item_category_grid.xml`
A custom `BaseAdapter` that displays the 6 complaint categories (`Hostel & Mess`, `Transport`, `Academic`, `Fee & Dues`, `Library`, `General`) inside a 2-column `GridView` on `NewComplaintFragment`. It tracks `selectedPosition` and highlights the chosen tile with a soft pink background, crimson border, crimson icon circle, and a white checkmark badge.
- **Q:** What is the difference between `GridView` and `RecyclerView`, and how does `BaseAdapter` work?
  **A:** `GridView` arranges items in a two-dimensional grid of rows and columns using a `BaseAdapter`, where `getView(position, convertView, parent)` inflates or reuses a tile view for each position. `RecyclerView` is more flexible (supporting linear, grid, or staggered LayoutManagers) and enforces the `ViewHolder` pattern.

### `user/fragments/NewComplaintFragment.java` and `res/layout/fragment_new_complaint.xml`
Implements the Figma `New Complaint (Crimson Dynamic)` screen (`2:253`). It collects the complaint title, the category from the `GridView`, the urgency priority from a 3-option pill `RadioGroup` (`Low`, `Medium`, `High`), and a detailed description with a live `0/500` character counter (`TextWatcher`). Submitting saves the complaint in SQLite with initial status `"Pending"` and switches the bottom navigation bar to `MyComplaintsFragment`.
- **Q:** How do we read which `RadioButton` is selected inside a `RadioGroup`?
  **A:** We call `rgPriority.getCheckedRadioButtonId()` and compare the returned resource ID against `R.id.rbPriorityLow`, `R.id.rbPriorityMedium`, and `R.id.rbPriorityHigh`.

### `adapters/ComplaintAdapter.java` and `res/layout/item_complaint.xml`
A `RecyclerView.Adapter` that binds each `Complaint` object to a MaterialCardView with a 6dp status accent stripe, monospace ticket ID (`#C-0001`), colored status badge (**Pending** = orange `#F59E0B`, **Resolved** = green `#16A34A` per PRD Section 9), colored priority label (**Low** = green, **Medium** = orange, **High** = red), category tag, and date.

### `user/fragments/MyComplaintsFragment.java` and `res/layout/fragment_my_complaints.xml`
Implements the Figma `My Complaints (Crimson Dynamic)` screen (`2:698`). It populates the 3 header stat tiles (`Total Filed`, `Pending`, `Resolved`) using `DatabaseHelper` `COUNT(*)` queries for the logged-in student, filters complaints live by search text and category chips, sorts them using a `Spinner` (`Sort by Date`, `Sort by Priority`, `Pending First`), and wires the **Call Desk** button to an implicit `Intent.ACTION_DIAL`.
- **Q:** How does the **Call Desk** button demonstrate a second implicit Intent?
  **A:** It creates `new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+924235761999"))`, which asks Android to open the phone dialer app with the campus help desk number pre-filled, without needing special call permissions.

### `user/ComplaintDetailActivity.java`, `user/EditComplaintActivity.java`, `res/layout/activity_complaint_detail.xml`, and `res/layout/activity_edit_complaint.xml`
`ComplaintDetailActivity` displays every field of a complaint plus the admin reply (or `"No reply yet"`). While the status is `Pending`, it shows **Edit Complaint** and **Delete Complaint** buttons (hiding them and showing a lock message once `Resolved`). Tapping **Edit Complaint** launches `EditComplaintActivity` using `ActivityResultLauncher<Intent>`. When the student saves changes in `EditComplaintActivity`, it calls `setResult(RESULT_OK)` and `finish()`, triggering the launcher callback to refresh the complaint details and list.
- **Q:** How does `ActivityResultLauncher` work when returning data from `EditComplaintActivity`?
  **A:** We register a launcher with `registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> ...)`. When `EditComplaintActivity` updates SQLite and calls `setResult(RESULT_OK); finish();`, the callback sees `result.getResultCode() == RESULT_OK` and immediately reloads the updated complaint from the database.
