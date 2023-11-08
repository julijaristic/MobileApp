package com.example.truckmypup2.data

data class User (
    val email:String,
    val firstName:String,
    val id: String,
    val imageUrl:String,
    val lastName:String,
    val phone: String,
    val points:Int,
    val reacted: HashMap<*,*>
)