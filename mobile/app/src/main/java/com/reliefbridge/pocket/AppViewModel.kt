package com.reliefbridge.pocket

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AssetRepository(application)

    val messages: List<ReliefMessage> = repository.loadMessages()
    val directory: ReliefDirectory = repository.loadDirectory()

    private val optimizedModel = repository.loadModel("optimized_model.json")
    private val engine = TriageEngine(optimizedModel, optimized = true)

    var inputText by mutableStateOf(
        "URGENT: Need wheelchair-accessible transport for 3 seniors at Riverside Apartments before road closes.",
    )
        private set

    var actionQueue by mutableStateOf<List<ActionItem>>(emptyList())
        private set

    var claimedIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var resolvedIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var benchmarkReport by mutableStateOf<BenchmarkReport?>(null)
        private set

    var isBenchmarking by mutableStateOf(false)
        private set

    var benchmarkError by mutableStateOf<String?>(null)
        private set

    init {
        reclassify()
    }

    fun updateInput(text: String) {
        inputText = text
        reclassify()
    }

    fun claim(id: String) {
        claimedIds = claimedIds + id
        resolvedIds = resolvedIds - id
    }

    fun resolve(id: String) {
        resolvedIds = resolvedIds + id
        claimedIds = claimedIds - id
    }

    fun resetStatus(id: String) {
        claimedIds = claimedIds - id
        resolvedIds = resolvedIds - id
    }

    fun runBenchmark() {
        if (isBenchmarking) return
        isBenchmarking = true
        benchmarkError = null

        viewModelScope.launch(Dispatchers.Default) {
            val result = runCatching { BenchmarkRunner(repository).run(messages) }
            withContext(Dispatchers.Main) {
                isBenchmarking = false
                result
                    .onSuccess { benchmarkReport = it }
                    .onFailure { benchmarkError = it.message ?: "Benchmark failed" }
            }
        }
    }

    fun statusOf(id: String): ClaimStatus = when {
        resolvedIds.contains(id) -> ClaimStatus.RESOLVED
        claimedIds.contains(id) -> ClaimStatus.CLAIMED
        else -> ClaimStatus.OPEN
    }

    private fun reclassify() {
        val custom = ReliefMessage(
            id = "custom",
            channelName = "field-intake",
            user = "Coordinator",
            text = inputText,
            timestamp = "now",
        )
        actionQueue = engine.buildActionQueue(listOf(custom) + messages, directory)
    }
}
