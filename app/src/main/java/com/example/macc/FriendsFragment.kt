package com.example.macc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class GetUsersResponse(val usernames: List<String>, val status: Int)
data class GetUsersByUsernameResponse(val usernames: List<String>, val status: Int)
interface GetUsersAPI {
    @GET("getUsers")
    fun getUsers(@Query("username") username: String?): Call<GetUsersResponse>
}
interface GetUsersByUsernameAPI {
    @GET("getUsersByUsername")
    fun getUsersByUsername(@Query("usernameSearch") usernameSearch: String?, @Query("myUsername") myUsername: String?): Call<GetUsersByUsernameResponse>
}

class FriendsFragment : Fragment() {

    private lateinit var searchView: SearchView

    private lateinit var friendRecyclerView: RecyclerView
    private lateinit var friendAdapter: FriendAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.friends_fragment, container, false)

        // Inizializza la RecyclerView
        friendRecyclerView = view.findViewById(R.id.friendRecyclerView)
        getUsersList { usersList ->
            friendAdapter = FriendAdapter(usersList) {userName ->
                // Click on user card
                openUserProfileActivity(userName)
            }
            friendRecyclerView.adapter = friendAdapter
            friendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }


        // Inizializza la variabile searchView
        searchView = view.findViewById(R.id.searchView)

        // Aggiungi un listener per la barra di ricerca
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Called on "enter"
                getSearchedUsersList(query) {returnedUsers ->
                    friendAdapter = FriendAdapter(returnedUsers) {userName ->
                        // Click on user card
                        openUserProfileActivity(userName)
                    }
                    friendRecyclerView.adapter = friendAdapter
                    friendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Do nothing
                return true
            }
        })
        return view
    }

    private fun getUsersList(callback: (MutableList<String>) -> Unit) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")

        val returnList: MutableList<String> = mutableListOf()

        val getUsersApiService = retrofit.create(GetUsersAPI::class.java)
        getUsersApiService.getUsers(username).enqueue(object : Callback<GetUsersResponse> {
            override fun onResponse(call: Call<GetUsersResponse>, response: Response<GetUsersResponse>) {
                try {
                    // Access the result using response.body()
                    val result: GetUsersResponse? = response.body()

                    // Check if the result is not null before accessing properties
                    result?.let {
                        val status = it.status
                        if (status == 200) {
                            for (name in it.usernames) {
                                returnList.add(name)
                            }
                        }
                    }
                    callback(returnList)
                } catch (e: Exception) {
                    // Do nothing
                    Log.e("ProfileFragment", e.toString())
                }
            }
            override fun onFailure(call: Call<GetUsersResponse>, t: Throwable) {
                // Do nothing
            }
        })
    }

    private fun getSearchedUsersList(submittedText: String?, callback: (MutableList<String>) -> Unit) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val myUsername = sharedPreferences.getString("username", "")

        val returnList: MutableList<String> = mutableListOf()

        val getUsersByUsernameApiService = retrofit.create(GetUsersByUsernameAPI::class.java)
        getUsersByUsernameApiService.getUsersByUsername(submittedText, myUsername).enqueue(object : Callback<GetUsersByUsernameResponse> {
            override fun onResponse(call: Call<GetUsersByUsernameResponse>, response: Response<GetUsersByUsernameResponse>) {
                try {
                    // Access the result using response.body()
                    val result: GetUsersByUsernameResponse? = response.body()

                    // Check if the result is not null before accessing properties
                    result?.let {
                        val status = it.status
                        if (status == 200) {
                            for (name in it.usernames) {
                                returnList.add(name)
                            }
                        }
                    }
                    callback(returnList)
                } catch (e: Exception) {
                    // Do nothing
                    Log.e("ProfileFragment", e.toString())
                }
            }
            override fun onFailure(call: Call<GetUsersByUsernameResponse>, t: Throwable) {
                // Do nothing
            }
        })
    }

    // Metodo per aprire l'activity del profilo dell'amico
    private fun openUserProfileActivity(friendName: String) {
        val intent = Intent(requireContext(), FriendProfileActivity::class.java)
        intent.putExtra("friendName", friendName)
        startActivity(intent)
    }
}