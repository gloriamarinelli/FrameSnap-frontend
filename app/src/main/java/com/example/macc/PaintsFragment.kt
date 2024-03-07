package com.example.macc

import android.os.Bundle
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

data class PaintBackend(val id: Int, val paint: String, val paintName: String)
data class GetPaintsResponse(val paints: List<PaintBackend>, val status: Int)
interface GetPaintAPI {
    @GET("getPaints")
    fun getPaints(): Call<GetPaintsResponse>
}

val apiGetPaints = retrofit.create(GetPaintAPI::class.java)

class PaintsFragment: Fragment() {

    private lateinit var recyclerView: RecyclerView


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

    }



