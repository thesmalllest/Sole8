package com.example.sole8

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.sole8.models.api.UserRegisterRequest
import com.example.sole8.network.ApiClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.Calendar

class RegisterActivity : BaseActivity() {

    private lateinit var prefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        prefs = UserPreferences(this)

        val firstNameLayout = findViewById<TextInputLayout>(R.id.firstNameLayout)
        val lastNameLayout = findViewById<TextInputLayout>(R.id.lastNameLayout)
        val dateLayout = findViewById<TextInputLayout>(R.id.dateLayout)
        val usernameLayout = findViewById<TextInputLayout>(R.id.usernameLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passLayout = findViewById<TextInputLayout>(R.id.passwordLayout)
        val confirmPassLayout = findViewById<TextInputLayout>(R.id.confirmPasswordLayout)

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

        dateInput.inputType = InputType.TYPE_NULL
        dateInput.isFocusable = false
        dateInput.isFocusableInTouchMode = false
        dateInput.isClickable = true

        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    dateInput.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

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

        registerBtn.setOnClickListener {
            firstNameLayout.error = null
            lastNameLayout.error = null
            dateLayout.error = null
            usernameLayout.error = null
            emailLayout.error = null
            passLayout.error = null
            confirmPassLayout.error = null

            val firstName = firstNameInput.text?.toString()?.trim() ?: ""
            val lastName = lastNameInput.text?.toString()?.trim() ?: ""
            val birthDate = dateInput.text?.toString()?.trim() ?: ""
            val username = usernameInput.text?.toString()?.trim() ?: ""
            val email = emailInput.text?.toString()?.trim() ?: ""
            val password = passwordInput.text?.toString() ?: ""
            val confirmPassword = confirmPasswordInput.text?.toString() ?: ""
            val selectedGenderId = genderToggle.checkedButtonId

            var ok = true

            if (!isValidName(firstName)) {
                firstNameLayout.error = getString(R.string.error_first_name)
                ok = false
            }

            if (!isValidName(lastName)) {
                lastNameLayout.error = getString(R.string.error_last_name)
                ok = false
            }

            if (selectedGenderId == -1) {
                Toast.makeText(
                    this,
                    getString(R.string.error_gender),
                    Toast.LENGTH_SHORT
                ).show()

                ok = false
            }

            if (!isValidBirthDate(birthDate)) {
                dateLayout.error = getString(R.string.error_birth_young)
                ok = false
            }

            if (!isValidUsername(username)) {
                usernameLayout.error = getString(R.string.error_username_short)
                ok = false
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = getString(R.string.error_email_invalid)
                ok = false
            }

            if (!isValidPassword(password)) {
                passLayout.error = getString(R.string.error_password_short)
                ok = false
            }

            if (password != confirmPassword) {
                confirmPassLayout.error = getString(R.string.error_password_mismatch)
                ok = false
            }

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
                    ApiClient.userApi.register(request)

                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.register_success),
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.register_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        backToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun isValidName(value: String): Boolean {
        return value.matches(Regex("^[A-Za-zА-Яа-яЁё\\s\\-]{2,50}$"))
    }

    private fun isValidUsername(value: String): Boolean {
        return value.matches(Regex("^[A-Za-z0-9._]{3,20}$"))
    }

    private fun isValidPassword(value: String): Boolean {
        if (value.length < 8) return false

        val hasLetter = value.any { it.isLetter() }
        val hasDigit = value.any { it.isDigit() }

        return hasLetter && hasDigit
    }

    private fun isValidBirthDate(value: String): Boolean {
        return try {
            val parts = value.split("/")
            if (parts.size != 3) return false

            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            if (month !in 1..12) return false
            if (day !in 1..31) return false

            val today = Calendar.getInstance()

            val birthDate = Calendar.getInstance().apply {
                isLenient = false
                set(year, month - 1, day)
                time
            }

            if (birthDate.after(today)) return false

            var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)

            if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                age--
            }

            age >= 13

        } catch (e: Exception) {
            false
        }
    }
}