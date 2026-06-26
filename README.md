# ReliefBridge Pocket

**Track:** Mobile AI — Arm AI Optimization Challenge 2026  
**License:** [MIT](LICENSE)

ReliefBridge Pocket is a Mobile AI submission for the Arm AI Optimization
Challenge 2026. It runs private, on-device disaster-relief message triage on an
Arm Android phone and shows measurable optimization gains between a baseline
model path and an optimized mobile path.

The app turns messy field messages into a prioritized action queue: urgent
needs, resource offers, status updates, matched volunteers, matched supplies,
and recommended next actions. The judged artifact lives in `mobile/`.

## Why It Should Win

Relief teams often lose critical asks inside fast-moving chats. ReliefBridge
Pocket moves the first triage pass onto the coordinator's phone, so sensitive
messages can be classified locally with no cloud round trip.

The project is built around the challenge criteria:

- **Mobile AI:** runs on an Arm-powered Android device.
- **Optimization:** compares FP32 baseline assets against INT8 optimized assets.
- **Measurable proof:** exports latency, throughput, size, ABI, and device
  metadata as JSON and Markdown.
- **Developer experience:** the benchmark harness is built into the app and the
  setup path is documented.

## Android Quick Start

Requirements:

- Android Studio Ladybug or newer.
- JDK 17.
- An Arm Android phone or tablet with USB debugging enabled.

Run:

```sh
cd mobile
./gradlew :app:installDebug
```

On Windows:

```sh
cd mobile
gradlew.bat :app:installDebug
```

If you use Android Studio, open the `mobile/` folder, let Gradle sync, choose an
Android device, and press Run.

## Validate On Arm

1. Launch ReliefBridge Pocket on the Android device.
2. Tap **Run Benchmark**.
3. Confirm the app shows speedup and model size reduction numbers.
4. Tap **Export Proof**.
5. Use Android's share sheet to send the Markdown report to your laptop, or
   capture the benchmark screen for the Devpost proof artifact:
   - `benchmark-results.json`
   - `benchmark-results.md`

The benchmark compares:

- `baseline_model.json`: FP32 bag-of-words classifier.
- `optimized_model.json`: INT8 quantized classifier with cached tokenization and
  single-pass scoring.

## Repository Layout

- `mobile/`: Native Android app for the Mobile AI submission.
- `mobile/app/src/main/assets/`: Demo messages, resource directory, and local
  model artifacts.
- `docs/arm-ai-optimization.md`: Optimization methodology and benchmark
  interpretation.
- `docs/mobile-demo-script.md`: Three-minute video script.
- `src/`: Original TypeScript ReliefBridge prototype and Slack/MCP demo.

## Existing TypeScript Demo

The original ReliefBridge Slack/MCP prototype is still available:

```sh
npm install
npm run demo
```

Useful scripts:

- `npm run demo`: Run the local TypeScript relief-triage demo.
- `npm run dev`: Start the Slack Bolt app.
- `npm run mcp`: Start the MCP server over stdio.
- `npm test`: Run unit tests.
- `npm run lint`: Type-check the TypeScript project.

## License

MIT. See [LICENSE](LICENSE).

## Submit to GitHub / Devpost

Full upload checklist: [docs/GITHUB_SUBMISSION.md](docs/GITHUB_SUBMISSION.md)

Quick steps:

1. Create a **public** GitHub repo
2. Push this project (do not include `node_modules/` or `build/` folders)
3. Set repo **About** section to show **MIT** license
4. Submit on Devpost with repo URL, demo video, and benchmark screenshots

Devpost copy: [docs/devpost-description.md](docs/devpost-description.md)
