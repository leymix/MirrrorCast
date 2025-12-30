# 🔐 local.properties Keystore Kurulumu

## 📝 Adım Adım Talimatlar

### 1. local.properties Dosyasını Açın

Proje kök dizinindeki `local.properties` dosyasını bir metin editörü ile açın.

### 2. Keystore Bilgilerinizi Ekleyin

Dosyanın sonuna aşağıdaki satırları ekleyin ve **placeholder değerleri** kendi bilgilerinizle değiştirin:

```properties
KEYSTORE_FILE=keystore.jks
KEYSTORE_PASSWORD=GERÇEK_KEYSTORE_ŞİFRENİZ
KEY_ALIAS=mirrorcast
KEY_PASSWORD=GERÇEK_KEY_ŞİFRENİZ
```

### 3. Şifreleri Girin

**⚠️ ÖNEMLİ:** 
- `GERÇEK_KEYSTORE_ŞİFRENİZ` yerine keystore oluştururken girdiğiniz **keystore şifresini** yazın
- `GERÇEK_KEY_ŞİFRENİZ` yerine keystore oluştururken girdiğiniz **key şifresini** yazın
- Eğer key şifresi keystore şifresiyle aynıysa, aynı şifreyi yazın

### 4. Örnek

Eğer keystore şifreniz `MySecurePass123!` ve key şifreniz de aynıysa:

```properties
KEYSTORE_FILE=keystore.jks
KEYSTORE_PASSWORD=MySecurePass123!
KEY_ALIAS=mirrorcast
KEY_PASSWORD=MySecurePass123!
```

### 5. Dosyayı Kaydedin

Dosyayı kaydedin ve kapatın.

## ✅ Test Etme

Keystore bilgilerini ekledikten sonra release build yapabilirsiniz:

```powershell
./gradlew assembleDevRelease
```

Eğer build başarılı olursa, APK dosyası şu konumda olacak:
```
app/build/outputs/apk/dev/release/app-dev-release.apk
```

## ⚠️ Güvenlik Notları

1. **`local.properties` dosyasını ASLA Git'e commit etmeyin!**
   - Bu dosya zaten `.gitignore` dosyasında olmalı
   - Kontrol etmek için: `.gitignore` dosyasında `local.properties` var mı bakın

2. **Şifrelerinizi güvenli tutun**
   - Bu dosya sadece yerel build'ler için
   - GitHub Secrets'da ayrı bir keystore kopyası olmalı (CI/CD için)

3. **Dosya konumu**
   - `local.properties` dosyası proje kök dizininde olmalı
   - `app/build.gradle.kts` ile aynı seviyede

## 🔍 Sorun Giderme

### "Keystore file not found"
- `KEYSTORE_FILE=keystore.jks` yolunun doğru olduğundan emin olun
- `keystore.jks` dosyasının proje kök dizininde olduğunu kontrol edin

### "Signing config not found"
- Tüm değerlerin doğru girildiğinden emin olun
- Şifrelerde özel karakterler varsa, tırnak işareti kullanmayın (Gradle otomatik olarak işler)

### "Wrong password"
- Şifrelerin doğru olduğundan emin olun
- Büyük/küçük harf duyarlıdır
- Boşluk karakterlerine dikkat edin

## 📚 İlgili Dosyalar

- `app/build.gradle.kts` - Signing config burada tanımlı
- `keystore.jks` - Keystore dosyası (proje kök dizininde olmalı)
- `.gitignore` - `local.properties` burada olmalı

