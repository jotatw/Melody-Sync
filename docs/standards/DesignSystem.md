# Design System — Hi-Fi Editorial Station

> Melody Sync Visual Identity & Interaction Standard.

---

## 1. Vision & Core Philosophy

Melody Sync is not an automated bulk-tagger, nor is it a generic administrative CRUD application. It is a **Personal Music Curation Workstation**.

Our design philosophy rests on two core pillars:

1. **Hi-Fi Editorial Aesthetic:** A hybrid visual style that merges the tactile, technical feel of analog studio gear (aluminum surfaces, toggle switches, precise technical data) with the high-contrast refinement of editorial music magazines (imposing serif typography, spacious layouts, strong editorial hierarchy).
2. **Assisted Curation (Curadoria Assistida):** We do not automate blindly. The software acts as an expert assistant, presenting intelligent suggestions (from local heuristics and the YouTube API), while the user acts as the curator and final editor who grants the approval with a single click.

---

## 2. Visual Palette (The Hi-Fi Editorial Theme)

We move away from standard Material 3 color schemes to adopt a custom, sophisticated palette that highlights technical data against rich, organic surfaces.

### 2.1 Dark Theme (The "Studio Console" Mode)
The default theme, designed to feel like a high-end physical receiver in a dimly lit studio.

| Token | Color | Hex | Purpose |
|---|---|---|---|
| **Background** | Matte Charcoal | `#161618` | Main application background |
| **Surface** | Brushed Obsidian | `#1D1D20` | Cards, sidebar, and container elements |
| **Border** | Studio Grey | `#323236` | 1px separator lines and wireframe borders |
| **Primary/Accent** | Neon Amber | `#FF6B00` | VU meter peaks, highlights, selected states |
| **Secondary** | VU Gold | `#FFCC00` | Warning states, suggestion alerts, active knobs |
| **Text Primary** | Paper White | `#F3F3F3` | Primary reading text |
| **Text Secondary** | Tape Silver | `#A1A1AA` | Technical labels, metadata info, subtitles |

### 2.2 Light Theme (The "Music Review" Mode)
An elegant, high-contrast theme resembling editorial magazines like *Pitchfork* or printed album liner notes.

| Token | Color | Hex | Purpose |
|---|---|---|---|
| **Background** | Linen White | `#FAF8F5` | Main application background |
| **Surface** | Smooth Alabaster | `#F1ECE4` | Cards, sidebars, and dropdowns |
| **Border** | Ink Black Thin | `#1A1A1A` | Thin, high-contrast borders and separators |
| **Primary/Accent** | Editorial Crimson | `#B22222` | Highlights, branding accents, primary actions |
| **Secondary** | Forest Dark | `#1A4331` | Headers, active sub-states, safe statuses |
| **Text Primary** | Deep Ink | `#1A1A1A` | Primary text and major labels |
| **Text Secondary** | Dust Grey | `#5F5F5F` | Technical data, helper text, subtitles |

---

## 3. Typography

A hybrid font system is crucial for achieving the "Hi-Fi Editorial" feel.

```text
       ┌────────────────────────┐
       │   Editorial Headers    │  ──► Large Serif (e.g., Lora / Playfair)
       └────────────────────────┘
                   │
                   ▼
       ┌────────────────────────┐
       │     Main Content       │  ──► Clean Sans-Serif (e.g., Inter / Rubik)
       └────────────────────────┘
                   │
                   ▼
       ┌────────────────────────┐
       │     Technical Data     │  ──► Monospace (e.g., JetBrains Mono)
       └────────────────────────┘
```

* **Editorial Headers (Serif):** Used for section titles (e.g., "Statistics", "Health", "Duplicates") and prominent UI metrics. Gives a literary, high-quality music publication feel.
* **Main Content & UI (Sans-Serif):** Used for navigation, song titles, buttons, and settings. Extremely clean, geometric, and readable.
* **Technical Data (Monospace):** Used for file extensions, bitrates, sample rates, durations, and paths. Highlights the "engineering" aspect of music organization.

---

## 4. UI Layout & Component Guidelines

To avoid the "generic CRUD" look, components should follow these guidelines:

### 4.1 Layout Boundaries
* **Brutalist Wireframes:** Use clean 1px borders instead of heavy, soft drop shadows. Cards and panels should look flat, sharp, and physically constructed.
* **Negative Space:** Give components room to breathe. Avoid dense packing of text. This is what makes the design feel expensive and artistic.

### 4.2 Tactile Controls
* **Toggle Buttons:** Instead of material switches, style toggle controls to look like physical, metallic slider switches.
* **Active Statuses:** Use tiny colored "LED" indicators (glowing green/amber/red dots) next to labels (e.g., "File Watcher [🟢 Active]", "Library Status [🟠 Needs Sync]").

### 4.3 Operational Flow Pattern

Every operational screen should communicate one compact sequence:

```text
Primary verb-led action → visible running state → outcome → one named next action
```

- Use one primary action per screen state; supporting actions stay contextual.
- `ProgressCard` communicates an in-progress operation; `StatusPill` communicates compact success, warning, or error outcomes; snackbars remain transient confirmation only.
- A control that navigates elsewhere must name that destination (for example, "Open duplicate detection" rather than "Detect").
- Data that opens a filtered view must show a directional cue (for example, "View →"); color alone cannot carry that behavior.
- Keep outcomes scoped to their workflow. A file-trash result belongs in Duplicates, not in a global error region that can surface in another section.

### 4.4 Responsive Desktop Behavior

Melody Sync is desktop-first but the window can be resized. Layouts adapt to three **width classes**, decided by the available window width (not a physical resolution):

| Class | Width | Behavior |
|-------|-------|----------|
| **Compact** | < 900dp | Sidebar collapses to icons; "Melody Sync" title hides; stat cards and side-by-side panels stack into rows/columns; Quick Fix panel narrows; SongList hides secondary columns (Album/Format/Bitrate) |
| **Medium** | 900–1299dp | Sidebar expandable; standard toolbars; cards side-by-side |
| **Expanded** | ≥ 1300dp | Sidebar expanded; generous margins; full column set |

Rules:

- Decisions come from the **available window width**, exposed through `LocalWindowSizeClass`, never from a fixed resolution or device assumption.
- Keep the primary action of each screen reachable at every width (e.g. the Scan button stays visible in compact).
- Secondary information degrades before primary information (e.g. SongList hides Album/Format before Title/Artist).
- Prefer a few explicit layout states over many ad-hoc size checks; concentrate adaptive logic rather than scattering width checks across the UI.

---

## 5. Interaction Model: Assisted Curation

Our flagship interaction design is the **"Quick-Fix HUD" (Heads-Up Display)**. It is designed to minimize friction while keeping the user in full control.

```text
┌───────────────────────────────────────┬────────────────────────────────┐
│  Song List Table                      │  Quick-Fix HUD (Right Panel)   │
│                                       │                                │
│  [■] Smells Like Teen Spirit          │  SELECTED:                     │
│  [ ] Come As You Are                  │  "Smells Like Teen Spirit.mp3" │
│  [ ] In Bloom                         │                                │
│                                       │  ⚠️ Missing: Album, Genre       │
│                                       │                                │
│                                       │  💡 SUGGESTED FIXES:           │
│                                       │  ┌──────────────────────────┐  │
│                                       │  │ [Icon] YouTube Candidate │  │
│                                       │  │ Title: Smells Like...    │  │
│                                       │  │ Album: Nevermind (1991)  │  │
│                                       │  │ [ Apply Suggestion ]     │  │
│                                       │  └──────────────────────────┘  │
│                                       │  ┌──────────────────────────┐  │
│                                       │  │ [Icon] Local Folder Match│  │
│                                       │  │ Title: Smells Like...    │  │
│                                       │  │ Artist: Nirvana          │  │
│                                       │  │ [ Apply Suggestion ]     │  │
│                                       │  └──────────────────────────┘  │
└───────────────────────────────────────┴────────────────────────────────┘
```

### 5.1 The Quick-Fix HUD Panel
When any song is clicked in the Library or Health view, a split-screen panel slides in from the right:

1. **Diagnosis Area:** Displays a crisp summary of what's wrong with the file (e.g., "No Metadata", "Low Bitrate", "Mismatched Folder").
2. **Local Heuristic Suggestions:** The system analyzes the folder structure and filename using regex pattern matchers. If a pattern matches, it suggests titles and artists (e.g., `Nirvana/Nevermind/01-Smells Like Teen Spirit.mp3` yields a high-confidence match).
3. **External YouTube Suggestions:** If a YouTube API Key is present, it displays the top matched candidate with its title, channel (artist), duration, and a miniature thumbnail.
4. **Accept / Merge Actions:**
   * Each suggestion card has a clear, physical **"Apply"** button.
   * Clicking it instantly populates the tags of the file, updates the SQLite cache in background, and shows an elegant "LED green" toast indicating success.
   * The user never had to open a complex dialog, search manually, or type anything — yet they validated the edit personally.
