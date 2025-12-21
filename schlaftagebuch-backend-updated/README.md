## Backend starten – Kurzanleitung

### 1. Voraussetzungen

- **JDK 17** installiert  
  (z.B. Temurin 17, Oracle JDK 17)
- **IntelliJ IDEA** (Community Edition reicht)
- **Internetverbindung** für Gradle-Dependencies

> **Hinweis:** Gradle muss **nicht** manuell installiert werden.  
> Das Projekt verwendet den Gradle Wrapper (`gradlew` / `gradlew.bat`).

---

### 2. Projekt aus GitLab öffnen

**Variante A – direkt klonen**

1. In GitLab auf **Clone → HTTPS** klicken und die URL kopieren.
2. In IntelliJ:  
   `File → New → Project from Version Control → Git`
3. GitLab-URL einfügen → Zielordner wählen → **Clone**.

**Variante B – ZIP herunterladen**

1. In GitLab auf **Download as ZIP** klicken.
2. ZIP lokal entpacken.
3. In IntelliJ:  
   `File → Open...` → entpackten Ordner `schlaftagebuch-backend` auswählen.

Beim ersten Öffnen startet IntelliJ einen **Gradle-Sync** und lädt alle Dependencies.  
Falls es Fehler gibt: im Gradle-Fenster rechts auf **Reload All Gradle Projects** klicken.

---

### 3. Server starten

1. In IntelliJ die Klasse  
   `de.schlaftagebuch.SchlaftagebuchBackendApplication` öffnen.
2. Auf den grünen **Run-Button** (Dreieck) klicken.
3. Warten, bis im Run-Log steht:

    - `Started SchlaftagebuchBackendApplication ...`
    - `Tomcat started on port(s): 8080`

Der Backend-Server läuft jetzt unter:

```text
http://localhost:8080
