# OpenHub OAuth Callback Page

Diese Datei ist die OAuth-Callback-Seite für OpenHub und wird über GitHub Pages bereitgestellt.

## Was macht diese Seite?

Diese Seite fungiert als Zwischenseite im GitHub OAuth-Authentifizierungsprozess:

1. **GitHub leitet hierher**: Nach erfolgreicher Anmeldung bei GitHub wird der Benutzer zu dieser Seite weitergeleitet
2. **Empfängt OAuth-Parameter**: Die Seite empfängt den Authorization Code und State-Parameter von GitHub
3. **Leitet zur App weiter**: Die Seite leitet die Parameter über ein Custom URL Scheme (`openhub://login`) zurück zur App
4. **App vervollständigt OAuth**: Die App tauscht den Code gegen ein Access Token aus

## Setup

Um diese Seite zu verwenden:

1. **GitHub Pages aktivieren**:
   - Gehen Sie zu Repository Settings → Pages
   - Wählen Sie Source: `main` (oder `master`) und Folder: `/docs`
   - Speichern Sie die Einstellungen

2. **Warten Sie auf Deployment** (2-3 Minuten)

3. **Überprüfen Sie die URL**: 
   - Die Seite sollte unter `https://YOUR_USERNAME.github.io/OpenHub/docs/index.html` erreichbar sein

4. **Konfigurieren Sie Ihre GitHub OAuth App**:
   - Gehen Sie zu [GitHub Developer Settings](https://github.com/settings/developers)
   - Setzen Sie die "Authorization callback URL" auf Ihre GitHub Pages URL

## Fehlerbehebung

### 404 Fehler auf dieser Seite

**Problem**: Die OAuth-Seite zeigt einen 404-Fehler.

**Mögliche Ursachen**:
1. GitHub Pages ist nicht aktiviert
2. Der `docs/` Ordner wurde nicht deployed
3. Die URL ist falsch

**Lösung**:
- Überprüfen Sie die Repository-Einstellungen unter Pages
- Stellen Sie sicher, dass der Branch und Folder korrekt eingestellt sind
- Warten Sie einige Minuten nach Aktivierung
- Testen Sie die URL direkt im Browser

### "Missing required parameters"

**Problem**: Die Callback-Seite zeigt "Missing required parameters".

**Mögliche Ursachen**:
1. Die GitHub OAuth App ist nicht korrekt konfiguriert
2. Die Client ID in der App ist falsch oder fehlt
3. Die Redirect URI stimmt nicht überein

**Lösung**:
- Siehe [SETUP_OAUTH.md](../SETUP_OAUTH.md) für vollständige Setup-Anleitung
- Überprüfen Sie Ihre `public.properties` Datei
- Stellen Sie sicher, dass alle URLs übereinstimmen

## Dateien in diesem Verzeichnis

- `index.html` - Die OAuth-Callback-Seite (wird von GitHub Pages bereitgestellt)
- `README.md` - Diese Datei
- `README_OAUTH_SETUP.md` - Technische Dokumentation des OAuth-Flows

## Weitere Informationen

- **Vollständige Setup-Anleitung**: [SETUP_OAUTH.md](../SETUP_OAUTH.md)
- **Fehlerbehebung**: [OAUTH_TROUBLESHOOTING.md](../OAUTH_TROUBLESHOOTING.md)
- **Technische Details**: [README_OAUTH_SETUP.md](./README_OAUTH_SETUP.md)
