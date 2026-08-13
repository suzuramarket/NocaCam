# Cara Membuat NovaCam APK Hanya dengan HP

ZIP ini berisi source code, jadi belum bisa langsung di-install seperti APK. Cara paling mudah tanpa laptop adalah memakai GitHub Actions untuk membuat APK di cloud.

## Langkah di HP

1. Buka `github.com` di browser dan buat repository baru, misalnya `novacam`.
2. Extract `NovaCam-Android.zip` di aplikasi Files/ZArchiver.
3. Upload **isi ZIP** ke repository tersebut. Pastikan folder `novacam-android` dan `.github` berada di level paling atas repository.
4. Buka tab **Actions** pada repository.
5. Pilih workflow **Build NovaCam APK**.
6. Tekan **Run workflow**.
7. Setelah selesai, buka hasil workflow tersebut dan download artifact **NovaCam-debug-apk**.
8. Extract artifact, buka file `app-debug.apk`, lalu izinkan browser/file manager memasang aplikasi dari sumber tersebut.
9. Install dan buka NovaCam. Berikan izin kamera saat diminta.

Build cloud ini memakai Java 17, Android Gradle Plugin, dan Android SDK yang disiapkan oleh GitHub Actions. HP hanya dipakai untuk upload source dan download APK.

## Jika GitHub Actions tidak terlihat

Pastikan folder `.github/workflows` ikut ter-upload. Folder tersebut berisi `build-novacam-apk.yml`. Setelah folder terlihat di repository, buka tab **Actions** dan jalankan workflow secara manual.

## Catatan penggunaan

- APK debug ini cocok untuk pemakaian pribadi dan pengujian.
- Android dapat menampilkan peringatan keamanan saat memasang APK yang dibuat di luar Play Store.
- Jika kamera, RAW, OIS/EIS, atau fitur tertentu tidak tersedia pada HP, NovaCam akan menampilkan fallback yang aman.
- Untuk hasil foto, izinkan akses kamera. Izin media hanya diperlukan oleh versi Android tertentu saat menyimpan hasil.