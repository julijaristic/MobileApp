package com.example.truckmypup2.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.example.truckmypup2.data.MarkerData

class MapViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    private var _points = MutableLiveData<MutableList<MarkerData>>()
    val points: LiveData<MutableList<MarkerData>>
        get() = _points

    private var _dowloaded = MutableLiveData<Boolean>()
    val dowloaded: LiveData<Boolean>
        get() = _dowloaded

    fun fetchMapPoints() {
        _points.value = mutableListOf()
        FirebaseFirestore.getInstance().collection("posts").get().addOnSuccessListener { docs ->
            for (doc in docs) {
                val geoP = doc["geoPoint"] as GeoPoint
                _points.value?.add(
                    MarkerData(
                        LatLng(geoP.latitude, geoP.longitude),
                        doc["postName"] as String,
                        doc.id
                    )
                )
            }
            _dowloaded.value = true

        }
    }
}