# 🔐 Keystore Oluşturma - Adım Adım

## ⚠️ ÖNEMLİ: Şifre Gereksinimleri
- **En az 6 karakter** olmalıdır
- Güçlü bir şifre kullanın (harf, rakam, özel karakter)
- Şifrenizi **güvenli bir yerde saklayın** (kaybederseniz APK'ları güncelleyemezsiniz!)

## 📝 Adımlar

### 1. PowerShell'i Açın
Proje klasöründe PowerShell'i açın (sağ tık → "Open PowerShell window here")

### 2. Komutu Çalıştırın

Aşağıdaki komutu kopyalayıp PowerShell'e yapıştırın:

```powershell
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mirrorcast
```

### 3. Bilgileri Girin

Komut çalıştığında sırayla şunlar sorulacak:

1. **Enter keystore password:** 
   - En az 6 karakterlik bir şifre girin (örn: `MySecurePass123!`)
   - ⚠️ Bu şifreyi kaydedin! GitHub Secrets'a ekleyeceksiniz.

2. **Re-enter new password:**
   - Aynı şifreyi tekrar girin

3. **What is your first and last name?**
   - İsim girin (örn: `John Doe`) veya Enter'a basın

4. **What is the name of your organizational unit?**
   - Enter'a basın (opsiyonel)

5. **What is the name of your organization?**
   - Enter'a basın (opsiyonel)

6. **What is the name of your City or Locality?**
   - Enter'a basın (opsiyonel)

7. **What is the name of your State or Province?**
   - Enter'a basın (opsiyonel)

8. **What is the two-letter country code for this unit?**
   - Enter'a basın (opsiyonel)

9. **Is CN=John Doe, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown correct?**
   - `yes` yazın ve Enter'a basın

10. **Enter key password for <mirrorcast>**
    - Keystore şifresiyle aynı olması için Enter'a basın
    - Veya farklı bir şifre girebilirsiniz (bunu da kaydedin!)

### 4. Başarı Kontrolü

Komut başarılı olursa şu mesajı göreceksiniz:
```
[Storing keystore.jks]
```

Ve `keystore.jks` dosyası proje klasöründe oluşacak.

### 5. Dosyayı Kontrol Edin

```powershell
dir keystore.jks
```

Dosya görünüyorsa başarılı! ✅

## 🔄 Alternatif: Script Kullanımı

Eğer yukarıdaki adımlar çalışmazsa, script'i kullanabilirsiniz:

```powershell
.\create-keystore.ps1
```

**Not:** Script de interaktif olarak şifre soracaktır.

## ❓ Sorun Giderme

### "Keystore password is too short"
- Şifreniz en az 6 karakter olmalı
- Daha uzun bir şifre deneyin

### "Too many failures"
- Çok fazla hatalı deneme yaptınız
- PowerShell'i kapatıp yeniden açın
- Komutu tekrar çalıştırın

### "keytool is not recognized"
- Android Studio'nun keytool'unu kullanın (yukarıdaki komut)
- Veya Android Studio'yu açın ve Tools → SDK Manager → SDK Tools'dan JDK'yı kontrol edin

## ✅ Sonraki Adım

Keystore oluşturulduktan sonra:
1. Keystore'u base64'e çevirin (Adım 2)
2. GitHub Secrets'a ekleyin (Adım 3)

