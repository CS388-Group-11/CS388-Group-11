package com.example.studysync.ui

sealed class WikiItem {
    data class Section(val title: String) : WikiItem()
    data class Paragraph(val text: String) : WikiItem()
}
