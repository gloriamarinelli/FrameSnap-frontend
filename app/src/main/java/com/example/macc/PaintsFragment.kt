package com.example.macc

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET

data class PaintBackend(val id: Int, val paint: String, val paintName: String, val paintYear: String)
data class GetPaintsResponse(val paints: List<PaintBackend>, val status: Int)
interface GetPaintsAPI {
    @GET("getPaints")
    fun getPaints(): Call<GetPaintsResponse>
}

val apiGetPaints = retrofit.create(GetPaintsAPI::class.java)

class PaintsFragment: Fragment(), PaintsAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var paintsAdapter: PaintsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.paints_fragment, container, false)

        //RecyclerView Initialization
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Get list of paints
        var paintsList: MutableList<PaintBackend> = mutableListOf()
        apiGetPaints.getPaints().enqueue(object : Callback<GetPaintsResponse> {
            override fun onResponse(call: Call<GetPaintsResponse>, response: Response<GetPaintsResponse>) {
                Log.d("PaintsFragment", "JSON Response: ${response.body()}")
                try {
                    // Access the result using response.body()
                    val result: GetPaintsResponse? = response.body()

                    // Check if the result is not null before accessing properties
                    result?.let {
                        val status = it.status
                        if (status == 200) {
                            paintsList.addAll(it.paints)
                            Log.d("PaintsFragment", "Paints added")
                            setupAdapter(paintsList)
                        }
                        else {
                            Log.e("PaintsFragment", "${it.status}")
                        }
                    }
                } catch (e: Exception) {
                    // Do nothing
                    Log.e("PaintsFragment", "[ERROR] "+e.toString())
                }
            }
            override fun onFailure(call: Call<GetPaintsResponse>, t: Throwable) {
                Log.e("PaintsFragment", "[ERR] ${t.message}")
                // retry here
            }
        })

        return view
    }
    private fun setupAdapter(paintsList: List<PaintBackend>) {
        val finalPaintsList: MutableList<Paint> = mutableListOf()

        for (paint in paintsList) {
            val id = paint.id
            val paint_bytes = Base64.decode(paint.paint, Base64.DEFAULT)
            val paint_bitmap: Bitmap? = BitmapFactory.decodeByteArray(paint_bytes, 0, paint_bytes!!.size)
            val paint_name = paint.paintName
            val paint_year = paint.paintYear

            val new_paint = Paint(id, paint_bitmap, paint_name, paint_year)
            finalPaintsList.add(new_paint)
        }

        // Initialize and set the adapter
        paintsAdapter = PaintsAdapter(finalPaintsList, object : PaintsAdapter.OnItemClickListener {
            override fun onItemClick(paint: Paint) {
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putInt("paintId", paint.id)
                editor.apply()

                val intent = Intent(requireContext(), CameraFragment::class.java)
                startActivity(intent)
            }
        })
        recyclerView.adapter = paintsAdapter
    }

    override fun onItemClick(paint: Paint) {
        // Necessary for avoiding errors; handled in a different part of the code
    }

}

