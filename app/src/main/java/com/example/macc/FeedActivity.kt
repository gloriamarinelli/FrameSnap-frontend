package com.example.macc

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class Notification(val id: Int, val type: String, val text: String)
data class GetNotificationsResponse(val notifications: MutableList<Notification>, val status: Int)
interface GetNotificationsAPI {
    @GET("getNotifications")
    fun getNotifications(@Query("username") username: String?): Call<GetNotificationsResponse>
}

data class ReadNotificationRequest(val id: Int)
data class ReadNotificationResponse(val message: String, val status: Int)
interface ReadNotificationsAPI {
    @POST("readNotification")
    fun readNotification(@Body request: ReadNotificationRequest): Call<ReadNotificationResponse>
}

data class AcceptFriendshipRequestRequest(val notificationId: Int)
data class AcceptFriendshipRequestResponse(val message: String, val status: Int)
interface AcceptFriendshipRequestAPI {
    @POST("acceptFriendshipRequest")
    fun acceptFriendshipRequest(@Body request: AcceptFriendshipRequestRequest): Call<AcceptFriendshipRequestResponse>
}

data class RefuseFriendshipRequestRequest(val notificationId: Int)
data class RefuseFriendshipRequestResponse(val message: String, val status: Int)
interface RefuseFriendshipRequestAPI {
    @POST("refuseFriendshipRequest")
    fun refuseFriendshipRequest(@Body request: RefuseFriendshipRequestRequest): Call<RefuseFriendshipRequestResponse>
}

class FeedActivity: AppCompatActivity() {

    private lateinit var icon1: ImageView
    private lateinit var icon2: ImageView
    private lateinit var icon3: ImageView
    private lateinit var icon4: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var mainFragmentContainer: FrameLayout
    private lateinit var fragmentManager: FragmentManager
    private lateinit var  notificationIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val fragmentSharedPreferences = sharedPreferences.getString("window", "")
        var notifications: MutableList<Notification> = mutableListOf()

        notificationIcon = findViewById(R.id.notificationIcon)
        getUnreadNotificationsList { notificationsList ->
            notifications = notificationsList

            if(!(notificationsList.isEmpty())) {
                notificationIcon.setImageResource(R.drawable.active_bell)
            }
            else {
                notificationIcon.setImageResource(R.drawable.ic_bell)
            }
        }
        notificationIcon.setOnClickListener {
            showNotificationMenu(it, notifications)
        }


        mainFragmentContainer = findViewById(R.id.mainFragmentContainers)
        fragmentManager = supportFragmentManager

        val paintsFragment = PaintsFragment()
        setMainFragment(paintsFragment)

        if (fragmentSharedPreferences == "profile") {
            val editor = sharedPreferences.edit()
            editor.remove("window")
            editor.apply()

            val profileFragment = ProfileFragment()
            val username = sharedPreferences.getString("username", "")

            // Call for updating the UI
            apiService2.getProfileImage(username).enqueue(object :
                Callback<GetProfileImageResponse> {
                override fun onResponse(call: Call<GetProfileImageResponse>, response: Response<GetProfileImageResponse>) {
                    try {
                        // Access the result using response.body()
                        val result: GetProfileImageResponse? = response.body()

                        // Check if the result is not null before accessing properties
                        result?.let {
                            val status = it.status
                            if (status == 200) {
                                Log.d("FeedActivity", "RESPONSE OK")
                                val profileImageByteArray = it.profile_image
                                if (profileImageByteArray != null) {
                                    val originalBytes =
                                        Base64.decode(profileImageByteArray, Base64.DEFAULT)
                                    profileFragment.setProfileImage(originalBytes)
                                    profileFragment.setUsername(username)
                                    Log.d("FeedActivity", "UI updated.")
                                }
                                else {
                                    Log.e("FeedActivity", "profileImageByteArray is null!")
                                }
                            }
                            else {
                                Log.e("FeedActivity", "${status}")
                            }
                        }
                    } catch (e: Exception) {
                        // Do nothing
                        Log.e("FeedActivity", e.toString())
                    }
                }
                override fun onFailure(call: Call<GetProfileImageResponse>, t: Throwable) {
                    // Do nothing
                    Log.e("FeedActivity", "${t.message}")
                }
            })

            setMainFragment(profileFragment)
        }


        icon1 = findViewById(R.id.icon1)
        icon2 = findViewById(R.id.icon2)
        icon3 = findViewById(R.id.icon3)
        icon4 = findViewById(R.id.icon4)


        icon1.setOnClickListener {
            val friendsFragment = FriendsFragment()
            setMainFragment(friendsFragment)
        }

        icon2.setOnClickListener {
           setMainFragment(PaintsFragment())
        }


        icon3.setOnClickListener {
            val profileFragment = ProfileFragment()
            setMainFragment(profileFragment)
        }

        icon4.setOnClickListener {
            val mapFragment = MapFragment()
            setMainFragment(mapFragment)
        }
    }

    private fun setMainFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.mainFragmentContainers, fragment)
        transaction.commit()
    }

    private fun showNotificationMenu(view: View, notifications: MutableList<Notification>) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menu.clear() // Clear existing menu items

        // Inflate menu items dynamically based on the list
        for ((index, notification) in notifications.withIndex()) {
            popupMenu.menu.add(Menu.NONE, index, Menu.NONE, notification.text)
        }

        popupMenu.setOnMenuItemClickListener { item ->
            // Perform action based on selected item's index
            val selectedItemIndex = item.itemId
            val selectedNotification = notifications[selectedItemIndex]

            if (selectedNotification.type == "friendship_request") {
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Friendship request")
                    .setMessage(selectedNotification.text)
                    .setPositiveButton("ACCEPT", DialogInterface.OnClickListener {dialog, which ->
                        // Accept friendship request
                        val request = AcceptFriendshipRequestRequest(selectedNotification.id)
                        val acceptFriendshipRequestApiService = retrofit.create(AcceptFriendshipRequestAPI::class.java)

                        acceptFriendshipRequestApiService.acceptFriendshipRequest(request).enqueue(object :
                            Callback<AcceptFriendshipRequestResponse> {
                            override fun onResponse(call: Call<AcceptFriendshipRequestResponse>, response: Response<AcceptFriendshipRequestResponse>) {
                                try {
                                    // Access the result using response.body()
                                    val result: AcceptFriendshipRequestResponse? = response.body()

                                    // Check if the result is not null before accessing properties
                                    result?.let {
                                        val status = it.status
                                        if (status == 200) {

                                        }
                                        else {
                                            Log.e("FeedActivity", "${status}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Do nothing
                                    Log.e("FeedActivity", e.toString())
                                }
                            }
                            override fun onFailure(call: Call<AcceptFriendshipRequestResponse>, t: Throwable) {
                                // Do nothing
                                Log.e("FeedActivity", "${t.message}")
                            }
                        })

                    })
                    .setNegativeButton("REFUSE", DialogInterface.OnClickListener { dialog, which ->
                        // Refuse friendship request

                        val request = RefuseFriendshipRequestRequest(selectedNotification.id)
                        val refuseFriendshipRequestApiService = retrofit.create(RefuseFriendshipRequestAPI::class.java)

                        refuseFriendshipRequestApiService.refuseFriendshipRequest(request).enqueue(object :
                            Callback<RefuseFriendshipRequestResponse> {
                            override fun onResponse(call: Call<RefuseFriendshipRequestResponse>, response: Response<RefuseFriendshipRequestResponse>) {
                                try {
                                    // Access the result using response.body()
                                    val result: RefuseFriendshipRequestResponse? = response.body()

                                    // Check if the result is not null before accessing properties
                                    result?.let {
                                        val status = it.status
                                        if (status == 200) {

                                        }
                                        else {
                                            Log.e("FeedActivity", "${status}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Do nothing
                                    Log.e("FeedActivity", e.toString())
                                }
                            }
                            override fun onFailure(call: Call<RefuseFriendshipRequestResponse>, t: Throwable) {
                                // Do nothing
                                Log.e("FeedActivity", "${t.message}")
                            }
                        })

                    })
                    .show()
            }

            // Read notification
            val request = ReadNotificationRequest(selectedNotification.id)

            val readNotificationApiService = retrofit.create(ReadNotificationsAPI::class.java)
            readNotificationApiService.readNotification(request).enqueue(object :
                Callback<ReadNotificationResponse> {
                override fun onResponse(call: Call<ReadNotificationResponse>, response: Response<ReadNotificationResponse>) {
                    try {
                        // Access the result using response.body()
                        val result: ReadNotificationResponse? = response.body()

                        // Check if the result is not null before accessing properties
                        result?.let {
                            val status = it.status
                            if (status == 200) {
                                notifications.remove(selectedNotification)
                                Toast.makeText(this@FeedActivity, "Notification read!", Toast.LENGTH_SHORT).show()

                                if(notifications.isEmpty()) {
                                    notificationIcon.setImageResource(R.drawable.ic_bell)
                                }
                                else {}
                            }
                            else {
                                Log.e("FeedActivity", "${status}")
                            }
                        }
                    } catch (e: Exception) {
                        // Do nothing
                        Log.e("FeedActivity", e.toString())
                    }
                }
                override fun onFailure(call: Call<ReadNotificationResponse>, t: Throwable) {
                    // Do nothing
                    Log.e("FeedActivity", "${t.message}")
                }
            })

            true
        }

        popupMenu.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_notifications, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.notification_item -> {
                // Handle notification menu item click here if needed
                Toast.makeText(this, "Notification clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getUnreadNotificationsList(callback: (MutableList<Notification>) -> Unit) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")

        val getNotificationsApiService = retrofit.create(GetNotificationsAPI::class.java)
        getNotificationsApiService.getNotifications(username).enqueue(object :
            Callback<GetNotificationsResponse> {
            override fun onResponse(call: Call<GetNotificationsResponse>, response: Response<GetNotificationsResponse>) {
                try {
                    // Access the result using response.body()
                    val result: GetNotificationsResponse? = response.body()
                    var notifications: MutableList<Notification> = mutableListOf()

                    // Check if the result is not null before accessing properties
                    result?.let {
                        val status = it.status
                        if (status == 200) {
                            notifications = it.notifications
                        }
                        else {
                            Log.e("FeedActivity", "${status}")
                        }
                    }
                    callback(notifications)
                } catch (e: Exception) {
                    // Do nothing
                    Log.e("FeedActivity", e.toString())
                }
            }
            override fun onFailure(call: Call<GetNotificationsResponse>, t: Throwable) {
                // Do nothing
                Log.e("FeedActivity", "${t.message}")
            }
        })
    }
}