<div align="center">

# ⚡ QuickSettings (v3.0)
**A modern, highly customizable Material 3 Control Center & Notification Shade replacement for Android.**

[![Package](https://img.shields.io/badge/Package-com.fauyds.quicksettings-blue?style=for-the-badge&logo=android)](#)
[![Version](https://img.shields.io/badge/Version-3.0-success?style=for-the-badge)](#)

[![Execution](https://img.shields.io/badge/Engine-Non--Root%20%7C%20Shizuku%20%7C%20Root-orange?style=for-the-badge)](#)
[![Telegram](https://img.shields.io/badge/Telegram-@allphonecustomization-2CA5E0?style=for-the-badge&logo=telegram)](https://t.me/allphonecustomization)

*Personalize tile shapes, complications, glass refraction effects, custom QS tiles, notification categories, heads-up gestures, status bar triggers, and shell command overrides with fluid Material 3 design.*

</div>

## 📷 Screenshots
<p align="center">
<img src="screenshots/1.png" width="22%" />
<img src="screenshots/2.png" width="22%" />
<img src="screenshots/3.png" width="22%" />
<img src="screenshots/5.png" width="22%" />
<img src="screenshots/6.png" width="22%" />
<img src="screenshots/7.png" width="22%" />
<img src="screenshots/8.png" width="22%" />
<img src="screenshots/9.png" width="22%" />
<img src="screenshots/10.png" width="22%" />
<img src="screenshots/11.png" width="22%" />
<img src="screenshots/12.png" width="22%" />
<img src="screenshots/13.png" width="22%" />
<img src="screenshots/14.png" width="22%" />
<img src="screenshots/15.png" width="22%" />
<img src="screenshots/16.png" width="22%" />
<img src="screenshots/17.png" width="22%" />
<img src="screenshots/18.png" width="22%" />
<img src="screenshots/19.png" width="22%" />
<img src="screenshots/20.png" width="22%" />
<img src="screenshots/21.png" width="22%" />
<img src="screenshots/22.png" width="22%" />
<img src="screenshots/23.png" width="22%" />
<img src="screenshots/24.png" width="22%" />
<img src="screenshots/25.png" width="22%" />
<img src="screenshots/26.png" width="22%" />
<img src="screenshots/27.png" width="22%" />
<img src="screenshots/28.png" width="22%" />
<img src="screenshots/29.png" width="22%" />
<img src="screenshots/30.png" width="22%" />
<img src="screenshots/31.png" width="22%" />
<img src="screenshots/32.png" width="22%" />
<img src="screenshots/33.png" width="22%" />

<p align="center">
  Some Glass Variants
<br />
</p>

<p align="center">
<img src="screenshots/34.png" width="22%" /> <img src="screenshots/35.png" width="22%" /> <img src="screenshots/36.png" width="22%" /> <img src="screenshots/37.png" width="22%" />
</p>

<p align="center">
  <a href="./screenshots">
    <img src="https://img.shields.io/badge/More_Screenshots-📁_View-0066CC?style=for-the-badge" alt="More Screenshots">
  </a>
</p>

</div>

---

## ✨ Key Highlights

* 🧩 **Live Complications**: Live complication widgets inside the shade — including a `2x1` complications row in QQS with status and arc indicators (such as *BT Battery* and *Volume*) for quick glanceable info and actions.
* 🛠️ **Custom QS Tile Creator**: Build your own Quick Settings tiles from scratch with **500+ icons**, custom **ON / OFF** labels and subtitles, state toggle support, and primary or long-press actions (apps, shell commands, intents, URLs/deep links, or defaults).
* 🗂️ **QQS Panel Notification Categorizer ("Categories" Tab)**: Organize shade notifications into collapsible category groups (with unmatched items neatly tucked under *"Other"*) with starter templates, custom rules, and **1007** searchable Google icons so the panel stays clean and easy to scan.
* 💎 **Frosted & Liquid Glass (14 Styles)**: Bring depth and optical richness to your panel, notifications, and surface cards with a choice between classic blurred **Frosted Glass** and multi-style **Liquid Glass** refraction, plus performance optimization mode.
* 🛡️ **Multi-Mode Execution Engine**: Works seamlessly across **Non-Root (Standard API)**, **Shizuku Mode (Elevated ADB)**, and **Root (Full Control / SU, including Endless Root Mode)**.
* 🔔 **Lightweight Heads-Up & Notification Hub**: Custom top notification banner overlays with 4-directional swipe gestures, tap/double-tap/long-press actions, temporary snooze durations, and a real-time ongoing notification interceptor and package/channel blacklisting.
* 🎯 **Smart Shade Interception & Smoothness**: Automatically intercepts and collapses the native stock Android shade (Android 12–15+) with optional CPU prime-core thread pinning and instant no-animation toggles.

---

## 🛠️ Feature Breakdown

### 1. 🎛️ Control Center, Complications & Quick Settings Design
Customize every visual aspect of both the compact **Quick Quick Settings (QQS)** header and the expanded **Quick Settings (QS)** panel:

* **Complications**:
  * Live complication widgets inside the shade, including a `2x1` complications row in QQS with status and arc indicators (such as *BT Battery* and *Volume*).
  * **Active & Inactive Complication Shape Choosers**: Inside the tile shapes settings, pick separate vector shapes for active and inactive complication tiles (*Circle*, *Squircle*, *Square*, etc.) alongside QS tile shapes.
  * **Complication Arc Thickness**: Customize the stroke width of circular progress arcs on complication tiles.
  * **Complication Outline**: Switch complications to an inside stroke outline instead of a filled background for a lighter, minimalist look.
  * **Tile & Complication Top Rim**: Add a subtle top rim highlight and vertical background gradient across QS tiles and complications.
  * **Outline Thickness Slider**: Fine-tune stroke width smoothly so tile and complication outlines match your preferred visual weight.

* **Glass, Transparency & Surface Styling**:
  * **Glass Backdrop Effect (Off, Frosted & Liquid — 14 Styles)**: Choose between **Off**, **Frosted**, and **Liquid**, with **14 customizable refraction styles** (*Liquid*, *Grid*, *Ribbed*, *Hex*, *Hammer*, *Voronoi*, *Chevron*, *Wavy*, *Diamond*, *Scales*, *Louvre*, *Plasma*, *Octagon*, *Scan*) and controls like refraction sensitivity, pattern scale, chromatic split, facet depth, and internal cuts.
  * **Solid Surface Transparency Slider**: Control how much solid surface color versus glass transparency is applied behind the panel.
  * **Panel Top Rim & Sensitivity Slider**: Draw a top rim highlight and top-to-bottom gradient along the panel, complete with a sensitivity slider to tune highlight opacity.
  * **Glass & Blur Optimization Toggle**: Enable performance mode to downscale blur passes, skip zero blur, and cache static frames for smoother rendering and lower GPU/RAM overhead.

* **Colors, Header, Footer & Brightness**:
  * **Per-Element Accent Colors Redesign & Custom Colors**: Redesigned the per-element accent colors section and added a custom colors feature so you can set custom colors for UI elements.
  * **Header and Status Backgrounds**: Toggle rounded pill or card backgrounds independently for the **Date**, **Clock**, **Carrier Label**, and **Battery** indicators.
  * **Footer Icons Backgrounds & Customization Sliders**: Wrap QS footer action buttons in rounded backgrounds (`10dp` outer / `3dp` inner corners) with sliders and toggles for spacing and corner styling.
  * **Brightness Controls Backgrounds**: Group the brightness icon, `- / +` stepper buttons, and percentage readout inside pill backgrounds for cleaner visual separation.
  * **Brightness Slider & Progress Bar Styles**:
    * Move the brightness slider to the bottom of the QS panel or force it to always show inside the compact QQS view.
    * Choose between **M3 Slider**, **Sleek Straight Bar**, or **Android 13/14 Animated Wavy Track** for media players and brightness controls.

* **Layout, Grid, Page Indicator & Quick Settings Tweaks**:
  * **QQS Icon Sizing & Quantity**: Adjust circular icon sizes (`32dp` – `64dp`, default `48dp`/`52dp`) and set the number of toggles in the collapsed row (`2` to `6` icons).
  * **Custom Vector Tile Shapes & Outlines**: Choose distinct shapes for **Enabled (Active/ON)** and **Disabled (Inactive/OFF)** states (*Circle, Squircle, Square*, etc.), import custom **SVG / Vector XML Path Data** (`100x100` viewport preview) via clipboard or file picker, or enable a `1.2dp` stroke outline on tiles with the inner background hidden.
  * **Carrier & Header Details**: Show dynamic SIM card network operator names, and customize the **Date Position** (*Next to Time, Below Time, Header Right*) and **Edit Pencil Icon Location** (*Footer Only, Header Only, Header & Footer*).
  * **Grid Density & Drag Handle**: Configure expanded grid dimensions (`3x3`, `4x1`, `4x2`, `4x3`, `4x4`), adjust the expansion drag pill length (`20dp` – `80dp`), or hide the QQS drag handle completely.
  * **QS Page Indicator Style Chooser & Customization Sliders**: Pick between **Dots**, **Lines**, **Dots + Line**, and **Sliding Line**, with dedicated sliders for active and inactive dot radii.
  * **Quick Settings Tweaks**: Several tweaks and controls are organized in the **Quick Settings Tweaks** section for cleaner navigation, including **Replace Carrier Divider with Line Toggle** to keep the carrier label and QQS tiles inside a single unified card separated by a crisp divider line instead of splitting them into separate cards.

---

### 2. ⚙️ Execution Privileges, Custom QS Tiles & Tile Commands
QuickSettings adapts dynamically to your device's privilege level:
* **Non-Root (Safe / Default)**: Uses Accessibility Service, `WRITE_SETTINGS`, and native System Settings dialog fallbacks.
* **Shizuku Mode (Elevated ADB)**: Executes promptless elevated ADB/System shell commands via the Shizuku IPC service.
* **Root (Full Control)**: Direct `su` shell execution for immediate hardware toggles without confirmation dialogs, plus **Endless Root Mode** to keep a persistent background root shell daemon alive so privileged root control doesn't time out or drop during long sessions — paired with memory trimming and cache hygiene to keep long-term RAM usage steady (`~230–241 MB` over 2 hours).

#### 🧩 Custom QS Tile Creator & Complication Actions
* **Custom QS Tiles Creator**: Create custom tiles with **500+ icons**, custom **ON / OFF** labels and subtitles, state toggle support, primary and long-press actions (launch app, shell command, custom intent, URL/deep link, or default settings), execution engine selection (**Standard API**, **Shizuku**, **Root SU**), and toggles like **Close Panel on Press** and **Force State Change**.
* **Complication Actions**: Customize single-tap and long-press actions for individual complications (*BT Battery*, *Volume*, etc.) — open complication menus, run **Standard / Shizuku / Root** shell commands, pick your target execution engine, or reset everything back to defaults.

#### 🖥️ Per-Tile Command Overrides & Console
Override default system actions for individual tiles (e.g., Wi-Fi, Bluetooth, Flashlight):
* **Behavioral Modifiers**:
  * *Close panel on press*: Dismisses the overlay immediately after clicking the tile.
  * *Force state change anyway*: Instantly flips the visual state locally on tap.
  * *Block state changing*: Freezes the current state and ignores clicks.
  * *Always Active*: Permanently locks the tile visual state to `ACTIVE`.
* **Custom Shell Commands**: Define custom ON and OFF shell commands per tile (e.g., `cmd bluetooth_manager enable` / `cmd bluetooth_manager disable`) with built-in **Test ON**, **Test OFF**, and **Reset** buttons.
* **Dynamic Command Execution Console**: Built-in terminal runner inside the *Privileges* tab to test shell commands (e.g., `svc wifi enable`) directly against the active engine.

---

### 3. 👁️ Tile Visibility & Smart Blacklist
Keep your control center clean and relevant:
* **Compatibility Auto-Hide**: One-tap **Smart Hide Unsupported Tiles** automatically hides toggles that aren't supported in your current operational mode (e.g., Elevated ADB vs. Standard API).
* **Granular Search & Toggle**: Search through all 39+ system tiles, view their privilege requirements (`Standard API` vs. `Requires ADB/Root`), and toggle visibility on the fly.

---

### 4. 🔔 Heads-Up Design, Gestures, Categories & Notification Hub
Take full control of how alerts, notification categories, and persistent notifications behave:

* **Heads-Up Design**:
  * **Edge-to-Edge Mode**: Apply `8dp` horizontal side margins with uniform `8dp` corner rounding across all four corners for a crisp edge-to-edge banner look.
  * **Corner Radius**: Adjust the corner roundness of floating notification banners to match your overall shade and system style.
  * **Elevation / Shadow**: Control ambient drop shadow intensity and elevation depth behind heads-up cards so they stand out cleanly over active apps.
  * **Top Margin / Offset**: Adjust the top offset of the banner to give it breathing room below the status bar and camera cutout.

* **Custom Heads-Up Overlay & Gestures**: Lightweight Compose top overlay inflated via `WindowManager` for high-priority notifications.
  * **Interactive Testing**: Built-in *Test Alert*, *Test Alarm*, and *Test Incoming Call* triggers.
  * **Auto-Dismiss Timeout**: Adjustable banner duration from `2s` to `10s` (default `4s`).
  * **Gestures for Heads-Up (4 Directions)**: Assign independent actions to **Swipe Up**, **Swipe Down**, **Swipe Left**, and **Swipe Right** on floating banners — such as dismissing the notification, hiding just the banner, opening the notification, or opening the Custom Quick Settings Shade.
  * **Single Tap, Double Tap & Long Press Actions**: Fine-tune how banners respond to taps and presses, including opening the notification, dismissing, hiding the banner, or snoozing alerts from that app.
  * **Temporary Snooze Duration Set**: Temporarily mute heads-up popups for a specific app for a set duration.

* **The New "Categories" Tab & Notification Hub**:
  * **Category Grouping**: Group shade notifications into collapsible categories with custom icons, and route unmatched alerts to *"Other"*.
  * **Quick-Add Starter Categories**: Create common categories in one tap — *Messages*, *Work*, *Social*, *Finance*, *Shopping*, and *Media*.
  * **Custom Rules & Google Icon Library**: Build custom categories with assigned apps, comma-separated keywords, and **Expanded by default** behavior, choosing icons from a searchable **1007-icon** library organized by category.
  * **Notification Hub & Blacklist**: Analyzes ongoing and persistent notifications in real-time, lets you **Hide / Unhide** noisy persistent cards (e.g., *Battery Guru*, *Android System* overlay alerts), and manages package/channel blacklist rules.

---

### 5. 🎯 Triggers, Gestures & Performance
Engineered to feel like a native part of SystemUI:
* **Status Bar Trigger & Auto-Override**:
  * **Invisible Status Bar Trigger**: Keep top-edge swipe gestures active while making the trigger strip completely invisible.
  * **Use Auto System Panel Closing (A12–A15)**: Actively forces the stock Android 12–15 notification shade to collapse when opening the custom panel.
  * **Stock Shade Auto-Override**: Automatically intercepts native SystemUI shade expansions and substitutes them with QuickSettings.
  * **Fine-Tuned Timing**: Configure **Panel Replacement Delay** (`0ms Instant` up to `1000ms`) and **Detector Cooldown** (`10ms` to `1000ms`, default `280ms`).
  * **Whitelist & Blacklist Detection**: Define target whitelisted SystemUI packages (`com.android.systemui`, `com.google.android.systemui`, etc.) and reject unwanted package/class events.
* **Hardware Gestures**:
  * **Hide QS/QQS on Volume Key Press**: Automatically closes the Quick Settings panel when pressing hardware volume buttons.
* **Performance & Debug**:
  * **Disable Panel Animations**: Open, close, and expand Quick Settings instantaneously with zero transition delay.
  * **Prime Core High Smoothness Mode (Root)**: Pins CPU thread affinity to the prime core to eliminate swipe and scrolling micro-stutters.
  * **Debug Placeholder**: Force the Media Player card to remain visible with preview track data even when no music is playing.

*✨ ...and plenty of additional refinements, polish, and memory/stability improvements under the hood.*

---

## 🔒 Required Permissions Checklist

To provide a seamless system-level experience, QuickSettings utilizes the following permissions (configurable via the **Initial Setup Wizard** or **App Status / Privileges** tabs):

| Permission / Service | Purpose |
| :--- | :--- |
| **Draw Over Other Apps** (`SYSTEM_ALERT_WINDOW`) | Renders the custom Quick Settings overlay, floating triggers, and custom Heads-Up banners. |
| **QuickPanel Accessibility Service** | Enables top-edge gesture interception, global shade expansion, and automatic stock shade closing. |
| **Modify System Settings** (`WRITE_SETTINGS`) | Allows direct control over screen brightness, auto-rotation, and system parameters. |
| **Notification Policy Access** (`ACCESS_NOTIFICATION_POLICY`) | Required to toggle Do Not Disturb (DND) modes. |
| **Notification Access** (`NotificationListenerService`) | Powers Notification Categories, the Notification Hub, ongoing card hiding, media player integration, and custom Heads-Up alerts. |
| **Shizuku / Root Access** *(Optional)* | Unlocks silent hardware toggles (Wi-Fi, Mobile Data, Hotspot, Airplane Mode, etc.), custom QS tile actions, and shell execution. |

---

## 👨‍💻 Developer & Community

* **Creator & Lead Developer**: Maxim Tarna
* **Package Name**: `com.fauyds.quicksettings`
* **Telegram Channel**: [t.me/allphonecustomization](https://t.me/allphonecustomization) — *Join our community for custom themes, SVG shapes, setup guides, exclusive updates, and feedback!*
