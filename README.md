# File Master — Android project

Dark-mode file browser prototype for Android 15 (minimum Android 8 / API 26).

## Implemented in this starter
- Android Storage Access Framework folder picker; user grants access to a selected folder.
- Folder navigation and filename search within the selected folder.
- Built-in PDF page viewer using Android `PdfRenderer`.
- DOCX text extraction using Apache POI.
- ZIP archive entry listing (not extraction yet).
- Incoming `VIEW` and `SEND` intents for common document MIME types, so Android apps can offer File Master in “Open with” / Share flows.
- No network permission is requested; browsing/reading is intended to work offline.

## Important limitations
- Android deliberately restricts access to arbitrary app/private folders. File Master can browse locations the user explicitly selects, not bypass Android permissions.
- PDF and DOCX are handled internally. Legacy DOC, RAR, Excel editing, PowerPoint and ZIP extraction are not complete yet.
- For formats not yet supported internally, Android may offer another installed app.
- Incoming file intents vary by source app; some apps share a `content://` URI or a temporary permission. This project handles common `VIEW` and `SEND` cases, but requires device testing.

## Build APK without a PC (GitHub Actions)
1. Sign in to GitHub in your phone browser and create a new **private** repository.
2. Upload the contents of this ZIP into the repository (not the ZIP file itself).
3. Open the repository's **Actions** tab and allow workflows if GitHub asks.
4. Select **Build File Master APK** → **Run workflow**.
5. When the run completes, open it and download the artifact named `File-Master-debug-apk`.
6. Extract `app-debug.apk` on your phone and install it. Android may ask permission to install apps from that browser/file manager.

This workflow produces a debug APK for testing, not a Play Store release. It is not signed with a personal release key.
