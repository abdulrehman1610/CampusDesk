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
