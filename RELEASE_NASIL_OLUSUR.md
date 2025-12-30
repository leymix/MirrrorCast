# 🚀 Release Nasıl Oluşur?

## 📋 Mevcut Durum

Şu anda **release sadece manuel trigger ile** oluşturuluyor.

## 🎯 Release Oluşturma Yöntemleri

### Yöntem 1: Manuel Trigger (Şu An Aktif)

**Ne zaman:** İstediğiniz zaman manuel olarak

**Nasıl:**
1. GitHub repository'nize gidin
2. **Actions** sekmesine tıklayın
3. Sol menüden **"Android CI/CD"** workflow'unu seçin
4. Sağ üstteki **"Run workflow"** butonuna tıklayın
5. **Environment** seçin:
   - `dev` → Development ortamı için release
   - `stg` → Staging ortamı için release
   - `prod` → Production ortamı için release
6. **Run workflow** butonuna tıklayın

**Sonuç:**
- ✅ Release APK build edilir
- ✅ GitHub Release oluşturulur
- ✅ APK release'e eklenir
- ✅ Release notes otomatik oluşturulur

---

## 🔄 Otomatik Release Senaryoları (İsterseniz Eklenebilir)

### Senaryo A: Branch Merge'de Otomatik Release

**Örnek:**
- `stg` branch'ine merge → Staging release oluştur
- `main` branch'ine merge → Production release oluştur

**Nasıl eklenir:**
Workflow'a push trigger eklenir ve merge olduğunda release oluşturulur.

### Senaryo B: Tag Push'unda Release

**Örnek:**
```bash
git tag v1.0.0
git push origin v1.0.0
```

Tag push edildiğinde otomatik release oluşturulur.

### Senaryo C: Commit Mesajı ile Release

**Örnek:**
```bash
git commit -m "[release] Yeni özellikler"
git push origin main
```

Commit mesajında `[release]` varsa otomatik release oluşturulur.

---

## 📝 Şu Anki Workflow Yapısı

```
PR Açıldığında:
  └─> Build Job çalışır
      └─> Debug APK build edilir
      └─> Artifact olarak kaydedilir
      └─> Release Job çalışmaz ❌

Manuel Trigger:
  └─> Build Job çalışır
      └─> Debug APK build edilir
  └─> Release Job çalışır ✅
      └─> Release APK build edilir
      └─> GitHub Release oluşturulur
      └─> APK release'e eklenir
```

---

## 🎯 Hangi Branch'e Ne Yaparsanız Release Oluşur?

### Şu Anki Durum:

| Branch | Push | PR Açma | PR Merge | Manuel Trigger |
|--------|------|---------|----------|----------------|
| `dev` | ❌ | ❌ | ❌ | ✅ (dev release) |
| `stg` | ❌ | ✅ (debug build) | ❌ | ✅ (stg release) |
| `main` | ❌ | ✅ (debug build) | ❌ | ✅ (prod release) |

**Özet:** Şu anda **sadece manuel trigger ile release oluşuyor**.

---

## 💡 Öneriler

### Seçenek 1: PR Merge'de Otomatik Release

PR merge edildiğinde otomatik release oluşturulsun mu?

**Avantajlar:**
- ✅ Otomatik release
- ✅ Her merge'de yeni versiyon

**Dezavantajlar:**
- ⚠️ Her merge'de release oluşur (çok fazla release olabilir)

### Seçenek 2: Tag Push'unda Release

Tag push edildiğinde release oluşturulsun mu?

**Avantajlar:**
- ✅ Kontrollü release
- ✅ Sadece istediğinizde release

**Dezavantajlar:**
- ⚠️ Manuel tag oluşturmanız gerekir

### Seçenek 3: Commit Mesajı ile Release

Commit mesajında `[release]` varsa release oluşturulsun mu?

**Avantajlar:**
- ✅ Esnek kontrol
- ✅ İstediğinizde release

**Dezavantajlar:**
- ⚠️ Commit mesajını unutabilirsiniz

---

## 🚀 Hızlı Başlangıç: Release Oluşturma

### Adım 1: GitHub'a Git
Repository → **Actions** sekmesi

### Adım 2: Workflow Seç
Sol menüden **"Android CI/CD"** seçin

### Adım 3: Run Workflow
Sağ üstte **"Run workflow"** → **Environment** seçin → **Run workflow**

### Adım 4: Bekle
Workflow tamamlanınca **Releases** sekmesinden APK'yı indirebilirsiniz.

---

## 📦 Release İçeriği

Her release şunları içerir:
- ✅ Signed Release APK
- ✅ Release notes (son 10 commit)
- ✅ Version bilgisi
- ✅ Environment bilgisi
- ✅ Tag (örn: `prod-v1.0`, `stg-v1.0`)

---

## ❓ Hangi Yöntemi İstersiniz?

1. **Mevcut durum** (sadece manuel) - Devam edelim mi?
2. **PR merge'de otomatik** - Ekleyelim mi?
3. **Tag push'unda otomatik** - Ekleyelim mi?
4. **Commit mesajı ile** - Ekleyelim mi?

Hangisini istediğinizi söyleyin, workflow'u ona göre güncelleyeyim!

