# SIKUI Emoji Keyboard

Android InputMethodService tabanlı SIKUI özel emoji klavyesi.

## Özellikler
- Gerçek Android IME olarak sistem klavye listesine eklenir.
- 6 özel karakter: U+F0000, U+F0001, U+F0002, U+F0003, U+F0004, U+F0005.
- 6 özel PNG tuşu için kaynak eşlemesi.
- SIKUI fontu klavye içindeki emoji/metin düğmelerinde kullanılacak şekilde hazırlanmıştır.
- Klavye seçici ve Android giriş yöntemi ayarlarına kısayollar.
- GitHub Actions ile debug APK derleme.

## Özel emoji eşlemesi
1000422009.png → U+F0000
1000422013.png → U+F0001
1000422021.png → U+F0002
1000422022.png → U+F0003
1000422023.png → U+F0004
1000422024.png → U+F0005

## Font ve vanilla texture paketi
Son verilen doğru SIKUI_Emoji(1).ttf proje içinde SIKUI_Emoji.ttf adıyla kullanılmalıdır. SIKUI_Emoji_Textures.zip vanilla emoji texture/map kaynağıdır.

## Android sınırı
Bir IME, başka uygulamaların sistem emoji fontunu zorla SIKUI fontuyla değiştiremez. Klavye kendi görünümünde SIKUI fontunu kullanır ve U+F0000–U+F0005 karakterlerini aktif uygulamaya Unicode PUA olarak gönderir.

## Build
Gradle 8.2 + JDK 17 ile: gradle :app:assembleDebug
