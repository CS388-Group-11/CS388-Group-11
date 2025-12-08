package com.example.studysync.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studysync.databinding.ItemStudyGroupBinding
import com.example.studysync.models.StudyGroup

// this adapter binds the StudyGroup data to the RecyclerView, displaying each group in the list.
class StudyGroupAdapter : ListAdapter<StudyGroup, StudyGroupAdapter.StudyGroupViewHolder>(StudyGroupDiffCallback()) {

    // Called by RecyclerView to create a new view holder when there are no existing ones to reuse.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyGroupViewHolder {
        // Inflate the XML layout for a single list item using ViewBinding.
        val binding = ItemStudyGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        // create a new ViewHolder instance with the inflated view.
        return StudyGroupViewHolder(binding)
    }

    // called by RecyclerView to display the data at a specific position.
    override fun onBindViewHolder(holder: StudyGroupViewHolder, position: Int) {
        // Get the data item for this position and bind it to the ViewHolder.
        holder.bind(getItem(position))
    }

    // holds the view for a single item in the list, avoiding expensive findViewById calls.
    inner class StudyGroupViewHolder(private val binding: ItemStudyGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // populates the views in the list item with data from a StudyGroup object.
        fun bind(group: StudyGroup) {
            binding.groupNameText.text = group.topic
            binding.groupSubjectText.text = group.courseCode
            binding.groupDescriptionText.text = group.location
            binding.groupDateText.text = group.date
            binding.groupTimeText.text = group.time
        }
    }
}

// helper class for ListAdapter to calculate the differences between two lists.
// enables efficient updates and animations.
class StudyGroupDiffCallback : DiffUtil.ItemCallback<StudyGroup>() {
    // checks they have the same unique ID
    override fun areItemsTheSame(oldItem: StudyGroup, newItem: StudyGroup): Boolean {
        return oldItem.id == newItem.id
    }

    // checks if the data within the same item has changed.
    override fun areContentsTheSame(oldItem: StudyGroup, newItem: StudyGroup): Boolean {
        return oldItem == newItem
    }
}
