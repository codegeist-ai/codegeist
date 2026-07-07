# Spring Shell TerminalUI

Source-backed summary of the TerminalUI and TUI surfaces in the analyzed Spring
Shell checkout.

## Why This Matters

Codegeist currently uses Spring Shell `TerminalUI` for its terminal chat harness.
This note captures upstream contracts that affect layout, focus, event routing,
blocking execution, and cleanup.

## Main Runtime Type

`source/spring-shell-jline/src/main/java/org/springframework/shell/jline/tui/component/view/TerminalUI.java`
is the central TUI runtime. It coordinates:

- JLine `Terminal`, `BindingReader`, and `KeyMap`.
- `DefaultScreen` virtual display and JLine `Display` updates.
- Root view, optional modal view, and focused view.
- `DefaultEventLoop` for key, mouse, system, signal, and view events.
- Theme resolver and active theme name.

`run()` is a blocking loop. It enters raw terminal mode, optionally enters the
alternate screen, enables mouse tracking, renders, reads key/mouse bindings, and
restores terminal state in `finally`.

## Builder And Spring Boot Integration

`source/spring-shell-core-autoconfigure/src/main/java/org/springframework/shell/core/autoconfigure/TerminalUIAutoConfiguration.java`
contributes prototype-scoped `TerminalUIBuilder` and `ViewComponentBuilder` beans
when TerminalUI is on the classpath. The builder receives the active theme,
theme resolver, terminal, and ordered `TerminalUICustomizer` beans.

The docs recommend autowired `TerminalUIBuilder` rather than manual construction.

## View Configuration Contract

`TerminalUI.configure(View)` calls `view.init()` and injects the event loop,
theme resolver, theme name, and view service. Views should not directly know the
TerminalUI instance; modal behavior is exposed through `ViewService`.

## Focus, Modal, Key, And Mouse Routing

Root view rendering is layered with one optional modal view. The modal view is
drawn above the root view and receives mouse input instead of the root when
present.

Key handling is ordered:

- Root hotkey handler sees the event first and may consume it.
- If not consumed, the focused root view key handler receives the event.
- A handler result can move focus by returning a focus target.

Mouse handling uses the modal view when one exists, otherwise the root view. A
mouse handler can also move focus.

## Event Loop

`source/spring-shell-jline/src/main/java/org/springframework/shell/jline/tui/component/view/event/DefaultEventLoop.java`
uses Reactor sinks and processors. It exposes typed Flux streams for key, mouse,
system, signal, and view events. Built-in processors include animation and task
event processing.

## Built-In View And Component Families

The Graphify communities and docs identify these high-value TUI concepts:

- View and screen abstractions: `View`, `AbstractView`, `DefaultScreen`, cells,
  rectangles, layers, and cursor state.
- Layout and containers: `BoxView`, `GridView`, `AppView`, `MenuBarView`,
  `StatusBarView`, and dialogs.
- Interactive controls: `ButtonView`, `InputView`, `ListView`, `MenuView`, and
  progress/status views.
- Components: string, number, path, confirmation, single-select, multi-select,
  and path-search components.
- Theming: `ThemeResolver`, active theme, style settings, and renderer support.

## Follow-Up Pointers

- Ask for a source trace from `TerminalUI.run()` through `read()` and
  `handleKeyEvent()`.
- Ask which Spring Shell view classes Codegeist wraps today and which upstream
  constructors or fluent APIs are safe to expose.
- Ask how modal views interact with focus and mouse routing before adding
  Codegeist prompt dialogs or permission prompts.
