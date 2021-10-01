package com.misit.faceidchecklogptabp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.Adapter.AbsenAdapter
import com.misit.faceidchecklogptabp.Response.DataItem
import com.misit.faceidchecklogptabp.Response.ListAbsenResponse
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_index.*
import kotlinx.android.synthetic.main.activity_lihat_absen.*
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class LihatAbsenActivity : AppCompatActivity(), AbsenAdapter.OnItemClickListener {
    private var adapter: AbsenAdapter? = null
    private var absenList: MutableList<DataItem>? = null
    private var absenListFilter: MutableList<DataItem>? = null
    private var nik: String? = null
    private var page : Int=1
    private var visibleItem : Int=0
    private var loading : Boolean=false
    private var total : Int=0
    private var pastVisibleItem : Int=0
    lateinit var rvLoading:RelativeLayout
    lateinit var rvListAbsen: RecyclerView
    var curentPosition: Int=0
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var mAdView : AdView
    private var mFirebaseAnalytics: FirebaseAnalytics? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Lihat Data Telah Absen"
        setContentView(R.layout.activity_lihat_absen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        swipeRefreshLayout = findViewById(R.id.lihatPullRefresh) as SwipeRefreshLayout
        ABSEN="Masuk"
        nik = intent.getStringExtra(NIK)
        absenList = ArrayList()
        absenListFilter = ArrayList()
        adapter= AbsenAdapter(this,absenList!!)
        rvLoading = findViewById(R.id.relativeLoading)
        rvListAbsen = findViewById(R.id.rvListAbsen)
        rvListAbsen.layoutManager = GridLayoutManager(this,2)
        rvListAbsen.adapter= adapter
        adapter?.setListener(this)
        swipeRefreshLayout.setOnRefreshListener(object :SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                page=1
                absenListFilter!!.clear()
                absenList!!.clear()
                loadAbsen(page, ABSEN)
            }

        })
        MobileAds.initialize(this) {}

        val tabLayout =  findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                page=1
                absenList?.clear()
                absenListFilter?.clear()
                if(p0!!.position==0){
                    ABSEN="Masuk"
                    loadAbsen(page, ABSEN)
                }else if(p0!!.position==1){
                    ABSEN="Pulang"
                    loadAbsen(page, ABSEN)
                }
            }

        })

    }

    override fun onResume() {
        loadAbsen(page, ABSEN)
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adViewLIHATABSEN)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode : Int) {
                Log.d("errorCode",errorCode.toString())
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onResume()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search,menu)
        val menuItem = menu!!.findItem(R.id.searchAbsen)
        val searchView = menuItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText.let {
                    filter(it!!.toLowerCase())
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }
    private fun filter(search: String) {
        if (search.isNotEmpty()){
            absenList?.clear()
            absenListFilter?.forEach {
                var tanggal = LocalDate.parse(it.tanggal)
                var fmt: DateTimeFormatter = DateTimeFormat.forPattern("d MMMM yyyy")
                var tgl = tanggal?.toString(fmt)?.toLowerCase(Locale.getDefault())
                if(tgl!!.contains(search)){
                    absenList?.add(it)
                }
                rvListAbsen.adapter?.notifyDataSetChanged()
            }
        }else{
            absenList?.clear()
            absenList?.addAll(absenListFilter!!)
            rvListAbsen.adapter?.notifyDataSetChanged()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
    private fun loadAbsen(hal:Int,absen:String) {
        try {
            swipeRefreshLayout.isRefreshing=true
            val apiEndPoint = ApiClient.getClient(this)!!.create(ApiEndPoint::class.java)
            val call = apiEndPoint.getAbsen(nik!!, absen,hal)
            call?.enqueue(object : Callback<ListAbsenResponse?> {
                override fun onFailure(call: Call<ListAbsenResponse?>, t: Throwable) {
                    swipeRefreshLayout.isRefreshing=false

                }
                override fun onResponse(
                    call: Call<ListAbsenResponse?>,
                    response: Response<ListAbsenResponse?>
                ) {
                    val absenRes = response.body()
                    if(absenRes!=null){
                        swipeRefreshLayout.isRefreshing=false
                        rvLoading.visibility=View.GONE
                        loading=true
                        if(absenList?.size==0){
                            absenRes.listAbsen?.data?.forEach {
                                if(it!!.status== absen){
                                    absenListFilter?.add(it!!)
                                    absenList?.add(it!!)
                                }
                            }

                            adapter?.notifyDataSetChanged()
                        }else{
                            curentPosition = (rvListAbsen.layoutManager as GridLayoutManager).findLastVisibleItemPosition()
                            absenRes.listAbsen?.data?.forEach {
                                if(it!!.status==absen){
                                    absenListFilter?.add(it!!)
                                    absenList?.add(it!!)
                                }
                            }
                            adapter?.notifyDataSetChanged()
                        }
                        rvListAbsen.addOnScrollListener(object:RecyclerView.OnScrollListener(){
                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                super.onScrolled(recyclerView, dx, dy)
                                if(dy>0){
                                    visibleItem = (recyclerView.layoutManager as GridLayoutManager).childCount
                                    total=(recyclerView.layoutManager as GridLayoutManager).itemCount
                                    pastVisibleItem=(recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
                                    if(loading) {
                                        if ((visibleItem + pastVisibleItem) >= total) {
                                            //Toasty.info(this@LihatAbsenActivity,visibleItem.toString(),Toasty.LENGTH_SHORT).show()
                                            //rvLoading.visibility=View.VISIBLE

                                            if (page < absenRes.listAbsen?.lastPage!!){
                                                loading = false
                                                page++
                                                loadAbsen(page, ABSEN)
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                }

            })
        }catch (e:Exception){
            Log.e("ErrorLihatAbsen","Cek loadAbsen :${e.message}")
            loadAbsen(page, ABSEN)
        }catch (e:InterruptedException){
            Log.e("ErrorLihatAbsen","Cek loadAbsen :${e.message}")
            loadAbsen(page, ABSEN)
        }

    }

    override fun onItemClick(nik: String?,nama: String?,gambar: String?,tgl: String?,jam: String?,status: String?,lat: String?,lng: String?) {
        var intent = Intent(this@LihatAbsenActivity,ViewImageActivity::class.java)
        intent.putExtra(ViewImageActivity.NIK,nik)
        intent.putExtra(ViewImageActivity.GAMBAR,gambar)
        intent.putExtra(ViewImageActivity.NAMA,nama)
        intent.putExtra(ViewImageActivity.TGL,tgl)
        intent.putExtra(ViewImageActivity.JAM,jam)
        intent.putExtra(ViewImageActivity.STATUS,status)
        intent.putExtra(ViewImageActivity.LAT,lat)
        intent.putExtra(ViewImageActivity.LNG,lng)
        startActivity(intent)
    }

    companion object{
        var NIK = "NIK"
        var ABSEN = "Masuk"

    }
}
