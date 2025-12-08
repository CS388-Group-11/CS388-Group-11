package com.example.studysync.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studysync.databinding.ItemStudyGroupBinding
import com.example.studysync.models.StudyGroup

class StudyGroupAdapter(
    private val currentUserId: String?,
    private val onJoinClicked: (StudyGroup) -> Unit,
    private val onLeaveClicked: (StudyGroup) -> Unit,
    private val onDeleteClicked: (StudyGroup) -> Unit
) : ListAdapter<StudyGroup, StudyGroupAdapter.StudyGroupViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyGroupViewHolder {
        val binding = ItemStudyGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudyGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyGroupViewHolder, position: Int) {
        val studyGroup = getItem(position)
        holder.bind(studyGroup)
    }

    inner class StudyGroupViewHolder(private val binding: ItemStudyGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(group: StudyGroup) {
            binding.topicTextView.text = group.topic
            binding.subjectTextView.text = group.courseCode
            binding.dateTimeTextView.text = group.date
            binding.timeTextView.text = group.time
            binding.locationTextView.text = group.location

            val isMember = group.members.contains(currentUserId)
            val isCreator = group.creatorUid == currentUserId

            if (isCreator) {
                binding.deleteButton.visibility = View.VISIBLE
                binding.leaveButton.visibility = View.GONE
                binding.joinButton.visibility = View.GONE

                binding.deleteButton.setOnClickListener { onDeleteClicked(group) }

            } else if (isMember) {
                binding.leaveButton.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.GONE
                binding.joinButton.visibility = View.GONE

                binding.leaveButton.setOnClickListener { onLeaveClicked(group) }

            } else {
                binding.joinButton.visibility = View.VISIBLE
                binding.leaveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.GONE

                binding.joinButton.setOnClickListener { onJoinClicked(group) }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<StudyGroup>() {
        override fun areItemsTheSame(oldItem: StudyGroup, newItem: StudyGroup): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: StudyGroup, newItem: StudyGroup): Boolean {
            return oldItem == newItem
        }
    }
}
