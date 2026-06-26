package com.reliefbridge.pocket

import android.os.Build
import kotlin.system.measureNanoTime

class BenchmarkRunner(
    private val repository: AssetRepository,
) {
    fun run(messages: List<ReliefMessage>): BenchmarkReport {
        val baselineModel = repository.loadModel("baseline_model.json")
        val optimizedModel = repository.loadModel("optimized_model.json")

        val baseline = measurePath(
            path = "baseline-fp32",
            engine = TriageEngine(baselineModel, optimized = false),
            messages = messages,
            modelSizeBytes = baselineModel.sizeBytes,
        )
        val optimized = measurePath(
            path = "optimized-int8-arm",
            engine = TriageEngine(optimizedModel, optimized = true),
            messages = messages,
            modelSizeBytes = optimizedModel.sizeBytes,
        )

        val speedup = baseline.warmLatencyMs / optimized.warmLatencyMs.coerceAtLeast(0.0001)
        val sizeReduction = 1.0 - (optimized.modelSizeBytes.toDouble() / baseline.modelSizeBytes.toDouble())
        val memoryReduction = 1.0 - (optimized.peakMemoryKb.toDouble() / baseline.peakMemoryKb.coerceAtLeast(1).toDouble())

        return BenchmarkReport(
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            abi = Build.SUPPORTED_ABIS.firstOrNull().orEmpty(),
            androidVersion = Build.VERSION.RELEASE.orEmpty(),
            baseline = baseline,
            optimized = optimized,
            speedup = speedup,
            sizeReduction = sizeReduction,
            memoryReduction = memoryReduction,
        )
    }

    private fun measurePath(
        path: String,
        engine: TriageEngine,
        messages: List<ReliefMessage>,
        modelSizeBytes: Long,
    ): BenchmarkSample {
        // JVM warmup: let JIT compile the hot paths before measuring
        repeat(25) { messages.forEach { engine.classify(it) } }

        // Memory snapshot before cold start
        System.gc()
        Thread.sleep(30)
        val memBeforeKb = heapUsedKb()

        val coldStartNs = measureNanoTime {
            messages.forEach { engine.classify(it) }
        }

        val rounds = 300
        val warmNs = measureNanoTime {
            repeat(rounds) { messages.forEach { engine.classify(it) } }
        }

        System.gc()
        Thread.sleep(30)
        val memAfterKb = heapUsedKb()

        val totalMessages = rounds * messages.size
        val warmMs = warmNs / 1_000_000.0
        return BenchmarkSample(
            path = path,
            coldStartMs = coldStartNs / 1_000_000.0,
            warmLatencyMs = warmMs / totalMessages.coerceAtLeast(1),
            throughputMessagesPerSecond = totalMessages / (warmNs / 1_000_000_000.0),
            modelSizeBytes = modelSizeBytes,
            peakMemoryKb = maxOf(0L, memAfterKb - memBeforeKb),
            earlyExits = engine.earlyExitCount,
        )
    }

    private fun heapUsedKb(): Long {
        val rt = Runtime.getRuntime()
        return (rt.totalMemory() - rt.freeMemory()) / 1024L
    }
}

fun BenchmarkReport.toMarkdown(): String =
    buildString {
        appendLine("# ReliefBridge Pocket — Arm Optimization Benchmark")
        appendLine()
        appendLine("## Device")
        appendLine("- Device: $device")
        appendLine("- ABI: $abi")
        appendLine("- Android: $androidVersion")
        appendLine()
        appendLine("## Summary")
        appendLine("- Speedup: **${speedup.fmt(2)}×** faster")
        appendLine("- Model size: **${(sizeReduction * 100).fmt(1)}%** smaller")
        if (optimized.earlyExits > 0) {
            appendLine("- Early exits: **${optimized.earlyExits}** messages classified before full token scan")
        }
        appendLine()
        appendLine("## Results")
        appendLine()
        appendLine("| Metric | Baseline FP32 | Optimized INT8 | Improvement |")
        appendLine("| --- | ---: | ---: | ---: |")
        appendLine("| Model size | ${baseline.modelSizeBytes} B | ${optimized.modelSizeBytes} B | ${(sizeReduction * 100).fmt(1)}% smaller |")
        appendLine("| Cold start | ${baseline.coldStartMs.fmt(2)} ms | ${optimized.coldStartMs.fmt(2)} ms | — |")
        appendLine("| Warm latency | ${baseline.warmLatencyMs.fmt(4)} ms/msg | ${optimized.warmLatencyMs.fmt(4)} ms/msg | ${speedup.fmt(2)}× faster |")
        appendLine("| Throughput | ${baseline.throughputMessagesPerSecond.fmt(0)} msg/s | ${optimized.throughputMessagesPerSecond.fmt(0)} msg/s | ${(optimized.throughputMessagesPerSecond / baseline.throughputMessagesPerSecond.coerceAtLeast(0.001)).fmt(2)}× higher |")
        appendLine("| Peak memory Δ | ${baseline.peakMemoryKb} KB | ${optimized.peakMemoryKb} KB | — |")
    }

fun BenchmarkReport.toJson(): String =
    """
    {
      "device": "$device",
      "abi": "$abi",
      "androidVersion": "$androidVersion",
      "speedup": ${speedup.fmt(6)},
      "sizeReduction": ${sizeReduction.fmt(6)},
      "memoryReduction": ${memoryReduction.fmt(6)},
      "baseline": ${baseline.toJson()},
      "optimized": ${optimized.toJson()}
    }
    """.trimIndent()

private fun BenchmarkSample.toJson(): String =
    """
    {
      "path": "$path",
      "coldStartMs": ${coldStartMs.fmt(4)},
      "warmLatencyMs": ${warmLatencyMs.fmt(8)},
      "throughputMessagesPerSecond": ${throughputMessagesPerSecond.fmt(2)},
      "modelSizeBytes": $modelSizeBytes,
      "peakMemoryKb": $peakMemoryKb,
      "earlyExits": $earlyExits
    }
    """.trimIndent()

private fun Double.fmt(digits: Int): String = "%.${digits}f".format(this)
