# Chat Memory

## Zielbild

- `codegeist.ai` soll als anpassbarer Coding-Agent fuer CLI, TUI und Web wachsen.
- Die Entwicklungsumgebung bleibt repo-lokal: Devcontainer, Regeln, Kommandos und Projekt-Memory liegen im Repository.

## Aktueller Stand

- `main` wird gerade als neuer Root-Commit aufgebaut und enthaelt den gewollten frischen Projektstand.
- `.opencode` ist als Git-Submodul auf `main` eingebunden.
- `start.sh` ist der zentrale Einstieg, um das Repo-Root oder ein Repo-Worktree direkt in einer VS-Code-Devcontainer-Session zu oeffnen.
- Der Devcontainer oeffnet den echten Checkout-Pfad direkt und bekommt `UID`, `GID`, `PROJECT_NAME`, `COMPOSE_PROJECT_NAME`, `CODEGEIST_REPO_ROOT`, `CODEGEIST_REPO_WORKTREE` und `CODEGEIST_HOSTNAME` zur Laufzeit von `start.sh`.
- `app/codegeist` ist ein Spring-Boot- und Spring-Shell-Bootstrap mit Java 25, Maven und vorbereitetem GraalVM-Native-Build.
- `app/codegeist/Taskfile.yml` bietet `test`, `build`, `run` und `native`.

## Wichtige Entscheidungen

- Build-Artefakte wie `target/`, `bin/`, `.class` und `.jar` bleiben aus Git heraus.
- Lokale Devcontainer-Overrides bleiben in `.devcontainer/.local.env`; die versionierte `.devcontainer/.env` enthaelt nur sichere Standardwerte.
- Repo-Memory wird unter `docs/memory-bank/chat.md` gepflegt.

## Offene Punkte

- Den frischen `main`-Root-Commit mit dem aktuellen gestagten Projektstand auf `github/main` veroeffentlichen.
- Danach den Devcontainer-Start und den Build- oder Testpfad einmal im neuen Verlauf pruefen.
