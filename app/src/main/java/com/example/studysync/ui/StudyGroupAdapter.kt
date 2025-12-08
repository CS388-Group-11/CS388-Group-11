package com.example.studysync.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studysync.databinding.ItemStudyGroupBinding
import com.example.studysync.models.StudyGroup

class StudyGroupAdapter : ListAdapter<StudyGroup, StudyGroupAdapter.StudyGroupViewHolder>(StudyGroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyGroupViewHolder {
        val binding = ItemStudyGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StudyGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyGroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudyGroupViewHolder(private val binding: ItemStudyGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: StudyGroup) {
            binding.groupNameText.text = group.topic
            binding.groupSubjectText.text = group.courseCode
            binding.groupDescriptionText.text = group.location
            binding.groupDateText.text = group.date
            binding.groupTimeText.text = group.time
        }
    }
}

class StudyGroupDiffCallback : DiffUtil.ItemCallback<StudyGroup>() {
    override fun areItemsTheSame(oldItem: StudyGroup, newItem: StudyGroup): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StudyGroup, newItem: StudyGroup): Boolean {
        return oldItem == newItem
    }
}
