# Chat Memory

## Zielbild

- `codegeist.ai` soll als anpassbarer Coding-Agent fuer CLI, TUI und Web wachsen.
- Die Entwicklungsumgebung bleibt repo-lokal: Devcontainer, Regeln, Kommandos und Projekt-Memory liegen im Repository.

## Aktueller Stand

- `main` enthaelt den aktuellen Projektstand und `.opencode` ist als Git-
  Submodul eingebunden; es folgt dem `release`-Branch von
  `codegeist-agent-kit` und zeigt aktuell auf `d441527`.
- `.devcontainer/` ist als eigenes Git-Submodul eingebunden, folgt in
  `.gitmodules` dem `release`-Branch von `codegeist-devcontainer-kit` und zeigt
  aktuell auf `c715224`.
- `start.sh` ist entfernt; der Devcontainer wird direkt ueber VS Code Dev
  Containers oder `devcontainer up --workspace-folder .` gestartet.
- `.devcontainer/initialize.sh` aus dem Kit erzeugt root `.local.env`,
  `compose.local.yml`, root `.oc_local/`, `.devcontainer/.env` und
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
- Das lokale OpenCode-Overlay `.oc_local/` enthaelt jetzt die Commands
  `/analyse-project`, `/ask-project` und `/ask-project-repomix`, den Skill
  `/repository-analysis`, den Subagent `@repomix` sowie das AI-Script
  `render-mermaid.sh` fuer Mermaid-SVG-Rendering.
- `docs/third-party/opencode/source` ist als Submodul fuer
  `https://github.com/anomalyco/opencode.git` auf Branch `dev` eingebunden und
  zeigt aktuell auf `22e64ca`.
- `docs/third-party/opencode/` enthaelt jetzt die initiale Third-Party-
  Dokumentations-Workspace fuer OpenCode: `README.md`, `ANALYSIS_REPORT.md`,
  `REGENERATE.md`, Feature-, User-, Developer-Notizen und Mermaid-Quellen.
- Die `opencode`-Analyse nutzt fuer Graphify eine fokussierte Runtime-Corpus
  statt des ganzen Repos. Der letzte Graphify-Lauf erzeugte 1.247 Nodes, 2.008
  Edges und 78 Communities; Graphify-, Repomix- und Verify-Ausgaben bleiben
  regenerierbar und ignoriert.
- Source-nahe Fragen zu Third-Party-Projekten sollen ueber
  `/ask-project-repomix <project> "<frage>"` oder direkt `@repomix` laufen.
  Der Subagent laedt `docs/third-party/<project>/repomix-output.xml` in seinem
  eigenen Kontext ueber Repomix-Tools, damit der Hauptkontext klein bleibt.

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
- Fuer gemeinsame Kit-Updates gibt es jetzt den OpenCode-Workflow
  `/update-submodules`, der `.opencode` und `.devcontainer` auf die in
  `.gitmodules` konfigurierten Branches setzt.
- Projektspezifische Analyse-Workflows gehoeren ins lokale Overlay `.oc_local/`,
  nicht in das geteilte `.opencode`-Submodul. Third-Party-Analyseartefakte werden
  unter `docs/third-party/<project-name>/` abgelegt; Mermaid-Quellen liegen dort
  unter `diagrams/source/` und gerenderte SVGs unter `diagrams/rendered/`.
  Regenerierbare schwere Artefakte wie `repomix-output.*`, `graphify-out/`,
  Logs, Manifeste, Verify-Reports und gerenderte SVGs bleiben per `.gitignore`
  aus Git heraus und werden ueber `REGENERATE.md` neu erzeugt.
- `/analyse-project` nutzt kein eigenes Analyse-Shellscript mehr. Die alte
  `.oc_local/ai-scripts/analyse-project.sh`-Orchestrierung ist entfernt;
  Graph-Erzeugung laeuft ueber den geteilten `graphify`-Skill auf einer
  gefilterten Code-/Dokumentations-Corpus.

## Offene Punkte

- Keine konkreten naechsten Schritte aus diesem Chat offen.
