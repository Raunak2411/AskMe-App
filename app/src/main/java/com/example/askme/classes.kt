package com.example.askme

data class user(val name: String? = null, val email: String? = null, val profileURL: String? = null)
data class questionClass(val postID: String? = null, val question: String? = null, val publisherID: String? = null, val date: String? = null, val pubName: String? = null, val profileURL: String? = null, val questionURL: String? = null)
data class commentClass(val commentID: String? = null, val postID: String? = null, val comment: String? = null, val date: String? = null, val pubName: String? = null, val profileURL: String? = null)
