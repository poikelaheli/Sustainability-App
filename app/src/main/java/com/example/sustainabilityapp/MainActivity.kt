package com.example.sustainabilityapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sustainabilityapp.databinding.ActivityMainBinding
import com.example.sustainabilityapp.ui.theme.SustainabilityAppTheme

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*setContent {
            SustainabilityAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }*/
    }

    fun onClick(v: View?) {

        when (v?.id) {
            R.id.openLogin -> {
                updateLayoutVisibility("openLogin")
            }
            R.id.openRegistration -> {
                updateLayoutVisibility("openRegistration")
            }
            R.id.returnButton -> {
                updateLayoutVisibility("returnButton")
            }
            R.id.loginFormButton -> {
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainer, HomeFragment())
                fragmentTransaction.commit()
            }
            R.id.regFormButton -> {
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainer, HomeFragment())
                fragmentTransaction.commit()
            }
            R.id.logoutButton -> {
                val fragmentManager = supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainer, LoginRegistrationFragment())
                fragmentTransaction.commit()
            }
        }
    }

    fun updateLayoutVisibility (buttonName: String) {
        val loginButtons: LinearLayout = this.findViewById<LinearLayout>(R.id.loginButtons)
        val loginForm: LinearLayout = this.findViewById<LinearLayout>(R.id.loginForm)
        val registrationForm: LinearLayout = this.findViewById<LinearLayout>(R.id.registrationForm)
        val navigationButtons: LinearLayout = this.findViewById<LinearLayout>(R.id.navigationButtons)
        when (buttonName) {
            "openLogin" -> {
                loginButtons.visibility = View.GONE
                loginForm.visibility = View.VISIBLE
                registrationForm.visibility = View.GONE
                navigationButtons.visibility = View.VISIBLE
            }

            "openRegistration" -> {
                loginButtons.visibility = View.GONE
                loginForm.visibility = View.GONE
                registrationForm.visibility = View.VISIBLE
                navigationButtons.visibility = View.VISIBLE
            }

            "returnButton" -> {
                loginButtons.visibility = View.VISIBLE
                loginForm.visibility = View.GONE
                registrationForm.visibility = View.GONE
                navigationButtons.visibility = View.INVISIBLE
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SustainabilityAppTheme {
        Greeting("Android")
    }
}