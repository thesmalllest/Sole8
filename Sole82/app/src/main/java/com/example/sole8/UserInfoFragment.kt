package com.example.sole8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import android.widget.Button
import com.example.sole8.models.api.UserProfileDto
import com.example.sole8.network.ApiClient
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch

class UserInfoFragment : Fragment() {

    private lateinit var prefs: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_user_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        prefs = UserPreferences(requireContext())
        val user = prefs.getUser()

        val firstName = view.findViewById<TextInputEditText>(R.id.userFirstNameInput)
        val lastName = view.findViewById<TextInputEditText>(R.id.userLastNameInput)
        val username = view.findViewById<TextInputEditText>(R.id.userUsernameInput)
        val email = view.findViewById<TextInputEditText>(R.id.userEmailInput)
        val birth = view.findViewById<TextInputEditText>(R.id.userBirthInput)

        val genderToggle = view.findViewById<MaterialButtonToggleGroup>(R.id.genderToggleInfo)
        val maleBtn = view.findViewById<MaterialButton>(R.id.maleBtnInfo)
        val femaleBtn = view.findViewById<MaterialButton>(R.id.femaleBtnInfo)

        fun applyUi(u: UserProfileDto) {
            firstName.setText(u.firstName)
            lastName.setText(u.lastName)
            username.setText("@${u.username}")
            email.setText(u.email)
            birth.setText(u.birthDate)

            if (u.gender == "M") {
                genderToggle.check(R.id.maleBtnInfo)
                maleBtn.setBackgroundColor(requireContext().getColor(R.color.black))
                femaleBtn.setBackgroundColor(requireContext().getColor(R.color.dark_bg))
            } else {
                genderToggle.check(R.id.femaleBtnInfo)
                femaleBtn.setBackgroundColor(requireContext().getColor(R.color.black))
                maleBtn.setBackgroundColor(requireContext().getColor(R.color.dark_bg))
            }
        }

        applyUi(user)

        lifecycleScope.launch {
            try {
                val fromApi = ApiClient.userApi.getProfile()

                prefs.saveUserFromApi(fromApi)
                applyUi(fromApi)

            } catch (_: Exception) {

            }
        }

        // EDIT BUTTON
        view.findViewById<Button>(R.id.editProfileBtn).setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.userFragmentContainer, UserEditFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
