package com.example.styleup

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.macc.R
import com.example.macc.backendURL
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.ByteArrayOutputStream
import java.io.InputStream

data class Person2(val username: String, val email: String, val password: String, val profile_image: ByteArray?, val old_username: String?)
data class UpdateAccountResponse(val message: String, val status: Int)

interface UpdateAccountAPI {
    @POST("updateAccount")
    fun updateAccount(@Body person: Person2): Call<UpdateAccountResponse>
}

class ModAccActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_acc)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")

        val newUsernameEditText: EditText = findViewById(R.id.newUsernameEditText)
        val newEmailEditText: EditText = findViewById(R.id.newEmailEditText)
        val newPasswordEditText: EditText = findViewById(R.id.newPasswordEditText)
        val repeatPasswordEditText: EditText = findViewById(R.id.repeatPasswordEditText)

        var fragmentManager: FragmentManager = supportFragmentManager
        fun setMainFragment(fragment: Fragment) {
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.mainFragmentContainers, fragment)
            transaction.commit()
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        // For profile picture change
        var imageByteArray: ByteArray? = null
        lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
        val newProfilePictureImageView: ImageView = findViewById(R.id.profileImage)
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data
                // Now you can use the selectedImageUri to do something with the selected image
                if (selectedImageUri != null) {
                    newProfilePictureImageView.setImageURI(selectedImageUri)

                    // Convert the selected image to a byte array
                    val inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream?.read(buffer).also { length = it!! } != -1) {
                        byteArrayOutputStream.write(buffer, 0, length)
                    }
                    imageByteArray = byteArrayOutputStream.toByteArray()
                }
            }
        }
        val chooseImageButton: Button = findViewById(R.id.chooseImage)
        chooseImageButton.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // Use the ActivityResultLauncher to start the activity for result
            imagePickerLauncher.launch(galleryIntent)
        }

        // Call REST API logic
        val retrofit = Retrofit.Builder()
            .baseUrl(backendURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(UpdateAccountAPI::class.java)
        val apiService2 = retrofit.create(GetProfileImageAPI::class.java)

        apiService2.getProfileImage(username).enqueue(object : Callback<GetProfileImageResponse> {
            override fun onResponse(call: Call<GetProfileImageResponse>, response: Response<GetProfileImageResponse>) {

                fun setProfileImage(imageByteArray: ByteArray) {
                    val bitmap: Bitmap? = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                    newProfilePictureImageView.setImageBitmap(bitmap)
                }

                try {
                    // Access the result using response.body()
                    val result: GetProfileImageResponse? = response.body()

                    // Check if the result is not null before accessing properties
                    result?.let {
                        val status = it.status
                        if (status == 200) {
                            val profileImageByteArray = it.profile_image
                            if (profileImageByteArray != null) {
                                val originalBytes = Base64.decode(profileImageByteArray, Base64.DEFAULT)
                                setProfileImage(originalBytes)
                            }
                            else {
                                // Do nothing
                            }
                        }
                        else {
                            // Do nothing
                        }
                    }
                } catch (e: Exception) {
                    // Do nothing
                    Log.e("ProfileFragment", e.toString())
                }
            }
            override fun onFailure(call: Call<GetProfileImageResponse>, t: Throwable) {
                // Do nothing
            }
        })

        val confirmChangesButton: Button = findViewById(R.id.confirmChangesButton)
        confirmChangesButton.setOnClickListener {
            val newUsername = newUsernameEditText.text.toString()
            val newEmail = newEmailEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val repeatPassword = repeatPasswordEditText.text.toString()

            var canUpdate: Boolean = true

            if (newUsername != "" || newEmail != "" || newPassword != "" || repeatPassword != "" || imageByteArray != null) {
                if (newPassword != "" && repeatPassword != "") {
                    if (newPassword != repeatPassword) {
                        // LOG ERROR
                        canUpdate = false
                        val ko = AlertDialog.Builder(this@ModAccActivity)
                        ko.setTitle("ERROR")
                            .setMessage("The newly inserted passwords do not match!")
                            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                // Do nothing :)
                            })
                            .show()
                    }
                }
                else if (newPassword != "" && repeatPassword == "") {
                    // LOG ERROR
                    canUpdate = false
                    val ko = AlertDialog.Builder(this@ModAccActivity)
                    ko.setTitle("ERROR")
                        .setMessage("All the password fields must be filled!")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            // Do nothing :)
                        })
                        .show()
                }
            }
            else {
                // LOG ERROR: you should not press the button without any change
                canUpdate = false
                val ko = AlertDialog.Builder(this@ModAccActivity)
                ko.setTitle("ERROR")
                    .setMessage("There is no change!")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        // Do nothing :)
                    })
                    .show()
            }

            if (canUpdate) {
                Log.d("ModifyAccountActivity", imageByteArray.toString())
                val person = Person2(newUsername, newEmail, newPassword, imageByteArray, username)
                apiService.updateAccount(person).enqueue(object : Callback<UpdateAccountResponse> {
                    override fun onResponse(call: Call<UpdateAccountResponse>, response: Response<UpdateAccountResponse>) {
                        try {
                            // Access the result using response.body()
                            val result: UpdateAccountResponse? = response.body()

                            // Check if the result is not null before accessing properties
                            result?.let {
                                val status = it.status
                                if (status == 200) {
                                    if (newUsername != username) {
                                        // Modify login information
                                        val editor = sharedPreferences.edit()
                                        editor.remove("username")
                                        editor.apply()

                                        editor.putString("username", newUsername)
                                        editor.apply()
                                    }

                                    val ok = AlertDialog.Builder(this@ModAccActivity)
                                    ok.setTitle("Message")
                                        .setMessage("${it.message}")
                                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                            val editor = sharedPreferences.edit()
                                            editor.putString("window", "profile")
                                            editor.apply()

                                            val intent = Intent(this@ModAccActivity, FeedActivity::class.java)
                                            startActivity(intent)
                                        })
                                        .show()
                                }
                                else {
                                    val ko = AlertDialog.Builder(this@ModAccActivity)
                                    ko.setTitle("ERROR")
                                        .setMessage("${it.message}")
                                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                            // Do nothing :)
                                        })
                                        .show()
                                }
                            }
                        } catch (e: Exception) {
                            val ko = AlertDialog.Builder(this@ModAccActivity)
                            ko.setTitle("ERROR")
                                .setMessage("${e.toString()}")
                                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                    // Do nothing :)
                                })
                                .show()
                        }
                    }
                    override fun onFailure(call: Call<UpdateAccountResponse>, t: Throwable) {
                        val ko = AlertDialog.Builder(this@ModAccActivity)
                        ko.setTitle("ERROR")
                            .setMessage("${t.message}")
                            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                // Do nothing :)
                            })
                            .show()
                    }
                })
            }
        }
    }
}
