package nir.wolff.model

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot

data class Group(
    var id: String = "",
    val name: String = "",
    val adminEmail: String = "",
    val members: List<GroupMember> = emptyList(),
    val memberEmails: List<String> = emptyList(),
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        private const val TAG = "Group"

        fun fromDocument(doc: DocumentSnapshot): Group? {
            return try {
                val data = doc.data ?: return null
                Log.d(TAG, "Converting document to group. Raw data: $data")

                val membersData = data["members"] as? List<Map<String, Any>> ?: emptyList()
                val members = membersData.mapNotNull { memberMap ->
                    try {
                        GroupMember.fromMap(memberMap)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting member map: $memberMap", e)
                        null
                    }
                }

                val memberEmails = members.map { it.email }

                Group(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    adminEmail = data["adminEmail"] as? String ?: "",
                    members = members,
                    memberEmails = memberEmails,
                    createdBy = data["createdBy"] as? String ?: "",
                    createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis()
                ).also {
                    Log.d(TAG, "Successfully converted to group: $it")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error converting document to group", e)
                null
            }
        }

        fun fromMap(data: Map<String, Any>, id: String): Group {
            val members = (data["members"] as? List<Map<String, Any>>)?.map {
                GroupMember.fromMap(it)
            } ?: emptyList()

            val memberEmails = members.map { it.email }

            return Group(
                id = id,
                name = data["name"] as? String ?: "",
                adminEmail = data["adminEmail"] as? String ?: "",
                members = members,
                memberEmails = memberEmails,
                createdBy = data["createdBy"] as? String ?: "",
                createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis()
            )
        }
    }

    fun toMap(): Map<String, Any> {
        Log.d(TAG, "Converting group to map: $this")
        return mapOf(
            "name" to name,
            "adminEmail" to adminEmail,
            "members" to members.map { it.toMap() },
            "memberEmails" to memberEmails,
            "createdBy" to createdBy,
            "createdAt" to createdAt
        ).also {
            Log.d(TAG, "Converted map: $it")
        }
    }
}
