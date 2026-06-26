# ReliefBridge Pocket Demo Script

Target length: under 3 minutes.

## 0:00-0:20 Opening

"ReliefBridge Pocket is a Mobile AI app for Arm Android phones. It helps
disaster-response coordinators triage messy field messages privately on-device,
then proves the optimization gains with a built-in benchmark."

Show the app home screen on the Android device.

## 0:20-0:55 Message Triage

Paste or use the seeded field message:

```text
URGENT: Need accessible transport for two evacuees near Riverside before the road closes.
```

Show the prioritized action queue. Point out:

- Label: `NEED`
- Urgency score
- Category: transport
- Matched volunteer
- Matched resource
- Recommended action

## 0:55-1:45 Optimization Benchmark

Tap **Run Benchmark**.

Explain:

"The app compares a baseline FP32 local model path against an optimized INT8
mobile path. The optimized path also caches tokenization and uses single-pass
scoring, which is better suited to on-device Arm constraints."

Show the speedup and model size reduction numbers.

## 1:45-2:15 Proof Export

Tap **Export Proof**.

Show the saved `benchmark-results.md` path or the benchmark screen.

Say:

"This exports the device, ABI, Android version, latency, throughput, model size,
speedup, and size reduction so judges can inspect the result."

## 2:15-2:50 Close

"ReliefBridge Pocket is useful because sensitive relief messages stay on the
phone, and developers get a reusable benchmark pattern for optimizing mobile AI
on Arm. The repo includes the Android app, model artifacts, setup instructions,
optimization notes, and open-source license."
