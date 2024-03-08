package com.example.truckmypup2.rank

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.truckmypup2.R
import com.example.truckmypup2.data.DownloadImageTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RankFragment : Fragment() {

    companion object {
        fun newInstance() = RankFragment()
    }
    private val bestLimit: Long = 10
    private lateinit var viewModel: RankViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewOfLayout = inflater.inflate(R.layout.fragment_rank, container, false)
        val linearLayout = viewOfLayout.findViewById<LinearLayout>(R.id.rankList)
        val myRank = viewOfLayout.findViewById<LinearLayout>(R.id.myRank)
        /*Firebase.firestore.collection("users").where(Filter.equalTo("id", FirebaseAuth.getInstance().uid))
            .get().addOnSuccessListener { docs->
                for(doc in docs){
                    val customView = inflater.inflate(R.layout.myrank, linearLayout, false)
                    val points= doc.data["points"] as Long
                    customView.findViewById<TextView>(R.id.accountPoints).text = points.toString()
                    customView.findViewById<TextView>(R.id.accountName).text =
                        doc.data["firstName"] as String +
                                " "+
                                doc.data["lastName"] as String
                    var dTask = DownloadImageTask(customView.findViewById<ImageView>(R.id.accountImage))
                    dTask.execute(doc.data["imageUrl"] as String)
                    myRank.addView(customView)
                }
            }*/
        Firebase.firestore.collection("users").orderBy("points", Query.Direction.DESCENDING).limit(bestLimit)
            .get().addOnSuccessListener {  docs->
                linearLayout.removeAllViews()
                for (doc in docs){
                    val customView = inflater.inflate(R.layout.scoreboard_item, linearLayout, false)
                    customView.findViewById<TextView>(R.id.accountName).text =
                        doc.data["firstName"] as String +
                                " "+
                                doc.data["lastName"] as String
                    val points= doc.data["points"] as Long
                    customView.findViewById<TextView>(R.id.accountPoints).text = points.toString()
                    var dTask = DownloadImageTask(customView.findViewById<ImageView>(R.id.accountImage))
                    dTask.execute(doc.data["imageUrl"] as String)
                    linearLayout.addView(customView)
                }
            }
        return viewOfLayout

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RankViewModel::class.java)
        // TODO: Use the ViewModel
    }

}