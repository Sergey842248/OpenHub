# OpenHub OAuth Setup

⚠️ **WICHTIG**: Für eine vollständige Schritt-für-Schritt-Anleitung zur Einrichtung siehe [SETUP_OAUTH.md](../SETUP_OAUTH.md)

📖 **Bei Problemen**: Siehe [OAUTH_TROUBLESHOOTING.md](../OAUTH_TROUBLESHOOTING.md) für eine Fehlerbehebungs-Checkliste

## Problem und Lösung

### Das Problem
Der ursprüngliche OAuth-Flow verwendete ein Custom URL Scheme (`openhub://login`) als Redirect-URI. GitHub kann diese Custom Schemes nicht direkt verarbeiten, was zu einem 404-Fehler führte.

### Die Lösung
Wir verwenden jetzt eine Intermediate-HTTPS-Redirect-URL über GitHub Pages:

1. **GitHub OAuth Redirect URI**: `https://YOUR_GITHUB_USERNAME.github.io/OpenHub/docs/index.html`
2. **Intermediate Web-Seite**: Empfängt OAuth-Parameter von GitHub
3. **Custom Scheme Callback**: Leitet Parameter an App weiter (`openhub://login`)
4. **App**: Verarbeitet die Parameter und schließt OAuth-Flow ab

## Konfiguration

### GitHub OAuth App Setup
In Ihrer GitHub OAuth App müssen Sie folgende Redirect URI konfigurieren (ersetzen Sie YOUR_GITHUB_USERNAME mit Ihrem echten GitHub-Benutzernamen):
```
https://YOUR_GITHUB_USERNAME.github.io/OpenHub/docs/index.html
```

⚠️ **Hinweis**: Sie müssen auch GitHub Pages in Ihrem Repository aktivieren! Siehe [SETUP_OAUTH.md](../SETUP_OAUTH.md) für Details.

### Verzeichnisstruktur
```
docs/
├── index.html          # OAuth Callback Web-Seite
└── README_OAUTH_SETUP.md # Diese Anleitung
```

## Funktionsweise

1. **OAuth-Start**: App öffnet GitHub OAuth Seite mit PKCE
2. **GitHub Authentifizierung**: Benutzer meldet sich bei GitHub an
3. **GitHub Redirect**: GitHub leitet zu GitHub Pages weiter
4. **Web Callback**: Web-Seite empfängt OAuth-Parameter
5. **App Callback**: Web-Seite leitet Parameter an App weiter
6. **Token Exchange**: App tauscht Code gegen Access Token um

## Vorteile

- ✅ GitHub OAuth-kompatible Redirect-URI
- ✅ Kein 404-Fehler mehr
- ✅ Sichere PKCE-Implementierung
- ✅ Benutzerfreundliche Callback-Seite
- ✅ Funktioniert in allen modernen Browsern

## Entwicklung

### Lokale Tests
Für lokale Entwicklung können Sie die Web-Seite mit einem lokalen Server testen:
```bash
cd docs
python3 -m http.server 8000
```

Dann verwenden Sie `http://localhost:8000/` als Redirect-URI (nur für Tests).

### Deployment
Pushen Sie den `docs/` Ordner in Ihr Repository, aktivieren Sie GitHub Pages in den Repository-Einstellungen, und verwenden Sie die generierte GitHub Pages URL als Redirect-URI in Ihrer GitHub OAuth App.

## Sicherheit

- PKCE (Proof Key for Code Exchange) wird verwendet für zusätzliche Sicherheit
- State-Parameter validiert gegen CSRF-Angriffe
- Code Verifier wird sicher zwischen App und Web-Seite übertragen
- HTTPS-gesicherte Kommunikation