package com.example.truckmypup2.addPost

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.example.truckmypup2.IHomeActivity
import com.example.truckmypup2.R
import com.example.truckmypup2.databinding.FragmentAddPostBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

interface  IAddPost{
    fun onSuccess()
    fun onFail()
}
class AddPostFragment(_callback: IHomeActivity) : Fragment(), IAddPost {
    private var callback=_callback

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddPostViewModel
    private var imageUri: Uri? = null
    private var imageBitmap: Bitmap? = null

    private val GALLERY_REQUEST_CODE = 1001
    private val CAMERA_REQUEST_CODE = 1002
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val LOCATION_PERMISSION_REQUEST_CODE=123

    private lateinit var locationProvider: FusedLocationProviderClient

    private lateinit var currentLoc: LatLng

    private var petsType:Long = 2
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        binding.fromdevicePost.setOnClickListener{
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }

        binding.fromcameraPost.setOnClickListener{
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
        binding.dogRB.setOnClickListener{
            petsType=0
        }
        binding.catRB.setOnClickListener{
            petsType=1
        }
        binding.otherRB.setOnClickListener{
            petsType=2
        }
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AddPostViewModel::class.java)
        locationProvider = LocationServices.getFusedLocationProviderClient(requireActivity());
        // TODO: Use the ViewModel
        binding.addPost.setOnClickListener {
            addPost()
        }
        viewModel.postForm.observe(viewLifecycleOwner,
            Observer { postForm ->
                if (postForm == null) {
                    return@Observer
                }
                binding.addPost.isEnabled = postForm.isDataValid
                postForm.postNameError?.let {
                    binding.postNameEdit.error = getString(it)
                }
                postForm.postDescError?.let {
                    binding.postDescEdit.error = getString(it)
                }
            })
        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                viewModel.registerDataChanged(
                    binding.postNameEdit.text.toString(),
                    binding.postDescEdit.text.toString(),
                )
            }
        }
        binding.postNameEdit.addTextChangedListener(afterTextChangedListener)
        binding.postDescEdit.addTextChangedListener(afterTextChangedListener)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun addPost(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                locationPermissions,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        else {
            binding.loading.visibility= View.VISIBLE
            locationProvider.getLastLocation().addOnSuccessListener { loc ->
                currentLoc= LatLng(loc.latitude,loc.longitude)
                if(imageUri==null) {
                    viewModel.addPost(
                        imageBitmap!!,
                        binding.postNameEdit.text.toString(),
                        binding.postDescEdit.text.toString(),
                        currentLoc,
                        petsType,
                        this
                    )
                }
                else{
                    viewModel.addPost(
                        imageUri!!,
                        binding.postNameEdit.text.toString(),
                        binding.postDescEdit.text.toString(),
                        currentLoc,
                        petsType,
                        this
                    )
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    imageBitmap = null
                    imageUri = data?.data

                    binding.slika.setImageURI(imageUri)
                    binding.addPost.isEnabled=true

                }
                CAMERA_REQUEST_CODE -> {
                    imageUri = null
                    imageBitmap = data?.extras?.get("data") as Bitmap

                    binding.slika.setImageBitmap(imageBitmap)
                    binding.addPost.isEnabled=true

                }
            }
        }
        else{
            binding.slika.setImageDrawable(requireContext().resources.getDrawable(R.drawable.baseline_person_24,requireContext().theme))
            binding.addPost.isEnabled=false
        }
    }
    override fun onSuccess() {
        binding.loading.visibility= View.GONE
        callback.backToMain()
    }

    override fun onFail() {
        TODO("Not yet implemented")
    }

}