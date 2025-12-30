# 🔐 Keystore Şifre Kontrolü

## ⚠️ Önemli Bilgi

**Keystore şifresi geri alınamaz!** Keystore dosyası şifrelenmiş olarak saklanır ve şifreyi dosyadan okumak mümkün değildir.

## 📝 Şifrenizi Nerede Bulabilirsiniz?

### 1. local.properties Dosyası

Eğer şifrenizi `local.properties` dosyasına kaydettiyseniz, orada görebilirsiniz:

```properties
KEYSTORE_PASSWORD=şifreniz_burada
KEY_PASSWORD=şifreniz_burada
```

**Not:** Bu dosya sadece yerel bilgisayarınızda. Eğer silinirse veya farklı bir bilgisayarda çalışıyorsanız, şifreyi hatırlamanız gerekir.

### 2. Güvenli Notlarınız

Keystore oluştururken şifrenizi güvenli bir yere kaydetmiş olmalısınız:
- Şifre yöneticisi (LastPass, 1Password, vb.)
- Güvenli bir not dosyası
- Fiziksel bir not defteri

## 🧪 Şifrenizi Test Etme

Şifrenizin doğru olup olmadığını test etmek için:

### Yöntem 1: Keystore'u Listeleme

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore keystore.jks
```

Bu komut şifre soracak. Şifreyi doğru girerseniz, keystore içeriğini göreceksiniz.

### Yöntem 2: Build Yapma

```powershell
./gradlew assembleDevRelease
```

Eğer şifre doğruysa, build başarılı olacaktır.

## 🔄 Şifreyi Unuttuysanız

Eğer keystore şifresini unuttuysanız:

### ⚠️ ÖNEMLİ UYARI

**Eski keystore'u kaybederseniz:**
- Mevcut APK'ları güncelleyemezsiniz
- Google Play Store'da yeni sürüm yayınlayamazsınız
- Kullanıcılar uygulamanızı güncelleyemez

### Çözüm: Yeni Keystore Oluşturma

1. Yeni bir keystore oluşturun:
```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkey -v -keystore keystore_new.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mirrorcast
```

2. **Şifrenizi bu sefer mutlaka kaydedin!**

3. `local.properties` dosyasını güncelleyin

4. GitHub Secrets'ı güncelleyin (CI/CD için)

## 💡 Şifre İpuçları

- **Güçlü şifre kullanın:** En az 6 karakter, harf, rakam ve özel karakter
- **Şifreyi kaydedin:** Güvenli bir yerde saklayın
- **Aynı şifreyi kullanın:** Key password genelde keystore password ile aynıdır
- **Yedek alın:** Keystore dosyasını güvenli bir yerde yedekleyin

## 🔍 Mevcut Durum Kontrolü

`local.properties` dosyanızda şu şifreler kayıtlı:

```
KEYSTORE_PASSWORD=Tn.200260
KEY_PASSWORD=Tn.20026
```

**Not:** Bu şifrelerin doğru olup olmadığını sadece test ederek anlayabilirsiniz.

## ✅ Şifre Doğrulama

Şifrenizi doğrulamak için yukarıdaki test yöntemlerinden birini kullanın. Eğer şifre yanlışsa, keystore oluştururken girdiğiniz gerçek şifreyi hatırlamanız veya yeni bir keystore oluşturmanız gerekir.

