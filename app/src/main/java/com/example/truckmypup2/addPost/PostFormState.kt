package com.example.truckmypup2.addPost
data class PostFormState(
    val postNameError: Int? = null,
    val postDescError: Int? = null,
    val postImgError: Int?=null,
    val isDataValid: Boolean = false)
