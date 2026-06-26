package com.reliefbridge.pocket

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

@Composable
fun BenchmarkScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item { InfoCard() }

        item {
            RunCard(
                isRunning = vm.isBenchmarking,
                error = vm.benchmarkError,
                onRun = vm::runBenchmark,
            )
        }

        vm.benchmarkReport?.let { report ->
            item { SummaryCard(report) }
            item { MetricsCard(report) }
            if (report.optimized.earlyExits > 0) {
                item { EarlyExitCard(report) }
            }
            item {
                ExportCard(
                    onExport = { exportBenchmark(context, report) },
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun InfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7F3)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Arm Optimization Benchmark",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Compares a baseline FP32 model path against an optimized INT8 path with token caching, unique-token scoring, channel priors, and early-exit on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF555555),
            )
        }
    }
}

@Composable
private fun RunCard(isRunning: Boolean, error: String?, onRun: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onRun,
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF245B45)),
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Running…")
                    } else {
                        Text("Run benchmark")
                    }
                }
                if (isRunning) {
                    Text(
                        text = "300 rounds × 12 messages · please wait",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888),
                    )
                }
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color(0xFFD32F2F), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SummaryCard(report: BenchmarkReport) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF245B45)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryMetric(value = "${report.speedup.fmt(2)}×", label = "faster")
            Divider()
            SummaryMetric(value = "${(report.sizeReduction * 100).fmt(1)}%", label = "smaller model")
            Divider()
            SummaryMetric(
                value = report.abi.ifBlank { "Arm" },
                label = "ABI",
            )
        }
    }
}

@Composable
private fun SummaryMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFFB0D4C0), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .width(1.dp)
            .height(40.dp)
            .background(Color(0xFF3D7A5E)),
    )
}

@Composable
private fun MetricsCard(report: BenchmarkReport) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text = "Metric breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            ComparisonBar(
                label = "Warm latency",
                unit = "ms/msg",
                baselineValue = report.baseline.warmLatencyMs,
                optimizedValue = report.optimized.warmLatencyMs,
                higherIsBetter = false,
                format = { "%.4f".format(it) },
            )

            ComparisonBar(
                label = "Throughput",
                unit = "msg/s",
                baselineValue = report.baseline.throughputMessagesPerSecond,
                optimizedValue = report.optimized.throughputMessagesPerSecond,
                higherIsBetter = true,
                format = { "%.0f".format(it) },
            )

            ComparisonBar(
                label = "Cold start",
                unit = "ms",
                baselineValue = report.baseline.coldStartMs,
                optimizedValue = report.optimized.coldStartMs,
                higherIsBetter = false,
                format = { "%.2f".format(it) },
            )

            ComparisonBar(
                label = "Model size",
                unit = "B",
                baselineValue = report.baseline.modelSizeBytes.toDouble(),
                optimizedValue = report.optimized.modelSizeBytes.toDouble(),
                higherIsBetter = false,
                format = { "%.0f".format(it) },
            )

            if (report.baseline.peakMemoryKb > 0 || report.optimized.peakMemoryKb > 0) {
                ComparisonBar(
                    label = "Memory Δ",
                    unit = "KB",
                    baselineValue = report.baseline.peakMemoryKb.toDouble().coerceAtLeast(1.0),
                    optimizedValue = report.optimized.peakMemoryKb.toDouble().coerceAtLeast(1.0),
                    higherIsBetter = false,
                    format = { "%.0f".format(it) },
                )
            }

            DeviceRow(report)
        }
    }
}

@Composable
private fun ComparisonBar(
    label: String,
    unit: String,
    baselineValue: Double,
    optimizedValue: Double,
    higherIsBetter: Boolean,
    format: (Double) -> String,
) {
    val max = maxOf(baselineValue, optimizedValue).coerceAtLeast(0.001)
    val baselineFraction = (baselineValue / max).toFloat().coerceIn(0.05f, 1f)
    val optimizedFraction = (optimizedValue / max).toFloat().coerceIn(0.05f, 1f)

    val optimizedIsWinner = if (higherIsBetter) optimizedValue >= baselineValue else optimizedValue <= baselineValue

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$label ($unit)",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF888888),
            fontWeight = FontWeight.SemiBold,
        )
        BarRow(
            label = "Baseline",
            fraction = baselineFraction,
            valueText = format(baselineValue),
            color = Color(0xFFBBBBBB),
        )
        BarRow(
            label = "Optimized",
            fraction = optimizedFraction,
            valueText = format(optimizedValue),
            color = if (optimizedIsWinner) Color(0xFF245B45) else Color(0xFFD96C2C),
        )
    }
}

@Composable
private fun BarRow(label: String, fraction: Float, valueText: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF555555),
        )
        Box(
            Modifier
                .weight(1f)
                .height(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF0F0F0)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color),
            )
            Text(
                text = valueText,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (fraction > 0.55f) Color.White else Color(0xFF333333),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EarlyExitCard(report: BenchmarkReport) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFA000))
                    .align(Alignment.CenterVertically),
            )
            Text(
                text = "${report.optimized.earlyExits} of ${report.optimized.earlyExits + (300 * 12 - report.optimized.earlyExits)} classifications used early-exit — token scanning stopped once label confidence exceeded threshold, reducing unnecessary compute.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5D4037),
            )
        }
    }
}

@Composable
private fun DeviceRow(report: BenchmarkReport) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Device info", style = MaterialTheme.typography.labelSmall, color = Color(0xFF888888))
        Text(
            text = "${report.device} · Android ${report.androidVersion} · ${report.abi}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF555555),
        )
    }
}

@Composable
private fun ExportCard(onExport: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Export proof",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Share benchmark-results.md and benchmark-results.json as hackathon judging artifacts.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF555555),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onExport) {
                Text("Export & share proof")
            }
        }
    }
}

private fun exportBenchmark(context: Context, report: BenchmarkReport) {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val jsonFile = File(exportDir, "benchmark-results.json")
    val markdownFile = File(exportDir, "benchmark-results.md")
    jsonFile.writeText(report.toJson())
    markdownFile.writeText(report.toMarkdown())

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", markdownFile)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/markdown"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, "Share benchmark proof").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

private fun Double.fmt(digits: Int) = "%.${digits}f".format(this)
