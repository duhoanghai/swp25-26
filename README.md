# SWP25_26 - Schlaftagebuch App – Gruppe A

## Gruppe

**Mitglieder**
- Marwa Al Siyamji Al Mousli -- [AlSiyamjiAlMousli.Marwa@b-tu.de](mailto:AlSiyamjiAlMousli.Marwa@b-tu.de)
- Valentina Aseeva -- [Valentina.Aseeva@b-tu.de](mailto:Valentina.Aseeva@b-tu.de)
- Bruno Schreier -- [Bruno.Schreier@b-tu.de](mailto:Bruno.Schreier@b-tu.de)
- Eric Staudt -- [Eric.Staudt@b-tu.de](mailto:IEric.Staudt@b-tu.de)
- Hai Hoang Du -- [HaiHoang.Du@b-tu.de](mailto:HaiHoang.Du@b-tu.de)

**Architektur- & Qualitäts-Coaches**
* **Isa Zenker** -- [Isabelle.Zenker@b-tu.de](mailto:Isabelle.Zenker@b-tu.de)

**Tutor:in**
* **Leonie Ziechmann** -- [Leonie.Ziechmann@b-tu.de](mailto:Leonie.Ziechmann@b-tu.de)


---

## Projektübersicht

Dieses Projekt entsteht im Rahmen der Lehrveranstaltung **Softwarepraktikum (SWP) WS 2025/26** an der BTU Cottbus-Senftenberg.  
Das **Schlaftagebuch** ist ein System zur digitalen Erfassung des Schlafverhaltens von Jugendlichen ab 14 Jahren.
Patient:innen füllen täglich ein Morgenprotokoll in der Android-App aus (optional mit Smartwatch‑Daten),
Ärzt:innen erhalten automatisch ausgewertete Berichte als PDF.
Das System besteht aus einem Spring‑Boot‑Backend, einer Android‑App (mit optionaler Wear‑OS‑App)
und einer Web‑GUI für Ärzt:innen.

---

## Installation

### Repository klonen
```bash
git clone <gitlab-url>
cd swp-25-26-groupe-a
```

### Voraussetzungen
- **JDK 17**
- **Docker Desktop** (inkl. Docker Compose)
- **Android Studio** (für Android/Wear‑OS)
- **VS Code + Live Server** (für die Web‑GUI)

### Backend (Spring Boot + PostgreSQL)
```bash
cd src/back
# Datenbank starten
docker compose up -d
# Backend starten
./gradlew bootRun
```
### API-Base
- **API-Base URL:** `http://localhost:8080`
- Die angezeigten **Hibernate-Abfragen** sowie der laufende `bootRun`-Prozess zeigen, dass das Backend erfolgreich gestartet ist und betriebsbereit ist.
- Der **Gradle-Fortschritt** verbleibt typischerweise bei ca. **80 %** (erwartetes Verhalten).

> **Hinweis:**  
> Eine Überprüfung über den Webbrowser ist nicht möglich, da das Backend ausschließlich REST-Endpunkte bereitstellt.

Die Funktionsfähigkeit kann wie folgt überprüft werden:

- **Über ein API-Testtool**  
  z. B. Postman

- **Über angebundene Frontends**
  - Android-App
  - Web-GUI

### Datenbank
- **PostgreSQL** läuft lokal auf Port `5433`
- Alle notwendigen Verbindungsdaten (Port, User, Passwort, Datenbankname) sind in der Datei `docker-compose.yml` definiert.
- Alle Datenbanktabellen sind direkt in der IDE (z. B. IntelliJ IDEA) über das integrierte Database-Tool einsehbar. 

---

### Web‑GUI (Ärzt:innen‑Dashboard)
- Ordner `src/HTML` öffnen
- `login.html` über einen lokalen Webserver öffnen (z. B. VS Code Live Server)
- Optional: API‑Base setzen
  ```js
  localStorage.setItem('api_base', 'http://localhost:8080')
  ```

### Android‑App (Patient:innen)
- Projektordner `src/version1` in Android Studio öffnen
- Modul **app** ausführen (Emulator oder Gerät)

### Wear‑OS‑App (optional)
- Modul **wear** ausführen (Wear‑OS‑Emulator oder Gerät)

---

## Nutzung / Features

**Patient:innen (Android App)**
- Erstlogin via Zugangscode
- Tägliches Schlafprotokoll (Morgenfragebogen)
- Erinnerungen & Einstellungen
- Optional: Smartwatch‑Sync
- Bericht als PDF abrufen

**Ärzt:innen (Web‑GUI & API)**
- Login und Dashboard (Statistiken, Aktivitäten)
- Patient:innenübersicht
- Zugangscodes generieren
- Berichte abrufen

**Testzugänge (Dev Seed)**
- Arzt: `arzt1` / `arztpass`
- Patient: `patient_test` / `patientpass`

---

## Technische Details

**Backend**
- Java 17, Spring Boot 3
- PostgreSQL, JWT‑Auth
- HAPI‑FHIR (R4)
- PDF‑/Report‑Erstellung (OpenPDF, JFreeChart)

**Android / Wear OS**
- Kotlin, Jetpack Compose
- Retrofit/OkHttp
- Wearable API

**Web‑GUI**
- HTML/CSS/JavaScript

---

## Lizenz

Dieses Projekt dient ausschließlich **Lehr- und Studienzwecken** im Rahmen des Softwarepraktikums.

---

## Ansprechpartner

* **Taisiya Khakharova** -- [Taisiya.Khakharova@b-tu.de](mailto:Taisiya.Khakharova@b-tu.de)
* **Roland Pankatz** -- [Roland.Pankatz@b-tu.de](mailto:Roland.Pankatz@b-tu.de)
* **Leonie Ziechmann** -- [Leonie.Ziechmann@b-tu.de](mailto:Leonie.Ziechmann@b-tu.de)
* **Isa Zenker** -- [Isabelle.Zenker@b-tu.de@b-tu.de](mailto:Isabelle.Zenker@b-tu.de)
* **Patrick Kekes** -- [Patrick.A.Kekes@b-tu.de](mailto:Patrick.A.Kekes@b-tu.de)
