package com.example.sole8

import android.app.DatePickerDialog
import android.os.Bundle
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
import java.util.*

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
            val c = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d -> birthInput.setText(String.format("%02d/%02d/%04d", d, m + 1, y)) },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val saveBtn = view.findViewById<Button>(R.id.saveProfileBtn)
        saveBtn.text = "Save Changes"

        saveBtn.setOnClickListener {

            val first = firstNameInput.text.toString().trim()
            val last = lastNameInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val birth = birthInput.text.toString().trim()
            val gender = if (genderToggle.checkedButtonId == R.id.maleBtnEdit) "M" else "F"

            var ok = true
            if (first.isEmpty()) { firstNameLayout.error = "First name cannot be empty"; ok = false }
            if (last.isEmpty()) { lastNameLayout.error = "Last name cannot be empty"; ok = false }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = "Invalid email address"; ok = false
            }
            if (username.length < 3) { usernameLayout.error = "Username is too short"; ok = false }
            if (birth.isEmpty()) { birthLayout.error = "Birth date cannot be empty"; ok = false }

            if (!ok) return@setOnClickListener

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
                    val response = ApiClient.userApi.updateProfile(dto)

                    val updated = user.copy(
                        firstName = first,
                        lastName = last,
                        username = username,
                        email = email,
                        gender = gender,
                        birthDate = birth
                    )

                    prefs.saveUserFromApi(updated)

                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
