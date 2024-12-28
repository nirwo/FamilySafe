package nir.wolff.model

import java.io.Serializable

data class AlertEvent(
    val id: String = "",
    val groupId: String = "",
    val userEmail: String = "",
    val eventType: EventType = EventType.NEED_HELP,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    enum class EventType {
        NEED_HELP,
        ALL_CLEAR
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf(
            "groupId" to groupId,
            "userEmail" to userEmail,
            "eventType" to eventType.name,
            "message" to message,
            "timestamp" to timestamp
        )
        if (latitude != null && longitude != null) {
            map["latitude"] = latitude
            map["longitude"] = longitude
        }
        return map
    }

    companion object {
        fun fromMap(data: Map<String, Any>, id: String): AlertEvent {
            return AlertEvent(
                id = id,
                groupId = data["groupId"] as? String ?: "",
                userEmail = data["userEmail"] as? String ?: "",
                eventType = try {
                    EventType.valueOf(data["eventType"] as? String ?: EventType.NEED_HELP.name)
                } catch (e: IllegalArgumentException) {
                    EventType.NEED_HELP
                },
                message = data["message"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                latitude = (data["latitude"] as? Number)?.toDouble(),
                longitude = (data["longitude"] as? Number)?.toDouble()
            )
        }
    }
}
