package com.reliefbridge.pocket

data class ReliefMessage(
    val id: String,
    val channelName: String,
    val user: String,
    val text: String,
    val timestamp: String,
)

data class Volunteer(
    val id: String,
    val name: String,
    val skills: List<String>,
    val location: String,
    val availability: String,
    val capacity: Int,
)

data class Resource(
    val id: String,
    val type: String,
    val name: String,
    val location: String,
    val quantity: Int,
    val unit: String,
    val owner: String,
)

data class ReliefDirectory(
    val volunteers: List<Volunteer>,
    val resources: List<Resource>,
)

data class ModelSpec(
    val name: String,
    val format: String,
    val scale: Float,
    val labels: List<String>,
    val bias: FloatArray,
    val weights: Map<String, FloatArray>,
    val sizeBytes: Long,
)

data class Classification(
    val label: String,
    val confidence: Float,
    val category: String,
    val urgency: Int,
)

enum class ClaimStatus { OPEN, CLAIMED, RESOLVED }

data class ActionItem(
    val message: ReliefMessage,
    val classification: Classification,
    val volunteer: Volunteer?,
    val resource: Resource?,
    val recommendedAction: String,
)

data class BenchmarkSample(
    val path: String,
    val coldStartMs: Double,
    val warmLatencyMs: Double,
    val throughputMessagesPerSecond: Double,
    val modelSizeBytes: Long,
    val peakMemoryKb: Long,
    val earlyExits: Int = 0,
)

data class BenchmarkReport(
    val device: String,
    val abi: String,
    val androidVersion: String,
    val baseline: BenchmarkSample,
    val optimized: BenchmarkSample,
    val speedup: Double,
    val sizeReduction: Double,
    val memoryReduction: Double,
)
