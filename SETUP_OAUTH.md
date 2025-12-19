# OpenHub OAuth Setup Anleitung

Diese Anleitung erklärt, wie Sie die GitHub OAuth-Authentifizierung für OpenHub einrichten.

## Problem

Wenn Sie versuchen, sich mit Ihrem GitHub-Account anzumelden und einen **404-Fehler** auf einer GitHub-Seite sehen mit einer URL wie:
```
https://github.com/login/oauth/authorize?client_id=YOUR_CLIENT_ID&...
```

Das bedeutet, dass die OAuth-Konfiguration noch nicht vollständig eingerichtet ist.

## Lösung: Vollständige OAuth-Einrichtung

### Schritt 1: GitHub OAuth App erstellen

1. Gehen Sie zu [GitHub Developer Settings](https://github.com/settings/developers)
2. Klicken Sie auf **"New OAuth App"**
3. Füllen Sie die Felder aus:
   - **Application name**: `OpenHub` (oder ein beliebiger Name)
   - **Homepage URL**: `https://github.com/YOUR_GITHUB_USERNAME/OpenHub`
   - **Authorization callback URL**: `https://YOUR_GITHUB_USERNAME.github.io/OpenHub/docs/index.html`
     - ⚠️ **WICHTIG**: Ersetzen Sie `YOUR_GITHUB_USERNAME` mit Ihrem GitHub-Benutzernamen!
     - Beispiel: Wenn Ihr Benutzername `sergey842248` ist, verwenden Sie:
       ```
       https://sergey842248.github.io/OpenHub/docs/index.html
       ```
4. Klicken Sie auf **"Register application"**
5. Notieren Sie sich die **Client ID**
6. Generieren Sie ein **Client Secret** und notieren Sie es sich ebenfalls

### Schritt 2: GitHub Pages aktivieren

1. Gehen Sie zu Ihrem OpenHub-Repository auf GitHub
2. Klicken Sie auf **Settings** (Einstellungen)
3. Scrollen Sie nach unten zu **Pages** im linken Menü
4. Wählen Sie unter **Source**:
   - Branch: `main` (oder `master`)
   - Folder: `/docs`
5. Klicken Sie auf **Save**
6. Warten Sie einige Minuten, bis die Seite deployed ist
7. Überprüfen Sie, ob `https://YOUR_GITHUB_USERNAME.github.io/OpenHub/docs/index.html` erreichbar ist

### Schritt 3: Redirect-URI in AppConfig.java aktualisieren

Öffnen Sie die Datei `app/src/main/java/com/thirtydegreesray/openhub/AppConfig.java` und ändern Sie die `REDIRECT_URL`:

```java
public final static String REDIRECT_URL = "https://YOUR_GITHUB_USERNAME.github.io/OpenHub/docs/index.html";
```

Ersetzen Sie `YOUR_GITHUB_USERNAME` mit Ihrem echten GitHub-Benutzernamen.

### Schritt 4: public.properties Datei erstellen

1. Kopieren Sie die Datei `public.properties.example` zu `public.properties`:
   ```bash
   cp public.properties.example public.properties
   ```

2. Öffnen Sie `public.properties` und tragen Sie Ihre Werte ein:
   ```properties
   openhub_client_id=IHRE_CLIENT_ID_VON_SCHRITT_1
   openhub_client_secret=IHR_CLIENT_SECRET_VON_SCHRITT_1
   bugly_appid=
   debug_bugly_appid=
   ```

   ⚠️ **WICHTIG**: Die Datei `public.properties` wird NICHT in Git committed (steht in .gitignore), um Ihre Credentials zu schützen!

### Schritt 5: App neu bauen

```bash
./gradlew clean build
```

## So funktioniert der OAuth-Flow

```
┌─────────────┐
│  OpenHub    │
│     App     │
└──────┬──────┘
       │ 1. Öffnet GitHub OAuth
       ▼
┌─────────────────────────────────────────┐
│  https://github.com/login/oauth/...    │
│  (GitHub OAuth Seite)                   │
└──────┬──────────────────────────────────┘
       │ 2. Benutzer meldet sich an
       │ 3. GitHub leitet weiter
       ▼
┌───────────────────────────────────────────┐
│  https://YOUR_USERNAME.github.io/...      │
│  (GitHub Pages - OAuth Callback Seite)    │
└──────┬────────────────────────────────────┘
       │ 4. Web-Seite leitet weiter
       ▼
┌─────────────┐
│  openhub:// │  ← Custom URL Scheme
│    login    │  ← Öffnet die App wieder
└──────┬──────┘
       │ 5. App verarbeitet OAuth-Code
       ▼
┌─────────────┐
│  Erfolgreich│
│  eingeloggt │
└─────────────┘
```

## Häufige Probleme und Lösungen

### Problem 1: "YOUR_CLIENT_ID" in der URL
**Ursache**: Die `public.properties` Datei wurde nicht korrekt erstellt oder enthält noch Platzhalter.
**Lösung**: Führen Sie Schritt 4 aus und bauen Sie die App neu.

### Problem 2: 404 auf GitHub Pages
**Ursache**: GitHub Pages wurde nicht aktiviert oder die Seite wurde noch nicht deployed.
**Lösung**: 
- Überprüfen Sie, ob GitHub Pages in den Repository-Einstellungen aktiviert ist
- Warten Sie 2-3 Minuten nach der Aktivierung
- Prüfen Sie, ob die URL im Browser direkt erreichbar ist

### Problem 3: Redirect-URI stimmt nicht überein
**Ursache**: Die URL in `AppConfig.java` stimmt nicht mit der in der GitHub OAuth App konfigurierten URL überein.
**Lösung**: 
- Überprüfen Sie beide URLs
- Stellen Sie sicher, dass beide URLs identisch sind (inkl. https://)
- Bauen Sie die App nach Änderungen neu

### Problem 4: "The redirect_uri MUST match the registered callback URL"
**Ursache**: Die in der GitHub OAuth App eingetragene Callback URL stimmt nicht mit der tatsächlich verwendeten URL überein.
**Lösung**: 
- Gehen Sie zu [GitHub Developer Settings](https://github.com/settings/developers)
- Bearbeiten Sie Ihre OAuth App
- Korrigieren Sie die "Authorization callback URL"

## Sicherheitshinweise

- ⚠️ **NIEMALS** Ihre Client ID und Client Secret öffentlich teilen oder committen!
- Die Datei `public.properties` ist bereits in `.gitignore` enthalten
- Verwenden Sie für jede Fork/Deployment eigene OAuth Credentials
- Ändern Sie regelmäßig Ihr Client Secret aus Sicherheitsgründen

## Alternative für Entwicklung/Tests

Wenn Sie nur lokal testen möchten ohne GitHub Pages:

1. Erstellen Sie eine separate OAuth App für Entwicklung
2. Verwenden Sie als Callback URL: `http://localhost:8080/oauth/callback`
3. Starten Sie einen lokalen Server im `docs/` Ordner:
   ```bash
   cd docs
   python3 -m http.server 8080
   ```
4. Ändern Sie temporär die `REDIRECT_URL` in `AppConfig.java`

**Hinweis**: Dies funktioniert nur für Tests auf Ihrem lokalen Gerät!

## Support

Falls Sie weiterhin Probleme haben:
1. Überprüfen Sie alle URLs sorgfältig
2. Stellen Sie sicher, dass GitHub Pages aktiv ist
3. Warten Sie einige Minuten nach Änderungen
4. Bauen Sie die App komplett neu (Clean Build)

## Technische Details

- **OAuth 2.0** mit PKCE (Proof Key for Code Exchange)
- **Custom URL Scheme**: `openhub://login`
- **Intermediate Redirect**: GitHub Pages
- **Sicherheit**: State-Parameter gegen CSRF-Angriffe
