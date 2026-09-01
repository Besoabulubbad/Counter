# Counter

Counter is a Compose Multiplatform demonstration: a dense operational **counter** — a
virtualized reservation grid plus a point-of-sale order flow — built from a single
`commonMain` codebase that runs on **Android**, **desktop (JVM / Windows)**, and compiles
for **iOS**. The domain is generic (a reservations business: slots, resources, a ticket);
the sample data is shaped like a pro-shop tee sheet.

It exists to answer one question honestly: *can a demanding operational UI live in one
shared Compose tree, without forking a screen per platform?* The hard parts are built the
hard way on purpose — real 2D virtualization, offline-first writes with version-conflict
resolution, keyboard-first desktop operation, and a layout that adapts from a phone bottom
sheet to a desktop rail.

## What's in it

| Feature | Notes |
|---|---|
| **Reservation grid** | A custom 2D-virtualized layout over one shared scroll offset — *not* a `LazyColumn` of `LazyRow`s. Sticky time column and header; cells carry status signals for held, confirmed, checked-in, no-show, partial payment, online booking, and the keyboard cursor. |
| **List view** | The same data as an adaptive table on wide screens, or a grouped-by-slot list on a phone. |
| **Order screen** | A category column and an adaptive item grid; tiles show SKU, price, and stock (amber under five on hand, red at zero). |
| **Session ticket** | One Koin-singleton ticket that both the grid (green fees) and the order screen (merchandise) write to. `Pay` reads its running total. |
| **Offline + conflict** | Every mutation writes locally and appends to an outbox; a fake backend round-trips through the sync path; version conflicts raise a resolution banner (keep-mine-retry / discard). |
| **Keyboard nav (desktop)** | Arrow keys move a cell cursor, Enter runs the primary action, Escape clears; the grid auto-scrolls to keep the cursor visible. |
| **Adaptive layout** | One layout tree keyed on window size and input mode: phone (bottom sheet), tablet (side rail), desktop (rail + keyboard). No `PhoneScreen` / `DesktopScreen` split. |

## Architecture

- **`commonMain`-first.** All UI and logic live in `shared/src/commonMain`. No `java.*` in
  shared code — `kotlinx.datetime`, `kotlin.time`, and `kotlinx.coroutines` instead.
- **Exactly four `expect`/`actual` boundaries:** `inputMode`, `formatCurrency`,
  `DatabaseDriverFactory`, `ReceiptPrinter`. (`platformModule()` is the Koin injection seam
  that provides these per target — DI wiring, not a fifth functional boundary.)
- **Form factor is a runtime value** (`Pane`, derived from window width and `inputMode`),
  never a source set or build flavor. `inputMode` is read in exactly one place — the theme
  provider that populates `LocalCounterDimens`; every composable reads the tokens, never the
  input mode.
- **One palette (`CounterColors`), one type scale (`CounterType`).** No color or size
  literals outside `ui/theme`.
- **The local SQLDelight database is the source of truth.** The UI observes `Flow`s; derived
  values (the counters strip, the ticket total) are computed reactively, never stored.
- **No architecture framework.** One plain `StateFlow` state-holder per screen. DI is Koin,
  and only Koin.

## Dependency injection: migrating from Hilt/Dagger to Koin

A native Android app usually wires its object graph with **Hilt** (Dagger underneath). That
does not survive the move to a shared Compose Multiplatform codebase: **Dagger and Hilt are
JVM/Android-only** — they depend on annotation processing and generated code, and cannot
compile to Kotlin/Native (iOS) or run from `commonMain`. **Koin** is a pure-Kotlin,
multiplatform DI container that resolves at runtime, so the *same* graph declaration works on
every target.

The mapping is mechanical:

| Hilt / Dagger | Koin |
|---|---|
| `@HiltAndroidApp` on `Application` | `initKoin { }` from `Application.onCreate()` |
| `@Module @InstallIn(...)` | a top-level `val fooModule = module { }` |
| `@Provides fun` | `single { }` (one instance) / `factory { }` (new each call) |
| `@Binds` (interface → impl) | `single<Interface> { Impl(get()) }` |
| constructor `@Inject` | `single { Repo(get()) }` — `get()` resolves dependencies |
| `@Singleton` | `single` |
| `@ActivityScoped` / custom scopes | `scope { scoped { } }` |
| `@HiltViewModel` + `hiltViewModel()` | `koinViewModel()` — or, here, a plain `StateFlow` holder as a `single` |
| `@Named` / qualifiers | `named("...")` |
| test-time module replacement | `loadKoinModules(...)` / `startKoin` with a test module — runtime, no codegen |

In this repo the whole graph is a few declarations:

```kotlin
val dataModule = module {
    single { createCounterDatabase(get()) }
    single { CounterRepository(get()) }
    single { FakeBackend() }
    single { SyncEngine(get(), get()) }
    single { Ticket() }
}
val uiModule = module { single { GridViewModel(get(), get(), get()) } }

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(platformModule(), dataModule, uiModule)
    }
}
```

The one dependency that must differ per target is the SQLDelight driver — it needs an Android
`Context`, a JVM JDBC URL, or an iOS native driver. Hilt would express this as a separate
Android-only module; Koin expresses it as one `expect`/`actual`:

```kotlin
// commonMain
expect fun platformModule(): Module

// androidMain — the Application hands the Context in
fun initKoinAndroid(context: Context) = initKoin { androidContext(context.applicationContext) }

// jvmMain
actual fun platformModule(): Module = module { single { DatabaseDriverFactory() } }
```

**The trade-off, stated plainly:** Koin resolves at runtime, so you give up Dagger's
compile-time graph validation. You gain multiplatform reach, zero code generation, and
faster builds. For a shared UI that must also reach iOS, the trade is not optional — Dagger
and Hilt cannot compile there at all.

## Running

| Target | Command |
|---|---|
| Desktop | `./gradlew :desktopApp:run` (or `:desktopApp:hotRun --auto` for hot reload) |
| Android | `./gradlew :androidApp:assembleDebug`, or run from the IDE onto an emulator/device |
| iOS | open `iosApp/` in Xcode and run — this builds the shared framework |
| Tests | `./gradlew :shared:jvmTest` — the `commonTest` suite also runs on Android and iOS targets |

## The `ReceiptPrinter` boundary — real, not a stub

`ReceiptPrinter` is a genuine implementation on every target, not a `Result.success(Unit)`
no-op: it encodes the receipt to **ESC/POS** bytes and persists them — to a temp file via
`java.io` on JVM and Android, and via `NSData.writeToFile` on iOS. Swapping the file sink for
a real thermal printer is a one-file change per target.

## Honest status

- The **backend is a deliberate in-memory fake.** It round-trips mutations through the outbox
  and sync path exactly as a real one would; it never writes the local database directly.
- **Performance numbers are not published here yet.** The claims that matter — "only visible
  cells recompose," frame times, cold start, installer size — are things to *measure*, not
  assert. A live recomposition counter is wired into the desktop debug panel to demonstrate
  the virtualization claim; measured figures will be added here after a profiling run, not
  before.

## Versions

Kotlin 2.4.10 · Compose Multiplatform 1.11.1 · AGP 9.0.1 · SQLDelight 2.3.2 · Koin 4.2.2 ·
kotlinx-coroutines 1.11.0 · kotlinx-datetime 0.8.0 · kotlinx-serialization 1.11.0. Android
`compileSdk` 36, `minSdk` 24. Exact coordinates live in `gradle/libs.versions.toml`.

## Fonts

Type is **IBM Plex Sans** and **IBM Plex Mono**, bundled as static weights (Regular / Medium /
SemiBold) via Compose resources and loaded once through the theme into a CompositionLocal. IBM
Plex is licensed under the SIL Open Font License 1.1 — see `licenses/IBM-Plex-OFL.txt`.