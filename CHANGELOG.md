# Changelog

> **This is a fork of [YuvDwi/Steve](https://github.com/YuvDwi/Steve).** Changes listed under *[Fork]* sections were made in this fork (`guilhermeDSB/Steve-AI`). All other entries describe the original project baseline.

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased]

### [Fork] Changed
- Updated `README.md` to clearly identify this repository as a fork of [YuvDwi/Steve](https://github.com/YuvDwi/Steve)
- Translated README content into English and simplified the structure
- Updated `git clone` URL to point to `guilhermeDSB/Steve-AI` instead of the upstream repository
- Added **Useful Links** section in README pointing back to the original project and its documentation
- Updated credits section to give explicit attribution to the original project

---

## [1.0.0] — Inherited from YuvDwi/Steve

> The following items describe the features and architecture present in the original project that this fork is based on.

### Added

#### Core Mod
- Minecraft Forge mod entry point (`SteveMod`) with full lifecycle management (FML events, server start/stop)
- In-game command interface: open panel with `K`, issue natural-language commands via `/steve spawn <name>`

#### Agent & Action System
- `ActionExecutor` — orchestrates action execution with retry logic and error handling
- `CollaborativeBuildManager` — multi-agent workload balancing and conflict resolution for building tasks
- `Task` / `ActionResult` — typed task/result models for the agent pipeline
- Built-in action implementations:
  - `BuildStructureAction` — autonomous structure planning and block-by-block placement
  - `CombatAction` — threat assessment and combat coordination
  - `CraftItemAction` — recipe lookup and crafting execution
  - `FollowPlayerAction` — pathfinding to track a target player
  - `GatherResourceAction` — resource location and gathering
  - `IdleFollowAction` — passive follow behaviour when idle
  - `MineResourceAction` — optimal mining location selection and execution
  - `NavigateAction` — point-to-point navigation

#### LLM Integration
- `TaskPlanner` — converts natural-language input into a structured action plan using LLMs
- `PromptBuilder` / `ResponseParser` — prompt construction and response parsing utilities
- Synchronous clients: `OpenAIClient`, `GroqClient`, `GeminiClient`
- Asynchronous clients: `AsyncOpenAIClient`, `AsyncGroqClient`, `AsyncGeminiClient` (non-blocking LLM calls)
- `LLMCache` — response caching to reduce API calls and latency
- `LLMExecutorService` — managed thread pool for async LLM requests
- `LLMFallbackHandler` / `ResilientLLMClient` — automatic provider fallback and resilience policies
- `ResilienceConfig` — configurable retry, timeout, and circuit-breaker settings
- `LLMException` / `LLMResponse` — typed error and response models

#### Execution Engine
- `SteveAPI` — public API surface for issuing commands to agents
- `CodeExecutionEngine` — safe execution sandbox for generated code snippets
- `MetricsInterceptor` — records execution metrics (latency, success rate, token usage)

#### Memory & World Knowledge
- `SteveMemory` — per-agent short-term and long-term memory store
- `WorldKnowledge` — shared world state cache (blocks, entities, resources)
- `StructureRegistry` — persistent registry of built and planned structures

#### Plugin System
- `ActionPlugin` / `ActionFactory` / `ActionRegistry` — extensible plugin API for registering custom actions
- `CoreActionsPlugin` — default plugin bundling all built-in actions
- `PluginManager` — runtime plugin discovery and lifecycle management via Java ServiceLoader

#### Structure Generation
- `StructureGenerators` — procedural generators for common structures (houses, walls, platforms, etc.)
- `StructureTemplateLoader` — loads structure templates from resource files
- `BlockPlacement` — value type representing a single block placement operation

#### Utilities & Events
- `ActionUtils` — shared helper methods for action implementations
- `SteveCommands` — registers and handles all Forge commands
- `ServerEventHandler` — listens to server-side Minecraft events (player join/leave, world tick, etc.)

#### Configuration
- TOML config file (`config/steve-common.toml`) supporting OpenAI, Groq, and Gemini API credentials and model settings
- Config example file (`config/steve-common.toml.example`)

#### Testing
- Unit test stubs for `ActionExecutor`, `TaskPlanner`, `WorldKnowledge`, and `StructureGenerators`

#### Infrastructure
- Gradle build system with Minecraft Forge (`1.20.1-47.3.0`) setup
- `gradlew` / `gradlew.bat` wrapper scripts
- `scripts/run_steve.sh` helper script for local development
- Resource files: `mods.toml`, `en_us.json` language file, `pack.mcmeta`
- `.gitignore` covering Gradle, IDE, and OS-specific files

---

[Unreleased]: https://github.com/guilhermeDSB/Steve-AI/compare/74f388e...HEAD
[1.0.0]: https://github.com/guilhermeDSB/Steve-AI/commit/74f388e9b771554e8dd7661b7abfb3020b5818d2
