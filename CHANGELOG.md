# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] - 2026-04-19

### Added
- Initial public release of STT Translator for NeoForge 1.21.1.
- Live translation support for in-game chat messages.
- Integration with translation providers:
  - LibreTranslate (free)
  - Google Translate (free)
  - DeepL (API key)
  - Claude AI (API key)
- Voice-to-text pipeline integration for Simple Voice Chat.
- Automatic speech chunking, silence detection, and server submission flow for STT.
- Overhead speech bubbles with language indicators and configurable style.
- Side translation panel with configurable position and theme.
- In-game configuration screen with:
  - Source and target language selection
  - Provider selection
  - API key fields for premium providers
  - Bubble size and duration controls
  - Original text visibility toggle
  - Dark/light style toggles
- Main menu and pause menu quick access button.
- Welcome overlay showing active language and provider status.

### Changed
- Improved fallback strategy between translation services when a provider fails.
- Improved rendering and wrapping behavior for long translated messages.

### Fixed
- Fixed panel rendering logic so the non-translation panel displays original text instead of translated text.
- Fixed widespread text encoding/mojibake issues in UI and chat formatting strings.
- Fixed Unicode symbol rendering in labels, arrows, status icons, and language names.

### Compatibility
- Minecraft: 1.21.1
- Loader: NeoForge
- Java: 21

### Notes
- For voice translation features, Simple Voice Chat must be installed and configured.
