package com.futurelinegen.maptracer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.futurelinegen.maptracer.relam.LocationModel
import com.futurelinegen.maptracer.relam.MapListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_maps.*
import org.jetbrains.anko.alert
import java.lang.Exception
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val polylineOptions = PolylineOptions().width(5f).color(Color.RED)
    private val calendar = Calendar.getInstance()
    private var beforeLatLng = LatLng(-0.0,-0.0)

    val realm = Realm.getDefaultInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("mapLocation","onCreate start")
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initListView()

    }

    override fun onResume() {
        super.onResume()

        fusedLocationProviderClient = FusedLocationProviderClient(this)
    }

    override fun onPause() {
        super.onPause()

        if (checkMapPermission()) {
            removeLocationListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    fun checkMapPermission(): Boolean {
       return ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
    }

    //request
    fun requestPermission(okBlock: () -> Unit  ) {

        if (checkMapPermission()) {
            okBlock()
            return
        }
        
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1000)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug", "checkSelfPermission true")
                locationInit()
                addLocationUpdateListener()

            } else {
                // それでも拒否された時の対応
                Toast.makeText(this,
                    "これ以上なにもできません", Toast.LENGTH_SHORT).show()

            }
        }
    }

    @SuppressLint("MissingPermission")
    fun addLocationUpdateListener() {

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,null)
        } catch (e:SecurityException) {
            e.printStackTrace()
            // lets the user know there is a problem with the gps
        }
    }

    fun removeLocationListener() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    fun locationInit() {
        //ポシション取得方法を要求
        locationRequest = LocationRequest().apply {
            interval = 10000                                   // 最遅の更新間隔(但し正確ではない。)
            fastestInterval = 5000                             // 最短の更新間隔
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
        }

        // コールバック
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                // 更新直後の位置が格納されているはず
                locationResult?.lastLocation?.run {

                    val latitudeStr =  getString(R.string.latitude)
                    val longitudeStr =  getString(R.string.longitude)

                    Toast.makeText(this@MapsActivity,
                    "$latitudeStr${latitude}, $longitudeStr${longitude}", Toast.LENGTH_LONG)
                    .show()

//                // 初回起動でしか位置情報を取得しないサンプルなので位置情報の定期取得を止める。
//                fusedLocationProviderClient.removeLocationUpdates(this)

                    Log.d("mapLocation", "latitude:$latitude ,longitude: $longitude ")

                    val latLng = LatLng(latitude, longitude)

                    if (beforeLatLng.latitude == latitude && beforeLatLng.longitude == longitude) {
                        return
                    }
                    beforeLatLng = latLng

                    addLocationModel(latLng)

                    polylineOptions.add(latLng)

                    displayMap(latLng, "")

                    googleMap.addPolyline(polylineOptions)


                }
            }
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        // Add a marker in tokyo and move the camera

        //val location = LatLng(35.681167, 139.767052)
        //val name = "tokyo"
        //displayMap(googleMap, location, name)
        //displayCircle(location)

        this.googleMap = googleMap

        val okBlock = {
            locationInit()
            addLocationUpdateListener()

        }

        requestPermission(okBlock)

    }

    private fun displayMap( location: LatLng, name: String, isZoom: Boolean = true) {

        googleMap.addMarker(MarkerOptions().position(location).title("Marker in " + name))
        if (isZoom) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17.0f))
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        }
        //zoom
        googleMap.uiSettings.isZoomGesturesEnabled = true
        //toolbar
        googleMap.uiSettings.isMapToolbarEnabled = true
        //compass
        googleMap.uiSettings.isCompassEnabled = true
        //current position  permisson required
        //mMap.isMyLocationEnabled = true
        //wrong
        //mMap.isMyLocationButtonEnabled = true

    }

    private fun displayCircle(location: LatLng) {
        val radius = 1000 * 1.0 // 1km
        googleMap.addCircle(
            CircleOptions()
                .center(location)          // 円の中心位置
                .radius(radius)          // 半径 (メートル単位)
                .strokeColor(Color.BLUE) // 線の色
                .strokeWidth(2f)         // 線の太さ
                .fillColor(0x400080ff)   // 円の塗りつぶし色
        )
    }

    fun addLocationModel(latLng: LatLng) {
        try {

            realm.beginTransaction()

            val model = realm.createObject<LocationModel>(getNextId())
            model.latitude = latLng.latitude
            model.longtitude = latLng.longitude
            model.date = calendar.timeInMillis

            Log.d("mapLocation", "mId=${getNextId()}, date=${model.date}" )

            realm.commitTransaction()

        } catch (e: Exception) {
            Log.d("mapLocation", " error latitude:$latLng.latitude ,longitude: $latLng.longitude ")
        }
        //alert("saveLocationModel")

    }

    fun loadAllLocationModel(): RealmResults<LocationModel> {
        val results = realm.where<LocationModel>().findAll().sort("mapId", Sort.DESCENDING)

        for ( model  in results) {
            model.run {
                Log.d("mapLocation","loadAllLocationModel => $mapId , ${longtitude}, ${latitude}, $title ")
            }

        }
        return results
    }

    fun updateLocationModel(mapId: Int, latLng: LatLng) {

        realm.beginTransaction()

        val model = realm.where<LocationModel>().equalTo("mapId", mapId).findFirst()
        model?.mapId = 0
        model?.latitude = latLng.latitude
        model?.longtitude = latLng.longitude
        model?.date = calendar.timeInMillis

        realm.commitTransaction()


        alert("updateLocationModel") {
            yesButton { finish() }
        }.show()
    }

    fun deleteLocationModel(mapId: Int, latLng: LatLng) {
        realm.beginTransaction()

        val model = realm.createObject<LocationModel>(getNextId())
        model.deleteFromRealm()

        realm.commitTransaction()


        alert("deleteLocationModel") {
            yesButton { finish() }
        }.show()
    }


    fun getNextId() : Int {
        val maxId = realm.where<LocationModel>().max("mapId")
        if (maxId != null) {
            return maxId.toInt() + 1
        }
        return 0
    }

    fun initListView() {

        loadAllLocationModel().run {
            val adapter = MapListAdapter(this)
            listView.adapter = adapter

            this.addChangeListener { _ ->

                adapter.notifyDataSetChanged()
                //listView.invalidateViews()
                //listView.refreshDrawableState()

            }
        }


        listView.setOnItemClickListener { parent, view, position, id ->
            //startActivity<EditActivity>("mapId" to mapId)
        }
    }

}
