package com.example.studysync.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studysync.models.StudyGroup

class GroupViewModel : ViewModel() {

    private val _groups = MutableLiveData<MutableList<StudyGroup>>(mutableListOf())

    val groups: LiveData<MutableList<StudyGroup>> = _groups

    fun addGroup(newGroup: StudyGroup) {
        val currentList = _groups.value ?: mutableListOf()

        currentList.add(newGroup)

        _groups.value = currentList
    }
}