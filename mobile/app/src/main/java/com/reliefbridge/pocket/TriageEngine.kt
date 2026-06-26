package com.reliefbridge.pocket

import kotlin.math.exp
import kotlin.math.max

class TriageEngine(
    private val model: ModelSpec,
    private val optimized: Boolean,
) {
    private val tokenCache = mutableMapOf<String, List<String>>()
    private val categoryKeywords = mapOf(
        "medical" to setOf("insulin", "clinic", "cooler", "medical", "fridge", "medication", "nurse", "hospital", "injury", "oxygen"),
        "transport" to setOf("transport", "wheelchair", "van", "vans", "pickup", "road", "vehicle", "driver", "ambulance", "bus"),
        "food" to setOf("meal", "meals", "kitchen", "food", "drop-off", "hungry", "water", "supplies", "allergy", "dietary"),
        "shelter" to setOf("shelter", "gym", "evacuee", "apartments", "building", "floor", "rooms", "cots", "blankets", "overnight"),
        "logistics" to setOf("coordinate", "list", "map", "assign", "schedule", "plan", "status", "update", "confirm", "ready"),
    )
    private val urgentKeywords = setOf("urgent", "need", "now", "before", "sensitive", "time", "critical", "immediate", "emergency", "asap")

    // Channel name priors: some channels strongly suggest message type
    private val channelPriors = mapOf(
        "flood-response" to floatArrayOf(0.8f, -0.2f, 0.0f, -0.4f),
        "medical" to floatArrayOf(1.0f, 0.2f, 0.0f, -0.5f),
        "volunteers" to floatArrayOf(-0.3f, 1.2f, 0.0f, -0.3f),
        "shelter-ops" to floatArrayOf(0.3f, 0.6f, 0.2f, -0.4f),
        "field-intake" to floatArrayOf(0.5f, 0.0f, 0.0f, -0.3f),
    )

    // When score gap exceeds this threshold in optimized path, bail early
    private val earlyExitGap = 4.5f

    var earlyExitCount = 0
        private set

    fun classify(message: ReliefMessage): Classification {
        val tokens = tokenize(message.text)
        val scores = model.bias.copyOf()

        if (optimized) {
            channelPriors[message.channelName]?.forEachIndexed { i, v -> scores[i] += v }
            val exited = scoreOptimized(tokens, scores)
            if (exited) earlyExitCount++
        } else {
            scoreBaseline(tokens, scores)
        }

        val bestIndex = scores.indices.maxBy { scores[it] }
        val confidence = softmaxConfidence(scores, bestIndex)
        return Classification(
            label = model.labels[bestIndex],
            confidence = confidence,
            category = detectCategory(tokens),
            urgency = scoreUrgency(message.text, tokens),
        )
    }

    fun buildActionQueue(
        messages: List<ReliefMessage>,
        directory: ReliefDirectory,
    ): List<ActionItem> =
        messages
            .map { message ->
                val classification = classify(message)
                ActionItem(
                    message = message,
                    classification = classification,
                    volunteer = directory.volunteers.bestFor(classification.category),
                    resource = directory.resources.bestFor(classification.category),
                    recommendedAction = actionFor(classification, directory),
                )
            }
            .filter { it.classification.label != "noise" }
            .sortedWith(
                compareByDescending<ActionItem> { it.classification.label == "need" }
                    .thenByDescending { it.classification.urgency }
                    .thenByDescending { it.classification.confidence },
            )

    private fun scoreBaseline(tokens: List<String>, scores: FloatArray) {
        for (token in tokens) {
            model.weights[token]?.forEachIndexed { i, v -> scores[i] += v }
        }
    }

    /** Returns true if early-exit fired (only used in optimized path). */
    private fun scoreOptimized(tokens: List<String>, scores: FloatArray): Boolean {
        val seen = HashSet<String>(tokens.size)
        for (token in tokens) {
            if (!seen.add(token)) continue
            val weight = model.weights[token] ?: continue
            for (i in scores.indices) scores[i] += weight[i]
            // Early-exit once top label is unambiguously clear
            val sorted = scores.sortedDescending()
            if (sorted[0] - sorted[1] > earlyExitGap) return true
        }
        return false
    }

    private fun tokenize(text: String): List<String> {
        if (optimized) tokenCache[text]?.let { return it }
        val tokens = text
            .lowercase()
            .replace(Regex("[^a-z0-9\\- ]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
        if (optimized) tokenCache[text] = tokens
        return tokens
    }

    private fun detectCategory(tokens: List<String>): String {
        var bestCategory = "logistics"
        var bestScore = 0
        for ((category, keywords) in categoryKeywords) {
            val score = tokens.count { it in keywords }
            if (score > bestScore) {
                bestScore = score
                bestCategory = category
            }
        }
        return bestCategory
    }

    private fun scoreUrgency(text: String, tokens: List<String>): Int {
        val tokenHits = tokens.count { it in urgentKeywords }
        val phraseHits = listOf("second floor", "road closes", "time sensitive", "before dark", "runs out")
            .count { text.contains(it, ignoreCase = true) }
        return max(1, tokenHits + phraseHits).coerceAtMost(5)
    }

    private fun softmaxConfidence(scores: FloatArray, bestIndex: Int): Float {
        val maxScore = scores.maxOrNull() ?: 0f
        val expScores = scores.map { exp((it - maxScore).toDouble()) }
        val total = expScores.sum().coerceAtLeast(0.0001)
        return (expScores[bestIndex] / total).toFloat()
    }

    private fun actionFor(classification: Classification, directory: ReliefDirectory): String {
        val volunteer = directory.volunteers.bestFor(classification.category)
        val resource = directory.resources.bestFor(classification.category)
        return when (classification.label) {
            "need" -> "Claim with ${volunteer?.name ?: "nearest coordinator"} · ${resource?.name ?: "check supplies"}"
            "offer" -> "Attach offer to open ${classification.category} cases · confirm availability"
            "update" -> "Log update · notify coordinator channel"
            else -> "No action required"
        }
    }
}

private fun List<Resource>.bestFor(category: String): Resource? =
    firstOrNull { it.type == category } ?: firstOrNull()

private fun List<Volunteer>.bestFor(category: String): Volunteer? =
    firstOrNull { category in it.skills } ?: firstOrNull()
