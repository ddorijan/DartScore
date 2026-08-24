# DartScore – Arhitektura

Ovaj dokument opisuje detaljniju arhitekturu aplikacije: slojeve, tok podataka, pravila igre i modele. Za opći pregled i pokretanje projekta pogledajte [README.md](../README.md).

## 1. Pregled slojeva

```
ui/screen  →  game (poslovna logika)  →  model (podaci)
     ↓
data (repozitoriji)  ↔  Firebase (Auth/Firestore) / SharedPreferences
```

- **`ui/screen`** – Compose ekrani. Drže UI state (`remember`, `mutableStateOf`), pozivaju `game` engine za lokalne izračune i `data` repozitorije za mrežne pozive. Nema izravnih Firebase poziva iz UI sloja.
- **`ui/components`** – dijeljene Compose komponente: `DartboardCanvas` (vizualizacija mete), `TrainingKeypad` (unos rezultata), `ScreenTopBar`, `SafeArea`, `MatchSetupComponents`.
- **`game`** – čista Kotlin logika bez Android ovisnosti (osim modela), lako je jedinično testirati.
- **`data`** – repozitoriji koji enkapsuliraju Firebase Auth/Firestore pozive i lokalnu pohranu (SharedPreferences za trening rekorde). Svi javni pozivi vraćaju `Result<T>` (za jednokratne operacije) ili `Flow<T>` (za real-time streamove, npr. lobiji).
- **`model`** – nepromjenjivi data razredi/enumi koje dijele svi slojevi.

## 2. Navigacija

`MainActivity.kt` implementira jednostavan router bez Navigation Compose biblioteke:

- `private sealed class AppScreen` definira sva moguća stanja ekrana (uključujući parametre poput `LocalGame(settings)`, `MatchStats(detail, fromHistory)`, `LobbyRoom(lobbyId)`, `TrainingGame(mode)`).
- `var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }` drži trenutni ekran.
- `when (val screen = currentScreen) { ... }` renderira odgovarajući Composable i prosljeđuje navigacijske lambde (`onNavigateBack`, `onNavigateToX`) koje mijenjaju `currentScreen`.
- `BackHandler` presreće sistemsku tipku/gestu natrag i mapira svaki `AppScreen` na "roditeljski" ekran (npr. `LocalGame → MatchSetup → LocalPlay → Home`).

## 3. Lokalna igra (`game/LocalGameEngine.kt`)

Objekt `LocalGameEngine` je čisti, bez-state singleton koji transformira `LocalGameState` na temelju unesenog rezultata:

- `createInitialState(settings, matchId)` – postavlja početni `remaining` za svakog igrača na `startScore`; `hasOpened` je odmah `true` ako je `InRule.STRAIGHT`.
- `submitVisit(state, score)` – glavna funkcija za obradu jednog "visita" (do 3 strelice, unesen kao zbroj):
  - `score == 0` → prazan visit, samo se izmjenjuje red.
  - Provjera **in pravila** (`STRAIGHT`/`DOUBLE`/`MASTER`) preko `isValidOpeningScore` – ako igrač još nije "otvorio" leg, mora pogoditi dvostruko/25+/50 polje (aproksimirano parnim brojem za double, 25/50 ili parni broj za master).
  - Ako `newRemaining < 0` ili (`OutRule.DOUBLE` i `newRemaining == 1`) → **bust**, rezultat se poništava na stanje prije visita (`lastTurnStartRemaining`).
  - Ako `newRemaining == 0` → provjerava `CheckoutChart.isValidFinish(remainingBefore, outRule)`; ako je validno, vraća `VisitOutcome.CHECKOUT_PENDING` i čeka potvrdu broja strelica (`confirmCheckout`) jer UI treba upitati koliko je lotki iskorišteno za checkout.
  - Inače normalno oduzima rezultat i ažurira `matchTotalScore`/`matchVisitCount` (za 3-dart prosjek).
- `confirmCheckout(state, score, dartsUsed)` – validira izlaz i poziva `handleLegWin`.
- `handleLegWin` – dodaje leg/set pobjedniku, provjerava je li dosegnut cilj meča (`hasReachedTarget` prema `MatchFormat.FIRST_TO`/`BEST_OF`); ako nije gotovo, resetira sve igrače na `startScore` za sljedeći leg i rotira početnog igrača (`legStarterIndex`).
- `undoLastVisit` – vraća stanje na prethodni visit (koristi se za "undo" gumb u UI).

Pravila ukratko:
| Pravilo | STRAIGHT | DOUBLE | MASTER |
|---|---|---|---|
| **In** | Igra od prve strelice | Mora otvoriti parnim (dvostrukim) pogotkom | Mora otvoriti s 25/50 ili parnim brojem |
| **Out** | Bilo koji zadnji pogodak na 0 | Mora završiti dvostrukim poljem (`remaining == 1` je uvijek bust) | Slično double, provjerava `CheckoutChart` |

## 4. Trening (`game/TrainingEngine.kt`, `CheckoutTrainingEngine.kt`)

`TrainingMode` enum definira 4 moda (`CHECKOUT_121`, `RANDOM_CHECKOUT`, `SINGLES`, `SCORE`), svaki sa svojim `TrainingGameState` podtipom (sealed class). `TrainingEngine`/`CheckoutTrainingEngine` obrađuju unos i vraćaju `TrainingVisitResult(state, outcome)` gdje je `outcome` jedan od `SCORED, BUST, CHECKOUT, INVALID, FINISHED`.

Osobni rekordi (`TrainingBestScoresStore`) spremaju se u `SharedPreferences` ("training_best_scores") po modu:
- `CHECKOUT_121` – najviši dosegnuti checkout iznad 121.
- `RANDOM_CHECKOUT` – omjer uspješnih/pokušanih (uspoređuje se stopa uspješnosti).
- `SINGLES` – ukupni bodovi (max teoretski 20×3=60 po krugu × 20 brojeva).
- `SCORE` – prosjek ×10 (za preciznost bez float pohrane), s brojem odigranih rundi.

## 5. Statistika utakmice (`game/MatchStatsCalculator.kt`, `model/MatchStatsModels.kt`)

`MatchStatsCalculator.build(...)` prima sirovi popis `VisitRecord` i settings te generira `MatchStatsDetail`:
- `playerStats: List<PlayerMatchStats>` – 3-dart prosjek, checkout % (pogodaka/pokušaja), najviši checkout, najviši score, najbolji broj strelica za leg.
- `keyMoments: List<MatchKeyMoment>` – istaknuti trenuci utakmice (npr. najviši checkout, najbrži leg).
- Koristi se i za lokalno završene mečeve i za one dohvaćene iz Firestore povijesti (`MatchRepository.getMatchDetail`).

`MatchShareFormatter` pretvara `MatchStatsDetail` u tekst pogodan za feed objavu (`FeedRepository.createMatchPost`).

## 6. Firebase repozitoriji (`data/`)

Svi repozitoriji koriste `FirebaseAuth.getInstance()` i `FirebaseFirestore.getInstance()` kao default parametre (lako zamjenjivo za testiranje/DI).

### `UserRepository`
- `getCurrentUserDisplayName()` / `getCurrentUserProfile()` – dohvaća ime iz Firestore `users/{uid}.displayName`, fallback na Firebase Auth `displayName`, pa na dio e-maila prije `@`.
- `updateDisplayName/updateEmail/updatePassword` – ažuriraju i Firebase Auth i odgovarajuće Firestore polje; osjetljive operacije (email/lozinka) prije toga rade `reauthenticate`.
- `getOnlineStats()` – čita `onlineStats.wins/losses` iz `users/{uid}`.

### `MatchRepository`
- `createMatch(settings)` – kreira dokument u `matches/{matchId}` sa statusom `in_progress`, uključujući `players` (array UID-ova za Firestore pravila) i `playerDetails`.
- `recordVisit(matchId, visit, gameState)` – dodaje visit u polje `visits` (`FieldValue.arrayUnion`) i ažurira trenutni leg/set/legsWon/setsWon. **Napomena:** `serverTimestamp()` se ne koristi unutar elemenata niza, pa se koristi `System.currentTimeMillis()`.
- `completeMatch(...)` – označava meč kao `completed`, sprema pobjednika i finalne rezultate.
- `getUserStats()` – agregira do 50 zadnjih mečeva korisnika za 3-dart prosjek, broj pobjeda, najviši checkout; kombinira s `UserRepository.getOnlineStats()`.
- `getMatchDetail(matchId)` / `getMatchHistory(limit)` – dohvat pojedinačnog meča ili povijesti (`whereArrayContains("players", uid)`, filtrirano na `status == "completed"`, sortirano po `createdAtMs` na klijentu).

### `LobbyRepository`
- `observeOpenLobbies()` / `observeLobby(lobbyId)` – `callbackFlow` s Firestore `addSnapshotListener` za real-time ažuriranje popisa/lobija.
- `createLobby(settings)` – generira ili validira kod lobija (`LOBBY_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"`, isključuje slova I/O i brojeve 0/1 radi čitljivosti), sprema u `lobbies/{lobbyId}` sa statusom `waiting`.
- `joinLobby(lobbyId)` / `joinLobbyByCode(code)` – koriste Firestore transakciju da atomarno provjere i postave `guestUid`, sprječavajući "race condition" pridruživanja dva gosta istovremeno.
- `leaveLobby(lobbyId)` – domaćin briše lobby, gost se uklanja i lobby se vraća u `waiting`.
- `recordOnlineResult(winnerUid, loserUid)` – transakcijski inkrementira `onlineStats.wins`/`onlineStats.losses` u `users` kolekciji.
- `formatCodeForDisplay` / `lobbyShareText` – pomoćne funkcije za prikaz koda u grupama od 4 znaka i generiranje teksta za dijeljenje.

### `SocialRepository`
- Pretraga korisnika po `displayNameLower` (case-insensitive nadimak).
- `friends/{uid}/list/{friendUid}` i `follows/{uid}/list/{targetUid}` subkolekcije – dodavanje/uklanjanje prijatelja i praćenja.
- `getNetworkAuthorUids()` – vraća UID-ove sebe + prijatelja + pratitelja (max 30) za filtriranje feed objava.

### `FeedRepository`
- `getNetworkFeed(limit)` – dohvaća objave autora iz `getNetworkAuthorUids()` (`whereIn`, max 10 vrijednosti po Firestore ograničenju – pazi na limit od 30 UID-ova iz Social repozitorija ako Firestore verzija podržava veći `whereIn`).
- `createMatchPost(detail)` – automatski generira poruku/objavu iz statistike odigranog meča putem `MatchShareFormatter`.
- `createPost(...)` – generička objava (tip `FeedPostType.GENERAL`/`GAME`).

### `TrainingBestScoresStore`
Jedini repozitorij koji ne koristi Firebase – čuva osobne rekorde lokalno po uređaju (`SharedPreferences`), jer treninzi nisu vezani za online profil (barem ne u trenutnoj verziji; `trainingSessions`/`statsDaily` kolekcije u Firestore pravilima su pripremljene za buduću sinkronizaciju).

## 7. Sigurnosna pravila (Firestore)

Pravila u `firestore.rules` slijede princip najmanjih prava:
- `users/{uid}` – čitanje bilo kojem prijavljenom korisniku (potrebno za pretragu nadimaka/prikaz imena), ali pisanje/kreiranje samo vlasniku; brisanje zabranjeno.
- `matches/{matchId}` – čitanje/pisanje ograničeno na korisnike navedene u polju `players` (zato `createMatch` uvijek uključuje `hostUid` u `players` array).
- `lobbies/{lobbyId}` – kreiranje samo ako je `hostUid == auth.uid`; ažuriranje dopušteno domaćinu uvijek, ili gostu koji se pridružuje praznom `waiting` lobiju, ili postojećem gostu.
- `feedPosts` – čitanje svima, pisanje/brisanje samo autoru objave.
- `friends`/`follows` subkolekcije – potpuno privatne (samo vlasnik `uid` segmenta putanje).
- `trainingSessions`/`statsDaily` – pripremljena pravila za buduće značajke (trenutno se ne pišu iz koda).

## 8. Ključni modeli (`model/`)

- **`MatchSettings`** – `startScore`, `playerNames` (2–8), `format` (`FIRST_TO`/`BEST_OF`), `unit` (`LEGS`/`SETS`), `count`, `inRule`, `outRule`. Ima izvedene labele (`formatLabel`, `unitLabel`, `modeLabel`) za UI.
- **`LocalGameState`** – trenutno stanje partije: igrači, tko je na redu, leg/set brojevi, cijela povijest visita (`matchVisits`), `isFinished`/`winnerIndex`.
- **`PlayerGameState`** – stanje jednog igrača: preostali bodovi, osvojeni legovi/setovi, povijest visita **samo za trenutni leg** (`visitHistory`, prikazuje se kao "Zadnji hitci"), te `matchTotalScore`/`matchVisitCount` za prosjek kroz cijeli meč.
- **`VisitRecord`** – zapis jednog visita (za slanje u Firestore i za `MatchStatsCalculator`).
- **`LobbySettings`** – raspon prosjeka igrača (`minAvg`/`maxAvg`) za matchmaking, plus ista pravila igre kao `MatchSettings`, i opcionalni prilagođeni kod.
- **`OnlineLobby`** – Firestore reprezentacija lobija uključujući `guestUid`/`guestName` i `status` (`waiting`/`ready`).
- **`TrainingMode`/`TrainingGameState`** – vidi §4.
- **`MatchStatsDetail`/`PlayerMatchStats`/`MatchKeyMoment`** – vidi §5.
- **`SocialModels`/`SocialUser`** – korisnik u kontekstu prijateljstva/praćenja (`isFriend`, `isFollowing`).
- **`AppNotification`** – model za in-app obavijesti (ako se koristi u UI slojevima za feed/social).

## 9. Tema i UI konvencije

- Tema se nalazi u `ui/theme/` (Material 3, tamna shema po defaultu – `enableEdgeToEdge()` + `isAppearanceLightStatusBars = false`).
- Prema projektnim preferencijama: koristiti **neutralne sive rubove** na UI elementima umjesto obojanih naglašenih rubova, i sve UI stringove pisati na **hrvatskom jeziku** (vidljivo kroz cijeli kod – npr. poruke grešaka, labele modova).

## 10. Testiranje

- `app/src/test` – JUnit testovi (trenutno minimalni predložak `libs.junit`), pogodno mjesto za testiranje `game/` sloja (npr. `LocalGameEngine`, `MatchStatsCalculator`) jer nema Android ovisnosti.
- `app/src/androidTest` – instrumentacijski testovi s Compose UI test bibliotekom (`ui-test-junit4`, Espresso) za end-to-end provjeru ekrana.

