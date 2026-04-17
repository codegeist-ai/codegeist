# Chat Memory

## Zielbild

- `codegeist.ai` soll als anpassbarer Coding-Agent fuer CLI, TUI und Web wachsen.
- Die Entwicklungsumgebung bleibt repo-lokal: Devcontainer, Regeln, Kommandos und Projekt-Memory liegen im Repository.

## Aktueller Stand

- `main` enthaelt den aktuellen Projektstand und `.opencode` ist als Git-
  Submodul eingebunden.
- `start.sh` ist der zentrale Einstieg, um das Repo-Root oder ein Repo-Worktree direkt in einer VS-Code-Devcontainer-Session zu oeffnen.
- Der Devcontainer oeffnet den echten Checkout-Pfad direkt und bekommt `UID`, `GID`, `PROJECT_NAME`, `COMPOSE_PROJECT_NAME`, `CODEGEIST_REPO_ROOT`, `CODEGEIST_REPO_WORKTREE` und `CODEGEIST_HOSTNAME` zur Laufzeit von `start.sh`.
- `app/codegeist` ist ein Spring-Boot- und Spring-Shell-Bootstrap mit Java 25, Maven und vorbereitetem GraalVM-Native-Build.
- `app/codegeist/Taskfile.yml` bietet `test`, `build`, `run` und `native`.
- `.devcontainer/Dockerfile` installiert zusaetzlich `nix`, ohne die bisherige
  apt-basierte Toolchain oder den Devcontainer-Workflow schon auf Nix umzustellen.
- `.devcontainer/Dockerfile` installiert jetzt auch `@devcontainers/cli` im
  bestehenden globalen npm-Toolblock.
- `.devcontainer/tests.sh` ist der Devcontainer-Selbsttest fuer die spaetere
  Auslagerung des gesamten `.devcontainer/`-Verzeichnisses in ein eigenes Repo.
- Der Nix-Profil-Hook wird global ueber `/etc/profile.d/nix.sh` eingebunden,
  damit `nix` auch in Login-Shells im Container im `PATH` liegt.

## Wichtige Entscheidungen

- Build-Artefakte wie `target/`, `bin/`, `.class` und `.jar` bleiben aus Git heraus.
- Lokale Devcontainer-Overrides bleiben in `.devcontainer/.local.env`; die versionierte `.devcontainer/.env` enthaelt nur sichere Standardwerte.
- Repo-Memory wird unter `docs/memory-bank/chat.md` gepflegt.
- Nix wird vorerst nur als zusaetzlicher Paketmanager installiert; es gibt noch
  keine Flakes und noch keine Migration der Toolchain auf Nix-Pakete.

## Offene Punkte

- Den neuen `.devcontainer/tests.sh`-Pfad weiter als Smoke-Test pflegen, waehrend
  der `.devcontainer/`-Ordner fuer eine spaetere Repo-Auslagerung vorbereitet
  wird.
