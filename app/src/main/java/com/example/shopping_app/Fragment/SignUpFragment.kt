package com.example.shopping_app.Fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.MemberInfoHelper
import com.example.shopping_app.ApiHelper.RegisterHelper
import com.example.shopping_app.ApiHelper.RegisterHelper.Companion.containsSpecialCharacter
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.FragmentShift.Companion.LOGIN_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.FragmentShift.Companion.MEMBER_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.FragmentShift.Companion.SIGNUP_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.Model
import com.example.shopping_app.MyApiManager
import com.example.shopping_app.R
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class SignUpFragment: Fragment() {
    private val myApiService = MyApiManager.myApiService
    private lateinit var memberInfoHelper: MemberInfoHelper
    private lateinit var fragmentShift: FragmentShift
    /* EditText */
    private lateinit var signUpEmail: EditText
    private lateinit var signUpPassword: EditText
    private lateinit var signUpUsername: EditText
    /* TextInputLayout */
    private lateinit var emailTextInputLayout: TextInputLayout
    private lateinit var passwordTextInputLayout: TextInputLayout
    private lateinit var usernameTextInputLayout: TextInputLayout
    /* TextView */
    private lateinit var logInRedirectText: TextView
    /* Button */
    private lateinit var signUpButton: Button
    private var colorhandler = Handler(Looper.myLooper()!!)
    /* String */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        fragmentShift.setNavigationBarViewVisibility(
            requireActivity(),
            R.id.bottomNavigation,
            View.VISIBLE
        )
        findView(view)
        textChangedListener()
        buttonClickListener()
        textViewClickListener()
        textViewTouchListener()
    }
    private fun findView(view: View) {
        signUpEmail = view.findViewById(R.id.signUpEmail)
        signUpPassword = view.findViewById(R.id.signUpPassword)
        signUpUsername = view.findViewById(R.id.signUpUsername)
        emailTextInputLayout = view.findViewById(R.id.emailTextInputLayout)
        passwordTextInputLayout = view.findViewById(R.id.passwordTextInputLayout)
        usernameTextInputLayout = view.findViewById(R.id.usernameTextInputLayout)
        logInRedirectText = view.findViewById(R.id.logInRedirectText)
        signUpButton = view.findViewById(R.id.signUpButton)
    }

    private fun textChangedListener() {
        /**  Text Changed Listener **/
        passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
        signUpEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    emailTextInputLayout.error  = getString(R.string.emailEmpty)
                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() && charCount > 0) {
                    emailTextInputLayout.error = getString(R.string.errorEmailMessage)
                } else {
                    emailTextInputLayout.error  = null
                }
            }
        })
        signUpUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    usernameTextInputLayout.error  = getString(R.string.usernameEmpty)
                } else if(containsSpecialCharacter(s.toString())) {
                    usernameTextInputLayout.error = getString(R.string.illegalCharacters)
                } else if(charCount in 1..2) {
                    usernameTextInputLayout.error  = getString(R.string.errorUsernameMessage)
                } else {
                    usernameTextInputLayout.error  = null
                }
            }
        })
        signUpPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    passwordTextInputLayout.error = getString(R.string.passwordEmpty)
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
                } else if(containsSpecialCharacter(s.toString())) {
                    passwordTextInputLayout.error = getString(R.string.illegalCharacters)
                } else if(charCount in 1..7) {
                    passwordTextInputLayout.error = getString(R.string.errorPasswordMessage)
                } else {
                    passwordTextInputLayout.error  = null
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
            }
        })
    }
    private fun buttonClickListener() {
        memberInfoHelper = MemberInfoHelper()
        signUpButton.setOnClickListener {
            /** Show progress indicators **/
            val builder = AlertDialog.Builder(requireContext())
            val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
            builder.setView(dialogView)
            val dialog: AlertDialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            // Prepare Map Data
            val requestMap = mapOf(
                Model.cName to signUpUsername.text.toString(),
                Model.cEmail to signUpEmail.text.toString(),
                Model.cPassword to signUpPassword.text.toString()
            )
            // Connect to Server and POST
            val call = myApiService.register(requestMap)
            call.enqueue(object : Callback<Model.MemberResponse> {
                override fun onResponse(call: Call<Model.MemberResponse>, response: Response<Model.MemberResponse>) {
                    if (response.isSuccessful) {
                        //成功
                        dialog.dismiss()
                        val infoList = response.body()
                        if (infoList != null) {
                            memberInfoHelper.createMemberInfoMap(infoList.cName, infoList.cNickName, infoList.cEmail,
                                infoList.cPhone, infoList.cAddr, infoList.cCountry, infoList.cBirthday, infoList.cSex, infoList.cImage)
                        }
                        CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.signUpSuccess))
                        RegisterHelper.memberLoginFlag = true
                        RegisterHelper.saveUserLogInData(
                            requireContext(),
                            signUpEmail.text.toString(),
                            signUpPassword.text.toString()
                        )
                        fragmentShift.returnBackStackFragment(requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG)
                    } else {
                        //失敗
                        dialog.dismiss()
                        val responseStatusCode = response.code()
                        CustomSnackbar.showSnackbar(view, requireContext(), responseStatusCode.toString())
                    }
                }
                override fun onFailure(call: Call<Model.MemberResponse>, t: Throwable) {
                    // 請求失敗
                    dialog.dismiss()
                    ItemInfoMap.getDataFlag = false
                    val message: String = when (t) {
                        is SocketTimeoutException -> getString(R.string.connectTimeOut)
                        is IOException -> getString(R.string.connectFailed)
                        else -> getString(R.string.serverError)
                    }
                    CustomSnackbar.showSnackbar(view, requireContext(), message)
                }
            })
        }
    }
    private fun textViewClickListener() {
        logInRedirectText.setOnClickListener{
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            val logInFragment = fragmentManager.findFragmentByTag(LOGIN_FRAGMENT_SHIFT_TAG)
            if( logInFragment != null) {
                fragmentManager.popBackStack(LOGIN_FRAGMENT_SHIFT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                transaction.commit()
            } else {
                fragmentShift.goToPreviousFragment(LoginFragment(), requireActivity(), SIGNUP_FRAGMENT_SHIFT_TAG, SIGNUP_FRAGMENT_SHIFT_TAG)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun textViewTouchListener() {
        logInRedirectText.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    when(v.id) {
                        R.id.logInRedirectText -> {
                            logInRedirectText.setTextColor(Color.GRAY)
                        }
                    }
                    // 延遲 2 秒後恢復原本的顏色
                    colorhandler.postDelayed({
                        when(v.id) {
                            R.id.logInRedirectText -> {
                                logInRedirectText.setTextColor(requireContext().getColor(R.color.textColor))
                            }
                        }
                    }, 600) // 0.6 秒後執行
                }
                MotionEvent.ACTION_UP -> {
                    // 取消延遲執行，防止在 2 秒內放開時顏色恢復的操作執行
                    colorhandler.removeCallbacksAndMessages(null)
                    when(v.id) {
                        R.id.logInRedirectText -> {
                            logInRedirectText.setTextColor(requireContext().getColor(R.color.textColor))
                        }
                    }
                }
            }
            false
        }
    }
}