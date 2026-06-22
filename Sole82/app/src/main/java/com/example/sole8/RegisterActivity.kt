package com.example.sole8

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.sole8.models.api.UserRegisterRequest
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.Button
import android.widget.TextView
import com.example.sole8.network.ApiClient
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var prefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        prefs = UserPreferences(this)

        // Layouts
        val firstNameLayout = findViewById<TextInputLayout>(R.id.firstNameLayout)
        val lastNameLayout = findViewById<TextInputLayout>(R.id.lastNameLayout)
        val dateLayout = findViewById<TextInputLayout>(R.id.dateLayout)
        val usernameLayout = findViewById<TextInputLayout>(R.id.usernameLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passLayout = findViewById<TextInputLayout>(R.id.passwordLayout)
        val confirmPassLayout = findViewById<TextInputLayout>(R.id.confirmPasswordLayout)

        // Inputs
        val firstNameInput = findViewById<TextInputEditText>(R.id.firstNameInput)
        val lastNameInput = findViewById<TextInputEditText>(R.id.lastNameInput)
        val dateInput = findViewById<TextInputEditText>(R.id.dateInput)
        val usernameInput = findViewById<TextInputEditText>(R.id.usernameInput)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.confirmPasswordInput)

        val genderToggle = findViewById<MaterialButtonToggleGroup>(R.id.genderToggle)
        val maleBtn = findViewById<MaterialButton>(R.id.maleBtn)
        val femaleBtn = findViewById<MaterialButton>(R.id.femaleBtn)
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val backToLogin = findViewById<TextView>(R.id.backToLogin)

        // Gender button color logic
        genderToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.maleBtn -> {
                        maleBtn.setBackgroundColor(getColor(R.color.black))
                        femaleBtn.setBackgroundColor(getColor(R.color.dark_bg))
                    }
                    R.id.femaleBtn -> {
                        femaleBtn.setBackgroundColor(getColor(R.color.black))
                        maleBtn.setBackgroundColor(getColor(R.color.dark_bg))
                    }
                }
            }
        }

        // Registration button logic
        registerBtn.setOnClickListener {
            val firstName = firstNameInput.text?.toString()?.trim() ?: ""
            val lastName = lastNameInput.text?.toString()?.trim() ?: ""
            val birthDate = dateInput.text?.toString()?.trim() ?: ""
            val username = usernameInput.text?.toString()?.trim() ?: ""
            val email = emailInput.text?.toString()?.trim() ?: ""
            val password = passwordInput.text?.toString()?.trim() ?: ""
            val confirmPassword = confirmPasswordInput.text?.toString()?.trim() ?: ""
            val selectedGenderId = genderToggle.checkedButtonId

            var ok = true

            // First Name
            if (firstName.isEmpty()) {
                firstNameLayout.error = getString(R.string.error_first_name)
                ok = false
            } else firstNameLayout.error = null

            // Last Name
            if (lastName.isEmpty()) {
                lastNameLayout.error = getString(R.string.error_last_name)
                ok = false
            } else lastNameLayout.error = null

            // Gender
            if (selectedGenderId == -1) {
                Toast.makeText(this, getString(R.string.error_gender), Toast.LENGTH_SHORT).show()
                ok = false
            }

            // Date of Birth validation
            if (birthDate.isEmpty()) {
                dateLayout.error = getString(R.string.error_birth_empty)
                ok = false
            } else {
                val parts = birthDate.split("/")
                if (parts.size == 3) {
                    val d = parts[0].toInt()
                    val m = parts[1].toInt() - 1
                    val y = parts[2].toInt()

                    val today = Calendar.getInstance()
                    val dob = Calendar.getInstance()
                    dob.set(y, m, d)

                    // Age check (13+)
                    val age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
                    val ageAdjusted =
                        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR))
                            age - 1 else age

                    if (ageAdjusted < 13) {
                        dateLayout.error = getString(R.string.error_birth_young)
                        ok = false
                    } else dateLayout.error = null
                } else {
                    dateLayout.error = getString(R.string.error_birth_invalid)
                    ok = false
                }
            }

            // Username
            if (username.length < 3) {
                usernameLayout.error = getString(R.string.error_username_short)
                ok = false
            } else usernameLayout.error = null

            // Email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = getString(R.string.error_email_invalid)
                ok = false
            } else emailLayout.error = null

            // Password
            if (password.length < 4) {
                passLayout.error = getString(R.string.error_password_short)
                ok = false
            } else passLayout.error = null

            // Confirm password
            if (password != confirmPassword) {
                confirmPassLayout.error = getString(R.string.error_password_mismatch)
                ok = false
            } else confirmPassLayout.error = null

            if (!ok) return@setOnClickListener

            val gender = if (selectedGenderId == R.id.maleBtn) "M" else "F"

            val request = UserRegisterRequest(
                firstName = firstName,
                lastName = lastName,
                birthDate = birthDate,
                gender = gender,
                username = username,
                email = email,
                password = password
            )

            lifecycleScope.launch {
                try {
                    val response = ApiClient.userApi.register(request)

                    Toast.makeText(
                        this@RegisterActivity,
                        "Регистрация успешна!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Переход на логин
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


        }

        // Back to Login
        backToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
