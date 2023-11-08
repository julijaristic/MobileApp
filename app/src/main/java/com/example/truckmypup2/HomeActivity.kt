package com.example.truckmypup2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.truckmypup2.account.AccountFragment
import com.example.truckmypup2.addPost.AddPostFragment
import com.example.truckmypup2.data.DownloadImageTask
import com.example.truckmypup2.databinding.ActivityHomeBinding
import com.example.truckmypup2.home.HomeFragment
import com.example.truckmypup2.map.MapFragment
import com.example.truckmypup2.rank.RankFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.LatLng

interface IHomeActivity{
    fun logout()
    fun changeToMapAndCenter(ll: LatLng)
    fun backToMain()
}

class HomeActivity : AppCompatActivity() , IHomeActivity {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        actionBar?.hide()
        var w = this.window
        w.statusBarColor = this.resources.getColor(R.color.background)
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        var h = HomeFragment()
        h.setCallback(this)
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).selectedItemId = R.id.button_home

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(R.id.fragmentMainContainer, h)
            .commit()

        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.button_home-> {
                    var h = HomeFragment()
                    h.setCallback(this)
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                        .replace(R.id.fragmentMainContainer, h)
                        .commit()
                    true
                }
                R.id.button_add -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                        .replace(R.id.fragmentMainContainer, AddPostFragment(this))
                        .commit()
                    true
                }
                R.id.button_map -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                        .replace(R.id.fragmentMainContainer, MapFragment())
                        .commit()
                    true
                }
                R.id.button_rank -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                        .replace(R.id.fragmentMainContainer, RankFragment())
                        .commit()
                    true
                }
                R.id.button_account -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
                        .replace(R.id.fragmentMainContainer, AccountFragment(this))
                        .commit()
                    true
                }
                else -> {
                    false
                }
            }

        }
        findViewById<ImageButton>(R.id.logoutBtn).setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@HomeActivity, WelcomeActivity::class.java)
            startActivity(intent)
        }


        Firebase.firestore.collection("users").where(Filter.equalTo("id", FirebaseAuth.getInstance().uid))
            .get().addOnSuccessListener { docs->
                for(doc in docs){
                    findViewById<TextView>(R.id.name).text =
                        doc.data["firstName"] as String +
                                " "+
                                doc.data["lastName"] as String
                    var dTask = DownloadImageTask(findViewById<ImageView>(R.id.acc_image))
                    dTask.execute(doc.data["imageUrl"] as String)
                }
            }
    }

    override fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this@HomeActivity, WelcomeActivity::class.java)
        startActivity(intent)
    }

    override fun changeToMapAndCenter(ll:LatLng) {
        var m = MapFragment()
        m.setInitLoc(ll)
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).selectedItemId= R.id.button_map
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(R.id.fragmentMainContainer, m)
            .commit()
    }

    override fun backToMain() {
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).selectedItemId= R.id.button_home
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
            .replace(R.id.fragmentMainContainer, HomeFragment())
            .commit()
    }

}