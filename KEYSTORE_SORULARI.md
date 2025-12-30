# 🔐 Keystore Oluşturma - Sorular ve Cevaplar

## 📝 Şu Anda Neredesiniz?

Keytool size birkaç soru soruyor. İşte nasıl cevap vereceğiniz:

### 1. ✅ Keystore Password (Tamamlandı)
- En az 6 karakterlik şifre girdiniz
- Bu şifreyi kaydettiniz

### 2. 🔄 What is your first and last name?
**Şu anda buradasınız!**

**Cevap seçenekleri:**
- İsim girebilirsiniz: `John Doe` veya `MirrorCast Developer`
- Enter'a basabilirsiniz (varsayılan değer kullanılır)
- Tek nokta (`.`) girebilirsiniz (boş bırakmak için)

**Öneri:** İsim girin veya Enter'a basın (fark etmez)

### 3. Sonraki Sorular (Sırayla)

Aşağıdaki sorular sorulacak, hepsinde **Enter'a basabilirsiniz** (varsayılan değerler kullanılır):

```
What is the name of your organizational unit?
[Unknown]: (Enter'a basın)

What is the name of your organization?
[Unknown]: (Enter'a basın)

What is the name of your City or Locality?
[Unknown]: (Enter'a basın)

What is the name of your State or Province?
[Unknown]: (Enter'a basın)

What is the two-letter country code for this unit?
[Unknown]: (Enter'a basın)
```

### 4. Son Onay

En son şu soru sorulacak:
```
Is CN=..., OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown correct?
[no]: 
```

**Cevap:** `yes` yazın ve Enter'a basın

### 5. Key Password

Son olarak:
```
Enter key password for <mirrorcast>
        (RETURN if same as keystore password):
```

**Cevap:** Enter'a basın (keystore şifresiyle aynı olur) veya farklı bir şifre girebilirsiniz

## ✅ Başarı Mesajı

Başarılı olursa şunu göreceksiniz:
```
[Storing keystore.jks]
```

## 🎯 Hızlı Rehber

**Şu anki soruya cevap:**
- İsim girin veya **Enter'a basın**

**Sonraki sorular:**
- Hepsi için **Enter'a basın** (hızlı geçmek için)

**Son onay:**
- `yes` yazın

**Key password:**
- **Enter'a basın** (keystore şifresiyle aynı olur)

## ⚠️ Önemli Notlar

- Bu bilgiler sadece keystore metadata'sı için
- Production'da kullanmayacaksanız, varsayılan değerler yeterli
- Sadece şifreler önemli (keystore password ve key password)

