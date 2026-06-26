# Arm AI Optimization Notes

ReliefBridge Pocket targets the Mobile AI track of the Arm AI Optimization
Challenge 2026. The app runs relief-message triage locally on an Arm Android
device and compares a baseline inference path against an optimized mobile path,
then exports reproducible proof artifacts.

## What The Model Does

Each message is classified into one of four labels: `need`, `offer`, `update`,
or `noise`. The engine also computes urgency (1–5), detects a relief category,
and matches the message to bundled volunteer and resource data.

## Baseline Path (`baseline_model.json`)

- FP32 bag-of-words weights.
- Re-tokenizes every message on every call.
- Scores every token occurrence, including duplicates.
- Scans all tokens regardless of how confident the result already is.
- No channel-name context used.

## Optimized Path (`optimized_model.json`)

All optimizations are intentional and independently measurable:

| Optimization | Mechanism | Benefit |
| --- | --- | --- |
| INT8 weights | Scale factor applied on load, integers used at runtime | Smaller model asset |
| Token cache | `tokenCache` map in `TriageEngine` | Eliminates re-tokenization for repeated messages |
| Unique-token scoring | `HashSet` dedup before scoring | Avoids re-accumulating score for repeated tokens |
| Channel prior | Score offset from `channelPriors` map | Faster convergence using message context |
| Early-exit | Score-gap check after each token | Stops scanning when label is unambiguous |

### Early-Exit Example

For `"URGENT: URGENT: need transport now"` the "need" score exceeds the
early-exit gap after just 2–3 tokens, so the remaining 4–5 tokens are skipped.
The benchmark reports how many messages in the 300-round run triggered early
exit.

## Benchmark Methodology

The in-app benchmark is run entirely on-device from the Benchmark screen:

1. **JVM warmup**: 25 rounds of silent inference to allow JIT compilation of
   hot paths before timing begins.
2. **Cold start**: time for one full pass over all demo messages.
3. **Warm run**: 300 rounds × 12 messages = 3,600 calls per path.
4. **Memory**: heap snapshot before and after warm run (post-GC) to estimate
   working-set delta.
5. **Both paths run sequentially on the same thread** via `Dispatchers.Default`,
   so the device thermal state is comparable.

## Reported Metrics

| Metric | Description |
| --- | --- |
| Model size | On-disk bytes of the JSON asset |
| Cold start | First-pass latency in ms |
| Warm latency | Average ms per message over 3,600 calls |
| Throughput | Messages per second over the warm run |
| Memory Δ | Heap delta in KB after GC |
| Early exits | Messages classified before full token scan |
| Speedup | `baseline.warmLatency / optimized.warmLatency` |
| Size reduction | `1 − (optimized.sizeBytes / baseline.sizeBytes)` |

## Export Artifacts

The Benchmark screen exports:

- `benchmark-results.md` — human-readable report shared via Android share sheet
- `benchmark-results.json` — machine-readable report for Devpost submission

Both files include device, ABI, Android version, and all metrics above.

## Production Upgrade Path

The current model assets are intentionally small for zero-setup builds. A
production version can swap the JSON artifacts for a TensorFlow Lite or ONNX
Runtime Mobile classifier while reusing the same `TriageEngine` interface,
`BenchmarkRunner` harness, and benchmark export workflow.

Steps:
1. Convert a MobileBERT or DistilBERT text classifier to TFLite FP32 (baseline)
   and INT8 dynamic-range quantized (optimized).
2. Add `org.tensorflow:tensorflow-lite:*` and `org.tensorflow:tensorflow-lite-gpu:*`
   to `app/build.gradle.kts`.
3. Wrap the TFLite `Interpreter` in a new `TfliteTriageEngine` that implements
   the same `classify(message: ReliefMessage): Classification` contract.
4. Optionally enable XNNPACK delegate for additional CPU optimization on Arm.
5. Re-run the benchmark harness — no other changes needed.
