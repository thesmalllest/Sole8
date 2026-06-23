package com.example.sole8

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sole8.models.api.UserProfileUpdate
import com.example.sole8.network.ApiClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.Calendar

class UserEditFragment : Fragment() {

    private lateinit var prefs: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_user_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefs = UserPreferences(requireContext())
        val user = prefs.getUser()

        val firstNameLayout = view.findViewById<TextInputLayout>(R.id.firstNameLayoutEdit)
        val lastNameLayout = view.findViewById<TextInputLayout>(R.id.lastNameLayoutEdit)
        val usernameLayout = view.findViewById<TextInputLayout>(R.id.usernameLayoutEdit)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.emailLayoutEdit)
        val birthLayout = view.findViewById<TextInputLayout>(R.id.birthLayoutEdit)

        val firstNameInput = view.findViewById<TextInputEditText>(R.id.editFirstNameInput)
        val lastNameInput = view.findViewById<TextInputEditText>(R.id.editLastNameInput)
        val usernameInput = view.findViewById<TextInputEditText>(R.id.editUsernameInput)
        val emailInput = view.findViewById<TextInputEditText>(R.id.editEmailInput)
        val birthInput = view.findViewById<TextInputEditText>(R.id.editBirthInput)

        val genderToggle = view.findViewById<MaterialButtonToggleGroup>(R.id.genderToggleEdit)
        val maleBtn = view.findViewById<MaterialButton>(R.id.maleBtnEdit)
        val femaleBtn = view.findViewById<MaterialButton>(R.id.femaleBtnEdit)

        firstNameInput.setText(user.firstName)
        lastNameInput.setText(user.lastName)
        usernameInput.setText(user.username)
        emailInput.setText(user.email)
        birthInput.setText(user.birthDate)

        birthInput.inputType = InputType.TYPE_NULL
        birthInput.isFocusable = false
        birthInput.isFocusableInTouchMode = false
        birthInput.isClickable = true

        fun applyGender(checkedId: Int) {
            if (checkedId == R.id.maleBtnEdit) {
                maleBtn.setBackgroundColor(requireContext().getColor(R.color.black))
                femaleBtn.setBackgroundColor(requireContext().getColor(R.color.dark_bg))
            } else {
                femaleBtn.setBackgroundColor(requireContext().getColor(R.color.black))
                maleBtn.setBackgroundColor(requireContext().getColor(R.color.dark_bg))
            }
        }

        if (user.gender == "M") {
            genderToggle.check(R.id.maleBtnEdit)
            applyGender(R.id.maleBtnEdit)
        } else {
            genderToggle.check(R.id.femaleBtnEdit)
            applyGender(R.id.femaleBtnEdit)
        }

        genderToggle.addOnButtonCheckedListener { _, id, isChecked ->
            if (isChecked) applyGender(id)
        }

        birthInput.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    birthInput.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val saveBtn = view.findViewById<Button>(R.id.saveProfileBtn)
        saveBtn.text = getString(R.string.save_changes)

        saveBtn.setOnClickListener {
            firstNameLayout.error = null
            lastNameLayout.error = null
            usernameLayout.error = null
            emailLayout.error = null
            birthLayout.error = null

            val first = firstNameInput.text?.toString()?.trim() ?: ""
            val last = lastNameInput.text?.toString()?.trim() ?: ""
            val username = usernameInput.text?.toString()?.trim() ?: ""
            val email = emailInput.text?.toString()?.trim() ?: ""
            val birth = birthInput.text?.toString()?.trim() ?: ""
            val selectedGenderId = genderToggle.checkedButtonId

            var ok = true

            if (!isValidName(first)) {
                firstNameLayout.error = getString(R.string.error_first_name)
                ok = false
            }

            if (!isValidName(last)) {
                lastNameLayout.error = getString(R.string.error_last_name)
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

            if (!isValidBirthDate(birth)) {
                birthLayout.error = getString(R.string.error_birth_young)
                ok = false
            }

            if (selectedGenderId == -1) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_gender),
                    Toast.LENGTH_SHORT
                ).show()

                ok = false
            }

            if (!ok) return@setOnClickListener

            val gender = if (selectedGenderId == R.id.maleBtnEdit) "M" else "F"

            val dto = UserProfileUpdate(
                firstName = first,
                lastName = last,
                birthDate = birth,
                gender = gender,
                username = username,
                email = email
            )

            lifecycleScope.launch {
                try {
                    ApiClient.userApi.updateProfile(dto)

                    val updated = user.copy(
                        firstName = first,
                        lastName = last,
                        username = username,
                        email = email,
                        gender = gender,
                        birthDate = birth
                    )

                    prefs.saveUserFromApi(updated)

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.profile_updated),
                        Toast.LENGTH_SHORT
                    ).show()

                    parentFragmentManager.popBackStack()

                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.profile_update_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun isValidName(value: String): Boolean {
        return value.matches(Regex("^[A-Za-zА-Яа-яЁё\\s\\-]{2,50}$"))
    }

    private fun isValidUsername(value: String): Boolean {
        return value.matches(Regex("^[A-Za-z0-9._]{3,20}$"))
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