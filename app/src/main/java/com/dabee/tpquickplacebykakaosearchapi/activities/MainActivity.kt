package com.dabee.tpquickplacebykakaosearchapi.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.dabee.tpquickplacebykakaosearchapi.R
import com.dabee.tpquickplacebykakaosearchapi.databinding.ActivityMainBinding
import com.dabee.tpquickplacebykakaosearchapi.fragments.SearchListFragment
import com.dabee.tpquickplacebykakaosearchapi.fragments.SearchMapFragment
import com.dabee.tpquickplacebykakaosearchapi.model.KakaoSearchPlaceResponse
import com.dabee.tpquickplacebykakaosearchapi.model.Place
import com.dabee.tpquickplacebykakaosearchapi.model.PlaceMeta
import com.dabee.tpquickplacebykakaosearchapi.network.RetrofitApiService
import com.dabee.tpquickplacebykakaosearchapi.network.RetrofitHelper
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    
    // 카카오 지역검색(로컬) API -- 카카오개발자 사이트 참고
    // 1. 검색 장소명
    var searchQuery:String= "화장실" //앱 초기 키워드 - 내 주변 개방 화장실
    // 2. 현재 내위치 정보 객체 (위도, 경도 정보를 멤버로 보유)
    var myLocation:Location?=null

    // [ Google Fused API 사용 - 위치정보 라이브러리 play-services-location ]
    val providerClient:FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this)}

    // 카카오 검색 결과 응답객체 참조변수
    var searchPlaceResponse: KakaoSearchPlaceResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 툴바를 제목줄로 대체하여 설정
        setSupportActionBar(binding.toolbar)
        
        // 첫 실행될 프레그먼트를 동적 추가
        supportFragmentManager.beginTransaction().add(R.id.container_fragment, SearchListFragment()).commit()
        
        //탭레이아웃의 탭버튼 클릭시에 보여줄 프레그먼트를 변경
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.text=="LIST"){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment,SearchListFragment()).commit()
                }else if(tab?.text=="MAP"){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment, SearchMapFragment()).commit()
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        // 소프트키보드의 검색버튼 클릭했을때.
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            searchQuery=binding.etSearch.text.toString()
            searchPlaces()

            // return 값이 있는 메소드
            // 소프트키보드의 액션버튼이 클릭되었을때 여기서 모든 액션을 소모하지 않겠다는 뜻으로 false 리턴
            // SAM변환으로 메소드의 코드를 축약했다면.. return 키워드도 생략해야만 함.
            false

        }

        // 단축 검색어 choice 버튼들에 클릭이벤트 리스너 처리하기
        setChoiceButtonsListner()

        //내 위치정보 제공에 대한 사용자 동적퍼미션
        val permissions:Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if( checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_DENIED ){
            // 허가 되어 있지 않으므로 권한을 요청하는 다이얼로그 보이기
            requestPermissions(permissions,0)

        } else{
            // 허가 되어 있으므로 곧바로 내 위치값을 요구하기
            requestMyLocation()
        }


    }//onCreate method.....

    // 퍼미션요청 다이얼로그의 [허가/거부] 선택이 되었을때 자동 발동하는 콜백메소드
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) requestMyLocation()
        else Toast.makeText(this, "내 위치정보를 제공하지 않아 검색기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            
        
    }

    // 내 위치정보를 가져오는 기능 메소드
    private fun requestMyLocation(){
        
        // 위치검색을 위한 기준(요청)객체가 필요함.
        val request:LocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000).build()  //구글꺼 사용

        // 실시간 위치정보 갱신 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return
        }
        providerClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

    }

    // 위치정보 갱신될때 마다 발동하는 Callback 객체 생성
    private val locationCallback:LocationCallback = object:LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            myLocation = p0.lastLocation

            // 위치 탐색이 끝났으니 내 위치 정보 업데이트는 종료
            providerClient.removeLocationUpdates(this) // this : locationCallback

            // 내 위치정보도 있으니.. 카카오 로컬 API 키워드 검색작업 시작!
            searchPlaces()

        }
    }

    private fun searchPlaces(){

        //검색에 필요한 요청변수들.. 확인 [ 검색어, 내 위치 좌표 ]
        Toast.makeText(this, "$searchQuery : ${myLocation?.latitude},${myLocation?.longitude}", Toast.LENGTH_SHORT).show()

        //Kakao keyword search API base url 레트로핏 객체 생성
        val retrofit: Retrofit = RetrofitHelper.getRetrofitInstance("https://dapi.kakao.com")
        val retrofitApiService = retrofit.create(RetrofitApiService::class.java)
        retrofitApiService.searchPlaces(searchQuery, myLocation?.longitude.toString() ,myLocation?.latitude.toString()).enqueue(object : retrofit2.Callback<KakaoSearchPlaceResponse>{
            override fun onResponse(
                call: Call<KakaoSearchPlaceResponse>,
                response: Response<KakaoSearchPlaceResponse>
            ) {
                //응답된 json문자열을 파싱한 객체 참조하기
                searchPlaceResponse=response.body()
                //먼저 데이터가 온전히 잘 왔는지 파악하기 위해
                var meta: PlaceMeta? = searchPlaceResponse?.meta
                var documents:MutableList<Place>? =searchPlaceResponse?.documents

                AlertDialog.Builder(this@MainActivity).setMessage("${meta?.total_count}\n${documents?.get(0)?.place_name}").show()

                //무조건 검색이 완료되면 List Fragment를 먼저 보여주기.
                supportFragmentManager.beginTransaction().replace(R.id.container_fragment, SearchListFragment()).commit()

                // 탭버튼의 위치를 List Fragment Tab 으로 변경
                binding.tabLayout.getTabAt(0)?.select()

            }

            override fun onFailure(call: Call<KakaoSearchPlaceResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "서버 오류가 있습니다.\n잠시뒤에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }

        })

    }

    // 단축 검색어 버튼들에 리스너를 설정하는 작업 메소드
    private fun setChoiceButtonsListner(){
        binding.layoutChoice.choiceWc.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceMovie.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceGas.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceEv.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choicePharmacy.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choicePark.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceFood.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice01.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice02.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice03.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice04.setOnClickListener { clickChoice(it) }
    }
    var choiceID = R.id.choice_wc

    private fun clickChoice(view: View){


        //이전에 선택했던 버튼의 아이디값 저장용 변수
        findViewById<ImageView>(choiceID).setBackgroundResource(R.drawable.bg_choice)

        //선택한 단축 검색어 버튼의 배경그림을 변경
        view.setBackgroundResource(R.drawable.bg_choice_selected)

        //다음번 클릭때 이전 선택버튼의 id를 찾을 수 있도록..
        choiceID=view.id

        //선택한 뷰에 따라 검색어 값을 변경
        when(view.id){
            R.id.choice_wc-> searchQuery="화장실"
            R.id.choice_movie-> searchQuery="영화관"
            R.id.choice_gas-> searchQuery="주유소"
            R.id.choice_ev-> searchQuery="전기차충전소"
            R.id.choice_pharmacy-> searchQuery="약국"
            R.id.choice_park-> searchQuery="공원"
            R.id.choice_food-> searchQuery="맛집"
            R.id.choice_01-> searchQuery="화장실"
            R.id.choice_02-> searchQuery="화장실"
            R.id.choice_03-> searchQuery="화장실"
            R.id.choice_04-> searchQuery="화장실"

        }

        //새로운 검색 요청
        searchPlaces()

        //검색창에 검색어글씨가 있다면 지우기
        binding.etSearch.text.clear()
        binding.etSearch.clearFocus()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when( item.itemId){
            R.id.menu_help -> Toast.makeText(this, "도움말", Toast.LENGTH_SHORT).show()
            R.id.menu_logout-> Toast.makeText(this, "로그아웃", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }



}