package com.misit.faceidchecklogptabp.Masukan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.misit.abpenergy.api.ApiClient
import com.misit.abpenergy.api.ApiEndPoint
import com.misit.faceidchecklogptabp.Masukan.Adapter.MasukanAdapter
import com.misit.faceidchecklogptabp.Masukan.Response.DataItem
import com.misit.faceidchecklogptabp.Masukan.Response.MasukanModel
import com.misit.faceidchecklogptabp.Masukan.Response.MasukanResponse
import com.misit.faceidchecklogptabp.R
import com.misit.faceidchecklogptabp.Utils.PopupUtil
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_masukan.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MasukanActivity : AppCompatActivity(), View.OnClickListener,
MasukanAdapter.OnItemClickListener {
    private var adapter: MasukanAdapter? = null
    private var masukanList: ArrayList<String>? = null
    private var displayList:MutableList<DataItem>?=null
    private var call: Call<MasukanResponse>?=null
    private var page=1
    private var search= ""
    private var loading : Boolean=false
    var curentPosition: Int=0
    private var visibleItem : Int=0
    private var total : Int=0
    private var pastVisibleItem : Int=0
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masukan)
        title = "Masukan"
        masukanList = ArrayList()
        displayList = ArrayList()
        var actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        adapter = MasukanAdapter(
            this,
            displayList!!
        )
        val linearLayoutManager = LinearLayoutManager(this@MasukanActivity)
        rvMasukan?.layoutManager = linearLayoutManager
        rvMasukan.adapter =adapter
        adapter?.setListener(this)
        swipeRefreshLayout = findViewById(R.id.pullRefresh) as SwipeRefreshLayout

        swipeRefreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                page=1
                displayList?.clear()
                loadData(page,search)
            }
        })

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
    override fun onResume() {
        loadData(page,search)
        super.onResume()

    }
    private fun loadData(hal:Int,cari:String) {
        swipeRefreshLayout.isRefreshing=true
        PopupUtil.showLoading(this@MasukanActivity,"Loading","Mengambil Data!")
        val apiEndPoint = ApiClient.getClient(this@MasukanActivity)!!.create(ApiEndPoint::class.java)
        call = apiEndPoint.getListMasukan(hal,cari)
        call?.enqueue(object : Callback<MasukanResponse?> {
            override fun onFailure(call: Call<MasukanResponse?>, t: Throwable) {
                Toast.makeText(this@MasukanActivity, "Failed to Fetch Data\n" +
                        "e: $t", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing=false

            }

            override fun onResponse(
                call: Call<MasukanResponse?>,
                response: Response<MasukanResponse?>
            ) {
                val listMasukan = response.body()
                if(listMasukan!=null){
                    if (listMasukan.data != null) {
                        swipeRefreshLayout.isRefreshing=false

                        loading=true
                        if(displayList?.size==0){
                            displayList?.addAll(listMasukan.data!!)
                            this@MasukanActivity?.runOnUiThread {
                                adapter?.notifyDataSetChanged()
                                PopupUtil.dismissDialog()

                            }
                        }else{
                            curentPosition = (rvMasukan.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                            displayList?.addAll(listMasukan.data!!)
                            this@MasukanActivity?.runOnUiThread {
                                adapter?.notifyDataSetChanged()
                                PopupUtil.dismissDialog()
                            }
                        }
                        rvMasukan.addOnScrollListener(object :RecyclerView.OnScrollListener(){
                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                if (dy > 0) {
                                    visibleItem = recyclerView.layoutManager!!.childCount
                                    total = recyclerView.layoutManager!!.itemCount
                                    pastVisibleItem =
                                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                                    if (loading) {
                                        if ((visibleItem + pastVisibleItem) >= total) {
                                            if (page < listMasukan.lastPage!!) {
                                                loading = false
                                                page++
                                                loadData(page, search)
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }
        })

    }

    override fun onClick(v: View?) {

    }

    override fun onItemClick(idMasukan: Int?,nik:String?,nama:String?,masukan:String?,tgl:String?) {
//        Toasty.info(this@MasukanActivity, nik.toString()).show()
        val intent = Intent(this@MasukanActivity,MasukanDetailActivity::class.java)
        intent.putExtra(MasukanDetailActivity.NIK,nik)
        intent.putExtra(MasukanDetailActivity.NAMA,nama)
        intent.putExtra(MasukanDetailActivity.MASUKAN,masukan)
        intent.putExtra(MasukanDetailActivity.TANGGAL,tgl)
        startActivity(intent)
    }
}
