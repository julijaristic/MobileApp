package com.example.truckmypup2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        actionBar?.hide()
        var w = this.window
        w.statusBarColor = this.resources.getColor(R.color.background)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        if(FirebaseAuth.getInstance().currentUser!=null)
            skipLogin()

    }

    fun skipLogin(){
        val intent = Intent(this@WelcomeActivity, HomeActivity::class.java)
        startActivity(intent)
    }
}