# EpicCrates

EpicCrates ist ein Paper-Plugin für Minecraft 1.20.4+, das physische Crates mit Keys, Loot-Tabellen, Persistenz und einer mehrphasigen Öffnungsanimation bereitstellt.

## Voraussetzungen

- Paper 1.20.4 oder neuer
- Java 17 oder neuer auf dem Server
- Für den Build: Java 17 und Maven 3.9+

## Installation auf dem Server

1. Lade `EpicCrates-1.0.0.jar` aus dem GitHub-Release herunter.
2. Lege die JAR in den `plugins`-Ordner deines Paper-Servers.
3. Starte den Server einmal neu.
4. Passe bei Bedarf `plugins/EpicCrates/config.yml` an.
5. Starte den Server erneut oder nutze `/crate reload`.

## Befehle und Berechtigungen

Alle `/crate`-Befehle sind ausschließlich für Operatoren verfügbar. Die Konsole darf sie ebenfalls ausführen.

- `/crate give <spieler> <typ> [anzahl]` - gibt physische Crates aus; `epiccrates.give`
- `/crate give <typ> [anzahl]` - gibt eine Crate an dich selbst
- `/crate list` - listet alle Crate-Typen
- `/crate remove` - entfernt die Crate, die der Spieler ansieht; `epiccrates.admin`
- `/crate reload` - lädt die Konfiguration neu; `epiccrates.reload`
- `/crate preview <typ>` - öffnet die Loot-Vorschau
- `/crate key give <spieler> <typ> [anzahl]` - gibt passende Keys; `epiccrates.give`
- Rechtsklick auf eine Crate - öffnet sie mit dem passenden Key; `epiccrates.use`

Nach dem Platzieren zeigen zwei dauerhafte Hologramm-Zeilen direkt über der Kiste den Crate-Typ, den Lootpool und die Interaktionen an.
Linksklick auf eine Crate öffnet die Loot-Vorschau mit den tatsächlichen Wahrscheinlichkeiten. In Creative entfernt Linksklick die Crate direkt.

Standardtypen sind `common_crate`, `rare_crate`, `epic_crate` und `legendary_crate`. Loot, Chancen, Materialien, Model-Daten und Cooldown werden in `config.yml` definiert.

## Persistenz und Schutz

Crates werden in `plugins/EpicCrates/crates.yml` mit Welt, Koordinaten, Typ und Besitzer gespeichert. Zusätzlich wird der Crate-Typ am Chest-Block als PersistentDataContainer markiert. Crates sind gegen normalen Abbau geschützt; Administratoren mit `epiccrates.admin` dürfen sie entfernen.

Während einer Öffnung verhindert ein Lock, dass dieselbe Crate doppelt geöffnet wird. Der Standard-Cooldown beträgt 20 Sekunden und ist konfigurierbar.

## GitHub veröffentlichen

### 1. Projekt auf GitHub pushen

```powershell
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/DEIN-NAME/DEIN-REPOSITORY.git
git push -u origin main
```

Wenn bereits ein Remote existiert, überspringe `git remote add origin` oder ändere ihn mit `git remote set-url origin <URL>`.

### 2. Automatischer Build

Bei jedem Push auf `main` und bei Pull Requests startet `.github/workflows/build.yml`. Die Action richtet Temurin Java 17 ein, nutzt den Maven-Cache und führt `mvn clean package` aus. Die fertige JAR erscheint zusätzlich als GitHub-Actions-Artefakt.

### 3. GitHub-Release erzeugen und JAR herunterladen

Erstelle einen Versionstag und pushe ihn:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

Bei Tags, die mit `v` beginnen, baut die Action die JAR und hängt sie automatisch an ein GitHub Release. Öffne danach auf GitHub den Bereich **Releases** und lade `EpicCrates-1.0.0.jar` herunter.

### 4. JAR testen

Kopiere die JAR in den `plugins`-Ordner eines Paper-Testservers, starte den Server und prüfe:

```text
/crate list
/crate give DeinSpieler common_crate 1
/crate key give DeinSpieler common_crate 1
```

Platziere die Crate und öffne sie mit dem passenden Key. Prüfe außerdem Neustart, Cooldown, parallele Klicks, Schutz vor Abbau und `/crate preview`.

## Klassenübersicht

- `EpicCrates`: Plugin-Einstieg, Manager, Listener und Befehle.
- `Crate`: unveränderliches Crate-Modell mit Weltposition, Typ und Besitzer.
- `CrateType`: bekannte Crate-Typen und IDs.
- `CrateManager`: Laden, Speichern, Markieren, Erzeugen und Entfernen von Crates.
- `CrateCommand`: alle `/crate`-Befehle und Item-Erzeugung.
- `CrateTabCompleter`: Tab-Vervollständigung für Befehle und Typen.
- `CrateAnimation`: Lock, Cooldown, Sounds, Partikel, Display und Belohnungsablauf.
- `ItemRevealTask`: wiederverwendbarer ItemDisplay-Bewegungsablauf.
- `ParticleRunnable`: Hilfslogik für kreisende Partikel.
- `CrateInteractListener`: Platzieren, Keys prüfen und Öffnungen starten.
- `CrateProtectListener`: schützt Crates vor normalem Abbau.
- `Reward`: Loot-Eintrag mit Material, Menge, Seltenheit und Chance.
- `RewardManager`: lädt Loot aus YAML und wählt gewichtete Belohnungen.
- `PreviewGUI`: GUI zur Loot-Vorschau.
- `ColorUtil`: verarbeitet Farb-Codes und Platzhalter.

## Build lokal

```powershell
mvn clean package
```

Das Ergebnis liegt anschließend unter `target/EpicCrates-1.0.0.jar`.
