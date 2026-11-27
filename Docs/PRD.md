# PRD: Student Complaint & Notice Board App

**Course:** Mobile Application Development (Java + XML)
**Platform:** Native Android, built in Android Studio project, edited with Google Antigravity
**Deliverable:** Almost-functional app with a **User section** and an **Admin section**, plus a viva on the code.

---

## 0. Instructions for the AI Coding Agent (READ FIRST)

The student has **very little Java/XML experience** and must explain every file in a viva. So:

1. Work **one milestone at a time** (Section 12). After each milestone: stop, summarize what was created in 3 to 5 lines, and wait for the student to say **"next"**.
2. Write **simple, beginner-level code**. No clever tricks, no deep abstractions, no one-liner chains. Prefer clarity over brevity.
3. Add a **short comment above every method** and every class explaining what it does.
4. After each milestone, append a section to `/docs/VIVA_NOTES.md`: for every new file, give 2 to 3 lines on what it does and one likely viva question with its answer.
5. Use the **existing project** (package name, `AndroidManifest.xml`, Gradle files) already created from the Android Studio template. Do not create a new project and do not rename the package.
6. Never output placeholders like `// rest of code here`. Every file must be complete and compile.
7. If something would break a rule in Section 2, ask first instead of working around it.

---

## 1. Product Overview

A campus app where **students** submit complaints (hostel, transport, academics, etc.) and read official notices, and an **admin** posts notices and replies to or resolves complaints.

**Goals**
- Demonstrate every topic studied in class (see the coverage matrix in Section 3).
- Have a clean User flow and Admin flow driven by a single login screen.
- Keep all data **offline in SQLite** (no server, no internet).
- Keep the code explainable line by line in a viva.

**Non-goals**
- No web backend, no Firebase, no push notifications, no payments, no image upload (optional later).

---

## 2. Technical Constraints (STRICT)

| Rule | Detail |
|---|---|
| Language | **Java only**. No Kotlin. |
| UI | **XML layouts only**. No Jetpack Compose. |
| Template | The project must be an **Empty Views Activity (Java)** project. If it is Kotlin or Compose, stop and tell the student. |
| minSdk | 24 |
| IDs | Use `findViewById`. **No ViewBinding, no DataBinding.** |
| Database | `SQLiteOpenHelper` + raw `SQLiteDatabase` methods. **No Room.** |
| Architecture | Plain Activities and Fragments. **No ViewModel, LiveData, or Dependency Injection.** |
| Libraries | Only AndroidX AppCompat, ConstraintLayout, Material Components, RecyclerView, ViewPager2. Nothing else. |
| Strings | All UI text in `res/values/strings.xml`. |
| Orientation | Portrait only is fine. |
| Passwords | Stored as plain text for coursework simplicity (be ready to say in the viva that real apps hash passwords). Optional upgrade: SHA-256. |

Dependencies to ensure in `app/build.gradle`: `appcompat`, `material`, `constraintlayout` (already in template), plus add `recyclerview` and `viewpager2`.

---

## 3. Syllabus Coverage Matrix (MUST ALL BE USED)

| Topic studied | Where it is used in this app |
|---|---|
| **Basic views** (TextView, EditText, Button, ImageView, CheckBox, RadioGroup/RadioButton, Spinner) | Login, Register, New Complaint, Profile |
| **Layouts** (ConstraintLayout, LinearLayout, RelativeLayout, FrameLayout, ScrollView, CardView) | Every screen. Use at least 3 different layout types across the app. |
| **ListView** + custom `BaseAdapter`/`ArrayAdapter` | Admin → **Users tab** (list of registered users with delete) |
| **GridView** + custom `BaseAdapter` | User → New Complaint → **Category picker** (grid of category tiles) |
| **RecyclerView** + Adapter + ViewHolder | Notices list, My Complaints list, Admin complaints list, Admin notices list |
| **Spinner** | Status filter (admin), priority/category fallback, sort option |
| **Intents (explicit)** with `putExtra`/`getStringExtra`/`getIntExtra` | Login → dashboards, list → detail screens |
| **Intents (implicit)** | Share a notice via `ACTION_SEND` |
| **Activity result** (`ActivityResultLauncher`) | Edit complaint screen returns `RESULT_OK` so the list refreshes |
| **Fragments** + `FragmentManager` transactions | User dashboard: BottomNavigationView swaps fragments |
| **Fragment adapter** (`FragmentStateAdapter`) with **ViewPager2 + TabLayout** | Admin dashboard tabs |
| **SQLite** (`SQLiteOpenHelper`, CRUD, foreign key, `Cursor`, `ContentValues`) | All data storage |
| **SharedPreferences** | Remember logged-in user and "Remember me" |
| **Menus / Toolbar** | Options menu with Logout |
| **Dialogs / Toast / Snackbar** | Delete confirmation, success and error messages |
| **Activity lifecycle** | Refresh lists in `onResume()` |

---

## 4. Users and Roles

| Role | How created | Access |
|---|---|---|
| **User (student)** | Self-registers | User section only |
| **Admin** | **Seeded** in database on first run (`admin@uni.com` / `admin123`) | Admin section only |

A single `role` column (`"user"` or `"admin"`) decides routing at login. Role checks must also happen when each dashboard opens (if the stored role does not match, send back to Login).

---

## 5. Functional Requirements

Priority: **P0** = must have, **P1** = should have, **P2** = nice to have.

### 5.1 Authentication (common)
| ID | Requirement | Priority |
|---|---|---|
| A1 | Register with name, email, password, confirm password. New accounts always get role `user`. | P0 |
| A2 | Validate: no empty fields, valid email pattern, password ≥ 6 chars, passwords match, email not already used. | P0 |
| A3 | Login with email and password; route to User or Admin dashboard by role via explicit Intent. | P0 |
| A4 | "Show password" CheckBox on Login. | P1 |
| A5 | "Remember me" CheckBox: saves session in SharedPreferences; app opens straight to the dashboard on next launch. | P0 |
| A6 | Logout from the Toolbar menu clears the session and returns to Login (clear back stack). | P0 |
| A7 | `MainActivity` (from the template) acts as a launcher/router: reads SharedPreferences and forwards to Login, User dashboard, or Admin dashboard. | P0 |

### 5.2 User Section
| ID | Requirement | Priority |
|---|---|---|
| U1 | **Notices tab**: RecyclerView of all notices (newest first) in cards showing title, date, short content. | P0 |
| U2 | Tap a notice → **NoticeDetailActivity** (Intent extras) showing full content. | P0 |
| U3 | **Share** button on notice detail using implicit Intent (`ACTION_SEND`). | P1 |
| U4 | Search notices by title using a SearchView or EditText filter. | P1 |
| U5 | **New Complaint tab**: title, description, **category chosen from a GridView** (Hostel, Transport, Academic, Fees, Library, Other), **priority via RadioGroup** (Low/Medium/High). Submit saves to SQLite with status `Pending`. | P0 |
| U6 | **My Complaints tab**: RecyclerView of only the logged-in user's complaints, with a colored status badge (Pending = orange, Resolved = green). | P0 |
| U7 | Tap a complaint → **ComplaintDetailActivity** showing all fields and the **admin reply** (or "No reply yet"). | P0 |
| U8 | Edit a complaint (title, description, priority) **only while Pending**; opened with `ActivityResultLauncher`, returns `RESULT_OK` to refresh the list. | P1 |
| U9 | Delete own complaint (only while Pending) with a confirmation AlertDialog. | P1 |
| U10 | **Profile tab**: show name and email, change password, Logout button. | P1 |
| U11 | Empty-state text when a list has no items. | P0 |

### 5.3 Admin Section
| ID | Requirement | Priority |
|---|---|---|
| D1 | Admin dashboard uses **TabLayout + ViewPager2** with a **FragmentStateAdapter**. Tabs: **Overview, Complaints, Notices, Users**. | P0 |
| D2 | **Overview tab**: stat cards: total complaints, pending, resolved, total notices, total users (SQL `COUNT` queries). | P0 |
| D3 | **Complaints tab**: RecyclerView of all complaints with student name, title, category, priority, status. | P0 |
| D4 | **Spinner** filter: All / Pending / Resolved. | P0 |
| D5 | Tap complaint → **AdminComplaintDetailActivity**: view full complaint, write/update reply, button to mark **Resolved** (or back to Pending). | P0 |
| D6 | Delete a complaint with confirmation dialog. | P1 |
| D7 | **Notices tab**: RecyclerView of notices, **FloatingActionButton** to add, edit and delete via **AddEditNoticeActivity** (same screen for add and edit, mode passed via Intent extra). | P0 |
| D8 | **Users tab**: **ListView** with a custom adapter showing all registered users (name, email, role). Long-press or button to delete a user (admin account cannot be deleted); deleting a user also deletes their complaints. | P0 |
| D9 | Search/sort complaints (e.g. by priority or date) using a Spinner. | P2 |

---

## 6. Database Design (SQLite)

Database name: `complaint_app.db`, version `1`. Class: `DatabaseHelper extends SQLiteOpenHelper`.
Enable foreign keys in `onConfigure()` with `db.setForeignKeyConstraintsEnabled(true)`.

```sql
CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL,
  role TEXT NOT NULL            -- 'user' or 'admin'
);

CREATE TABLE complaints (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  title TEXT NOT NULL,
  category TEXT NOT NULL,
  priority TEXT NOT NULL,       -- 'Low' | 'Medium' | 'High'
  description TEXT NOT NULL,
  status TEXT NOT NULL,         -- 'Pending' | 'Resolved'
  reply TEXT,                   -- admin reply, nullable
  created_date TEXT NOT NULL,   -- 'yyyy-MM-dd HH:mm'
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE notices (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  content TEXT NOT NULL,
  created_date TEXT NOT NULL
);
```

**Seed data in `onCreate`:** one admin (`Admin`, `admin@uni.com`, `admin123`, `admin`) and 2 sample notices so the demo is never empty.

**Required `DatabaseHelper` methods**
- Users: `registerUser`, `loginUser` (returns User or null), `emailExists`, `getAllUsers`, `deleteUser`, `updatePassword`
- Complaints: `addComplaint`, `getComplaintsByUser`, `getAllComplaints`, `getComplaintsByStatus`, `getComplaintById`, `updateComplaint`, `replyToComplaint` (sets reply and status), `deleteComplaint`
- Notices: `addNotice`, `getAllNotices`, `getNoticeById`, `updateNotice`, `deleteNotice`
- Stats: `getComplaintCount`, `getComplaintCountByStatus`, `getNoticeCount`, `getUserCount`

Use `ContentValues` for insert/update and `Cursor` for reads. Always close cursors.

---

## 7. Screens and Navigation

```
MainActivity (router, checks SharedPreferences)
 ├─ LoginActivity ──── RegisterActivity
 │      │
 │      ├─(role=user)──► UserDashboardActivity  [BottomNavigationView + FragmentManager]
 │      │                 ├─ NoticesFragment ──► NoticeDetailActivity ──► (Share intent)
 │      │                 ├─ NewComplaintFragment  (GridView categories + RadioGroup)
 │      │                 ├─ MyComplaintsFragment ──► ComplaintDetailActivity ──► EditComplaintActivity (result)
 │      │                 └─ ProfileFragment (change password, logout)
 │      │
 │      └─(role=admin)─► AdminDashboardActivity [TabLayout + ViewPager2 + FragmentStateAdapter]
 │                        ├─ OverviewFragment (stat cards)
 │                        ├─ AdminComplaintsFragment ──► AdminComplaintDetailActivity
 │                        ├─ AdminNoticesFragment ──► AddEditNoticeActivity
 │                        └─ UsersFragment (ListView)
```

| Screen | Layout file(s) |
|---|---|
| Login | `activity_login.xml` |
| Register | `activity_register.xml` |
| User dashboard | `activity_user_dashboard.xml`, `menu_bottom_user.xml`, `menu_toolbar.xml` |
| Notices | `fragment_notices.xml`, `item_notice.xml` |
| Notice detail | `activity_notice_detail.xml` |
| New complaint | `fragment_new_complaint.xml`, `item_category_grid.xml` |
| My complaints | `fragment_my_complaints.xml`, `item_complaint.xml` |
| Complaint detail / edit | `activity_complaint_detail.xml`, `activity_edit_complaint.xml` |
| Profile | `fragment_profile.xml` |
| Admin dashboard | `activity_admin_dashboard.xml` |
| Overview | `fragment_overview.xml` |
| Admin complaints | `fragment_admin_complaints.xml`, `activity_admin_complaint_detail.xml` |
| Admin notices | `fragment_admin_notices.xml`, `activity_add_edit_notice.xml` |
| Users | `fragment_users.xml`, `item_user.xml` (ListView row) |

---

## 8. Project Structure

Inside the existing package:

```
app/src/main/java/<existing.package>/
├── MainActivity.java
├── auth/            LoginActivity, RegisterActivity
├── user/            UserDashboardActivity, NoticeDetailActivity, ComplaintDetailActivity, EditComplaintActivity
├── user/fragments/  NoticesFragment, NewComplaintFragment, MyComplaintsFragment, ProfileFragment
├── admin/           AdminDashboardActivity, AdminComplaintDetailActivity, AddEditNoticeActivity
├── admin/fragments/ OverviewFragment, AdminComplaintsFragment, AdminNoticesFragment, UsersFragment
├── adapters/        NoticeAdapter, ComplaintAdapter, UserListAdapter (ListView), CategoryGridAdapter (GridView), AdminPagerAdapter (FragmentStateAdapter)
├── database/        DatabaseHelper
├── models/          User, Complaint, Notice
└── utils/           Constants, SessionManager (SharedPreferences wrapper)
```

`Constants` holds role names (`ROLE_USER`, `ROLE_ADMIN`), status names, intent extra keys, and the SharedPreferences file name. `SessionManager` has `saveLogin`, `getUserId`, `getRole`, `isLoggedIn`, `logout`.

Remember to register **every Activity** in `AndroidManifest.xml`.

---

## 9. UI / UX Guidelines

- Material Components theme, one primary color (e.g. deep blue) and one accent.
- Cards (`MaterialCardView`) for list items, rounded corners, 16dp padding.
- Status badge colors: Pending `#F59E0B`, Resolved `#16A34A`. Priority: Low green, Medium orange, High red.
- Every list shows an empty-state `TextView` when there is no data.
- Forms show inline errors via `setError()` and Toasts for success.
- Destructive actions always ask for confirmation with an AlertDialog.
- Toolbar on dashboards with a Logout menu item.

---

## 10. Non-Functional Requirements

- App must run on an emulator (API 24+) with **no crashes** on the main flows.
- No hardcoded strings or colors in layouts (use `strings.xml`, `colors.xml`).
- Lists refresh in `onResume()` so changes appear after returning from another screen.
- Handle rotation or back navigation without losing the login session.
- All DB operations are small and run on the main thread (acceptable for coursework; mention this limitation in the viva).

---

## 11. Acceptance Test Checklist (Demo Script)

1. Fresh install → Login screen. Register a new student → success → login → User dashboard.
2. See 2 seeded notices; open one; share it via the system share sheet.
3. Submit a complaint choosing a category from the grid and a priority. It appears in My Complaints as **Pending**.
4. Edit it, then confirm the list refreshes. Delete another complaint with confirmation.
5. Log out → log in as `admin@uni.com` / `admin123` → Admin dashboard with 4 tabs.
6. Overview shows correct counts.
7. Open the student's complaint → reply → mark Resolved.
8. Log back in as the student → status shows **Resolved** with the admin reply.
9. Admin adds, edits, deletes a notice; the student sees the changes.
10. Admin opens Users tab (ListView), deletes the test student; their complaints vanish.
11. Close and reopen the app with "Remember me" ticked → lands directly on the dashboard.

---

## 12. Build Milestones (stop after each, wait for "next")

| # | Milestone | Done when |
|---|---|---|
| M0 | **Setup check**: confirm Java + Views project, add dependencies, create package folders, `Constants`, `SessionManager`, `/docs/VIVA_NOTES.md`. | Gradle sync succeeds, app launches. |
| M1 | **Database layer**: models + `DatabaseHelper` with all tables, seed data, CRUD methods. | A temporary test (Log/Toast) proves insert and read work. |
| M2 | **Auth**: Register, Login, role routing, `MainActivity` router, Remember me, Logout. | Both roles reach correct empty dashboards and session persists. |
| M3 | **Admin shell + Notices CRUD**: Admin dashboard with TabLayout + ViewPager2 + `FragmentStateAdapter`, Notices tab with RecyclerView, FAB, Add/Edit/Delete. | Admin can fully manage notices. |
| M4 | **User shell + Notices**: User dashboard with BottomNavigationView + fragments, Notices tab, detail screen, share intent, search. | User sees and opens admin's notices. |
| M5 | **User complaints**: New Complaint (GridView + RadioGroup), My Complaints, detail, edit with `ActivityResultLauncher`, delete. | Full complaint lifecycle from user side. |
| M6 | **Admin complaints + Overview + Users**: complaints list with Spinner filter, reply/resolve, stat cards, Users ListView with delete. | Whole acceptance checklist (Section 11) passes. |
| M7 | **Polish**: validation gaps, empty states, colors, icons, Profile tab, final `VIVA_NOTES.md` review. | No crashes, notes complete. |

---

## 13. Viva Readiness (agent must keep `/docs/VIVA_NOTES.md` updated)

The student must be able to answer:
- Why `SQLiteOpenHelper`? What do `onCreate` and `onUpgrade` do?
- Difference between ListView, GridView, and RecyclerView. Why RecyclerView is more efficient (ViewHolder, recycling).
- How does an Adapter connect data to a view?
- How is data passed between Activities (`putExtra`) and returned (`ActivityResultLauncher`)?
- Explicit vs implicit Intent, with the share example.
- Activity vs Fragment, and why fragments for tabs.
- What does `FragmentStateAdapter` do with `ViewPager2`?
- How is admin separated from user (role column + checks)?
- What is `SharedPreferences` and what is stored there?
- What is a foreign key and what does `ON DELETE CASCADE` do here?
- Known limitations: plain-text passwords, DB work on the main thread, no server sync.

---

## 14. Out of Scope / Future Ideas

Image attachments on complaints, push notifications, password hashing, Room migration, cloud sync with Firebase, complaint categories managed by admin.