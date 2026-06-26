# Devpost Description

## Project Overview

ReliefBridge Pocket is a Mobile AI app for Arm Android devices that helps
disaster-response coordinators triage messy field messages privately on-device.
It classifies messages as urgent needs, resource offers, updates, or noise; then
builds a prioritized action queue with matched volunteers, supplies, and next
steps.

What makes it interesting is the optimization story: the app includes a built-in
benchmark screen that compares a baseline FP32 local model path against an
optimized INT8 mobile path on the same phone. Judges can see speedup, throughput,
latency, model size reduction, ABI, Android version, and device metadata from the
app itself.

## Functionality / Output

- Paste or edit a relief message on an Android phone.
- Run private on-device triage with no network call.
- Review a prioritized queue of needs, offers, updates, urgency, and resource
  matches.
- Run baseline vs optimized inference benchmarks.
- Export benchmark proof as `benchmark-results.json` and
  `benchmark-results.md`.

Final output: an Android app plus reproducible benchmark artifacts showing how
the optimized mobile path improves local AI performance on Arm.

## Optimization Work

- Baseline model: FP32 bag-of-words classifier.
- Optimized model: INT8 quantized classifier.
- Runtime improvements: cached tokenization, unique-token scoring, and
  single-pass category/urgency scoring.
- Measurements: cold start, warm latency, throughput, model size, device, ABI,
  and Android version.

## Setup Instructions

1. Install Android Studio Ladybug or newer and JDK 17.
2. Connect an Arm Android phone/tablet with USB debugging enabled.
3. Open the `mobile/` folder in Android Studio.
4. Let Gradle sync.
5. Run the `app` configuration on the device.
6. Tap **Run Benchmark** in the app.
7. Tap **Export Proof** after the benchmark completes.

CLI option:

```sh
cd mobile
./gradlew :app:installDebug
```

Windows:

```sh
cd mobile
gradlew.bat :app:installDebug
```

## Why It Should Win

ReliefBridge Pocket combines social impact with measurable mobile AI
optimization. It is easy to demo in under three minutes, useful for real
community-response workflows, and gives developers a reusable template for
building privacy-preserving AI apps with benchmarkable Arm optimizations.
