package com.example.studysync.models

data class StudyGroup(
    val id: String? = null,
    val courseCode: String = "",
    val topic: String = "",
    val time: String = "",
    val date: String = "",
    val location: String = "",
    val creatorUid: String = "",
    val createdAt: Long = System.currentTimeMillis()
)