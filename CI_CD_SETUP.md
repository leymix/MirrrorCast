# CI/CD Kurulum Rehberi

Bu dokümantasyon, MirrorCast projesi için CI/CD sürecinin nasıl kurulacağını ve APK güncellemelerinin nasıl atılacağını açıklar.

## 📋 İçindekiler

1. [Ortamlar (Environments)](#ortamlar-environments)
2. [GitHub Actions Kurulumu](#github-actions-kurulumu)
3. [Keystore Yapılandırması](#keystore-yapılandırması)
4. [APK Güncelleme Süreci](#apk-güncelleme-süreci)
5. [Manuel Release Oluşturma](#manuel-release-oluşturma)
6. [Ortam Bazlı Build](#ortam-bazlı-build)

## 🌍 Ortamlar (Environments)

Proje üç farklı ortamda çalışabilir:

### Development (dev)
- **Branch**: `develop`
- **Application ID**: `com.mirrorcast.dev`
- **App Name**: CastLink Dev
- **Version Suffix**: `-dev`
- **API Base URL**: `https://api-dev.mirrorcast.com`
- **Kullanım**: Geliştirme ve test için

### Staging (stg)
- **Branch**: `staging`
- **Application ID**: `com.mirrorcast.stg`
- **App Name**: CastLink Staging
- **Version Suffix**: `-stg`
- **API Base URL**: `https://api-staging.mirrorcast.com`
- **Kullanım**: Production öncesi test için

### Production (prod)
- **Branch**: `main`
- **Application ID**: `com.mirrorcast`
- **App Name**: CastLink
- **Version Suffix**: Yok
- **API Base URL**: `https://api.mirrorcast.com`
- **Kullanım**: Canlı ortam

### BuildConfig Kullanımı

Kod içinde ortam bilgisine erişmek için:

```kotlin
import com.mirrorcast.BuildConfig

// Ortam bilgisi
val environment = BuildConfig.ENVIRONMENT // "dev", "staging", "production"

// API base URL
val apiUrl = BuildConfig.API_BASE_URL
```

## 🚀 GitHub Actions Kurulumu

### 1. GitHub Secrets Ayarlama

GitHub repository'nizde aşağıdaki secrets'ları eklemeniz gerekmektedir:

1. Repository'ye gidin → **Settings** → **Secrets and variables** → **Actions**
2. **New repository secret** butonuna tıklayın
3. Aşağıdaki secrets'ları ekleyin:

#### Gerekli Secrets:

- **`KEYSTORE_FILE`**: Keystore dosyanızın base64 encoded hali
- **`KEYSTORE_PASSWORD`**: Keystore şifreniz
- **`KEY_PASSWORD`**: Key şifreniz
- **`KEY_ALIAS`**: (Opsiyonel) Key alias'ı (varsayılan: "mirrorcast")

### 2. Keystore Dosyasını Base64'e Çevirme

```bash
# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore.jks")) | Out-File -Encoding ASCII keystore_base64.txt

# Linux/Mac
base64 -i keystore.jks -o keystore_base64.txt
```

Ardından `keystore_base64.txt` dosyasının içeriğini `KEYSTORE_FILE` secret'ına yapıştırın.

### 3. Keystore Oluşturma (Eğer yoksa)

```bash
keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mirrorcast
```

## 📦 APK Güncelleme Süreci

### Otomatik Build (Branch Bazlı)

Her branch'e push yaptığınızda otomatik olarak ilgili ortam için build yapılır:

- **`develop` branch** → `dev` ortamı için build
- **`staging` branch** → `stg` ortamı için build
- **`main` branch** → `prod` ortamı için build

Her push'ta:
1. Debug APK build edilir
2. Release APK build edilir (signed)
3. Artifacts olarak kaydedilir

### Manuel Release Oluşturma

1. GitHub Actions sekmesine gidin
2. **Android CI/CD** workflow'unu seçin
3. **Run workflow** butonuna tıklayın
4. **Environment** seçin (dev, stg, veya prod)
5. İsteğe bağlı olarak release version girebilirsiniz
6. **Run workflow** butonuna tıklayın

### Commit Mesajı ile Release

Main branch'e push yaparken commit mesajınıza `[release]` ekleyin:

```bash
git commit -m "[release] Yeni özellikler eklendi"
git push origin main
```

Bu durumda otomatik olarak GitHub Release oluşturulur (prod ortamı için).

## 🔧 Yerel Build

### Debug APK

```bash
# Development ortamı
./gradlew assembleDevDebug

# Staging ortamı
./gradlew assembleStgDebug

# Production ortamı
./gradlew assembleProdDebug

# Tüm ortamlar için
./gradlew assembleDebug
```

APK dosyaları:
- Dev: `app/build/outputs/apk/dev/debug/app-dev-debug.apk`
- Stg: `app/build/outputs/apk/stg/debug/app-stg-debug.apk`
- Prod: `app/build/outputs/apk/prod/debug/app-prod-debug.apk`

### Release APK (Yerel)

1. `local.properties` dosyasına keystore bilgilerini ekleyin:

```properties
KEYSTORE_FILE=keystore.jks
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=mirrorcast
KEY_PASSWORD=your_key_password
```

2. Build komutu:

```bash
# Development ortamı
./gradlew assembleDevRelease

# Staging ortamı
./gradlew assembleStgRelease

# Production ortamı
./gradlew assembleProdRelease
```

APK dosyaları:
- Dev: `app/build/outputs/apk/dev/release/app-dev-release.apk`
- Stg: `app/build/outputs/apk/stg/release/app-stg-release.apk`
- Prod: `app/build/outputs/apk/prod/release/app-prod-release.apk`

## 🌍 Ortam Bazlı Build

### Android Studio'da Build Variant Seçme

1. Android Studio'yu açın
2. **Build Variants** panelini açın (View → Tool Windows → Build Variants)
3. İstediğiniz variant'ı seçin:
   - `devDebug` / `devRelease`
   - `stgDebug` / `stgRelease`
   - `prodDebug` / `prodRelease`

### Gradle Komutları

```bash
# Belirli bir ortam için debug build
./gradlew assembleDevDebug
./gradlew assembleStgDebug
./gradlew assembleProdDebug

# Belirli bir ortam için release build
./gradlew assembleDevRelease
./gradlew assembleStgRelease
./gradlew assembleProdRelease

# Tüm ortamlar için build
./gradlew assembleDebug    # Tüm debug variant'ları
./gradlew assembleRelease  # Tüm release variant'ları
```

## 📱 Version Güncelleme

Version'ı güncellemek için `app/build.gradle.kts` dosyasını düzenleyin:

```kotlin
defaultConfig {
    versionCode = 2  // Her release'te artırın
    versionName = "1.1"  // Semantic versioning
}
```

## 🔍 Workflow Detayları

### Build Job

- Her push ve PR'da çalışır
- Branch'e göre otomatik ortam belirlenir:
  - `develop` → dev
  - `staging` → stg
  - `main` → prod
- Debug APK build eder
- Release APK da build eder (signed)
- Artifacts olarak kaydedilir

### Release Job

- Sadece manuel trigger veya `[release]` commit mesajı ile çalışır
- Manuel trigger'da ortam seçilebilir
- Otomatik trigger'da main branch için prod ortamı kullanılır
- Signed Release APK oluşturur
- GitHub Release oluşturur
- APK'yı release'e ekler
- Dev ve Stg ortamları için prerelease olarak işaretlenir

## 🐛 Sorun Giderme

### Keystore Bulunamadı Hatası

- `KEYSTORE_FILE` secret'ının doğru base64 encoded olduğundan emin olun
- Workflow'da keystore dosyasının doğru path'te olduğunu kontrol edin

### Signing Hatası

- Tüm secrets'ların doğru girildiğinden emin olun
- Key alias'ın doğru olduğunu kontrol edin

### Build Başarısız

- Gradle cache'i temizleyin: `./gradlew clean`
- JDK versiyonunu kontrol edin (17 gereklidir)

## 📚 Ek Kaynaklar

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Android App Signing](https://developer.android.com/studio/publish/app-signing)
- [Gradle Build Configuration](https://developer.android.com/studio/build)

