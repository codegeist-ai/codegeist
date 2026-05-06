# Chat Memory

## Zielbild

- `codegeist.ai` soll als anpassbarer Coding-Agent fuer CLI, TUI und Web wachsen.
- Die Entwicklungsumgebung bleibt repo-lokal: Devcontainer, Regeln, Kommandos und Projekt-Memory liegen im Repository.

## Aktueller Stand

- `main` enthaelt den aktuellen Projektstand und `.opencode` ist als Git-
  Submodul eingebunden; es folgt dem `release`-Branch von
  `codegeist-agent-kit` und zeigt aktuell auf `6901aa4`.
- `.devcontainer/` ist als eigenes Git-Submodul eingebunden, folgt in
  `.gitmodules` dem `release`-Branch von `codegeist-devcontainer-kit` und zeigt
  aktuell auf `97289ef`.
- `start.sh` ist entfernt; der Devcontainer wird direkt ueber VS Code Dev
  Containers oder `devcontainer up --workspace-folder .` gestartet.
- `.devcontainer/initialize.sh` aus dem Kit erzeugt root `.local.env`,
  `compose.local.yml`, `.devcontainer/.gen.env` und
  `.devcontainer/compose.local.gen.yml` bei Bedarf und kann per `BRANCH` einen
  Worktree unter `.worktrees/<branch>` als `/workspace` auswaehlen.
- `app/codegeist` ist ein Spring-Boot- und Spring-Shell-Bootstrap mit Java 25, Maven und vorbereitetem GraalVM-Native-Build.
- `app/codegeist/Taskfile.yml` bietet `test`, `build`, `run` und `native`.
- `.devcontainer/Dockerfile` installiert zusaetzlich `nix`, ohne die bisherige
  apt-basierte Toolchain oder den Devcontainer-Workflow schon auf Nix umzustellen.
- `.devcontainer/Dockerfile` installiert jetzt auch `@devcontainers/cli` im
  bestehenden globalen npm-Toolblock.
- `.devcontainer/tests.sh` liegt jetzt im ausgelagerten `.devcontainer`-
  Submodul und bleibt dort der Devcontainer-Selbsttest.
- Der Nix-Profil-Hook wird global ueber `/etc/profile.d/nix.sh` eingebunden,
  damit `nix` auch in Login-Shells im Container im `PATH` liegt.

## Wichtige Entscheidungen

- Build-Artefakte wie `target/`, `bin/`, `.class` und `.jar` bleiben aus Git heraus.
- Lokale Devcontainer-Umgebungswerte bleiben in root `.local.env`; root
  `compose.local.yml` ist der versionierte lokale Compose-Override, den
  `devcontainer.json` immer einbindet.
- Repo-Memory wird unter `docs/memory-bank/chat.md` gepflegt.
- Nix wird vorerst nur als zusaetzlicher Paketmanager installiert; es gibt noch
  keine Flakes und noch keine Migration der Toolchain auf Nix-Pakete.
- Checkouts ohne rekursive Submodule werden mit
  `git submodule update --init --recursive` repariert, nicht mehr ueber einen
  repo-lokalen Launcher.
- `.opencode` und `.devcontainer` sollen im Parent-Repo ueber ihre
  `release`-Branches aktualisiert werden, nicht ueber die frueher genutzten
  `main`-Branches oder repo-lokale Launcher-Skripte.

## Offene Punkte

- OpenCode im Devcontainer crasht, wenn `OPENCODE_CONFIG_DIR` auf das nicht
  vorhandene `/workspace/.oc_local` zeigt. Der aktuelle `v1.0.1`-Release-Tag
  enthaelt diesen Override noch; fuer den Fix braucht es einen neuen Kit-Release
  oder eine explizite Neuveroeffentlichung dieses Tags.
