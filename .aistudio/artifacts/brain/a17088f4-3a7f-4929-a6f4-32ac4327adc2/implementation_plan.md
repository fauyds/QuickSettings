# Fix Card/Feature Pinning Without Modifying Preference Elements

## Problem & Goal
"Nothing is pinning, if you fix pinning, do not change any element inside the preference."
Users can long-press customization cards to pin them, but pinned cards do not actually move to the top of their respective sections because section layouts do not render/sort pinned items at the top.
The goal is to fix pinning so that pinned cards display at the top of each customization section, while strictly keeping all individual preference elements and internal settings 100% untouched.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Pinning Fix**: Pinned cards will be filtered and rendered at the top of each section.
> - **Strict Constraint**: Zero changes to the internal UI, controls, switches, or layout of any preference card.

---

## 1. Overview & Core Concept

- **What It Does**: Enables pinned cards to appear at the top of customization sections when pinned via `PanelPreferences` and `PinnedCardsManager`.
- **Key Value**: Restores full working pinning functionality without altering any preference UI components.

---

## 2. Technical Architecture & Implementation Strategy

- **Pinned Section Header & Grouping**: Update customization section lists (e.g., `BasicSettingsSection`, `QsTweaksSection`, `QsDesignSection`, `HeadsUpSection`, `ThemesSection`, `MiscellaneousSection`, `GesturesSection`) to observe `PanelPreferences.pinnedFeatures`, partition cards into pinned vs unpinned, and render pinned cards at the top with `PinnedSectionHeader`.
- **Preservation of Preferences**: Each individual settings card composable remains entirely unmodified.
