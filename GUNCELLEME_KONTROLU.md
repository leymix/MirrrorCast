# 🔄 Uygulama Güncelleme Kontrolü

Bu dokümantasyon, manuel APK kurulumu yapılan uygulamalar için otomatik güncelleme kontrolü sistemini açıklar.

## 📋 Genel Bakış

Uygulama, Google Play Store kullanmadan GitHub Releases API üzerinden güncelleme kontrolü yapar. Yeni bir sürüm yayınlandığında, kullanıcıya otomatik olarak bildirim gösterilir ve APK indirme seçeneği sunulur.

## 🚀 Nasıl Çalışır?

1. **Uygulama Başlangıcı**: Uygulama açıldığında (sadece production ortamında) otomatik olarak GitHub Releases API'ye istek atar.

2. **Güncelleme Kontrolü**: 
   - GitHub Releases'tan en son production release'i alır
   - Mevcut uygulama versiyonu ile karşılaştırır
   - Yeni sürüm varsa kullanıcıya bildirim gösterir

3. **Güncelleme İndirme**: 
   - Kullanıcı "İndir ve Kur" butonuna tıkladığında
   - APK dosyası indirilir
   - İndirme tamamlandığında kurulum ekranı açılır

## ⚙️ Kurulum

### 1. GitHub Repository Bilgisini Ayarlama

`app/build.gradle.kts` dosyasında production flavor'ına GitHub repository bilgisini ekleyin:

```kotlin
create("prod") {
    dimension = "environment"
    resValue("string", "app_name", "CastLink")
    buildConfigField("String", "ENVIRONMENT", "\"production\"")
    buildConfigField("String", "API_BASE_URL", "\"https://api.mirrorcast.com\"")
    // GitHub repository bilgisi - kendi repository'nizi buraya girin
    buildConfigField("String", "GITHUB_REPO", "\"KULLANICI_ADI/REPO_ADI\"")
}
```

**Örnek:**
```kotlin
buildConfigField("String", "GITHUB_REPO", "\"berka/MirrorCast\"")
```

### 2. GitHub Releases'ta APK Yükleme

CI/CD workflow'unuz zaten GitHub Releases'a APK yüklüyor. Emin olun ki:

- Release tag'i formatı: `prod-v1.0.0-20240101-120000` (veya benzeri)
- APK dosya adı: `app-prod-release.apk` içinde "prod" ve "release" kelimeleri geçmeli
- Release, production ortamı için oluşturulmalı

### 3. Version Code ve Version Name

Her yeni release'te `app/build.gradle.kts` dosyasında version bilgilerini güncelleyin:

```kotlin
defaultConfig {
    versionCode = 2  // Her release'te artırın!
    versionName = "1.1"  // Semantic versioning
}
```

**Önemli:** Version code mutlaka artırılmalı. Aksi halde güncelleme algılanmayabilir.

## 🔍 Güncelleme Kontrolü Nasıl Yapılır?

### Otomatik Kontrol

Uygulama her açıldığında otomatik olarak kontrol yapar (sadece production ortamında).

### Manuel Kontrol (İsteğe Bağlı)

Eğer manuel kontrol butonu eklemek isterseniz, Settings ekranına bir buton ekleyebilirsiniz:

```kotlin
Button(onClick = { updateViewModel.checkForUpdate() }) {
    Text("Güncellemeleri Kontrol Et")
}
```

## 📱 Kullanıcı Deneyimi

### Güncelleme Bildirimi

Yeni sürüm bulunduğunda kullanıcıya şu bilgiler gösterilir:

- Yeni sürüm numarası
- Release notları (ilk 500 karakter)
- "Daha Sonra" ve "İndir ve Kur" butonları

### APK İndirme ve Kurulum

1. Kullanıcı "İndir ve Kur" butonuna tıklar
2. APK dosyası Downloads klasörüne indirilir
3. İndirme tamamlandığında kurulum ekranı otomatik açılır
4. Kullanıcı kurulumu onaylar
5. Eski sürüm otomatik olarak güncellenir (veriler korunur)

## ⚠️ Önemli Notlar

### 1. İnternet Bağlantısı

Güncelleme kontrolü için internet bağlantısı gereklidir. İnternet yoksa sessizce başarısız olur.

### 2. İzinler

Uygulama aşağıdaki izinlere ihtiyaç duyar:

- `INTERNET`: GitHub API'ye erişim için
- `WRITE_EXTERNAL_STORAGE` (Android 12 ve altı): APK indirme için
- `REQUEST_INSTALL_PACKAGES`: APK kurulumu için

### 3. Güvenlik

- APK sadece GitHub Releases'tan indirilir
- Kullanıcı her zaman kurulumu onaylamalıdır
- Android'in güvenlik ayarlarına göre "Bilinmeyen kaynaklardan yükleme" izni gerekebilir

### 4. Version Code Hesaplama

Version code, version name'den otomatik hesaplanır:

- `1.0.0` → `10000` (1 * 10000 + 0 * 100 + 0)
- `1.1.0` → `10100` (1 * 10000 + 1 * 100 + 0)
- `2.0.0` → `20000` (2 * 10000 + 0 * 100 + 0)

Bu nedenle version name formatı tutarlı olmalıdır.

## 🐛 Sorun Giderme

### Güncelleme Algılanmıyor

1. **GitHub Repository Bilgisi Kontrolü:**
   - `app/build.gradle.kts` dosyasında `GITHUB_REPO` doğru mu?
   - Format: `"KULLANICI_ADI/REPO_ADI"` (tırnak işaretleri dahil)

2. **Version Code Kontrolü:**
   - Yeni release'in version code'u mevcut sürümden yüksek mi?
   - `app/build.gradle.kts` dosyasında version code artırıldı mı?

3. **APK Dosya Adı:**
   - GitHub Release'teki APK dosya adında "prod" ve "release" kelimeleri var mı?
   - Örnek: `app-prod-release.apk` ✅

4. **Release Tag Formatı:**
   - Tag formatı: `prod-v1.0.0-20240101-120000` gibi olmalı
   - Version numarası tag'de görünür olmalı

### APK İndirme Çalışmıyor

1. **İnternet Bağlantısı:** İnternet bağlantısını kontrol edin
2. **İzinler:** Uygulama izinlerini kontrol edin
3. **Depolama:** Telefonda yeterli depolama alanı var mı?

### Kurulum Başarısız

1. **Bilinmeyen Kaynaklar:** Android ayarlarından "Bilinmeyen kaynaklardan yükleme" iznini verin
2. **Eski Sürüm:** Eski sürümü manuel olarak kaldırıp tekrar deneyin
3. **APK Bütünlüğü:** İndirilen APK dosyası bozuk olabilir, tekrar indirmeyi deneyin

## 📝 Örnek Senaryo

### Senaryo: v1.0 → v1.1 Güncelleme

1. **Geliştirici:**
   - `app/build.gradle.kts` dosyasında:
     ```kotlin
     versionCode = 2  // 1'den 2'ye artırıldı
     versionName = "1.1"  // "1.0"dan "1.1"e güncellendi
     ```
   - Değişiklikleri commit edip push yapar
   - GitHub Actions otomatik olarak release oluşturur

2. **Kullanıcı:**
   - Uygulamayı açar (v1.0 yüklü)
   - Uygulama otomatik olarak GitHub'a istek atar
   - Yeni sürüm (v1.1) bulunur
   - Güncelleme dialog'u gösterilir
   - "İndir ve Kur" butonuna tıklar
   - APK indirilir ve kurulur
   - Uygulama v1.1 ile açılır (veriler korunur)

## 🔗 İlgili Dosyalar

- `app/src/main/java/com/mirrorcast/data/update/UpdateRepository.kt` - Güncelleme kontrolü mantığı
- `app/src/main/java/com/mirrorcast/ui/update/UpdateCheckerViewModel.kt` - ViewModel
- `app/src/main/java/com/mirrorcast/ui/update/UpdateDialog.kt` - Güncelleme dialog UI
- `app/src/main/java/com/mirrorcast/MainActivity.kt` - Güncelleme kontrolü entegrasyonu

## 💡 İyileştirme Önerileri

1. **Periyodik Kontrol:** Uygulama açıkken periyodik olarak kontrol yapılabilir
2. **Arka Plan Kontrolü:** Arka planda periyodik kontrol yapılabilir
3. **Zorunlu Güncelleme:** Kritik güncellemeler için zorunlu güncelleme özelliği eklenebilir
4. **İndirme İlerlemesi:** İndirme ilerlemesi gösterilebilir
5. **Otomatik Kurulum:** Kullanıcı onayı ile otomatik kurulum yapılabilir

