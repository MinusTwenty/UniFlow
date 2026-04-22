A composeApp/src/commonMain/kotlin/com/uniflow/uniflow folderben található meg majdnem az összes App-al kapcsolatos programkódok

Rövid összefoglalók mi miket használ folderenként
"auth" folder:
Minden olyan autentikációs programkód ebben található meg ami a sikeresen hozzá járul az app-hoz továbbá a jelszavak hashelése is itt található meg

"data" folder:
Külön lett szedve a "model" folderben a felépítése a semesztereknek, a "semesters" folderben a 3 felhasználónak a szemeszterei vannak szétbontva nyári és téli szemeszterre, magában a "seed" folderben találhatóak azok a programkódok amik segitenek betölteni a DB-t az alkalmazás UI-jába.

"home" folder:
Minden olyan UI programkód egy helyen van ami kell a app UI megvalósításához

"settings" folder:
Itt csak egy .kt van, ami elmenti a témákat akkor is ha kiléptünk az alkalmazásból

"ui.theme" folder:
Benne található a témák beállításai és a UI váza ami meghatározza azt ahogy kinéz

"App.kt":
Itt található meg minden logikai és hozzárendelt programkód ami gördülékenyebbé teszi a munkát

"LoginScreenWithValidation.kt":
Ahogy a neve is sugalja a bejelentkezési fül

Ezek elérhetőek voltak a "kotlin"-ban
Az "sqldelight" folderben pedig az adatbázisok kaptak helyet
