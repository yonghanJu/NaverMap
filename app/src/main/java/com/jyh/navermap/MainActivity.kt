package com.jyh.navermap

import android.content.Intent
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.jyh.navermap.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private val binding by lazy{ ActivityMainBinding.inflate(layoutInflater) }

    private val mapView by lazy{ binding.mapView }

    private val viewPager by lazy{ binding.houseViewPager }

    private val viewPagerAdapter = HouseViewPagerAdapter{ }

    private val houseRecyclerView by lazy{ binding.bottomSheet.houseRecyclerView }

    private val houseAdapter = HouseAdapter()

    private lateinit var naverMap:NaverMap
    private lateinit var locationSource:FusedLocationSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mapView.onCreate(savedInstanceState)

        // 네이버 지도 가져오기
        mapView.getMapAsync(this)

        // 뷰 페이저 초기화
        viewPager.adapter = viewPagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // 선택된 house
                val selectedHouse = viewPagerAdapter.currentList[position]

                // 카메라 애니메이션으로 부드럽게 이동하기
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(selectedHouse.lat, selectedHouse.lng))
                    .animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)
            }
        })

        // 리사이클러뷰 초기화
        houseRecyclerView.adapter = houseAdapter
        houseRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        // 일반 적인 레벨
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        // 위도 경도로 지도 화면 이동
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(LatLng(37.498095, 127.027610),15.0)
        naverMap.moveCamera(cameraUpdate)

        // 현위치 표시(버튼을 누르면 바로 내위치 이동하기 기능, 위치 권한 필요)
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false
        binding.currentLocationButton.map = naverMap

        // 위치 및 권한을 받아옴 ( 위치 정보를 편하게 가져올 수 있음 )
        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        getHouseFromApi()
    }

    private fun getHouseFromApi() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(HouseService::class.java).also{ houseService->
            houseService.getHouseList()
                .enqueue(object : Callback<HouseDto> {
                    override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
                        if(response.isSuccessful.not()){
                            // 실패 처리
                            Log.d("retrofit",response.errorBody().toString())
                            return
                        }

                        response.body()?.let{ houseDto ->
                            // ViewPager 객체에 리스트 입력
                            viewPagerAdapter.submitList(houseDto.items)
                            houseAdapter.submitList(houseDto.items)

                            houseDto.items.forEach{ houseModel->
                                val marker = Marker()
                                marker.position = LatLng(houseModel.lat, houseModel.lng)
                                marker.onClickListener = this@MainActivity
                                marker.map = naverMap
                                marker.tag = houseModel.id
                                marker.icon = MarkerIcons.BLACK
                                marker.iconTintColor = Color.RED
                            }
                        }
                    }

                    override fun onFailure(call: Call<HouseDto>, t: Throwable) {
                        // 실패 처리
                        Log.d("retrofit",t.message.toString())
                    }
                })
        }
    }

    // 위치 권한 요구에 필요한 함수 오버라이딩
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // 위치 권한이 아닐 때는 넘김
        if(requestCode != LOCATION_PERMISSION_REQUEST_CODE){
            return
        }

        // 위치 권한일 때 처리
        if(locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)){
            if(!locationSource.isActivated){
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }

    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onClick(marker: Overlay): Boolean {

        // firstOrNull 리스트에서 일치하는 첫번째 값 반환
        val selectedModel = viewPagerAdapter.currentList.firstOrNull{
            it.id == marker.tag
        }

        selectedModel?.let{
            val position = viewPagerAdapter.currentList.indexOf(it)
            viewPager.currentItem = position
        }
        return true
    }
}