package nir.wolff.model

import android.util.Log

data class GroupMember(
    val email: String = "",
    val status: Status = Status.UNKNOWN
) {
    constructor() : this("", Status.UNKNOWN)

    companion object {
        private const val TAG = "GroupMember"

        enum class Status {
            UNKNOWN, SAFE, UNSAFE
        }

        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>): GroupMember {
            Log.d(TAG, "Converting map to GroupMember: $map")
            return GroupMember(
                email = (map["email"] as? String ?: "").also { 
                    if (it.isEmpty()) Log.w(TAG, "Empty email in map: $map") 
                },
                status = Status.valueOf((map["status"] as? String ?: "UNKNOWN").toUpperCase())
            ).also {
                Log.d(TAG, "Created GroupMember object: $it")
            }
        }
    }

    fun toMap(): Map<String, Any> {
        Log.d(TAG, "Converting member to map: $this")
        return mapOf(
            "email" to email,
            "status" to status.name
        ).also {
            Log.d(TAG, "Converted map: $it")
        }
    }

    fun isSafe(): Boolean = status == Status.SAFE
    fun isUnsafe(): Boolean = status == Status.UNSAFE
    fun needsHelp(): Boolean = status == Status.UNSAFE
}
