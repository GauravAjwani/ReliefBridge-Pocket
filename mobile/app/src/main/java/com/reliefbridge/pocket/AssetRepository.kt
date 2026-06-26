package com.reliefbridge.pocket

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AssetRepository(private val context: Context) {
    fun loadMessages(): List<ReliefMessage> {
        val json = JSONArray(readAsset("demo_messages.json"))
        return (0 until json.length()).map { index ->
            val item = json.getJSONObject(index)
            ReliefMessage(
                id = item.getString("id"),
                channelName = item.getString("channelName"),
                user = item.getString("user"),
                text = item.getString("text"),
                timestamp = item.getString("timestamp"),
            )
        }
    }

    fun loadDirectory(): ReliefDirectory {
        val json = JSONObject(readAsset("relief_directory.json"))
        return ReliefDirectory(
            volunteers = json.getJSONArray("volunteers").mapObjects { item ->
                Volunteer(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    skills = item.getJSONArray("skills").toStringList(),
                    location = item.getString("location"),
                    availability = item.getString("availability"),
                    capacity = item.getInt("capacity"),
                )
            },
            resources = json.getJSONArray("resources").mapObjects { item ->
                Resource(
                    id = item.getString("id"),
                    type = item.getString("type"),
                    name = item.getString("name"),
                    location = item.getString("location"),
                    quantity = item.getInt("quantity"),
                    unit = item.getString("unit"),
                    owner = item.getString("owner"),
                )
            },
        )
    }

    fun loadModel(assetName: String): ModelSpec {
        val raw = readAsset(assetName)
        val json = JSONObject(raw)
        val scale = json.optDouble("scale", 1.0).toFloat()
        val labels = json.getJSONArray("labels").toStringList()
        val weights = json.getJSONObject("weights")
        return ModelSpec(
            name = json.getString("name"),
            format = json.getString("format"),
            scale = scale,
            labels = labels,
            bias = json.getJSONArray("bias").toFloatArray(scale),
            weights = weights.keys().asSequence().associateWith { key ->
                weights.getJSONArray(key).toFloatArray(scale)
            },
            sizeBytes = raw.toByteArray(Charsets.UTF_8).size.toLong(),
        )
    }

    private fun readAsset(name: String): String =
        context.assets.open(name).bufferedReader().use { it.readText() }
}

private fun JSONArray.toStringList(): List<String> =
    (0 until length()).map { index -> getString(index) }

private fun JSONArray.toFloatArray(scale: Float): FloatArray =
    FloatArray(length()) { index -> getDouble(index).toFloat() * scale }

private fun <T> JSONArray.mapObjects(block: (JSONObject) -> T): List<T> =
    (0 until length()).map { index -> block(getJSONObject(index)) }
