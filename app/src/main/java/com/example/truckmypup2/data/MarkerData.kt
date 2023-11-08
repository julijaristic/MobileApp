package com.example.truckmypup2.data

import com.google.android.gms.maps.model.LatLng

data class MarkerData(
    val position: LatLng,
    val name: String,
    val postID:String,
)
