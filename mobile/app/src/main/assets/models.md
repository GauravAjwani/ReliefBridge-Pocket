# ReliefBridge Pocket Model Assets

The app ships two tiny local model artifacts so judges can run the Mobile AI demo without downloading a large model:

- `baseline_model.json`: FP32 bag-of-words classifier.
- `optimized_model.json`: INT8 quantized classifier with the same label space.

Both artifacts classify relief messages into `need`, `offer`, `update`, or `noise`. The optimized runtime also caches tokenization and uses a single-pass scorer for urgency/category hints.

For a production follow-up, replace these JSON artifacts with a TensorFlow Lite or ONNX Runtime Mobile text classifier and keep the same `TriageEngine` interface so the benchmark screen can continue comparing baseline and optimized paths.
