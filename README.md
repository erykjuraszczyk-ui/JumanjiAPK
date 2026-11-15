# JUMANJI — WORLD OF FANTASY

**Wersja:** 2.111.1  
**Platforma:** Android 13+  
**Typ:** natywna gra przygodowa 2D/2.5D  
**Technologia:** Java, Android Canvas, Gradle, Android SDK 36

## Wizja

JUMANJI — WORLD OF FANTASY to mobilna przygoda osadzona w świecie lewitujących wysp, pradawnych świątyń, magicznych pieczęci i Gwiezdnego Portu. Centralnym punktem świata jest Nexus ze Złotym Jajem.

Gra wykorzystuje własny renderer Canvas i proceduralne dekoracje. Nie pobiera zasobów z sieci i nie wymaga uprawnień do Internetu.

## Krainy

1. Zielone Królestwo — pradawne drzewa i znaki natury.
2. Kraina Ognia — wulkaniczne skały i kuźnia światła.
3. Kryształowe Arkana — kryształy pamięci i runiczne ścieżki.
4. Lodowe Pustkowie — lodowe iglice i latarnie zorzy.
5. Gwiezdny Port — finałowa brama odblokowywana po odbudowaniu czterech pieczęci.
6. Nexus — centralna wyspa, Złote Jajo i przewodniczka Auri.

## Funkcje w tej wersji

- natywna aplikacja Android, orientacja pozioma;
- ruch postaci za pomocą wirtualnego joysticka;
- kamera śledząca bohatera;
- duży, połączony mostami świat z pięcioma obszarami;
- proceduralne dekoracje biomów i autorski rendering wektorowy;
- 12 znaków do odnalezienia, 4 ołtarze i finałowy portal;
- przewodniczka Auri i dziennik misji;
- mapa świata i plecak;
- punkty doświadczenia, poziomy i esencje;
- automatyczny zapis w prywatnym obszarze aplikacji;
- ekran pauzy i wznowienia wyprawy;
- tryb offline.

Interakcje rozwiązują zagadki i przywracają równowagę krain. Gra nie wykorzystuje realistycznej przemocy.

## Wymagania

- Android 13 lub nowszy (minSdk 33).
- ARM64 / zgodne urządzenie Android.
- Android SDK Platform 36.
- JDK 17.
- Gradle 9.6.0.
- Android Gradle Plugin 9.4.0.

## Budowanie

W Android Studio otwórz katalog repozytorium jako projekt Gradle. Zainstaluj Android SDK Platform 36 i JDK 17, a następnie uruchom zadanie:

```bash
gradle :app:assembleDebug
```

Wynik:

```
app/build/outputs/apk/debug/app-debug.apk
```

W GitHub Actions workflow `.github/workflows/android.yml` automatycznie buduje APK po pushu do `main` i dla Pull Requestów. Plik APK jest publikowany jako artefakt zadania Actions.

## Sterowanie

- lewy joystick — ruch postaci;
- przycisk AKCJA — rozmowa, zebranie znaku, aktywacja ołtarza lub portalu;
- MAPA — podgląd świata i położenia;
- ZADANIA — dziennik wyprawy;
- PLECAK — esencje, doświadczenie i pieczęcie;
- przycisk Wstecz Androida — pauza / powrót do gry.

## Zapis

Stan gry jest zapisywany w SharedPreferences w prywatnym katalogu aplikacji. Postęp obejmuje pozycję postaci, XP, esencje, znaki, pieczęcie i zakończenie kampanii. Usunięcie danych aplikacji usuwa również zapis.

## Struktura

```
app/
  src/main/
    java/com/jumanji/worldoffantasy/
      MainActivity.java
      FantasyGameView.java
      GameProgress.java
    res/values/
      strings.xml
      styles.xml
.github/workflows/
  android.yml
docs/
  GAMEPLAY.md
```

## Zakres i status

To działająca baza natywnej gry mobilnej z eksploracją, interakcjami, progresją i kampanią. Grafika jest generowana kodem (2D/2.5D); repo nie zawiera jeszcze pełnych modeli 3D, animacji szkieletowych, profesjonalnej ścieżki dźwiękowej ani rozbudowanej kampanii z aktami. Te elementy są kolejnymi etapami produkcji, a nie funkcjami, których istnienie deklarujemy przed implementacją.
