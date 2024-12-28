package nir.wolff.ui.groups

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nir.wolff.R
import nir.wolff.databinding.FragmentGroupMembersBinding
import nir.wolff.model.Group
import nir.wolff.model.GroupMember
import nir.wolff.ui.groups.adapters.GroupMembersAdapter

class GroupMembersFragment : Fragment() {
    private var _binding: FragmentGroupMembersBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentGroup: Group? = null
    private val membersAdapter = GroupMembersAdapter { member ->
        if (currentGroup?.adminEmail == auth.currentUser?.email) {
            showRemoveMemberConfirmation(member)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupAddMemberButton()
        loadGroup()
    }

    private fun setupRecyclerView() {
        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }
    }

    private fun setupAddMemberButton() {
        // Initially hide the FAB - will show only for admin
        binding.addMemberFab.visibility = View.GONE
        
        binding.addMemberFab.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_member, null)
            val emailInput = dialogView.findViewById<TextInputEditText>(R.id.emailInput)
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Member")
                .setView(dialogView)
                .setPositiveButton("Add") { dialogInterface: DialogInterface, _: Int ->
                    val email = emailInput.text.toString().trim()
                    if (email.isNotEmpty()) {
                        addMemberToGroup(email)
                    }
                    dialogInterface.dismiss()
                }
                .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
                .show()
        }
    }

    private fun loadGroup() {
        val groupId = (activity as? GroupDetailsActivity)?.groupId ?: return
        
        firestore.collection("groups").document(groupId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error loading group", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        currentGroup = snapshot.toObject(Group::class.java)?.apply { 
                            id = snapshot.id
                        }
                        currentGroup?.let { group ->
                            updateUIForAdminStatus(group)
                            membersAdapter.submitList(group.members)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing group", e)
                        Toast.makeText(requireContext(), "Error loading group details", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun updateUIForAdminStatus(group: Group) {
        val isAdmin = auth.currentUser?.email == group.adminEmail
        binding.addMemberFab.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    private fun showRemoveMemberConfirmation(member: GroupMember) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Member")
            .setMessage("Are you sure you want to remove ${member.email} from the group?")
            .setPositiveButton("Remove") { dialog, _ ->
                removeMember(member)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun addMemberToGroup(email: String) {
        val groupId = (activity as? GroupDetailsActivity)?.groupId ?: return
        val group = currentGroup ?: return

        // Don't add if already a member
        if (group.members.any { it.email.equals(email, ignoreCase = true) }) {
            Toast.makeText(context, "Member already exists in the group", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create a new member document
        val member = GroupMember(
            email = email,
            status = GroupMember.Status.PENDING
        )
        
        // Update the group document
        val updatedMembers = group.members + member
        val updates = mapOf(
            "members" to updatedMembers.map { it.toMap() },
            "memberEmails" to updatedMembers.map { it.email }
        )

        firestore.collection("groups")
            .document(groupId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Member added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to add member: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeMember(member: GroupMember) {
        val group = currentGroup ?: return
        
        // Don't allow removing the admin
        if (member.email == group.adminEmail) {
            Toast.makeText(requireContext(), "Cannot remove group admin", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedMembers = group.members.filter { it.email != member.email }
        val updates = mapOf(
            "members" to updatedMembers.map { it.toMap() },
            "memberEmails" to updatedMembers.map { it.email }
        )

        firestore.collection("groups")
            .document(group.id)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Member removed successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error removing member", e)
                Toast.makeText(requireContext(), "Failed to remove member: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "GroupMembersFragment"
    }
}
