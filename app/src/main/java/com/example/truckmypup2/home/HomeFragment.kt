package com.example.truckmypup2.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.example.truckmypup2.IHomeActivity
import com.example.truckmypup2.R
import com.example.truckmypup2.data.DownloadImageTask
import com.example.truckmypup2.databinding.FragmentHomeBinding

interface IHome{
    fun setLike(btn:ImageButton, text: TextView)
    fun setDislike(btn:ImageButton, text: TextView)
    fun setNormalLike(btn:ImageButton, text: TextView)
    fun setNormalDislike(btn:ImageButton, text: TextView)
}
class HomeFragment : Fragment() , IHome{

    private var callback: IHomeActivity? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    /*companion object {
        fun newInstance() = HomeFragment()
    }*/

    private lateinit var viewModel: HomeViewModel
    private var petsType:Long?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setCallback(_callback:IHomeActivity){
        callback=_callback
    }

    private fun populateLinearLayout(){
        var linearLayout = binding.linearPosts
        linearLayout.removeAllViews()
        for (post in viewModel.posts.value!!){
            val customView = layoutInflater.inflate(R.layout.home_post, linearLayout, false)
            var type:Int = R.drawable.dog
            if(post.postType==1L) type = R.drawable.happy
            else if (post.postType==2L) type= R.drawable.more_info
            customView.findViewById<ImageView>(R.id.placeType).setImageDrawable(requireContext().resources.getDrawable(type,requireContext().theme))
            var dTask = DownloadImageTask(customView.findViewById<ImageView>(R.id.placeImage))
            dTask.execute(post.imgUrl)
            customView.findViewById<TextView>(R.id.postName).text = post.postName
            customView.findViewById<TextView>(R.id.postDesc).text = post.postDesc
            customView.findViewById<TextView>(R.id.postedBy).text = post.postUser
            customView.findViewById<TextView>(R.id.postDate).text = post.postDate
            customView.findViewById<TextView>(R.id.upvoteCount).text = post.upvoteCount.toString()
            customView.findViewById<TextView>(R.id.downvoteCount).text = post.downvoteCount.toString()
            var upvoteBtn = customView.findViewById<ImageButton>(R.id.upvoteBtn)
            var downvoteBtn = customView.findViewById<ImageButton>(R.id.downvoteBtn)
            var upvoteCnt = customView.findViewById<TextView>(R.id.upvoteCount)
            var downvoteCnt = customView.findViewById<TextView>(R.id.downvoteCount)
            if(viewModel.userReactions.value!!.contains(post.postID)){
                if(viewModel.userReactions.value!![post.postID]!!){
                    upvoteBtn.setImageDrawable(requireContext().resources.getDrawable(R.drawable.baseline_thumb_up_alt_white_24,requireContext().theme))
                }
                else{
                    downvoteBtn.setImageDrawable(requireContext().resources.getDrawable(R.drawable.baseline_thumb_down_black_24,requireContext().theme))
                }
            }
            customView.findViewById<ImageButton>(R.id.showOnMap).setOnClickListener{
                callback?.changeToMapAndCenter(
                    com.google.android.gms.maps.model.LatLng(
                        post.geoPoint.latitude,
                        post.geoPoint.longitude
                    )
                )
            }
            upvoteBtn.setOnClickListener{
                viewModel.upvotePost(post.postID,this,upvoteBtn,downvoteBtn,upvoteCnt,downvoteCnt)
            }
            downvoteBtn.setOnClickListener{
                viewModel.downvotePost(post.postID,this,upvoteBtn,downvoteBtn,upvoteCnt,downvoteCnt)
            }
            linearLayout.addView(customView)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
        viewModel.fetchPosts(petsType)
        viewModel.downloaded.observe(viewLifecycleOwner, Observer { downloaded->
            if(downloaded)
                populateLinearLayout()
        })
        binding.allRB.setOnClickListener{
            petsType=null
            viewModel.fetchPosts(petsType)
        }
        binding.dogsRB.setOnClickListener{
            petsType=0
            viewModel.fetchPosts(petsType)
        }
        binding.catsRB.setOnClickListener{
            petsType=1
            viewModel.fetchPosts(petsType)
        }
        binding.otherRB.setOnClickListener{
            petsType=2
            viewModel.fetchPosts(petsType)
        }
    }

    override fun setLike(btn: ImageButton,text: TextView) {
        text.text = (text.text.toString().toLong()+1).toString()
        btn.setImageDrawable(requireContext().resources.getDrawable(R.drawable.baseline_thumb_up_alt_black_24,requireContext().theme))
    }

    override fun setDislike(btn: ImageButton,text: TextView) {
        text.text = (text.text.toString().toLong()+1).toString()
        btn.setImageDrawable(requireContext().resources.getDrawable(R.drawable.baseline_thumb_down_black_24,requireContext().theme))
    }

    override fun setNormalLike(btn: ImageButton,text: TextView) {
        text.text = (text.text.toString().toLong()-1).toString()
        btn.setImageDrawable(requireContext().resources.getDrawable(R.drawable.baseline_thumb_up_alt_white_24,requireContext().theme))
    }

    override fun setNormalDislike(btn: ImageButton,text: TextView) {
        text.text = (text.text.toString().toLong()-1).toString()
        btn.setImageDrawable(requireContext().resources.getDrawable(R.drawable.baseline_thumb_down_white_24,requireContext().theme))
    }

}