package com.example.macc

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.styleup.FeedActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.regex.Pattern

fun isValidEmail(email: String): Boolean {
    val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$")
    return pattern.matcher(email).matches()
}

data class Person(val username: String, val email: String, val password: String)
data class CreateAccountResponse(val message: String, val status: Int)

interface RegisterAPI {
    @POST("register")
    fun createAccount(@Body person: Person): Call<CreateAccountResponse>
}

class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val nameEditText: EditText = findViewById(R.id.editTextName)
        val emailEditText: EditText = findViewById(R.id.editTextEmailCreate)
        val passwordEditText: EditText = findViewById(R.id.editTextPasswordCreate)
        val confirmPasswordEditText: EditText = findViewById(R.id.editTextConfirmPassword)
        val signUpButton: Button = findViewById(R.id.btnConfirmSignUp)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "")
        if (username != "") {
            val homepage = Intent(this, FeedActivity::class.java)
            startActivity(homepage)
        }

        // Call REST API logic
        val retrofit = Retrofit.Builder()
            .baseUrl(backendURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(RegisterAPI::class.java)

        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            val intent = Intent(this, LoginActivity::class.java)

            if (name != "" && email != "" && password != "" && confirmPassword != "") {
                if (password == confirmPassword) {
                    if (password.length >= 8) {
                        if(isValidEmail(email)) {
                            val person = Person(name, email, password)
                            apiService.createAccount(person).enqueue(object : Callback<CreateAccountResponse> {
                                override fun onResponse(call: Call<CreateAccountResponse>, response: Response<CreateAccountResponse>) {
                                    try {
                                        // Access the result using response.body()
                                        val result: CreateAccountResponse? = response.body()

                                        // Check if the result is not null before accessing properties
                                        result?.let {
                                            val status = it.status
                                            if (status == 200) {
                                                val ok = AlertDialog.Builder(this@CreateAccountActivity)
                                                ok.setTitle("Message")
                                                    .setMessage("${it.message}")
                                                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                                        startActivity(intent)
                                                    })
                                                    .show()
                                            }
                                            else {
                                                val ko = AlertDialog.Builder(this@CreateAccountActivity)
                                                ko.setTitle("ERROR")
                                                    .setMessage("${it.message}")
                                                    .setPositiveButton("OK", DialogInterface.OnClickListener {dialog, which ->
                                                        // Do nothing :)
                                                    })
                                                    .show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        val ko = AlertDialog.Builder(this@CreateAccountActivity)
                                        ko.setTitle("ERROR")
                                            .setMessage("${e.toString()}")
                                            .setPositiveButton("OK", DialogInterface.OnClickListener {dialog, which ->
                                                // Do nothing :)
                                            })
                                            .show()
                                    }
                                }
                                override fun onFailure(call: Call<CreateAccountResponse>, t: Throwable) {
                                    val ko = AlertDialog.Builder(this@CreateAccountActivity)
                                    ko.setTitle("ERROR")
                                        .setMessage("${t.message}")
                                        .setPositiveButton("OK", DialogInterface.OnClickListener {dialog, which ->
                                            // Do nothing :)
                                        })
                                        .show()
                                }
                            })
                        }
                        else {      // Email is not valid
                            val ko = AlertDialog.Builder(this)
                            ko.setTitle("ERROR")
                                .setMessage("The inserted email address is not valid. Try to follow the pattern try@example.com.")
                                .setPositiveButton("OK", DialogInterface.OnClickListener {dialog, which ->
                                    // Do nothing :)
                                })
                                .show()
                        }
                    }
                    else {      // The password is not long enough
                        val ko = AlertDialog.Builder(this)
                        ko.setTitle("ERROR")
                            .setMessage("The inserted password should be at least 8 characters long.")
                            .setPositiveButton("OK", DialogInterface.OnClickListener {dialog, which ->
                                // Do nothing :)
                            })
                            .show()
                    }
                }
                else {      // The inserted passwords do not match
                    val ko = AlertDialog.Builder(this)
                    ko.setTitle("ERROR")
                        .setMessage("The inserted password do not match.")
                        .setPositiveButton("OK", DialogInterface.OnClickListener {dialog, which ->
                            // Do nothing :)
                        })
                        .show()
                }
            }
            else {      // There is some empty field
                val ko = AlertDialog.Builder(this)
                ko.setTitle("ERROR")
                    .setMessage("There is some empty field.")
                    .setPositiveButton("OK", DialogInterface.OnClickListener {dialog, which ->
                        // Do nothing :)
                    })
                    .show()
            }
        }
    }
}