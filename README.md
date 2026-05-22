# 🎬 Trigger's Professional Downloading Kit

A Java desktop app that gives `yt-dlp` a clean GUI — download YouTube videos and audio without touching the terminal.

---

## 📸 What It Does

- Paste one or multiple YouTube URLs and download them in batch
- Choose format (MP4, WebM, MP3, WAV), quality (360p → 1080p / Best), and frame rate
- Watch live download progress per URL in a table (speed, %, status)
- Stop all downloads instantly with one button
- Auto-opens the save folder when done

---

## ⚙️ Requirements

| Requirement | Notes |
|---|---|
| **Java 17+** | Must be installed and on PATH |
| **yt-dlp** | Install via `winget install yt-dlp` or [yt-dlp releases](https://github.com/yt-dlp/yt-dlp/releases) |
| **Deno 2+ / Node 20+** | Optional but recommended — needed for some YouTube videos |
| **cookies.txt** | Optional — place next to the `.jar` for age-restricted / bot-blocked videos |

---

## 🚀 How to Run

```bash
# Compile
javac YoutubeDownloaderV2.java

# Run
java YoutubeDownloaderV2
```
Or

Double-click Launch V2.bat to start the downloader.

---

## 🖥️ Features

**Batch Downloads** — paste multiple URLs, one per line. All run in parallel (up to 3 at a time).

**Format & Quality Control**
- Video: MP4 or WebM
- Audio: MP3 or WAV
- Quality: 360p, 480p, 720p, 1080p, or Best
- FPS: 30, 60, or Best

**File Naming Styles**
- `Title.ext` (default)
- `1. Title.ext`
- `01. Title.ext` (zero-padded, great for playlists)

**Smart Environment Detection** — on startup it checks for yt-dlp, JS runtimes (Deno, Node, Bun, QuickJS), and a cookies file. Warns you if anything's missing before you hit download.

**Live Status Table** — each URL gets its own row showing current status, download speed, and progress percentage.

---

## 📁 Project Structure

```
YoutubeDownloaderV2/
├── YoutubeDownloaderV2.java   # Main app (single file)
├── cookies.txt                # Optional — place here for YouTube auth
└── image.jpg                  # Optional — app icon (C:\yt-dle\image.jpg)
```

---

## 🍪 YouTube Bot / Cookie Issues

Some videos will fail with errors like *"Sign in to confirm you're not a bot"*. Fix:

1. Export your YouTube cookies from your browser using a browser extension like **Get cookies.txt LOCALLY**
2. Save the file as `cookies.txt` next to the app
3. The app will auto-detect and use it

---

## ❌ Common Errors

| Status in Table | Meaning | Fix |
|---|---|---|
| `Needs Deno` | No JS runtime found | Install [Deno 2+](https://deno.com) |
| `Needs Cookies` | YouTube bot check failed | Add `cookies.txt` |
| `PO Token` | YouTube PO token required | Update yt-dlp + install Deno |
| `Error` | Generic failure | Check yt-dlp version (`yt-dlp -U`) |

---

## 🔧 Built With

- **Java Swing** — UI framework
- **yt-dlp** — the actual download engine (external CLI tool)
- **ExecutorService** — parallel download threading

---

*Personal project. Not affiliated with YouTube or yt-dlp.*
