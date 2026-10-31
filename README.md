# SAZFLOY — Video & Photo Editor

SAZFLOY adalah aplikasi pengedit video dan foto modern native Android (Kotlin & Jetpack Compose) yang terinspirasi dari alur kerja kreatif CapCut, dengan identitas visual orisinal, dark studio aesthetic, dan arsitektur pemrosesan media nyata.

---

## 🚀 Arsitektur & Teknologi Terpilih
- **Bahasa & UI:** Kotlin + Jetpack Compose + Material Design 3.
- **Tema:** Dark Studio Theme (`#0D0E12` background, neon cyan `#00E5FF` & electric violet `#8A3FFC` accents).
- **Video Engine:** Android native `MediaCodec` (H.264/AVC Hardware Video Encoder) + `MediaMuxer` (MP4 Container) + `Canvas` frame compositor.
- **Photo Engine:** Android `Bitmap` + `ColorMatrixColorFilter` + multi-layer canvas rendering.
- **Penyimpanan Lokal (Persistence):** AndroidX Room Database (`sazfloy_projects.db`) dengan serialisasi metadata klip, durasi, timeline, teks, audio, dan parameter filter.
- **Ekspor Media:** Integrasi Android `MediaStore` (`Movies/SAZFLOY` dan `Pictures/SAZFLOY`) dengan dukungan `Intent.ACTION_VIEW` dan `Intent.ACTION_SEND` (Share).

---

## 📱 Fitur Utama yang Berfungsi Penuh

### 1. Orientasi Layar (Portrait & Landscape)
- **Mode Portrait:** Tata letak vertikal satu tangan dengan preview di atas, multi-track timeline di tengah, dan tool panel di bawah.
- **Mode Landscape:** Tata letak horizontal lebar dengan area preview lapang di sisi kiri serta timeline lebar dan tool panel di sisi kanan.
- **Mode Switcher:** Pilihan `Auto Rotate`, `Portrait Only`, dan `Landscape Only` tanpa kehilangan progres editing atau posisi playhead.
- **Rasio Proyek Terpisah:** Mengubah orientasi HP tidak merusak rasio video (`9:16`, `16:9`, `1:1`, `4:5`, `Original`).

### 2. Video Editor & Multi-Track Timeline
- **Timeline Multi-Track:** Track video klip, track text overlay, dan track audio track dengan visual duration.
- **Scrubbing Playhead:** Playhead interaktif neon cyan yang dapat digeser ke detik mana pun.
- **Zoom Timeline:** Pembesaran timeline 50% hingga 250% untuk presisi frame demi frame.
- **Clip Editing:**
  - *Trim*: Pengaturan durasi awal dan akhir klip.
  - *Split*: Membagi klip menjadi dua bagian tepat pada posisi playhead.
  - *Speed Control*: 0.25x, 0.5x, 1.0x, 1.5x, 2.0x, 3.0x.
  - *Volume*: 0% hingga 200% dan mute toggle.
  - *Transform*: Rotate 90°, Flip Horizontal, Flip Vertical.
  - *Duplicate & Delete*: Menambah atau menghapus klip secara instan.
- **Transisi Antar Klip:** Fade, Dissolve, Slide Left, Slide Right, Zoom In, Flash White.

### 3. Photo Editor
- **Penyetelan Manual:** Brightness, Contrast, Saturation, Warmth/Temperature, Tint, Vignette.
- **10 Preset Warna SAZFLOY:**
  1. SAZFLOY Natural
  2. SAZFLOY Cinematic
  3. SAZFLOY Film
  4. SAZFLOY Portrait
  5. SAZFLOY Warm
  6. SAZFLOY Cool
  7. SAZFLOY Vintage
  8. SAZFLOY Night
  9. SAZFLOY Landscape
  10. SAZFLOY Black & White
- **Slider Intensitas Filter:** 0% hingga 100%.
- **Before / After Comparison:** Tombol perbandingan langsung dengan gambar asli.
- **Text & Stickers:** Penambahan teks kustom, pilihan warna, background, dan stiker emoji.
- **Transformasi:** Crop ratio, Rotate 90°, Flip Horizontal & Vertical.

### 4. Ekspor Video & Foto
- **Resolusi:** 480p, 720p (HD), 1080p (Full HD).
- **Frame Rate:** 24 FPS, 30 FPS, 60 FPS.
- **Kualitas:** Saving (Hemat), Standard, High.
- **Progres Nyata:** Progress bar rendering per frame dengan tombol pembatalan.
- **Galeri & Bagikan:** Hasil disimpan langsung ke galeri Android dan dapat langsung dibuka atau dibagikan ke TikTok, Reels, atau YouTube.

### 5. Template Siap Pakai (100% Offline)
Tersedia 11 template bawaan:
- Cinematic Urban
- Travel Wanderlust
- Daily Vlog Story
- School Memories
- Birthday Celebration
- Dynamic Photo Slideshow
- Portrait Viral Reel
- Landscape Epic Cinema
- Minimalist Clean
- Documentary Heritage

---

## 🛠 Panduan Membangun APK Android

### Di AI Studio (Otomatis)
1. Buka menu pengaturan (Settings) pada AI Studio.
2. Pilih **Export APK** atau **Download Project ZIP**.
3. File `.apk` siap diunduh dan dipasang pada HP Android.

### Menggunakan Gradle CLI / Android Studio
1. Clone / ekstrak proyek.
2. Pastikan Android SDK (API 34/36) dan Java 17/21 terpasang.
3. Jalankan perintah:
   ```bash
   gradle assembleDebug
   ```
4. File APK akan dihasilkan di:
   `app/build/outputs/apk/debug/app-debug.apk`
5. Pasang ke perangkat:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
