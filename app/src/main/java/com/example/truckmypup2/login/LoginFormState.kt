package com.example.truckmypup2.login

data class LoginFormState (
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false

)