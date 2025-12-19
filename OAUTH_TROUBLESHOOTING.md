# OAuth Troubleshooting Checklist

Wenn Sie Probleme mit der GitHub OAuth-Authentifizierung haben (z.B. 404-Fehler, "YOUR_CLIENT_ID" in der URL), gehen Sie diese Checkliste durch:

## ✅ Checkliste

### 1. GitHub OAuth App erstellt?
- [ ] Ich habe eine GitHub OAuth App unter https://github.com/settings/developers erstellt
- [ ] Ich habe die **Client ID** notiert
- [ ] Ich habe ein **Client Secret** generiert und notiert
- [ ] Die **Authorization callback URL** ist auf meine GitHub Pages URL gesetzt:
  - Format: `https://MEIN_GITHUB_USERNAME.github.io/OpenHub/docs/index.html`
  - Beispiel: `https://sergey842248.github.io/OpenHub/docs/index.html`

### 2. public.properties Datei konfiguriert?
- [ ] Ich habe `public.properties.example` zu `public.properties` kopiert
- [ ] Ich habe meine **Client ID** in `public.properties` eingetragen
- [ ] Ich habe mein **Client Secret** in `public.properties` eingetragen
- [ ] Die Datei enthält KEINE Platzhalter mehr (kein "YOUR_CLIENT_ID")

**Überprüfen Sie die Datei:**
```bash
cat public.properties
# Sollte ungefähr so aussehen:
# openhub_client_id=1234567890abcdef1234
# openhub_client_secret=abcdef1234567890abcdef1234567890abcdef12
```

### 3. AppConfig.java aktualisiert?
- [ ] Ich habe `app/src/main/java/com/thirtydegreesray/openhub/AppConfig.java` geöffnet
- [ ] Die Zeile `REDIRECT_URL` enthält MEINE GitHub Pages URL
- [ ] Die URL stimmt EXAKT mit der in der OAuth App konfigurierten URL überein
- [ ] Format: `https://MEIN_GITHUB_USERNAME.github.io/OpenHub/docs/index.html`

**Überprüfen Sie die Datei:**
```bash
grep "REDIRECT_URL" app/src/main/java/com/thirtydegreesray/openhub/AppConfig.java
# Sollte Ihre URL zeigen, nicht "ThirtyDegreesRay" oder "sergey842248"
```

### 4. GitHub Pages aktiviert?
- [ ] Ich habe in meinen Repository-Einstellungen "Pages" aufgerufen
- [ ] Source ist auf Branch `main` (oder `master`) und Folder `/docs` gesetzt
- [ ] Ich habe gespeichert und 2-3 Minuten gewartet
- [ ] Die URL `https://MEIN_GITHUB_USERNAME.github.io/OpenHub/docs/index.html` ist im Browser erreichbar
- [ ] Die Seite zeigt "OpenHub OAuth Callback" (wenn direkt aufgerufen)

**Testen Sie die URL:**
```bash
# Öffnen Sie diese URL in Ihrem Browser:
https://MEIN_GITHUB_USERNAME.github.io/OpenHub/docs/index.html
# Sie sollten die OAuth-Callback-Seite sehen
```

### 5. App neu gebaut?
- [ ] Ich habe die App nach allen Änderungen neu gebaut
- [ ] Ich habe `./gradlew clean` ausgeführt
- [ ] Ich habe `./gradlew build` ausgeführt
- [ ] Ich habe die neue APK installiert

**Rebuild-Befehle:**
```bash
./gradlew clean
./gradlew build
```

## 🔍 Häufige Fehler

### Fehler: "YOUR_CLIENT_ID" in der OAuth-URL

**Problem:** Die URL zeigt `client_id=YOUR_CLIENT_ID`

**Ursache:** Die `public.properties` Datei wurde nicht korrekt konfiguriert oder die App wurde nicht neu gebaut.

**Lösung:**
1. Überprüfen Sie `public.properties` - sie sollte echte Werte enthalten
2. Führen Sie einen Clean Build aus: `./gradlew clean build`
3. Installieren Sie die neue APK

### Fehler: 404 auf GitHub Pages URL

**Problem:** Die Redirect-URL zeigt einen GitHub 404-Fehler

**Ursache:** GitHub Pages ist nicht aktiviert oder die Seite wurde nicht deployed.

**Lösung:**
1. Repository Settings → Pages
2. Setzen Sie Source auf Branch `main` und Folder `/docs`
3. Speichern und 2-3 Minuten warten
4. Testen Sie die URL direkt im Browser

### Fehler: "The redirect_uri MUST match the registered callback URL"

**Problem:** GitHub zeigt diesen Fehler nach der Anmeldung

**Ursache:** Die URL in `AppConfig.java` stimmt nicht mit der in der OAuth App konfigurierten URL überein.

**Lösung:**
1. Überprüfen Sie die URL in der GitHub OAuth App
2. Überprüfen Sie die `REDIRECT_URL` in `AppConfig.java`
3. Beide URLs müssen EXAKT identisch sein (inklusive https://, Groß-/Kleinschreibung, etc.)
4. Ändern Sie eine der URLs, damit sie übereinstimmen
5. Bauen Sie die App neu

### Fehler: App startet nicht nach Login

**Problem:** Die OAuth-Seite zeigt "Success", aber die App öffnet sich nicht

**Ursache:** Das Custom URL Scheme wird möglicherweise nicht korrekt verarbeitet.

**Lösung:**
1. Überprüfen Sie, ob die App installiert ist
2. Prüfen Sie die AndroidManifest.xml auf Intent-Filter für `openhub://login`
3. Testen Sie auf einem anderen Gerät/Emulator

## 📋 Debugging

Wenn nichts funktioniert, sammeln Sie diese Informationen:

1. **Überprüfen Sie die OAuth-URL in der App:**
   - Versuchen Sie, sich anzumelden
   - Kopieren Sie die komplette URL aus dem Browser
   - Suchen Sie nach `client_id=` - welcher Wert steht dort?
   - Suchen Sie nach `redirect_uri=` - welcher Wert steht dort (URL-dekodiert)?

2. **Überprüfen Sie GitHub Pages:**
   - Öffnen Sie Ihre GitHub Pages URL direkt
   - Was sehen Sie? (OAuth Callback Seite oder 404?)
   - Öffnen Sie die Browser-Konsole (F12) - gibt es Fehler?

3. **Überprüfen Sie die Konfiguration:**
   ```bash
   # Zeige public.properties (ohne die Secrets preiszugeben)
   cat public.properties | grep "openhub_client_id"
   
   # Zeige die REDIRECT_URL
   grep "REDIRECT_URL" app/src/main/java/com/thirtydegreesray/openhub/AppConfig.java
   
   # Überprüfe ob GitHub Pages aktiv ist
   git branch -a
   ```

## 🆘 Immer noch Probleme?

Wenn Sie alle Schritte befolgt haben und es immer noch nicht funktioniert:

1. Lesen Sie [SETUP_OAUTH.md](./SETUP_OAUTH.md) nochmal sorgfältig durch
2. Erstellen Sie eine neue OAuth App und versuchen Sie es mit frischen Credentials
3. Testen Sie auf einem frischen Fork des Repositories
4. Verwenden Sie alternativ die Personal Access Token (PAT) Authentifizierung

## 🔗 Hilfreiche Links

- [GitHub OAuth Apps Dokumentation](https://docs.github.com/en/developers/apps/building-oauth-apps)
- [GitHub Pages Dokumentation](https://docs.github.com/en/pages)
- [OpenHub Setup Guide](./SETUP_OAUTH.md)
