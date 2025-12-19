# Quick OAuth Setup Guide

⚡ **5-Minuten-Setup für GitHub OAuth Login**

## Voraussetzungen

- Eigenes GitHub-Konto
- Eigener Fork von OpenHub
- Gradle installiert

## Schnellstart

### 1️⃣ GitHub OAuth App erstellen (2 Minuten)

1. Öffne: https://github.com/settings/developers
2. Klicke "New OAuth App"
3. Fülle aus:
   ```
   Application name: OpenHub (Dein Name)
   Homepage URL: https://github.com/DEIN_USERNAME/OpenHub
   Authorization callback URL: https://DEIN_USERNAME.github.io/OpenHub/docs/index.html
   ```
4. Klicke "Register application"
5. **Kopiere Client ID und generiere Client Secret**

### 2️⃣ GitHub Pages aktivieren (1 Minute)

1. Gehe zu deinem Fork: Repository Settings → Pages
2. Setze:
   - Source: `Deploy from a branch`
   - Branch: `main` (oder `master`)
   - Folder: `/docs`
3. Klicke "Save"
4. Warte 2-3 Minuten

### 3️⃣ App konfigurieren (2 Minuten)

```bash
# 1. Kopiere die Beispiel-Datei
cp public.properties.example public.properties

# 2. Bearbeite public.properties
nano public.properties  # oder vim, oder ein Editor deiner Wahl

# Füge ein:
# openhub_client_id=DEINE_CLIENT_ID
# openhub_client_secret=DEIN_CLIENT_SECRET
```

**Wichtig**: Ersetze `DEINE_CLIENT_ID` und `DEIN_CLIENT_SECRET` mit den Werten aus Schritt 1!

```bash
# 3. Bearbeite AppConfig.java
nano app/src/main/java/com/thirtydegreesray/openhub/AppConfig.java

# Ändere Zeile 71:
# public final static String REDIRECT_URL = "https://DEIN_USERNAME.github.io/OpenHub/docs/index.html";
```

**Wichtig**: Ersetze `DEIN_USERNAME` mit deinem GitHub-Benutzernamen!

### 4️⃣ App bauen und testen

```bash
# Baue die App
./gradlew clean build

# Installiere die APK (auf deinem Gerät/Emulator)
./gradlew installDebug

# Oder finde die APK unter:
# app/build/outputs/apk/normal/debug/app-normal-debug.apk
```

## Testen

1. Öffne die App
2. Tippe auf "LOGIN WITH GITHUB"
3. Du wirst zum GitHub-Login weitergeleitet
4. Nach der Anmeldung solltest du zurück zur App weitergeleitet werden
5. ✅ Fertig!

## ❌ Probleme?

### "YOUR_CLIENT_ID" in der URL?
→ Du hast `public.properties` nicht korrekt ausgefüllt. Überprüfe die Datei und baue neu.

### 404 auf GitHub Pages?
→ GitHub Pages ist nicht aktiviert oder noch nicht deployed. Warte 2-3 Minuten und versuche es erneut.

### "Redirect URI mismatch"?
→ Die URL in `AppConfig.java` stimmt nicht mit der OAuth App überein. Beide müssen identisch sein.

## 📚 Detaillierte Anleitungen

- **Vollständige Anleitung**: [SETUP_OAUTH.md](./SETUP_OAUTH.md)
- **Fehlerbehebung**: [OAUTH_TROUBLESHOOTING.md](./OAUTH_TROUBLESHOOTING.md)
- **Technische Details**: [docs/README_OAUTH_SETUP.md](./docs/README_OAUTH_SETUP.md)

## 💡 Tipp

Wenn OAuth zu kompliziert ist, kannst du auch einen **Personal Access Token** verwenden:

1. Gehe zu: https://github.com/settings/tokens
2. Generate new token (classic)
3. Wähle Scopes: `user`, `repo`, `gist`, `notifications`
4. In der App: "LOGIN WITH TOKEN" und Token eingeben

Fertig! 🎉
