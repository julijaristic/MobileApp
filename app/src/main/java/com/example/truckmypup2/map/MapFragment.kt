package com.example.truckmypup2.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.truckmypup2.R
import com.example.truckmypup2.data.DownloadImageTask
import com.example.truckmypup2.data.Post
import com.example.truckmypup2.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MapViewModel
    private lateinit var googleMap: GoogleMap
    private lateinit var locationProvider: FusedLocationProviderClient
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val LOCATION_PERMISSION_REQUEST_CODE=123
    private var initLocation: LatLng?=null
    private var clickedMarkerData: Post?=null
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        locationProvider = LocationServices.getFusedLocationProviderClient(requireActivity());
        viewModel.fetchMapPoints()
        viewModel.dowloaded.observe(viewLifecycleOwner) { downloaded ->
            if(downloaded!!){
                for(m in viewModel.points.value!!){
                    if(m!=null) {
                        var marker =
                            googleMap.addMarker(MarkerOptions().position(m.position).title(m.name))
                        marker?.tag =m.postID
                        googleMap.setOnMarkerClickListener { marker->
                            FirebaseFirestore.getInstance().collection("posts").document(marker.tag as String).get().addOnSuccessListener { doc->
                                val authorHashMap = doc["author"] as HashMap<*, *>
                                clickedMarkerData= Post(
                                    doc["authorID"] as String,
                                    doc.id,
                                    doc["postName"] as String,
                                    authorHashMap["firstName"] as String + " " + authorHashMap["lastName"] as String,
                                    doc["postDate"] as String,
                                    doc["postDesc"] as String,
                                    doc["postType"] as Long,
                                    doc["geoPoint"] as GeoPoint,
                                    doc["imgUrl"] as String,
                                    doc["upvoteCount"] as Long,
                                    doc["downvoteCount"] as Long
                                )
                                marker.showInfoWindow()
                            }
                            false
                        }

                        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                            override fun getInfoContents(m: Marker): View? {
                                if(clickedMarkerData==null) return null
                                var v = layoutInflater.inflate(R.layout.home_post, null)
                                var type: Int = R.drawable.dog
                                if (clickedMarkerData!!.postType == 1L) type = R.drawable.happy
                                else if (clickedMarkerData!!.postType >= 3L) type = R.drawable.more_info
                                v.findViewById<ImageView>(R.id.placeType)
                                    .setImageDrawable(
                                        requireContext().resources.getDrawable(
                                            type,
                                            requireContext().theme
                                        )
                                    )
                                var dTask =
                                    DownloadImageTask(v.findViewById<ImageView>(R.id.placeImage))
                                dTask.execute(clickedMarkerData!!.imgUrl as String)
                                v.findViewById<TextView>(R.id.postName).text = clickedMarkerData!!.postName
                                v.findViewById<TextView>(R.id.postDesc).text = clickedMarkerData!!.postDesc
                                v.findViewById<TextView>(R.id.postedBy).text = clickedMarkerData!!.postUser
                                v.findViewById<TextView>(R.id.postDate).text = clickedMarkerData!!.postDate
                                v.findViewById<ImageButton>(R.id.upvoteBtn).visibility=View.GONE
                                v.findViewById<ImageButton>(R.id.downvoteBtn).visibility=View.GONE
                                v.findViewById<TextView>(R.id.upvoteCount).visibility=View.GONE
                                v.findViewById<TextView>(R.id.downvoteCount).visibility=View.GONE
                                v.findViewById<ImageButton>(R.id.showOnMap).visibility=View.GONE
                                return v
                            }

                            override fun getInfoWindow(p0: Marker): View? {
                                return null
                            }

                        })
                    }
                }
            }
        }
        if(initLocation==null)
            centerToCurrentLoc()
        else
            centerToLocation(initLocation!!)
    }
    fun setInitLoc(ll:LatLng){
        initLocation=ll
    }
    private fun centerToCurrentLoc(){
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
            googleMap.isMyLocationEnabled=true
            locationProvider.getLastLocation().addOnSuccessListener { loc ->
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(loc.latitude,loc.longitude))
                    .zoom(14.0f)
                    .build()
                val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
                googleMap.animateCamera(cameraUpdate, 2000, null)
            }
        }
    }

    private fun centerToLocation(ll: LatLng){
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
            googleMap.isMyLocationEnabled=true
            locationProvider.getLastLocation().addOnSuccessListener { loc ->
                val cameraPosition = CameraPosition.Builder()
                    .target(ll)
                    .zoom(14.0f)
                    .build()
                val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
                googleMap.animateCamera(cameraUpdate, 2000, null)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                centerToCurrentLoc()
            }
            else{
                Toast.makeText(context,"Morate dozvoliti pristup lokaciji!", Toast.LENGTH_LONG)
            }
        }
    }

}

