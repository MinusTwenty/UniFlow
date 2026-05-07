**UniFlow futtatási és telepítési ótmutató**
**Fejlesztői környezet**

A fejlesztéshez Android Studio vagy IntelliJ IDEA használható, Android futtatáshoz Android Studio ajánlott. A projekt Gradle Wrapperrel érkezik, ezért külön Gradle telepítése nem szükséges; a repositoryban található `gradlew` / `gradlew.bat` használható. A wrapper verziója: Gradle `8.14.3`.
A projektben a Kotlin verzió `2.2.21`, a Compose Multiplatform verzió `1.9.1`, az Android Gradle Plugin verzió `8.11.2`. A Java célverzió a buildfájl alapján `Java 11` (`jvmTarget`, `sourceCompatibility`, `targetCompatibility`). Androidhoz Android SDK szükséges; a projekt `compileSdk = 36`, `targetSdk = 36`, `minSdk = 24` értékekkel van beállítva.
iOS futtatás is támogatott, mert a projekt tartalmaz `iosApp` Xcode projektet, valamint `iosX64`, `iosArm64` és `iosSimulatorArm64` Kotlin Multiplatform targeteket. ***iOS futtatáshoz macOS és Xcode szükséges***. Az Xcode projektben iOS deployment targetként `18.2` szerepel.

**Projekt megnyitása**

1. A GitHub repository klónozása:
```
git clone <repository-url>
cd UniFlow
```
2. A projekt megnyitása Android Studióban vagy IntelliJ IDEA-ban a repository gyökérmappájából.
3. Az IDE automatikusan felismeri a `settings.gradle.kts` fájlt és a `:composeApp` modult.
4. ***Megnyitás után Gradle Sync futtatása szükséges***. A függőségek a `google()`, `mavenCentral()` és `gradlePluginPortal()` repositorykból töltődnek le.

**Android futtatás**

Androidon a fő futtatható modul a `composeApp`. A belépési pont az Android oldalon a `MainActivity`, amely meghívja a közös `App` composable-t.
Futtatás Android Studióból:

1. Válassza ki a `composeApp` Android run configurationt.
2. Indítsa Android emulátoron vagy csatlakoztasson fizikai Android eszközt.
(Ha fizikai Android eszközt használna akkor fontos hogy kapcsolja be az USB debuggert)
4. Futtassa az alkalmazást az IDE `Run` gombjával.

Parancssorból debug build készíthető:
```
./gradlew :composeApp:assembleDebug
```
Windows alatt:
```
.\gradlew.bat :composeApp:assembleDebug
```
Az Android manifest alapján az alkalmazás az alábbi jogosultságokat használja: `INTERNET`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`. Az értesítési jogosultság Android 13-tól felhasználói engedélyhez kötött. A fájlmegnyitás FileProvideren keresztül történik.

***Alternatíva AJÁNLOTT!!!***
Az UniFlow mappában megtalálható az APK mappa abban van egy .apk file, Android esetén ezt kell telepíteni és ki lehet hagyni a fenti Android futtatást.
Első telepítésnél engedélyezni kellhet az ismeretlen forrásból telepítést!

**iOS futtatás**

Az iOS futtatás támogatott, ***de csak macOS-en és Xcode-dal végezhető***. A repositoryban található Xcode projekt: `iosApp/iosApp.xcodeproj`.

Futtatás menete:

1. Nyissa meg az `iosApp/iosApp.xcodeproj` fájlt Xcode-ban.
2. Válassza ki az `iosApp` targetet és egy iOS szimulátort vagy csatlakoztasson fizikai iOS eszközt.
(Ha fizikai iOS eszközt használna első buildeléskor ki kell választania a General/VPN & Device Management és "Trust"-olni kell az alkalmazást hogy elinduljon)
3. Indítsd el az alkalmazást Xcode-ból.

Az Xcode projekt tartalmaz egy `Compile Kotlin Framework` build phase-t, amely a következő Gradle taskot hívja:
```
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```
iOS-en a közös Compose felület a `MainViewController`-en keresztül töltődik be. A Google Drive import iOS oldalon részben megvalósított, Androidon teljesebb implementáció található.

**Adatbázis inicializálása**

Az alkalmazás SQLDelight adatbázist használ. A sémák a `composeApp/src/commonMain/sqldelight` mappában találhatók. Androidon az adatbázis neve `uniflow.db`, és `AndroidSqliteDriver` hozza létre. iOS-en `NativeSqliteDriver` használatos.
Az adatbázis inicializálása a `provideDatabase` függvényben történik:
```
val db = UniFlowDatabase(factory.createDriver())
seedAllDemoData(db)
```
A `seedAllDemoData` feltölti a demóadatokat: demófelhasználók, szemeszterek, tantárgyak, órák, termek, oktatók és beiratkozási kapcsolatok. A demóadatok három felhasználóhoz és felhasználónként két szemeszterhez kapcsolódnak. A Beállítások képernyőn elérhető a demóadatok visszaállítása is.

**Demó belépési adatok**

A projektben előre létrehozott demófelhasználók:

```
Felhasználónév / jelszó
134288 / asd123
111    / asd123
222    / asd123
```

**Kipróbálható funkciók**

A futtatás után kipróbálható a bejelentkezés, a főoldal, az órarend napi és heti nézete, az óra részletes nézete, a jegyzetek létrehozása és szerkesztése, az emlékeztetők kezelése, az értesítések, a fájlimport, a Google Drive import (lásd lentebb *Google Drive File import*), a témaválasztás, a kijelentkezés és a demóadatok visszaállítása.

**Biztonsági megjegyzés**

A repositoryban szereplő belépési adatok kizárólag demóadatok, nem valós felhasználói fiókok. Publikus repository közzététele előtt ellenőrizni kell, hogy ne kerüljön bele API-kulcs, privát token, aláírókulcs, személyes adat vagy lokális konfiguráció. Különösen ellenőrizendők a Google-integrációhoz kapcsolódó beállítások, valamint a `local.properties` és hasonló gépfüggő fájlok.

**Google Drive file import**

Fontos megjegyezni, hogy az alkalmazás test fázisban van, így csak a saját email címem fog működni. Ha ki szeretné próbálni ezt a funkciót akkor szívesen berakom Önt a tesztelési csapatba.
Elérhetőségeim:
Magán email cím: blodguardvenge@gmail.com
Egyetemi email cím: 134288@stundent.ujs.sk
Kérem, hogy csak az email címet küldje el és lehetőségeim szerint minél hamarabb hozzá rendelem a google cloud developerben, hogy kipróbálhassa a funkciót.
