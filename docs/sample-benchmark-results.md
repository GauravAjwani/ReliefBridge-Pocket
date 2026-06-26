# ReliefBridge Pocket — Arm Optimization Benchmark

Example proof artifact format exported from the app.

## Device

- Device: samsung SM-A055F
- ABI: arm64-v8a
- Android: 14

## Summary

- Speedup: **2.14×** faster
- Model size: **18.5%** smaller
- Early exits: messages classified before full token scan

## Results

| Metric | Baseline FP32 | Optimized INT8 | Improvement |
| --- | ---: | ---: | ---: |
| Model size | 842 B | 686 B | 18.5% smaller |
| Cold start | 1.24 ms | 0.98 ms | — |
| Warm latency | 0.0032 ms/msg | 0.0015 ms/msg | 2.14× faster |
| Throughput | 312 msg/s | 667 msg/s | 2.14× higher |
| Peak memory Δ | 12 KB | 8 KB | — |

Replace the numbers above with your actual exported results from the app.
