# ✅ Benutzer-Checkliste für OAuth-Setup

Nach dem Pullen dieser Änderungen müssen Sie folgende Schritte durchführen, um OAuth zu aktivieren.

## 🎯 Sofort-Aktion erforderlich

### ❗ Diese Schritte MÜSSEN durchgeführt werden, bevor die App funktioniert:

1. **[ ] GitHub OAuth App erstellen**
   - URL: https://github.com/settings/developers
   - Neue OAuth App registrieren
   - Client ID und Secret notieren

2. **[ ] GitHub Pages aktivieren**
   - Repository Settings → Pages
   - Source: Branch `main` oder `master`, Folder `/docs`
   - Speichern und 2-3 Minuten warten

3. **[ ] `public.properties` erstellen**
   ```bash
   cp public.properties.example public.properties
   ```
   - Datei mit Text-Editor öffnen
   - Client ID und Secret eintragen

4. **[ ] `AppConfig.java` anpassen**
   - Datei: `app/src/main/java/com/thirtydegreesray/openhub/AppConfig.java`
   - Zeile 71: `REDIRECT_URL` auf Ihre GitHub Pages URL setzen
   - Format: `https://IHR_USERNAME.github.io/OpenHub/docs/index.html`

5. **[ ] App neu bauen**
   ```bash
   ./gradlew clean build
   ```

## 📚 Dokumentation

**Starten Sie hier:**
- [QUICK_OAUTH_SETUP.md](./QUICK_OAUTH_SETUP.md) - Schnellstart (5 Min)
- [SETUP_OAUTH.md](./SETUP_OAUTH.md) - Detaillierte Anleitung

**Bei Problemen:**
- [OAUTH_TROUBLESHOOTING.md](./OAUTH_TROUBLESHOOTING.md) - Fehlerbehebung

**Übersicht:**
- [DOCUMENTATION_INDEX.md](./DOCUMENTATION_INDEX.md) - Alle Dokumentationen

## ⚠️ Wichtige Hinweise

- **Keine Credentials committen!** Die Datei `public.properties` ist in `.gitignore` und sollte NIEMALS committed werden.
- **Eigene OAuth App erforderlich:** Jeder Fork benötigt eigene GitHub OAuth Credentials.
- **GitHub Pages muss aktiv sein:** Die Redirect-URL muss erreichbar sein.
- **URLs müssen übereinstimmen:** GitHub OAuth App URL = AppConfig.java URL

## 🚨 Häufige Fehler

### Fehler beim Login: "YOUR_CLIENT_ID"
→ Sie haben `public.properties` nicht erstellt oder ausgefüllt.
→ Lösung: Siehe Schritt 3 oben

### 404 auf GitHub Seite
→ GitHub Pages ist nicht aktiviert.
→ Lösung: Siehe Schritt 2 oben

### "Redirect URI mismatch"
→ URLs stimmen nicht überein.
→ Lösung: Vergleichen Sie beide URLs genau

## 💡 Alternative

Wenn OAuth-Setup zu kompliziert ist:
- Verwenden Sie einen **Personal Access Token (PAT)**
- Anleitung in [README.md](./README.md#option-2-personal-access-token-pat)

## 📞 Hilfe

Bei Problemen:
1. Lesen Sie [OAUTH_TROUBLESHOOTING.md](./OAUTH_TROUBLESHOOTING.md)
2. Überprüfen Sie alle URLs sorgfältig
3. Testen Sie GitHub Pages URL direkt im Browser
4. Bauen Sie die App komplett neu

---

**Status nach diesen Schritten:** ✅ OAuth-Login funktioniert!
