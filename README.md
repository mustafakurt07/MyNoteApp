# MyNoteApp

Modern Android not uygulaması. MVVM + UDF + Clean Architecture ile yazılmış; Jetpack Compose (Material 3), Hilt, Room, Navigation ve KSP kullanır. Performans, kararlılık ve okunabilirlik önceliklidir.

## Amaç
- Basit ve hızlı bir not deneyimi
- Modern Compose UI ve Material 3 tasarımı
- Ölçeklenebilir, test edilebilir ve bakımı kolay mimari

## Özellikler
- Not oluşturma/düzenleme/silme (lokal Room DB)
- Liste ekranı (search + tag filtreleme)
- Detay ekranı (başlık, içerik, etiketler)
- Sola kaydırarak silme (M3 SwipeToDismissBox)
- FAB ile hızlı yeni not
- Dark/Light tema (modern M3 colorScheme)

## Mimarî
- Presention (Compose + ViewModel)
- Domain (UseCase’ler)
- Data (Repository + Room Dao/DB)
- UDF (Unidirectional Data Flow): Intent → ViewModel → UiState → UI

```
ui/            # Compose ekranları, UDF uçları
ui/note/list   # Liste ekranı + VM + UDF intent/state
ui/note/detail # Detay ekranı + VM + UDF intent/state + one-shot event

domain/        # UseCase'ler (ObserveNotes, UpsertNote, DeleteNote, GetNoteById)

data/local     # Room (Entity, Dao, Database, Converters)

di/            # Hilt modülleri (DB, Repo binding)
```

### Katman rollerine kısa notlar
- ViewModel: Sadece UI’ı besleyecek durumu üretir; filtreleme/arama gibi katma değerli iş kuralları burada.
- UseCase: Domain iş akışlarını temsil eder (tek sorumluluk, kolay test edilir).
- Repository: Data kaynaklarını soyutlar (Room, vs.).

## Kullanılan Teknolojiler
- Kotlin, Kotlin DSL, Version Catalogs (`libs.versions.toml`)
- Jetpack Compose (Material 3, Foundation, Navigation)
- Hilt (DI)
- Room (KSP)
- StateFlow / SharedFlow (one‑shot event)

## Performans ve Best Practices
- Compose
  - `@Immutable` veri modelleri (ör. `Note`), stabil referanslar
  - `LazyColumn` için `key` ve `contentType = { "note" }`
  - `remember/rememberSaveable` (liste scroll state’i vb.)
  - Stabil tag sıralaması: `remember(state.allTags) { state.allTags.toList().sorted() }` (recomposition/reuse iyileşir)
  - M3 `SwipeToDismissBox` (yalnızca gerçek swipe hedefinde arka plan/ikon gösterilir)
- UDF
  - Intent: Kullanıcı aksiyonları (girdi)
  - UiState: Immutable durum
  - Event: `SharedFlow` ile one‑shot (örn. save sonrası close), rotasyonda tekrar etmez
- Filtreleme
  - ViewModel’de reactive kombinasyon
  - Arama: `map { trim() } -> debounce(300) -> distinctUntilChanged()`
  - Listeye yeni öğe gelince akıllı auto-scroll (yalnızca gerekiyorsa)
- Renk/Tema
  - Material 3 colorScheme, modern dark palette (düşük yoruculuk + yüksek okunabilirlik)
  - Kart vurgu renkleri için sabit top-level paletler (performans/tutarlılık)
- Tarih formatı
  - `DateFormatUtils` (thread-safe, API 26+ `java.time`, alt sürümlerde `ThreadLocal<SimpleDateFormat>`, UI’da `remember` ile)
- Room Converters
  - `List<String>` ↔ `String` (pipe `'|'`) dönüştürücü
  - Immutable liste üretimi

## Kurulum ve Çalıştırma
- Gereksinimler: Android Studio Jellyfish/Koala+, JDK 11+, Android SDK 24+
- Derleme
```bash
./gradlew :app:assembleDebug
```
- Çalıştırma (adb yüklüyse)
```bash
./gradlew :app:installDebug && adb shell am start -n com.kurt.mynoteapp/.MainActivity
```

## Dikkat Edilen Noktalar
- Material 3 tutarlılığı (Scaffold/TopAppBar/SwipeToDismissBox)
- Tek yönlü veri akışı ve tek sorumluluk (SOLID)
- Filtreleme/arama işlemleri ViewModel’de (UI hafif)
- Recomposition azaltma: stable contentType, key, stable sıralama, `remember`
- Dark mode okunabilirliği (onSurface/onBackground kontrast)
- Erişilebilirlik: İkonlarda anlamlı `contentDescription` (gerektiği yerde null)

## Ekran Görüntüleri
<img width="1080" height="2280" alt="Screenshot_1755168698" src="https://github.com/user-attachments/assets/df44ae43-a762-4899-99bf-a3d07cf4a4b6" />
<img width="1080" height="2280" alt="Screenshot_1755168775" src="https://github.com/user-attachments/assets/512b9326-6b8c-4714-9e1c-4ae773ab3c92" />
<img width="1080" height="2280" alt="Screenshot_1755168730" src="https://github.com/user-attachments/assets/5595c1bf-95d1-4cfc-a179-8805c0d6709a" />
<img width="1080" height="2280" alt="Screenshot_1755168723" src="https://github.com/user-attachments/assets/295b2036-4d24-428b-885f-93b1da3f85ed" />
<img width="1080" height="2280" alt="Screenshot_1755168717" src="https://github.com/user-attachments/assets/748d0ebe-45b2-49e6-b56a-3e82869a415d" />

