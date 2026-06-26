# ReliefBridge Pocket Android App

This is the Arm Mobile AI artifact for ReliefBridge Pocket.

## Run

Open this `mobile/` folder in Android Studio and run the `app` configuration on
an Arm Android phone or tablet.

CLI:

```sh
cd mobile
./gradlew :app:installDebug
```

Windows:

```sh
cd mobile
gradlew.bat :app:installDebug
```

## Screens

- Field message intake.
- Prioritized action queue.
- Baseline vs optimized benchmark.
- Benchmark proof export.

## Assets

- `app/src/main/assets/demo_messages.json`: seeded relief messages.
- `app/src/main/assets/relief_directory.json`: demo volunteers and resources.
- `app/src/main/assets/baseline_model.json`: FP32 baseline model.
- `app/src/main/assets/optimized_model.json`: INT8 optimized model.

## Benchmark Output

Tap **Run Benchmark**, then **Export Proof**. The app writes the files and opens
Android's share sheet for the Markdown report:

- `benchmark-results.json`
- `benchmark-results.md`

The report includes device metadata, ABI, Android version, model size, cold
start, warm latency, throughput, speedup, and size reduction.
