package nir.wolff.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import nir.wolff.R
import nir.wolff.databinding.ActivityMainBinding
import nir.wolff.model.AlertEvent
import nir.wolff.model.Group
import nir.wolff.model.GroupMember
import nir.wolff.ui.auth.SignInActivity
import nir.wolff.ui.groups.CreateGroupActivity
import nir.wolff.ui.groups.GroupAdapter
import nir.wolff.ui.groups.GroupDetailsActivity
import nir.wolff.ui.map.MapActivity
import nir.wolff.ui.profile.ProfileActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var groupAdapter: GroupAdapter
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var groupsListener: ListenerRegistration? = null

    companion object {
        private const val TAG = "MainActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupFab()
        setupSignInButton()
        setupSignOutButton()
        checkAuthState()
    }

    private fun setupSignInButton() {
        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun setupSignOutButton() {
        binding.signOutButton.setOnClickListener {
            auth.signOut()
            checkAuthState()
        }
    }

    private fun updateUIForAuthState(isSignedIn: Boolean) {
        if (isSignedIn) {
            val user = auth.currentUser
            binding.welcomeText.text = "Welcome to Safety Alert"
            binding.userEmailText.text = user?.email ?: ""
            binding.welcomeCard.visibility = View.VISIBLE
            binding.signInButton.visibility = View.GONE
            binding.signOutButton.visibility = View.VISIBLE
            binding.createGroupFab.visibility = View.VISIBLE
            binding.groupsRecyclerView.visibility = View.VISIBLE
            binding.bottomActions.visibility = View.VISIBLE
        } else {
            binding.welcomeCard.visibility = View.GONE
            binding.signInButton.visibility = View.VISIBLE
            binding.signOutButton.visibility = View.GONE
            binding.createGroupFab.visibility = View.GONE
            binding.groupsRecyclerView.visibility = View.GONE
            binding.bottomActions.visibility = View.GONE
        }
        binding.noGroupsText.visibility = View.GONE
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        Log.d(TAG, "Checking auth state. Current user: ${currentUser?.email}")
        
        if (currentUser == null) {
            updateUIForAuthState(false)
            return
        }

        updateUIForAuthState(true)
        loadGroups()
    }

    override fun onResume() {
        super.onResume()
        checkAuthState()
    }

    private fun setupRecyclerView() {
        groupAdapter = GroupAdapter(
            onGroupClick = { group ->
                val intent = Intent(this, GroupDetailsActivity::class.java).apply {
                    putExtra("group_id", group.id)
                }
                startActivity(intent)
            },
            onMemberClick = { group, member ->
                // Handle member click if needed
            },
            onSafetyStatusChange = { group, isSafe ->
                updateSafetyStatus(group, isSafe)
            },
            onMapClick = { group ->
                val intent = Intent(this, MapActivity::class.java).apply {
                    putExtra("group_id", group.id)
                }
                startActivity(intent)
            },
            onDeleteGroup = { group ->
                showDeleteGroupConfirmation(group)
            }
        )

        binding.groupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = groupAdapter
            addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupFab() {
        binding.createGroupFab.setOnClickListener {
            startActivity(Intent(this, CreateGroupActivity::class.java))
        }
    }

    private fun loadGroups() {
        val currentUser = auth.currentUser?.email ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.groupsRecyclerView.visibility = View.GONE
        binding.noGroupsText.visibility = View.GONE

        firestore.collection("groups")
            .whereArrayContains("memberEmails", currentUser)
            .get()
            .addOnSuccessListener { snapshot ->
                val groups = snapshot.documents.mapNotNull { doc ->
                    try {
                        Group.fromMap(doc.data ?: return@mapNotNull null, doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing group", e)
                        null
                    }
                }

                binding.progressBar.visibility = View.GONE
                if (groups.isEmpty()) {
                    binding.noGroupsText.visibility = View.VISIBLE
                    binding.groupsRecyclerView.visibility = View.GONE
                } else {
                    binding.noGroupsText.visibility = View.GONE
                    binding.groupsRecyclerView.visibility = View.VISIBLE
                    groupAdapter.submitList(groups)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading groups", e)
                binding.progressBar.visibility = View.GONE
                binding.noGroupsText.visibility = View.VISIBLE
                binding.groupsRecyclerView.visibility = View.GONE
                Toast.makeText(this, "Failed to load groups: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateSafetyStatus(group: Group, isSafe: Boolean) {
        val currentUser = auth.currentUser ?: return
        val userEmail = currentUser.email ?: return

        // Get current location
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            // Create alert event with location
            val alertEvent = AlertEvent(
                groupId = group.id,
                userEmail = userEmail,
                eventType = if (isSafe) AlertEvent.EventType.ALL_CLEAR else AlertEvent.EventType.NEED_HELP,
                message = if (isSafe) "I'm safe" else "I need help",
                latitude = location?.latitude,
                longitude = location?.longitude
            )

            // Add the alert event
            firestore.collection("alerts")
                .add(alertEvent.toMap())
                .addOnSuccessListener { docRef ->
                    Log.d(TAG, "Alert event added with ID: ${docRef.id}")
                    
                    // Update member status
                    val updatedMembers = group.members.map { member ->
                        if (member.email == userEmail) {
                            member.copy(status = if (isSafe) 
                                GroupMember.Companion.Status.SAFE 
                            else 
                                GroupMember.Companion.Status.UNSAFE
                            )
                        } else {
                            member
                        }
                    }

                    val updatedGroup = group.copy(members = updatedMembers)
                    
                    firestore.collection("groups").document(group.id)
                        .set(updatedGroup.toMap())
                        .addOnSuccessListener {
                            binding.progressBar.visibility = View.GONE
                            Log.d(TAG, "Safety status updated for user $userEmail in group ${group.name}")
                            loadGroups() // Refresh the list
                        }
                        .addOnFailureListener { e ->
                            binding.progressBar.visibility = View.GONE
                            Log.e(TAG, "Error updating safety status", e)
                            Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    Log.e(TAG, "Error adding alert event", e)
                    Toast.makeText(this, "Failed to create alert: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showDeleteGroupConfirmation(group: Group) {
        val currentUser = auth.currentUser
        if (currentUser?.email != group.adminEmail) {
            Toast.makeText(this, "Only the group admin can delete the group", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to delete ${group.name}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteGroup(group)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteGroup(group: Group) {
        val batch = firestore.batch()
        
        // Delete all alerts for this group
        firestore.collection("alerts")
            .whereEqualTo("groupId", group.id)
            .get()
            .addOnSuccessListener { alertsSnapshot ->
                try {
                    // Add alert deletions to batch
                    for (alertDoc in alertsSnapshot.documents) {
                        batch.delete(alertDoc.reference)
                    }
                    
                    // Delete the group document
                    batch.delete(firestore.collection("groups").document(group.id))
                    
                    // Execute all deletions in a single batch
                    batch.commit()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error deleting group", e)
                            Toast.makeText(this, "Failed to delete group: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error preparing group deletion", e)
                    Toast.makeText(this, "Failed to prepare group deletion: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error querying alerts for deletion", e)
                Toast.makeText(this, "Failed to query alerts: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_map -> {
                // Open map without a specific group
                startActivity(Intent(this, MapActivity::class.java))
                true
            }
            R.id.action_logout -> {
                auth.signOut()
                checkAuthState()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        groupsListener?.remove()
    }
}
