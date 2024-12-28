package nir.wolff.ui.groups

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nir.wolff.R
import nir.wolff.databinding.ActivityGroupDetailsBinding
import nir.wolff.model.Group

class GroupDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    var groupId: String? = null // Public for fragments to access
    private var group: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        groupId = intent.getStringExtra("group_id")
        if (groupId == null) {
            finish()
            return
        }

        setupViewPager()
        loadGroup()
    }

    private fun setupViewPager() {
        val pagerAdapter = GroupDetailsPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Members"
                1 -> "Chat"
                else -> ""
            }
        }.attach()
    }

    private fun loadGroup() {
        firestore.collection("groups").document(groupId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    try {
                        group = Group.fromMap(document.data ?: return@addOnSuccessListener, document.id)
                        setupUI()
                        invalidateOptionsMenu() // Refresh menu to show/hide delete option
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing group", e)
                        Toast.makeText(this, "Error loading group details", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Group not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading group", e)
                Toast.makeText(this, "Failed to load group: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun setupUI() {
        group?.let { group ->
            title = group.name
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Only show delete option if current user is admin
        val currentUser = auth.currentUser?.email
        if (currentUser != null && group?.adminEmail == currentUser) {
            menuInflater.inflate(R.menu.menu_group_details, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_delete_group -> {
                showDeleteConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to delete this group? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteGroup()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteGroup() {
        val currentUser = auth.currentUser?.email ?: return
        val currentGroup = group ?: return

        if (currentGroup.adminEmail != currentUser) {
            Toast.makeText(this, "Only group admin can delete the group", Toast.LENGTH_SHORT).show()
            return
        }

        // Start deletion process
        binding.root.isEnabled = false // Disable UI during deletion

        // Delete all alerts for this group
        firestore.collection("alerts")
            .whereEqualTo("groupId", groupId)
            .get()
            .addOnSuccessListener { alertsSnapshot ->
                // Create a batch for deleting alerts
                val batch = firestore.batch()
                alertsSnapshot.documents.forEach { doc ->
                    batch.delete(firestore.collection("alerts").document(doc.id))
                }

                // Delete all chats for this group
                firestore.collection("chats")
                    .whereEqualTo("groupId", groupId)
                    .get()
                    .addOnSuccessListener { chatsSnapshot ->
                        // Add chat deletions to the same batch
                        chatsSnapshot.documents.forEach { doc ->
                            batch.delete(firestore.collection("chats").document(doc.id))
                        }

                        // Add group deletion to the batch
                        batch.delete(firestore.collection("groups").document(groupId!!))

                        // Execute all deletions in a single batch
                        batch.commit()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                binding.root.isEnabled = true
                                Log.e(TAG, "Error executing batch delete", e)
                                Toast.makeText(this, "Failed to delete group: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        binding.root.isEnabled = true
                        Log.e(TAG, "Error fetching chats", e)
                        Toast.makeText(this, "Failed to delete group: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.root.isEnabled = true
                Log.e(TAG, "Error fetching alerts", e)
                Toast.makeText(this, "Failed to delete group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val TAG = "GroupDetailsActivity"
    }
}

class GroupDetailsPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> GroupMembersFragment()
            1 -> GroupChatFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}
