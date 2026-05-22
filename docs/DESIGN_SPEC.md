# MovieReservationSystem — UI/UX Design Specification

**Version**: 1.0  
**Date**: 2026-05-22  
**Prepared for**: UI/UX Design Team  
**Backend stack**: Kotlin · Ktor · PostgreSQL · Paystack (payment gateway)  

---

## How to Read This Document

This document describes every screen, state, and interaction required across two separate applications:

- **Customer App** — used by moviegoers (web + mobile)
- **Admin Portal** — used by cinema staff (web only)

Each section lists: what the user sees, what data is shown, what actions they can take, and any important edge cases or states the design must handle.

**Currency**: All monetary values are in Nigerian Naira (NGN). Amounts from the API are in **kobo** — divide by 100 before displaying (e.g., `150000` kobo = ₦1,500).  
**Timestamps**: All timestamps from the API are ISO-8601 UTC. Display them in the user's local timezone.  
**Pagination**: List screens use `limit` + `offset`. Default page size is 10 items.

---

## Part 1 — Customer App (Web + Mobile)

The customer app is used by moviegoers to browse movies, book seats, and pay for reservations.

### Navigation Structure

**Mobile** — bottom tab bar with 4 tabs:
1. Home (movies)
2. Showtimes
3. My Reservations
4. Payments

**Web** — top navigation bar with the same 4 sections plus Login / Register in the top right. After login, show the user's email and a logout option.

---

### Screen 1.1 — Registration

**Purpose**: New users create an account.

**Fields**:
| Field | Type | Validation |
|---|---|---|
| Email address | Text input (email keyboard) | Required, valid email format |
| Password | Password input (hidden by default, show/hide toggle) | Required, minimum 8 characters |

**Actions**:
- **Register** button — submits the form. On success → navigate to OTP Verification screen (Screen 1.2), passing the `email_id` and `token` returned by the API.
- **Already have an account? Log in** link → Login screen (Screen 1.3)

**States to design**:
- Default (empty form)
- Loading (button in disabled/spinner state while API call is in-flight)
- Field-level validation errors (inline, shown below the field)
- API error (e.g., "An account with this email already exists" — shown as a banner above the form)

---

### Screen 1.2 — OTP Email Verification

**Purpose**: Verify the user's email address after registration.

**Context**: The API sends a one-time code to the user's email. This screen collects that code.

**Display**:
- Instruction text: "We sent a 6-digit code to [user's email address]"
- OTP input: 6 individual digit boxes (or a single code field) — auto-advance focus between boxes on mobile

**Fields**:
| Field | Notes |
|---|---|
| OTP code | 6 characters, numeric |

**Actions**:
- **Verify** button — submits the code. On success → navigate to Login screen (Screen 1.3) with a success toast: "Email verified. Please log in."
- **Resend code** link — re-sends the OTP. **Rate-limited**: after clicking, disable the link and show a 60-second countdown ("Resend in 57s…") before allowing another attempt. Show success toast "Code resent" on success.

**States to design**:
- Default
- Loading (after tapping Verify)
- Invalid code error ("The code you entered is incorrect or has expired")
- Resend cooldown (link disabled with countdown timer)

---

### Screen 1.3 — Login

**Purpose**: Existing users sign in.

**Fields**:
| Field | Type |
|---|---|
| Email address | Text input |
| Password | Password input with show/hide toggle |

**Actions**:
- **Log in** button — on success → navigate to Home (Screen 1.4). On failure → show inline error.
- **Don't have an account? Register** link → Registration screen (Screen 1.1)

**States to design**:
- Default
- Loading
- Wrong credentials error ("Incorrect email or password")
- **Rate-limit lockout**: after too many failed attempts, disable the login button and show: "Too many login attempts. Please try again in a few minutes." The button stays disabled until the lockout period expires.

**Note for dev handoff**: The API token response contains `access_token` and `refresh_token`. The client should store these securely. Token refresh is handled silently in the background — no screen needed for this.

---

### Screen 1.4 — Home / Movie List

**Purpose**: The main discovery screen. Shows all available movies.

**Layout**:
- **Genre filter bar** at the top — horizontal scrollable row of genre chips (e.g., "All", "Action", "Comedy", "Drama"). "All" is selected by default. Tapping a chip filters the list.
- **Movie grid** below — 2 columns on mobile, 3–4 on web. Each card shows:
  - Movie poster image (aspect ratio ~2:3)
  - Movie title (1–2 lines, truncated with ellipsis)
  - Tap → Movie Detail screen (Screen 1.5)

**Pagination**: Infinite scroll (load next page when user nears the bottom) or "Load more" button. Default page size: 10.

**States to design**:
- Default (populated list)
- Loading (skeleton cards — same grid shape, animated shimmer)
- Genre filtered (chips update the list, show a loading state during re-fetch)
- Empty state for genre filter: "No movies in this genre yet." with an illustration
- Network error: "Couldn't load movies. [Retry]" button

> **Future Feature — Movie Search**: A search bar for text-based movie lookup is not yet supported by the API. Reserve space in the top bar layout for it; the designer may stub it as "coming soon."

---

### Screen 1.5 — Movie Detail

**Purpose**: Full details about a specific movie, with a CTA to browse showtimes.

**Data displayed**:
| Element | Source field |
|---|---|
| Poster image | `posterUrl` |
| Title | `title` |
| Duration | `duration` (display as "1h 45m") |
| Release date | `releasedDate` (display as "15 Jan 2025") |
| Genre chips | `genres[]` — each genre as a small pill/tag |
| Description | `description` (full text, expandable if long) |

**Actions**:
- **See Showtimes** primary CTA → Showtimes screen (Screen 1.6), pre-filtered to this movie
- **Back** navigation

**States to design**:
- Default (populated)
- Loading (skeleton layout)
- Error / movie not found

---

### Screen 1.6 — Showtimes for a Movie

**Purpose**: Lists all upcoming showtimes for a selected movie so the user can pick one.

**Layout**:
- Movie title and poster thumbnail at the top as a header
- Showtimes grouped by date (e.g., "Today — May 22", "Tomorrow — May 23", ...)
- Each showtime card shows:
  - Start time & end time (e.g., "3:00 PM – 5:15 PM")
  - Room name (e.g., "Hall A")
  - Price (e.g., "₦1,500")
  - Status badge

**Showtime status badge states**:
| Status | Display | Tappable? |
|---|---|---|
| `SCHEDULED` | Green "Available" badge | Yes → Seat Selection |
| `CANCELLED` | Red "Cancelled" badge | No (greyed out card) |
| `COMPLETED` | Grey "Ended" badge | No (greyed out card) |

**Actions**:
- Tap a SCHEDULED showtime → Seat Selection screen (Screen 1.7)

**States to design**:
- Default (list with mixed statuses)
- All showtimes cancelled/completed: "No available showtimes for this movie right now."
- Empty (no showtimes at all): "No showtimes scheduled yet."
- Loading skeleton

---

### Screen 1.7 — Seat Selection

**Purpose**: The user picks which seats they want to book. This is the most visually complex screen.

**Layout**:
- **Screen indicator** at the top: a narrow arc/bar representing the cinema screen ("Screen" label centred above it)
- **Seat map**: a 2D grid of seat buttons. Rows labelled alphabetically (A, B, C…), columns numbered. Each button shows the seat number.
- **Legend** below the map:
  - Available (selectable colour)
  - Held (dimmed colour, lock icon or strikethrough)
  - Booked (dark/filled colour)
  - Selected (highlight/accent colour — user's current selection)
- **Selection summary bar** pinned to the bottom:
  - "X seats selected"
  - Total price: "₦X,XXX"
  - **Reserve** button (disabled until at least 1 seat is selected)
- **Countdown timer** visible somewhere on screen: "Reservation holds for 15:00" — starts only after a reservation is actually created. On this screen it can be shown as an informational note: "Seats are held for 15 minutes after reserving."

**Seat status colours** (to define in the design system):
| Status | Meaning |
|---|---|
| `AVAILABLE` | User can select this seat |
| `HELD` | Another user has a pending reservation — not selectable |
| `BOOKED` | Sold — not selectable |

**Actions**:
- Tap AVAILABLE seat → select it (highlighted). Tap again → deselect.
- Tap **Reserve** → calls the API to create a reservation. On success → navigate to Reservation Confirmation screen (Screen 1.8) and start the 15-minute countdown.
- On failure (409 Conflict — seat taken between selection and submission): show modal "One or more seats were just taken. Please re-select." and refresh the seat map.

**States to design**:
- Default (seats loaded)
- Some seats selected (summary bar active)
- Loading (skeleton grid)
- Fully booked: "This showtime is sold out." with a CTA to go back and pick another showtime
- Network error

---

### Screen 1.8 — Reservation Confirmation

**Purpose**: Confirms what the user has reserved, shows the expiry countdown, and leads them to payment.

**Data displayed**:
| Element | Source |
|---|---|
| Movie title | From showtime context |
| Room name | From showtime context |
| Date and time | `startsAt` / `endsAt` |
| Selected seats | List: e.g., "A1, A2, B3" |
| Total amount | `totalAmount` ÷ 100, formatted as ₦X,XXX |
| Reservation ID | `id` |
| Expiry countdown | Live timer counting down from `expiresAt` — `now()`. Format: "MM:SS" |

**Actions**:
- **Pay Now** primary CTA → Payment Initiation screen (Screen 1.9)
- **Cancel Reservation** secondary action → Cancel confirmation modal. On confirm → navigate back to Home.

**Countdown behaviour**:
- Countdown ticks in real time.
- When it reaches 0:00 → show a modal: "Your reservation has expired. The seats have been released." with a CTA to go back to the showtime.

**States to design**:
- Default (active countdown)
- Expiry warning (e.g., under 2 minutes remaining — highlight the timer in red)
- Expired modal

---

### Screen 1.9 — Payment Initiation

**Purpose**: Order summary before redirecting to the Paystack payment page.

**Data displayed**:
- Order summary (movie, date/time, seats, total)
- Payment note: "You'll be redirected to Paystack to complete your payment securely."
- Paystack logo / badge for trust signal

**Actions**:
- **Pay ₦X,XXX** primary CTA — calls the API to initialize payment. On success → open the `authorization_url` (Paystack-hosted checkout page) in a browser / webview. On the payment form, show a loading overlay during the API call.
- **Back** — returns to Reservation Confirmation (countdown still running).

**States to design**:
- Default
- Loading (after tapping Pay)
- API error: "Couldn't initialize payment. Please try again."

---

### Screen 1.10 — Payment Result

**Purpose**: Shown after returning from the Paystack checkout page (redirect callback).

**Three result states** (the API verifies the payment and returns a status):

| Status | Heading | Body | CTA |
|---|---|---|---|
| `SUCCESS` | "Booking Confirmed!" | "Your seats are booked. Enjoy the show!" | "View Reservation" |
| `FAILED` | "Payment Failed" | "Something went wrong with your payment. Your reservation is still held — you can try again." | "Try Again" / "Cancel Reservation" |
| `PENDING` | "Payment Processing…" | "Your payment is still being processed. We'll update your booking status shortly." | "View My Reservations" |

**Pending note**: The system has a background reconciliation job that resolves stuck payments within 5–24 minutes. Design should not suggest a specific time, just "shortly."

> **Future Feature — Ticket Download**: A downloadable PDF ticket or QR code for gate entry is not yet supported. Reserve a placeholder button on the SUCCESS state ("Download Ticket — Coming Soon").

---

### Screen 1.11 — My Reservations

**Purpose**: Lists all of the logged-in user's reservations.

**Each list item shows**:
- Movie title
- Showtime date and time
- Number of seats
- Total amount (₦)
- Status badge

**Reservation status badges**:
| Status | Colour | Meaning |
|---|---|---|
| `PENDING` | Amber | Reserved but not yet paid (expiry timer may be relevant) |
| `CONFIRMED` | Green | Paid and confirmed |
| `CANCELLED` | Grey | Cancelled (by user or expired) |

**Actions**:
- Tap a reservation → Reservation Detail screen (Screen 1.12)

**States to design**:
- Default (list)
- Loading (skeleton list items)
- Empty: "You have no reservations yet." with a CTA "Browse Movies"

---

### Screen 1.12 — Reservation Detail

**Purpose**: Full details of a single reservation.

**Data displayed**:
| Element | Source |
|---|---|
| Movie title | Context |
| Room | Context |
| Date and time | `startsAt` / `endsAt` |
| Seats | `seats[]` — each seat's row label + seat number |
| Total amount | `totalAmount` ÷ 100 |
| Status | `status` badge |
| Created at | `createdAt` |
| Expiry (if PENDING) | Live countdown from `expiresAt` |
| Payment status | From linked payment (if any) |

**Actions**:
- **Pay Now** button — visible only when status is `PENDING` and not expired → Payment Initiation (Screen 1.9)
- **Cancel** button — visible only when status is `PENDING` → Cancel confirmation modal. On confirm, show success toast and update status to CANCELLED.

**States to design**:
- CONFIRMED (no actions, just details)
- PENDING with time remaining (Pay Now + Cancel actions)
- PENDING expired (countdown at 0:00 — show "Reservation Expired" banner, no Pay Now)
- CANCELLED (greyed out, no actions)

---

### Screen 1.13 — Payment History

**Purpose**: Lists all payment attempts made by the user.

**Each list item shows**:
- Payment reference (abbreviated, e.g., "mrs_42_abc…")
- Amount (₦)
- Status badge
- Date

**Payment status badges**:
| Status | Colour |
|---|---|
| `INITIATED` | Grey |
| `PENDING` | Amber |
| `SUCCESS` | Green |
| `FAILED` | Red |
| `ABANDONED` | Grey |
| `REFUND_PENDING` | Orange |
| `REFUNDED` | Blue |
| `REFUND_FAILED` | Dark Red |

**Actions**:
- Tap a payment → Payment Detail screen (Screen 1.14)

**States to design**:
- Default (list)
- Loading
- Empty: "No payments yet."

---

### Screen 1.14 — Payment Detail

**Data displayed**:
| Field | Notes |
|---|---|
| Reference | Full reference string |
| Amount | ₦X,XXX |
| Currency | Always NGN |
| Status | Badge |
| Gateway response | Text from Paystack (e.g., "Approved") — show only if present |
| Paid at | Date/time — show only if payment succeeded |
| Refunded at | Date/time — show only if refunded |
| Created at | Date/time |

**Actions**: None — read-only view. Back navigation only.

---

## Global Customer App States

These apply across all screens:

| Scenario | Behaviour |
|---|---|
| **Session expired** (401) | Silently attempt token refresh. If refresh fails, clear session and redirect to Login with toast: "Your session has expired. Please log in again." |
| **403 Forbidden** | Show: "You don't have permission to view this." |
| **404 Not Found** | Show: "This content no longer exists." with a back CTA |
| **409 Conflict** (seat taken) | Show in-context error modal with guidance to re-select |
| **Network offline** | Show a persistent banner: "You're offline. Check your connection." |
| **Empty list** | Every list screen must have a designed empty state with an illustration and a helpful CTA |
| **Loading** | Skeleton loaders preferred over spinners for content-heavy screens |

> **Future Feature — User Profile**: No profile management endpoint exists (no name, avatar, or password change flow). The tab bar / nav should not include a Profile tab in the current version.  
> **Future Feature — Email / Push Notifications**: The backend emits internal events but does not send emails or push notifications. Do not design notification settings or opt-in flows for the current version.

---

---

## Part 2 — Admin Portal (Separate Web Application)

The Admin Portal is a dedicated web application for cinema operations staff. It is accessed at a separate URL from the customer app. Admins log in with their credentials (role is `ADMIN` — assigned at account creation by the system administrator).

### Navigation

**Left sidebar** (always visible on desktop):
1. Dashboard
2. Movies
3. Rooms
4. Showtimes
5. Reservations
6. Reports

Top bar: Admin's email + Logout button.

---

### Screen 2.1 — Admin Login

Same fields as the customer login (email + password). On success, the admin's JWT is stored and the admin is redirected to the Dashboard. Rate limiting and lockout states apply identically to Screen 1.3.

**Difference from customer login**: The portal should clearly brand itself as the Admin Portal (e.g., "Cinema Admin — Staff Login") so staff don't confuse it with the customer app.

---

### Screen 2.2 — Admin Dashboard (Overview)

**Purpose**: At-a-glance health of the cinema business.

**Metrics cards** (top of page, 4-across on desktop, 2-across on tablet):
| Card | Metric | Note |
|---|---|---|
| Total Revenue | Net revenue for the current month | From Revenue Report API, `totalNet` ÷ 100, formatted as ₦X,XXX |
| Tickets Sold | Total tickets sold this month | From Revenue Report API, `totalTicketsSold` |
| Average Occupancy | Average seat fill rate | From Capacity Report API, `avgOccupancyRate` as percentage |
| Active Showtimes | Count of SCHEDULED showtimes today | Derived from Showtime list |

**Quick action buttons** below the cards:
- "+ Add Movie"
- "+ Add Showtime"
- "+ Add Room"

Each button navigates to the respective create form.

**States to design**:
- Default (all metrics loaded)
- Loading (skeleton cards)
- Error loading metrics (show "—" in each card with a Retry button)

---

### Screen 2.3 — Movie Management — List

**Purpose**: Browse and manage all movies.

**Table columns**:
| Column | Notes |
|---|---|
| Poster | Small thumbnail (40×60 px) |
| Title | Full title |
| Genres | Comma-separated genre names |
| Duration | "Xh Ym" format |
| Release Date | Formatted date |
| Actions | Edit icon, Delete icon |

**Actions**:
- **+ Add Movie** button (top right) → Create Movie form (Screen 2.4)
- **Edit icon** → Edit Movie form (Screen 2.4, pre-filled)
- **Delete icon** → Delete confirmation modal (Screen 2.5)
- **Manage Genres** link/button (top area) → Genre Management section (Screen 2.6)

**States to design**:
- Default (table with data)
- Loading (skeleton table rows)
- Empty: "No movies yet. Add your first movie."

---

### Screen 2.4 — Create / Edit Movie Form

**Purpose**: Add a new movie or edit an existing one. Same form for both — edit pre-fills the fields.

**Fields**:
| Field | Type | Validation |
|---|---|---|
| Title | Text input | Required, max 255 characters |
| Description | Textarea | Required |
| Poster URL | Text input (URL) | Required. Show a live image preview thumbnail beside the field. |
| Duration | Number input | Required, in minutes (e.g., 105). Display helper: "= 1h 45m" |
| Release Date | Date picker | Required |
| Genres | Multi-select dropdown / checkbox list | Optional. Pulls from the genres list. At least one recommended. |

**Actions**:
- **Save Movie** (Create) / **Update Movie** (Edit) — primary button. On success → navigate back to Movie List with success toast.
- **Cancel** — navigate back without saving.

**States to design**:
- Default (empty for create, pre-filled for edit)
- Loading (submit button in spinner state)
- Validation errors (inline, per field)
- API error banner

---

### Screen 2.5 — Delete Confirmation Modal

**Used for**: Movies, Rooms, Showtimes, and Genres.

**Content**:
- Warning heading: "Delete [Movie/Room/Showtime/Genre]?"
- Body: "This action cannot be undone. Any associated data may be affected."
- Two buttons: **Delete** (destructive, red) and **Cancel**.

**States**: Loading state on the Delete button during API call.

---

### Screen 2.6 — Genre Management

**Purpose**: Manage the list of genres used to tag movies.

**Layout**: Can be a sub-page or a modal/side panel from the Movie Management screen.

**Content**:
- List of all genres (name + delete icon per row)
- **+ Add Genre** form: single text input + "Add" button inline

**Actions**:
- Add genre → inline form submits, list refreshes
- Delete genre → Delete confirmation modal (Screen 2.5)

**States**:
- Default list
- Empty: "No genres yet."
- Duplicate genre error: "A genre with this name already exists."

---

### Screen 2.7 — Room Management — List

**Purpose**: Manage cinema rooms (halls).

**Table columns**:
| Column | Notes |
|---|---|
| Name | Room/hall name |
| Rows | Number of rows |
| Columns | Number of columns per row |
| Total Seats | Rows × Columns |
| Actions | View, Edit, Delete icons |

**Actions**:
- **+ Add Room** → Create Room form (Screen 2.8)
- **View icon** → Room Detail screen (Screen 2.9)
- **Edit icon** → Edit Room form (Screen 2.8, pre-filled)
- **Delete icon** → Delete confirmation modal

---

### Screen 2.8 — Create / Edit Room Form

**Fields**:
| Field | Type | Validation |
|---|---|---|
| Room Name | Text input | Required, max 255 characters |
| Rows | Number input | Required, min 1 |
| Columns | Number input | Required, min 1 |

**Live preview**: As the admin enters rows and columns, show a small grid preview of the seat layout (rows × columns of dots/squares).

**Actions**:
- **Save** → on success → navigate to Room Detail (Screen 2.9) with a prompt: "Room created. Would you like to generate seats now?" with a **Generate Seats** CTA.
- **Cancel**

---

### Screen 2.9 — Room Detail

**Purpose**: View a room's configuration and manage its seats.

**Top section**: Room name, rows, columns, total seat count.

**Seat grid visualisation**: A 2D grid displaying each seat (row label + seat number). Visual-only — no interaction on this screen.

**Seat list table** below the grid:
| Column | Notes |
|---|---|
| Row Label | e.g., "A", "B", "C" |
| Seat Number | e.g., 1, 2, 3 |

**Actions**:
- **Generate Seats** button — calls the bulk-create seats endpoint using the room's rows × columns. Show confirmation dialog first: "This will create [N] seats for [Room Name]. Continue?" Disabled if seats already exist.
- **Edit Room** → Edit form (Screen 2.8)
- **Delete Room** → Delete confirmation modal

**States**:
- Seats not yet generated: empty grid with "No seats yet. Click Generate Seats to set up this room."
- Seats generated: populated grid and table.

---

### Screen 2.10 — Showtime Management — List

**Purpose**: Browse and manage all showtimes across all movies and rooms.

**Table columns**:
| Column | Notes |
|---|---|
| Movie | Title |
| Room | Room name |
| Start | Date + time (local timezone) |
| End | Time |
| Price | ₦X,XXX |
| Status | Badge (see below) |
| Actions | Edit, Status Update, Populate Seats, Delete |

**Showtime status badges**:
| Status | Colour |
|---|---|
| `SCHEDULED` | Green |
| `CANCELLED` | Red |
| `COMPLETED` | Grey |

**Pagination**: limit + offset with page controls.

**Actions**:
- **+ Create Showtime** → Create Showtime form (Screen 2.11)
- **Edit icon** → Edit Showtime form (Screen 2.11, pre-filled)
- **Status chip** → Inline dropdown to change status to CANCELLED (a SCHEDULED showtime cannot be set back to SCHEDULED once cancelled — disable that option)
- **Populate Seats icon/button** → Confirmation dialog: "Initialise seat inventory for this showtime? This creates seat records for all [N] seats in [Room Name]." On confirm → API call. Show success toast. Disable the button afterwards (idempotent).
- **Delete icon** → Delete confirmation modal

---

### Screen 2.11 — Create / Edit Showtime Form

**Fields**:
| Field | Type | Validation / Notes |
|---|---|---|
| Movie | Searchable dropdown (from movie list) | Required |
| Room | Searchable dropdown (from room list) | Required |
| Start Date & Time | Date-time picker | Required |
| End Date & Time | Date-time picker | Required. Must be after Start. Auto-suggest based on movie duration. |
| Price | Number input (₦) | Required, min ₦0. Backend stores in kobo — multiply by 100 on submit. |

**Conflict warning**: If the selected room already has a showtime overlapping the chosen time window, the API will reject it (409 Conflict). Show an inline error: "This room is already booked for that time slot. Please choose a different time or room."

**Actions**:
- **Save Showtime** → on success → navigate back to Showtime List. Show success toast. Prompt: "Showtime created. Remember to populate seat inventory before opening for bookings."
- **Cancel**

---

### Screen 2.12 — Reservation Management — List

**Purpose**: View and manage all customer reservations.

**Filter tabs** (above the table): ALL | PENDING | CONFIRMED | CANCELLED

**Table columns**:
| Column | Notes |
|---|---|
| ID | Reservation ID |
| User | User ID (no name available in current API) |
| Movie / Showtime | Movie title + start time |
| Seats | Count of seats in the reservation |
| Total | ₦X,XXX |
| Status | Badge |
| Created | Date/time |
| Actions | View, Cancel |

**Pagination**: limit + offset.

**Actions**:
- **View icon** → Reservation Detail panel/page (Screen 2.13)
- **Cancel icon** (only visible for PENDING reservations) → Cancel confirmation modal. On confirm → status updates to CANCELLED.

---

### Screen 2.13 — Reservation Detail (Admin View)

**Purpose**: Full detail view of a reservation including its payment history.

**Top section — Reservation info**:
Same fields as Screen 1.12 (user-facing), plus the User ID.

**Seats sub-section**:
Table of seats in the reservation: seat ID, row label, seat number, seat status.

**Payments sub-section**:
Table of all payment attempts for this reservation:
| Column | Notes |
|---|---|
| Reference | Payment reference |
| Amount | ₦X,XXX |
| Status | Badge (all 8 statuses — see Screen 1.13 for colour guide) |
| Gateway Response | Paystack's message (e.g., "Approved", "Declined") |
| Paid At | Date/time (if applicable) |
| Refunded At | Date/time (if applicable) |
| Created | Date/time |

**Actions**:
- **Force Cancel** button — only shown for PENDING reservations. Opens confirmation modal. On confirm → reservation cancelled, seats released.
- **Back** navigation

---

### Screen 2.14 — Revenue Report

**Purpose**: Financial overview of ticket sales revenue over a date range.

**Controls** (top of page, inline):
- **From** date picker
- **To** date picker
- **Granularity** toggle: DAY | WEEK | MONTH (default: DAY)
- **Apply** button to trigger the report

**Summary cards** (shown after data loads):
| Card | Metric |
|---|---|
| Total Gross | Total revenue before refunds |
| Total Refunds | Total refunded amount |
| Total Net | Gross minus refunds |
| Total Tickets Sold | Count |

**Chart**: Bar chart (or stacked bar) with one bar per time bucket. X-axis: bucket start date. Y-axis: amount in ₦. Show three series: Gross (primary), Refunds (accent), Net (secondary). Toggle series visibility via legend.

**Detail table** below the chart:
| Column | Notes |
|---|---|
| Period | Bucket start date (formatted per granularity) |
| Gross | ₦X,XXX |
| Refunds | ₦X,XXX |
| Net | ₦X,XXX |
| Tickets Sold | Count |

**States to design**:
- Loading (skeleton chart + skeleton table)
- Empty range (no data): "No revenue data for the selected period."
- Error loading report

---

### Screen 2.15 — Capacity Report

**Purpose**: Seat occupancy analysis across showtimes.

**Controls** (top of page):
- **From** date picker
- **To** date picker
- **Movie** filter dropdown (optional — "All Movies" by default)
- **Room** filter dropdown (optional — "All Rooms" by default)
- **Apply** button

**Summary cards**:
| Card | Metric |
|---|---|
| Total Showtimes | Count in the selected period |
| Average Occupancy | `avgOccupancyRate` as a percentage |
| Total Seats Booked | Count |
| Total Seats | Total capacity across all showtimes |

**Detail table**:
| Column | Notes |
|---|---|
| Movie | Title |
| Room | Name |
| Showtime | Start date/time |
| Total Seats | Capacity |
| Booked | Count |
| Held | Count (pending reservations) |
| Available | Count |
| Occupancy | Percentage + mini progress bar |

The **occupancy column** should display both the percentage number and a horizontal progress bar in the same cell. Colour gradient: 0–50% = grey, 50–80% = amber, 80–100% = green.

**States to design**:
- Loading (skeleton table)
- Empty range: "No showtime data for the selected period."
- Error loading report

---

## Global Admin Portal States

| Scenario | Behaviour |
|---|---|
| **Session expired** (401) | Redirect to Admin Login with toast: "Your session has expired." |
| **403 Forbidden** | Show "Access denied" full-page error — this should not normally occur in the admin portal |
| **409 Conflict** (room overlap on showtime) | Inline error on the showtime form |
| **Destructive actions** | Always gated by a confirmation modal before executing |
| **Empty tables** | Every table must have a designed empty state |
| **Loading** | Skeleton rows for tables, skeleton cards for metrics |

---

## Data Model Reference

This section gives the designer context for field names and value ranges seen in the API.

### Enums Quick Reference

| Enum | Values |
|---|---|
| `Role` | `USER`, `ADMIN` |
| `ShowtimeStatus` | `SCHEDULED`, `CANCELLED`, `COMPLETED` |
| `ReservationStatus` | `PENDING`, `CONFIRMED`, `CANCELLED` |
| `SeatStatus` | `AVAILABLE`, `HELD`, `BOOKED` |
| `PaymentStatus` | `INITIATED`, `PENDING`, `SUCCESS`, `FAILED`, `ABANDONED`, `REFUND_PENDING`, `REFUNDED`, `REFUND_FAILED` |
| `ReportBucketGranularity` | `DAY`, `WEEK`, `MONTH` |

### Key Business Rules

| Rule | Design implication |
|---|---|
| Reservations expire 15 minutes after creation | Countdown timer is a critical UX element on Screens 1.7, 1.8, and 1.12 |
| Seats transition AVAILABLE → HELD on reservation, HELD → BOOKED on payment | Seat map must refresh in near-real-time or on user action |
| A room cannot host two overlapping showtimes | Showtime form must handle 409 Conflict clearly |
| Amounts are stored in kobo | Always divide by 100 for display; multiply by 100 on input |
| Seat inventory must be explicitly populated after showtime creation | Admin must be prompted to do this step — easy to miss |

---

## API Summary for Developers

| Feature area | API base path | Auth required |
|---|---|---|
| Auth | `/v1/api/authentication` | None |
| Movies (public) | `/v1/api/movie` | None |
| Genres (public) | `/v1/api/genre` | None |
| Rooms (public) | `/v1/api/room` | None |
| Seats (public) | `/v1/api/seat` | None |
| Showtimes (public) | `/v1/api/showtime` | None |
| Reservations (user) | `/v1/api/reservation` | JWT (USER) |
| Payments (user) | `/v1/api/payment` | JWT (USER) / Paystack HMAC (webhook) |
| Admin — all resources | `/v1/api/admin/*` | JWT (ADMIN) |
| Reports | `/v1/api/admin/reports` | JWT (ADMIN) |

All responses are wrapped: `{ "data": <payload> }`. Errors: `{ "error": "<message>" }`.

---

*End of Design Specification v1.0*
