package nir.wolff.ui.groups

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nir.wolff.databinding.ActivityCreateGroupBinding
import nir.wolff.model.Group
import nir.wolff.model.GroupMember

class CreateGroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGroupBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "CreateGroupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar as Toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create New Group"

        binding.createButton.setOnClickListener {
            if (!binding.createButton.isEnabled) {
                Log.d(TAG, "Create button clicked but disabled")
                return@setOnClickListener
            }
            Log.d(TAG, "Create button clicked")
            createGroup()
        }
    }

    private fun createGroup() {
        val groupName = binding.groupNameInput.text.toString().trim()
        if (groupName.isEmpty()) {
            binding.groupNameInput.error = "Group name is required"
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to create a group", Toast.LENGTH_SHORT).show()
            return
        }

        val userEmail = currentUser.email ?: return

        val group = Group(
            name = groupName,
            adminEmail = userEmail,
            members = listOf(
                GroupMember(
                    email = userEmail,
                    status = GroupMember.Companion.Status.UNKNOWN
                )
            ),
            createdBy = userEmail
        )

        firestore.collection("groups")
            .add(group.toMap())
            .addOnSuccessListener {
                Toast.makeText(this, "Group created successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
