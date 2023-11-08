package com.example.truckmypup2.home

import android.app.DownloadManager.Query
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.truckmypup2.data.Post
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot

class HomeViewModel : ViewModel() {
    private val _posts = MutableLiveData<MutableList<Post>>()
    val posts: LiveData<MutableList<Post>> = _posts

    private val _userReactions = MutableLiveData<HashMap<String, Boolean>>()
    val userReactions: LiveData<HashMap<String, Boolean>> = _userReactions

    private val _downloaded = MutableLiveData<Boolean>()
    val downloaded: LiveData<Boolean> = _downloaded

    private val DISLAKE_POENS = 10
    private val LIKE_POINTS = 10

    fun getPostsWithFilter(petsType: Long?): Task<QuerySnapshot> {
        if(petsType == null){
            return FirebaseFirestore.getInstance().collection("posts").get()

        }
        else{
            return FirebaseFirestore.getInstance().collection("posts").where(Filter.equalTo("postType", petsType)).get()
        }
    }

    fun fetchPosts(petsType: Long?){
        _downloaded.value = false
        getPostsWithFilter(petsType).addOnSuccessListener { docs ->
            _posts.value = mutableListOf()
            for(doc in docs) {
                val authorHashMap = doc["author"] as HashMap<*, *>
                _posts.value?.add(
                    Post(
                        doc["authorID"] as String,
                        doc.id,
                        doc["postName"] as String,
                        authorHashMap["firstName"] as String + " " + authorHashMap["firstName"] as String,
                        doc["postDate"] as String,
                        doc["postDesc"] as String,
                        doc["postType"] as Long,
                        doc["geoPoint"] as GeoPoint,
                        doc["imgUrl"] as String,
                        doc["upvoteCount"] as Long,
                        doc["downvoteCount"] as Long
                    )
                )
            }
            FirebaseFirestore.getInstance().collection("users")
                .where(Filter.equalTo("id", FirebaseAuth.getInstance().uid))
                .get().addOnSuccessListener { docs ->
                    for (doc in docs) {
                        _userReactions.value = doc["reacted"] as HashMap<String, Boolean>
                    }
                    _downloaded.value = true
                }
        }
    }

    private fun addUserReaction(){
        val updates = hashMapOf(
            "reacted" to _userReactions.value!!
            // Add other fields you want to update
        )
        FirebaseFirestore.getInstance().collection("users").where(Filter.equalTo("id",FirebaseAuth.getInstance().uid))
            .get().addOnSuccessListener { docs->
                for(doc in docs){
                    doc.reference.update(updates as Map<String, Any>)
                }
            }
    }

    private fun changePostCount(postID: String, upvotedChange:Long, downvoteChange:Long){
        FirebaseFirestore.getInstance().collection("posts").document(postID).get()
            .addOnSuccessListener { doc->
                val upvotes = doc.data?.get("upvoteCount") as Long
                val downvotes  = doc.data?.get("downvoteCount") as Long
                val updates = hashMapOf(
                    "upvoteCount" to (upvotes+upvotedChange),
                    "downvoteCount" to (downvotes+downvoteChange)
                )
                doc.reference.update(updates as Map<String, Any>)
                updatePosterPoints(doc.data?.get("authorID") as String,upvotedChange,downvoteChange)
            }
    }

    private fun updatePosterPoints(posterID:String, upvotedChange:Long, downvoteChange:Long){
        FirebaseFirestore.getInstance().collection("users").where(Filter.equalTo("id",posterID))
            .get()
            .addOnSuccessListener { docs->
                for (doc in docs){
                    val points = doc.data?.get("points") as Long
                    val updates = hashMapOf(
                        "points" to (points+upvotedChange*LIKE_POINTS-downvoteChange*DISLAKE_POENS)
                    )
                    doc.reference.update(updates as Map<String, Any>)
                }
            }
    }
    fun upvotePost(postID:String, callback:IHome, upBtn: ImageButton, downBtn: ImageButton, upvoteCnt: TextView, downvoteCnt: TextView){
        var userReactedToPost:Boolean = userReactions.value!!.contains(postID)
        if(userReactedToPost){
            //User vec reagovao
            var userUpvoted:Boolean = userReactions.value!![postID]!!
            //Uklanjamo upvote
            if(userUpvoted){
                callback.setNormalLike(upBtn,upvoteCnt)
                _userReactions.value?.remove(postID)
                changePostCount(postID,-1,0)
            }
            //Uklanjamo downvote, stavljamo upvote
            else{
                callback.setNormalDislike(downBtn,downvoteCnt)
                callback.setLike(upBtn,upvoteCnt)
                changePostCount(postID,1,-1)
                _userReactions.value?.set(postID, true)
            }
        }
        else{
            //User nije reagovao na post, sada se prvi put dodaje reakcija
            callback.setLike(upBtn,upvoteCnt)
            changePostCount(postID,1,0)
            _userReactions.value?.set(postID, true)
        }
        addUserReaction()
    }
    fun downvotePost(postID:String, callback:IHome, upBtn: ImageButton, downBtn: ImageButton, upvoteCnt: TextView, downvoteCnt: TextView){
        var userReactedToPost:Boolean = userReactions.value!!.contains(postID)
        if(userReactedToPost){
            //User vec reagovao
            var userUpvoted:Boolean = userReactions.value!![postID]!!
            //Uklanjamo upvote, stavljamo downvote
            if(userUpvoted){
                _userReactions.value?.set(postID, false)
                callback.setNormalLike(upBtn,upvoteCnt)
                callback.setDislike(downBtn,downvoteCnt)
                changePostCount(postID,-1,1)
            }
            //Uklanjamo downvote
            else{
                callback.setNormalDislike(downBtn,downvoteCnt)
                changePostCount(postID,0,-1)
                _userReactions.value?.remove(postID)
            }
        }
        else{
            //User nije reagovao na post, sada se prvi put dodaje reakcija
            callback.setDislike(downBtn,downvoteCnt)
            changePostCount(postID,0,1)
            _userReactions.value?.set(postID, false)
        }
        addUserReaction()
    }
}