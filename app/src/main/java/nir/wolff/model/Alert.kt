package nir.wolff.model

data class Alert(
    val id: String = "",
    val groupId: String = "",
    val userEmail: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0,
    val type: AlertType = AlertType.UNKNOWN
) {
    enum class AlertType {
        SAFE,
        UNSAFE,
        UNKNOWN
    }
}
