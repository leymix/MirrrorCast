# 📱 Uygulama Güncelleme Senaryoları

## 🎯 Production Release Sonrası Durum

### Senaryo: Eski Sürümü Olan Telefon

Bir telefonda eski sürüm (örn: v1.0) var, siz yeni bir production release (örn: v1.1) yaptınız.

## ✅ Ne Olur?

### 1. Google Play Store Üzerinden

**Otomatik Güncelleme:**
- Google Play Store otomatik olarak yeni sürümü algılar
- Kullanıcıya güncelleme bildirimi gönderilir
- Kullanıcı "Güncelle" butonuna tıklarsa → Yeni sürüm yüklenir
- **Eski sürüm otomatik olarak kaldırılır**
- **Uygulama verileri korunur** (ayarlar, kullanıcı verileri, vb.)

**Manuel Güncelleme:**
- Kullanıcı Play Store'u açıp "Güncelle" butonuna tıklayabilir
- Aynı şekilde eski sürüm kaldırılır, yeni sürüm yüklenir

### 2. APK İndirerek (Doğrudan Yükleme)

**Aynı Application ID ile:**
- Yeni APK'yı indirip yüklerse → **Eski sürüm otomatik güncellenir**
- Veriler korunur
- Sorunsuz güncelleme yapılır

**Farklı Application ID ile:**
- Eğer farklı bir uygulama olarak görülürse → İki uygulama yan yana olur
- Bu durumda eski sürümü manuel kaldırması gerekir

## 🔢 Version Code ve Version Name

### Version Code (versionCode)
```kotlin
versionCode = 1  // İlk sürüm
versionCode = 2  // İkinci sürüm
versionCode = 3  // Üçüncü sürüm
```

**Önemli:**
- Her yeni release'te **mutlaka artırılmalı**
- Google Play Store bunu kontrol eder
- Aynı veya daha düşük version code ile güncelleme yapılamaz

### Version Name (versionName)
```kotlin
versionName = "1.0"   // İlk sürüm
versionName = "1.1"   // İkinci sürüm (minor update)
versionName = "2.0"   // Major update
```

**Önemli:**
- Kullanıcıya gösterilen sürüm numarası
- Semantic versioning önerilir (1.0.0, 1.1.0, 2.0.0)

## 📋 Mevcut Durumunuz

`app/build.gradle.kts` dosyanızda:
```kotlin
versionCode = 1
versionName = "1.0"
```

### Yeni Release Yaparken

**Örnek: v1.1 Release:**
```kotlin
versionCode = 2  // Artırın!
versionName = "1.1"  // Güncelleyin
```

**Örnek: v2.0 Release:**
```kotlin
versionCode = 3  // Artırın!
versionName = "2.0"  // Güncelleyin
```

## 🔄 Güncelleme Süreci

### Adım 1: Version Güncelleme
```kotlin
// app/build.gradle.kts
defaultConfig {
    versionCode = 2  // Önceki: 1
    versionName = "1.1"  // Önceki: "1.0"
}
```

### Adım 2: Release Build
```bash
./gradlew assembleProdRelease
```

### Adım 3: Google Play Store'a Yükleme
- Google Play Console'a giriş yapın
- Yeni APK/AAB yükleyin
- Version code otomatik kontrol edilir
- Eğer önceki sürümden yüksekse → Yükleme kabul edilir

### Adım 4: Kullanıcılar İçin
- Play Store yeni sürümü algılar
- Otomatik güncelleme bildirimi gönderilir
- Kullanıcı güncellemeyi onaylarsa → Eski sürüm güncellenir

## ⚠️ Önemli Notlar

### 1. Version Code Zorunluluğu
- **Version code mutlaka artırılmalı**
- Aynı version code ile güncelleme yapılamaz
- Daha düşük version code ile güncelleme yapılamaz

### 2. Veri Korunması
- Güncelleme sırasında **uygulama verileri korunur**
- SharedPreferences, DataStore, veritabanı verileri korunur
- Sadece APK dosyası değişir

### 3. Geriye Dönük Uyumluluk
- Eski veri formatları ile uyumlu olmalı
- Veritabanı şeması değişirse migration gerekebilir

### 4. Force Update (Zorunlu Güncelleme)
Eğer eski sürümlerle uyumluluk sorunu varsa:
- Uygulama içinde version kontrolü yapın
- Eski sürüm kullanıcılarına güncelleme zorunluluğu gösterin

## 🎯 Senaryolar

### Senaryo 1: Normal Güncelleme ✅
```
Eski: versionCode = 1, versionName = "1.0"
Yeni: versionCode = 2, versionName = "1.1"
Sonuç: Sorunsuz güncelleme, veriler korunur
```

### Senaryo 2: Version Code Artırılmamış ❌
```
Eski: versionCode = 1
Yeni: versionCode = 1  // HATA!
Sonuç: Google Play Store yüklemeyi reddeder
```

### Senaryo 3: Version Code Düşürülmüş ❌
```
Eski: versionCode = 2
Yeni: versionCode = 1  // HATA!
Sonuç: Google Play Store yüklemeyi reddeder
```

## 📝 Best Practices

### 1. Her Release'te Version Code Artırın
```kotlin
// Her release'te +1 artırın
versionCode = previousVersionCode + 1
```

### 2. Semantic Versioning Kullanın
```kotlin
// Major.Minor.Patch
versionName = "1.0.0"  // İlk sürüm
versionName = "1.1.0"  // Minor update (yeni özellik)
versionName = "1.1.1"  // Patch (bug fix)
versionName = "2.0.0"  // Major update (breaking changes)
```

### 3. Changelog Tutun
- Her release için değişiklikleri not edin
- Kullanıcılara ne değiştiğini bildirin

## 🔍 Kontrol Listesi

Yeni release yapmadan önce:

- [ ] `versionCode` artırıldı mı?
- [ ] `versionName` güncellendi mi?
- [ ] Release notes hazırlandı mı?
- [ ] Test edildi mi?
- [ ] Google Play Console'a yüklendi mi?

## 💡 Özet

**Eski sürümü olan telefon:**
- ✅ Yeni sürüm yüklendiğinde **otomatik güncellenir**
- ✅ **Veriler korunur**
- ✅ **Eski sürüm kaldırılır**
- ✅ Sorunsuz çalışır (version code doğruysa)

**Önemli:** Her release'te `versionCode` mutlaka artırılmalı!

