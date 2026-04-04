# Steve AI - Autonomous AI Agent for Minecraft

> **This is a fork of [YuvDwi/Steve](https://github.com/YuvDwi/Steve).** All credit for the original project goes to the original authors. This fork is maintained for personal use, experimentation, and contributions.

Cursor for Minecraft. Instead of AI that helps you write code, you get AI agents that actually play the game with you.

https://github.com/user-attachments/assets/23f0ccdd-7a7a-4d49-9dd9-215ebf67265a

## What It Does

Steve acts as an Agent, or a series of Agents if you choose to employ all of them. You describe what you want, and he understands the context and executes — same concept as Cursor, except instead of code, the agent operates inside Minecraft.

The interface is simple: press K to open a panel, type what you need. The agents handle the interpretation, planning, and execution. Say "mine some iron" and the agent reasons about where iron spawns, navigates there, and starts mining.

Key capabilities:
- **Resource extraction** — agents determine optimal mining locations and strategies
- **Autonomous building** — agents plan layouts and material usage
- **Combat and defense** — agents assess threats and coordinate responses
- **Exploration and gathering** — pathfinding and resource location
- **Collaborative execution** — automatic workload balancing and conflict resolution

## Quick Start

**Requirements:**
- Minecraft 1.20.1 with Forge
- Java 17
- An OpenAI API key (or Groq/Gemini if you prefer)

**Installation:**
1. Download the JAR from [releases](https://github.com/YuvDwi/Steve/releases)
2. Put it in your `mods` folder
3. Launch Minecraft
4. Copy `config/steve-common.toml.example` to `config/steve-common.toml`
5. Add your API key to the config

**Config example:**
```toml
[openai]
apiKey = "your-api-key-here"
model = "gpt-3.5-turbo"
maxTokens = 1000
temperature = 0.7
```

Then spawn a Steve with `/steve spawn Bob` and press K to start giving commands.

## Usage Examples

```
"mine 20 iron ore"
"build a house near me"
"help Alex with the tower"
"defend me from zombies"
"follow me"
"gather wood from that forest"
"make a cobblestone platform here"
"attack that creeper"
```

The agents are pretty good at figuring out what you mean. You don't need to be super specific.

## Building from Source

```bash
git clone https://github.com/guilhermeDSB/Steve-AI.git
cd Steve-AI
./gradlew build
```

Output JAR will be in `build/libs/`. To test in development:

```bash
./gradlew runClient
```

## Configuration

Edit `config/steve-common.toml`:

```toml
[llm]
provider = "groq"  # Options: openai, groq, gemini

[openai]
apiKey = "sk-..."
model = "gpt-3.5-turbo"
maxTokens = 1000
temperature = 0.7

[groq]
apiKey = "gsk_..."
model = "llama3-70b-8192"
maxTokens = 1000

[gemini]
apiKey = "AI..."
model = "gemini-1.5-flash"
maxTokens = 1000
```

**Performance Tips:**
- Use Groq for fastest inference (recommended for gameplay)
- GPT-4 for better planning but higher latency
- Lower temperature (0.5-0.7) for more deterministic actions

## Useful Links

- 🔗 **Original project:** [YuvDwi/Steve](https://github.com/YuvDwi/Steve)
- 🐛 **Original issues:** [YuvDwi/Steve/issues](https://github.com/YuvDwi/Steve/issues)
- 📖 **Full documentation & architecture:** See the original repo's [README](https://github.com/YuvDwi/Steve#readme)

## Credits

- [YuvDwi/Steve](https://github.com/YuvDwi/Steve) — the original project this fork is based on
- OpenAI/Groq/Google for LLM APIs
- Minecraft Forge for the modding framework
- LangChain/AutoGPT for agent architecture inspiration

## License

MIT