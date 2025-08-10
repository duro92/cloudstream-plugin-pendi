
# Cloudstream Plugins — Starter Repo

Repo ini dibuat mengikuti panduan resmi Cloudstream.

## Cara pakai cepat
1) Buka project ini di Android Studio atau Gradle CLI.
2) Ganti metadata di `MyProvider/build.gradle.kts` bagian `cloudstream { ... }`.
3) Build plugin:
   ```bash
   ./gradlew MyProvider:make
   ```
4) File APK plugin akan ada di `MyProvider/build/outputs/plugin/`.

## Referensi resmi
- https://recloudstream.github.io/csdocs/devs/gettingstarted/
