# DartScore

DartScore je Android aplikacija za bilježenje i igranje pikada (darts), izgrađena u Kotlinu s Jetpack Compose sučeljem i Firebase pozadinom (Auth + Firestore).

## Sadržaj

- [Značajke](#značajke)
- [Tehnologije](#tehnologije)
- [Struktura projekta](#struktura-projekta)
- [Pokretanje projekta](#pokretanje-projekta)
- [Firebase](#firebase)
- [Arhitektura](#arhitektura)
- [Dodatna dokumentacija](#dodatna-dokumentacija)

## Značajke

- **Lokalna igra (Local Play)** – igra 501/301 s 2–8 igrača na jednom uređaju, s podesivim formatom (prvi do / najbolji od), legovima/setovima, in/out pravilima (straight, double, master) i prikazom prosjeka, zadnjih hitaca i statistike po legu.
- **Online igra (Online Play)** – multiplayer lobiji putem Firestore-a: stvaranje lobija s rasponom prosjeka igrača, pridruživanje putem koda ili liste otvorenih lobija, praćenje pobjeda/poraza po korisniku.
- **Trening (Training)** – četiri moda za samostalnu vježbu:
  - **121 Checkout** – checkout od 121 naviše (double out, max 9 lotki).
  - **Random checkout** – nasumični checkout 40–170 (3 lotke, double out).
  - **Singles training** – redom brojevi 1–20 (single = 1, double = 2, triple = 3 boda).
  - **Score training** – maksimalan rezultat po visitu (3 lotke), prati prosjek.
  - Osobni rekordi spremaju se lokalno (SharedPreferences) putem `TrainingBestScoresStore`.
- **Statistika** – prosjek na 3 lotke, broj odigranih/pobijeđenih utakmica, ukupan broj visita, online omjer pobjeda/poraza, najbolji checkout; povijest utakmica s detaljima svake odigrane partije.
- **Društvene značajke (Social)** – zid objava (Feed Wall) s objavama iz mreže prijatelja/pratitelja, praćenje/prijateljstvo drugih korisnika po nadimku, lige (Leagues, u razvoju).
- **Korisnički računi** – registracija/prijava putem Firebase Auth (e-mail/lozinka), uređivanje profila (ime, e-mail, lozinka) u `AccountScreen`.

## Tehnologije

- **Jezik:** Kotlin 2.2.x
- **UI:** Jetpack Compose (Material 3), edge-to-edge, vlastita tema u `ui/theme`
- **Backend:** Firebase Authentication (email/password) i Cloud Firestore
- **Async:** Kotlin Coroutines (uklj. `kotlinx-coroutines-play-services` za `.await()`)
- **Build:** Gradle Kotlin DSL, Android Gradle Plugin 9.2.0
- **Min SDK:** 24, **Target/Compile SDK:** 36

## Struktura projekta

```
app/src/main/java/com/example/dartscore/
├── MainActivity.kt          # Root Composable, ručna navigacija preko sealed class AppScreen
├── data/                    # Repozitoriji za Firebase/lokalnu perzistenciju
│   ├── UserRepository.kt        # Auth profil, ime, e-mail, lozinka, online statistika
│   ├── MatchRepository.kt       # Kreiranje/ažuriranje/dohvat utakmica i povijesti
│   ├── LobbyRepository.kt       # Online lobiji: kreiranje, pridruživanje, kodovi, rezultati
│   ├── SocialRepository.kt      # Prijatelji, praćenje, pretraga korisnika
│   ├── FeedRepository.kt        # Objave na zidu (feed) i objavljivanje rezultata utakmice
│   └── TrainingBestScoresStore.kt # Lokalni rekordi treninga (SharedPreferences)
├── game/                    # Čista poslovna logika (bez Android/Compose ovisnosti)
│   ├── LocalGameEngine.kt       # Pravila lokalne igre: bodovanje, bust, in/out, legovi/setovi
│   ├── TrainingEngine.kt        # Logika za sve treninge
│   ├── CheckoutTrainingEngine.kt# Specifična logika za checkout treninge
│   ├── CheckoutChart.kt         # Prijedlozi checkout kombinacija
│   ├── CheckoutDartOptions.kt   # Moguće kombinacije zadnjeg dvostrukog/trostrukog polja
│   ├── MatchStatsCalculator.kt  # Izračun statistike utakmice iz zapisanih visita
│   └── MatchShareFormatter.kt   # Formatiranje teksta za dijeljenje/feed objave
├── model/                   # Data klase i enumi (MatchSettings, VisitRecord, TrainingModels...)
└── ui/
    ├── screen/               # Ekrani (HomeScreen, LoginScreen, RegisterScreen, AccountScreen...)
    │   ├── local/                # Postavljanje i igranje lokalne utakmice
    │   ├── online/               # Lobiji i online igra
    │   ├── social/               # Feed, prijatelji, lige, statistika
    │   ├── training/             # Ekrani za trening
    │   └── match/                # Prikaz statistike odigrane utakmice
    ├── components/           # Ponovno iskoristive komponente (dartboard, tipkovnica, top bar...)
    └── theme/                # Compose tema (boje, tipografija)
```

Firebase/Firestore konfiguracija na razini projekta:

```
firebase.json               # Firebase CLI konfiguracija (deploy targeti)
firestore.rules             # Sigurnosna pravila za Firestore kolekcije
firestore.indexes.json      # Composite indeksi za upite
app/google-services.json    # Firebase konfiguracija za Android app (projekt: dartscore-7df93)
```

## Pokretanje projekta

1. Otvorite **root folder** projekta (`DartScore/`) u Android Studiju — ne `app/` podfolder, jer to ruši Gradle sync i sakriva Android run target.
2. Provjerite da je Android SDK dostupan (obično `~/Library/Android/sdk` na macOS-u ili standardna lokacija na Windowsu). `settings.gradle.kts` automatski kreira `local.properties` ako ne postoji.
3. Sinkronizirajte Gradle i pokrenite `app` konfiguraciju na emulatoru ili fizičkom uređaju (minSdk 24).
4. Za CLI build na macOS-u po potrebi postavite `JAVA_HOME` na Android Studio JBR: `/Applications/Android Studio.app/Contents/jbr/Contents/Home`.

### Build iz komandne linije (Windows)

```powershell
./gradlew.bat assembleDebug
```

## Firebase

Detaljne upute za postavljanje Firebase projekta (Auth + Firestore, polja u `users/{uid}`, popis kolekcija) nalaze se u [`FIREBASE_SETUP.md`](FIREBASE_SETUP.md).

Kratki pregled Firestore kolekcija:

| Kolekcija | Svrha | Pristup (firestore.rules) |
|---|---|---|
| `users/{uid}` | Profil korisnika, defaultne postavke igre, online statistika | Čitanje svim prijavljenima, pisanje samo vlasnik |
| `matches/{matchId}` | Povijest i status utakmica (lokalnih i online) | Čitanje/pisanje samo igrači navedeni u `players` |
| `lobbies/{lobbyId}` | Online lobiji za multiplayer | Čitanje svima, pisanje domaćin/gost prema statusu |
| `feedPosts/{postId}` | Objave na zidu (rezultati, poruke) | Čitanje svim prijavljenima, pisanje/brisanje samo autor |
| `friends/{uid}/list/{friendUid}` | Popis prijatelja korisnika | Samo vlasnik |
| `follows/{uid}/list/{targetUid}` | Popis praćenih korisnika | Samo vlasnik |
| `trainingSessions/{sessionId}` | (planirano) sesije treninga | Samo vlasnik |
| `statsDaily/{docId}` | (planirano) dnevna agregirana statistika | Samo vlasnik |

Deploy pravila i indeksa:

```powershell
npx firebase-tools@latest deploy --only firestore
```

## Arhitektura

Navigacija je implementirana ručno u `MainActivity.kt` pomoću `sealed class AppScreen` i `mutableStateOf`, bez Navigation Compose biblioteke — svaki prijelaz ekrana je eksplicitna lambda (`onNavigateTo...`). `BackHandler` mapira fizičku/gesturalnu tipku natrag na odgovarajući prethodni ekran.

Slojevi aplikacije:

- **`ui/screen`** – Compose ekrani, drže lokalni UI state i pozivaju `data`/`game` slojeve.
- **`game`** – čista Kotlin logika igre i treninga (bez Android ovisnosti), lako testabilna.
- **`data`** – repozitoriji koji komuniciraju s Firebase Auth/Firestore ili lokalnom pohranom, vraćaju `Result<T>` ili `Flow<T>` (za real-time lobi podatke).
- **`model`** – nepromjenjivi (immutable) data razredi i enumi dijeljeni kroz slojeve.

Za više detalja pogledajte [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Dodatna dokumentacija

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) – detaljan opis modula, tokova podataka i pravila igre.
- [`FIREBASE_SETUP.md`](FIREBASE_SETUP.md) – postavljanje Firebase projekta.
- [`AGENTS.md`](AGENTS.md) – naučene preferencije i činjenice o radnom prostoru (za AI asistente).

