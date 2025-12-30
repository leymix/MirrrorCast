# 🚀 Sıfırdan CI/CD Kurulum Rehberi

Bu rehber, MirrorCast projesi için CI/CD sürecini sıfırdan nasıl kuracağınızı adım adım açıklar.

## 📋 İçindekiler

1. [Ön Hazırlık](#ön-hazırlık)
2. [Keystore Oluşturma](#1-keystore-oluşturma)
3. [GitHub Secrets Ayarlama](#2-github-secrets-ayarlama)
4. [İlk Build ve Test](#3-ilk-build-ve-test)
5. [Branch Yapısı](#4-branch-yapısı)
6. [APK Güncelleme Süreci](#5-apk-güncelleme-süreci)
7. [Sorun Giderme](#6-sorun-giderme)

---

## Ön Hazırlık

### Gereksinimler

- ✅ GitHub hesabı ve repository
- ✅ Android Studio veya Android SDK
- ✅ Java JDK 17 veya üzeri
- ✅ Git kurulu

### Proje Durumu Kontrolü

Projenizde şu dosyaların olduğundan emin olun:
- ✅ `.github/workflows/android-ci-cd.yml` (CI/CD workflow)
- ✅ `app/build.gradle.kts` (Product flavors ile güncellenmiş)
- ✅ `app/src/main/AndroidManifest.xml` (App name dinamik)

---

## 1. Keystore Oluşturma

### Adım 1.1: Keystore Dosyası Oluştur

#### Yöntem A: PowerShell Script ile (Önerilen)

Proje klasöründe `create-keystore.ps1` scriptini çalıştırın:

```powershell
.\create-keystore.ps1
```

#### Yöntem B: Manuel Komut ile

Terminal/PowerShell'de proje klasörüne gidin ve şu komutu çalıştırın:

**Windows'ta (Android Studio keytool kullanarak):**
```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mirrorcast
```

**Eğer keytool PATH'te ise:**
```bash
keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mirrorcast
```

**Komut çalıştırıldığında sorulacaklar:**
1. **Keystore şifresi**: Güçlü bir şifre belirleyin (örn: `MySecurePass123!`)
2. **Tekrar keystore şifresi**: Aynı şifreyi tekrar girin
3. **Adınız ve soyadınız**: İsim girin (örn: `John Doe`)
4. **Organizasyon birimi**: (Opsiyonel, Enter'a basabilirsiniz)
5. **Organizasyon**: (Opsiyonel, Enter'a basabilirsiniz)
6. **Şehir**: (Opsiyonel, Enter'a basabilirsiniz)
7. **Eyalet**: (Opsiyonel, Enter'a basabilirsiniz)
8. **Ülke kodu**: (Opsiyonel, Enter'a basabilirsiniz)
9. **Onay**: `yes` yazın
10. **Key şifresi**: Keystore şifresiyle aynı olabilir (Enter'a basın) veya farklı bir şifre girin

**Önemli:** Keystore şifrenizi ve key şifrenizi güvenli bir yerde saklayın! Kaybederseniz APK'ları güncelleyemezsiniz.

### Adım 1.2: Keystore Dosyasını Kontrol Et

Keystore dosyasının oluştuğunu kontrol edin:

```bash
# Windows
dir keystore.jks

# Linux/Mac
ls -la keystore.jks
```

**⚠️ ÖNEMLİ:** `keystore.jks` dosyasını **ASLA** Git'e commit etmeyin! Bu dosya `.gitignore` dosyasında olmalı.

---

## 2. GitHub Secrets Ayarlama

### Adım 2.1: Keystore'u Base64'e Çevir

GitHub Secrets'a keystore'u eklemek için önce base64 formatına çevirmemiz gerekiyor.

#### Windows (PowerShell):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore.jks")) | Out-File -Encoding ASCII keystore_base64.txt
```

Ardından `keystore_base64.txt` dosyasını açın ve içeriğini kopyalayın.

#### Linux/Mac:

```bash
base64 -i keystore.jks -o keystore_base64.txt
```

Ardından dosyayı açın:
```bash
cat keystore_base64.txt
```

**Not:** Base64 string çok uzun olacak, tamamını kopyalayın.

### Adım 2.2: GitHub Repository'ye Git

1. GitHub'da projenizin repository sayfasına gidin
2. **Settings** sekmesine tıklayın (sağ üstte)
3. Sol menüden **Secrets and variables** → **Actions** seçeneğine tıklayın

### Adım 2.3: Secrets Ekle

**New repository secret** butonuna tıklayın ve aşağıdaki secrets'ları tek tek ekleyin:

#### Secret 1: KEYSTORE_FILE
- **Name**: `KEYSTORE_FILE`
- **Secret**: `keystore_base64.txt` dosyasının **tam içeriği** (tüm satırı kopyalayın)
- **Add secret** butonuna tıklayın

#### Secret 2: KEYSTORE_PASSWORD
- **Name**: `KEYSTORE_PASSWORD`
- **Secret**: Keystore oluştururken girdiğiniz keystore şifresi
- **Add secret** butonuna tıklayın

#### Secret 3: KEY_PASSWORD
- **Name**: `KEY_PASSWORD`
- **Secret**: Keystore oluştururken girdiğiniz key şifresi (genelde keystore şifresiyle aynı)
- **Add secret** butonuna tıklayın

#### Secret 4: KEY_ALIAS (Opsiyonel)
- **Name**: `KEY_ALIAS`
- **Secret**: `mirrorcast` (keystore oluştururken kullandığınız alias)
- **Add secret** butonuna tıklayın

**Not:** KEY_ALIAS eklemezseniz, varsayılan olarak "mirrorcast" kullanılacaktır.

### Adım 2.4: Secrets Kontrolü

Secrets listesinde şunların olduğundan emin olun:
- ✅ `KEYSTORE_FILE`
- ✅ `KEYSTORE_PASSWORD`
- ✅ `KEY_PASSWORD`
- ✅ `KEY_ALIAS` (opsiyonel)

---

## 3. İlk Build ve Test

### Adım 3.1: Yerel Build Testi

CI/CD'yi test etmeden önce yerel olarak build'in çalıştığından emin olalım.

#### Debug Build Testi:

```bash
# Development ortamı için
./gradlew assembleDevDebug

# Staging ortamı için
./gradlew assembleStgDebug

# Production ortamı için
./gradlew assembleProdDebug
```

Build başarılı olursa şu mesajı göreceksiniz:
```
BUILD SUCCESSFUL in Xs
```

#### Release Build Testi (Keystore ile):

1. `local.properties` dosyasını oluşturun veya düzenleyin (proje kök dizininde):

```properties
KEYSTORE_FILE=keystore.jks
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=mirrorcast
KEY_PASSWORD=your_key_password
```

**Not:** `your_keystore_password` ve `your_key_password` yerine gerçek şifrelerinizi yazın.

2. Release build yapın:

```bash
./gradlew assembleDevRelease
```

Build başarılı olursa APK dosyası şu konumda olacak:
```
app/build/outputs/apk/dev/release/app-dev-release.apk
```

### Adım 3.2: Git Commit ve Push

Değişiklikleri commit edin:

```bash
git add .
git commit -m "CI/CD kurulumu ve ortam yapılandırması eklendi"
git push origin main
```

**Not:** İlk kez push yapıyorsanız:
```bash
git remote add origin https://github.com/kullaniciadi/repo-adi.git
git push -u origin main
```

---

## 4. Branch Yapısı

### Adım 4.1: Branch'leri Oluştur

Projenizde üç ana branch olmalı:

```bash
# Development branch
git checkout -b develop
git push -u origin develop

# Staging branch
git checkout -b staging
git push -u origin staging

# Production branch (main zaten var)
git checkout main
```

### Adım 4.2: Branch Stratejisi

- **`develop`**: Günlük geliştirme çalışmaları
  - Her push'ta **dev** ortamı için APK build edilir
  - Debug ve Release APK'lar artifacts olarak kaydedilir

- **`staging`**: Production öncesi test
  - Her push'ta **stg** ortamı için APK build edilir
  - Debug ve Release APK'lar artifacts olarak kaydedilir

- **`main`**: Production (canlı)
  - Her push'ta **prod** ortamı için APK build edilir
  - Debug ve Release APK'lar artifacts olarak kaydedilir
  - `[release]` commit mesajı ile GitHub Release oluşturulur

---

## 5. APK Güncelleme Süreci

### Senaryo 1: Development Ortamı için APK

1. `develop` branch'ine geçin:
```bash
git checkout develop
```

2. Değişikliklerinizi yapın ve commit edin:
```bash
git add .
git commit -m "Yeni özellik eklendi"
git push origin develop
```

3. GitHub Actions otomatik olarak çalışacak:
   - GitHub repository'nizde **Actions** sekmesine gidin
   - Workflow'un çalıştığını göreceksiniz
   - Tamamlandığında **Artifacts** bölümünden APK'yı indirebilirsiniz

### Senaryo 2: Staging Ortamı için APK

1. `staging` branch'ine geçin:
```bash
git checkout staging
git merge develop  # develop'daki değişiklikleri al
```

2. Push yapın:
```bash
git push origin staging
```

3. GitHub Actions otomatik olarak **stg** ortamı için APK build edecek

### Senaryo 3: Production Release

#### Yöntem A: Otomatik Release (Commit Mesajı ile)

1. `main` branch'ine geçin:
```bash
git checkout main
git merge staging  # staging'deki değişiklikleri al
```

2. Commit mesajına `[release]` ekleyin:
```bash
git commit -m "[release] Production'a yeni özellikler eklendi"
git push origin main
```

3. GitHub Actions:
   - APK build edilir
   - GitHub Release oluşturulur
   - APK release'e eklenir

#### Yöntem B: Manuel Release

1. GitHub repository'nizde **Actions** sekmesine gidin
2. Sol menüden **Android CI/CD** workflow'unu seçin
3. Sağ üstteki **Run workflow** butonuna tıklayın
4. **Environment** seçin: `prod`
5. **Run workflow** butonuna tıklayın
6. Workflow tamamlandığında **Releases** sekmesinden APK'yı indirebilirsiniz

---

## 6. Sorun Giderme

### Problem 1: "Keystore file not found"

**Çözüm:**
- GitHub Secrets'da `KEYSTORE_FILE` secret'ının doğru base64 encoded olduğundan emin olun
- Base64 string'in tamamını kopyaladığınızdan emin olun (çok uzun olacak)
- Yeni satır karakterleri olmamalı

### Problem 2: "Signing config not found"

**Çözüm:**
- `app/build.gradle.kts` dosyasında signing config'in doğru tanımlandığından emin olun
- Keystore dosyasının doğru path'te olduğunu kontrol edin

### Problem 3: "Build failed"

**Çözüm:**
1. Yerel olarak build yapmayı deneyin:
```bash
./gradlew clean
./gradlew assembleDevDebug
```

2. Gradle cache'i temizleyin:
```bash
./gradlew cleanBuildCache
```

3. JDK versiyonunu kontrol edin (17 gereklidir):
```bash
java -version
```

### Problem 4: "Workflow çalışmıyor"

**Çözüm:**
1. `.github/workflows/android-ci-cd.yml` dosyasının doğru branch'te olduğundan emin olun
2. GitHub Actions'ın repository'de aktif olduğundan emin olun (Settings → Actions → General)
3. Workflow dosyasının syntax'ının doğru olduğundan emin olun

### Problem 5: "APK bulunamıyor"

**Çözüm:**
- Workflow tamamlandıktan sonra **Artifacts** bölümüne bakın
- APK path'lerinin doğru olduğundan emin olun:
  - Dev: `app/build/outputs/apk/dev/release/app-dev-release.apk`
  - Stg: `app/build/outputs/apk/stg/release/app-stg-release.apk`
  - Prod: `app/build/outputs/apk/prod/release/app-prod-release.apk`

---

## 📱 APK İndirme ve Yükleme

### GitHub Actions'dan İndirme

1. GitHub repository'nizde **Actions** sekmesine gidin
2. Tamamlanmış bir workflow run'ı seçin
3. Sayfanın altında **Artifacts** bölümünü bulun
4. İstediğiniz APK'yı (debug veya release) indirin

### GitHub Releases'den İndirme

1. GitHub repository'nizde **Releases** sekmesine gidin
2. En son release'i seçin
3. APK dosyasını indirin

### Android Cihaza Yükleme

1. İndirdiğiniz APK dosyasını Android cihazınıza aktarın
2. Dosya yöneticisinden APK'yı açın
3. "Bilinmeyen kaynaklardan yükleme" izni verin (gerekirse)
4. Yükleme tamamlandığında uygulamayı açın

**Not:** Her ortam için farklı Application ID olduğu için (dev, stg, prod) aynı cihazda birden fazla ortamı yükleyebilirsiniz.

---

## ✅ Kontrol Listesi

Kurulumun tamamlandığını doğrulamak için:

- [ ] Keystore dosyası oluşturuldu (`keystore.jks`)
- [ ] Keystore base64'e çevrildi
- [ ] GitHub Secrets eklendi (KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_PASSWORD)
- [ ] Yerel build test edildi (başarılı)
- [ ] Branch'ler oluşturuldu (develop, staging, main)
- [ ] İlk push yapıldı
- [ ] GitHub Actions workflow çalıştı
- [ ] APK artifacts olarak kaydedildi
- [ ] Release oluşturuldu (opsiyonel)

---

## 🎉 Tebrikler!

CI/CD kurulumunuz tamamlandı! Artık:

- ✅ Her branch push'unda otomatik APK build ediliyor
- ✅ Her ortam için ayrı APK'lar oluşturuluyor
- ✅ Release APK'lar otomatik olarak imzalanıyor
- ✅ GitHub Releases ile APK dağıtımı yapılabiliyor

**Sonraki Adımlar:**
- Version numaralarını güncelleyin (`app/build.gradle.kts`)
- API endpoint'lerini ortamlarınıza göre güncelleyin
- Test süreçlerinizi otomatikleştirin

---

## 📞 Yardım

Sorun yaşarsanız:
1. `CI_CD_SETUP.md` dosyasına bakın (detaylı dokümantasyon)
2. GitHub Actions log'larını kontrol edin
3. Yerel build hatalarını test edin

