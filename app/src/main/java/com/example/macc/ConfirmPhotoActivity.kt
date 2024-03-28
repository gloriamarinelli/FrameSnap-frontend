package com.example.macc

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.ByteArrayOutputStream
import java.util.Base64

data class PostBackend(
    val username: String?,
    val imageData: String
)
data class PostBackendResponse(
    val message: String,
    val status: Int
)
interface CreatePostAPI {
    @POST("createPost")
    fun createPost(@Body request: PostBackend): Call<PostBackendResponse>
}
class ConfirmPhotoActivity : AppCompatActivity() {
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_photo_activity)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val photoImageView = findViewById<ImageView>(R.id.photoImageView)
        val savedUri = sharedPreferences.getString("savedUri", "")?.toUri()
        val bitmap = BitmapFactory.decodeFile(savedUri?.path)
        val matrix = Matrix()
        matrix.postRotate(0f)
        val rotatedBitmap = Bitmap.createBitmap(bitmap,0, 0, bitmap.width, bitmap.height, matrix, true)
        photoImageView.setImageBitmap(rotatedBitmap)

        val editor = sharedPreferences.edit()
        editor.remove("savedUri")
        editor.apply()

        // Puoi impostare l'immagine nella ImageView utilizzando un'immagine o un percorso dell'immagine
        // Ad esempio, se stai passando un'immagine Bitmap:
        // val bitmap: Bitmap = ...
        // photoImageView.setImageBitmap(bitmap)
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }

        val cancelButton = findViewById<ImageView>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            onCancelClick()
        }

        val acceptButton = findViewById<ImageView>(R.id.acceptButton)
        acceptButton.setOnClickListener {
            val username = sharedPreferences.getString("username", "")

            val byteArrayOutputStream = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            val base64String: String = Base64.getEncoder().encodeToString(byteArray)

            val request = PostBackend(username, base64String)

            val apiService = retrofit.create(CreatePostAPI::class.java)
            apiService.createPost(request).enqueue(object : Callback<PostBackendResponse> {
                override fun onResponse(call: Call<PostBackendResponse>, response: Response<PostBackendResponse>) {
                    try {
                        // Access the result using response.body()
                        val result: PostBackendResponse? = response.body()

                        // Check if the result is not null before accessing properties
                        result?.let { it ->
                            val status = it.status
                            if (status == 200) {
                                val ok = AlertDialog.Builder(this@ConfirmPhotoActivity)
                                ok.setTitle("Message")
                                    .setMessage("${it.message}")
                                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                        val editor = sharedPreferences.edit()
                                        editor.putString("window", "profile")
                                        editor.apply()

                                        val intent = Intent(this@ConfirmPhotoActivity, FeedActivity::class.java)
                                        startActivity(intent)
                                    })
                                    .show()
                            }
                            else {
                                val ko = AlertDialog.Builder(this@ConfirmPhotoActivity)
                                ko.setTitle("ERROR")
                                    .setMessage("${it.message}")
                                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                        // Do nothing :)
                                    })
                                    .show()
                            }
                        }
                    } catch (e: Exception) {
                        val ko = AlertDialog.Builder(this@ConfirmPhotoActivity)
                        ko.setTitle("ERROR")
                            .setMessage("${e.toString()}")
                            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                // Do nothing :)
                            })
                            .show()
                    }
                }
                override fun onFailure(call: Call<PostBackendResponse>, t: Throwable) {
                    val ko = AlertDialog.Builder(this@ConfirmPhotoActivity)
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

    private fun onAcceptClick() {
        val intent = Intent(this, ProfileFragment::class.java)
        startActivity(intent)
    }

    private fun onCancelClick() {
        // Logica da eseguire quando viene cliccato il pulsante di accettazione
        // Ad esempio, puoi gestire il salvataggio dell'immagine e altre azioni
        // Chiudi l'attività se necessario
        finish()
    }
}