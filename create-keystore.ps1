# Keystore Oluşturma Scripti
# Kullanım: .\create-keystore.ps1

$keytoolPath = "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Keystore Oluşturma" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Keystore oluşturuluyor..." -ForegroundColor Yellow
Write-Host "Not: Şifre en az 6 karakter olmalıdır!" -ForegroundColor Yellow
Write-Host ""

& $keytoolPath -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mirrorcast

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Keystore başarıyla oluşturuldu: keystore.jks" -ForegroundColor Green
    Write-Host ""
    Write-Host "Sonraki adım: Keystore'u base64'e çevirin:" -ForegroundColor Cyan
    Write-Host '[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore.jks")) | Out-File -Encoding ASCII keystore_base64.txt' -ForegroundColor Gray
} else {
    Write-Host ""
    Write-Host "❌ Keystore oluşturulamadı. Lütfen tekrar deneyin." -ForegroundColor Red
}

