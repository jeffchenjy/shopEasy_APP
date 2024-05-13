package com.example.shopping_app.Fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
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
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.MemberInfoHelper
import com.example.shopping_app.ApiHelper.RegisterHelper
import com.example.shopping_app.ApiHelper.RegisterHelper.Companion.saveUserLogInData
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

class LoginFragment: Fragment() {
    private val myApiService = MyApiManager.myApiService
    private lateinit var connectToServerHelper: ConnectToServerHelper
    private lateinit var memberInfoHelper: MemberInfoHelper
    private lateinit var fragmentShift: FragmentShift
    private lateinit var registerHelper: RegisterHelper
    private lateinit var view: View
    /* EditText */
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    /* TextInputLayout */
    private lateinit var emailTextInputLayout : TextInputLayout
    private lateinit var passwordTextInputLayout : TextInputLayout
    /* TextView */
    private lateinit var signUpRedirectText: TextView
    private lateinit var forgotPassword: TextView
    /* Button */
    private lateinit var loginButton: Button
    private var colorhandler = Handler(Looper.myLooper()!!)
    private lateinit var dialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        connectToServerHelper = ConnectToServerHelper()
        registerHelper = RegisterHelper()
        fragmentShift.setNavigationBarViewVisibility(
            requireActivity(),
            R.id.bottomNavigation,
            View.VISIBLE
        )
        findView(view)
        buttonClickListener()
        TextChangedListener()
        textViewClickListener()
        textViewTouchListener()
    }

    private fun findView(view: View) {
        loginEmail = view.findViewById(R.id.loginEmail)
        loginPassword = view.findViewById(R.id.loginPassword)
        emailTextInputLayout = view.findViewById(R.id.emailTextInputLayout)
        passwordTextInputLayout = view.findViewById(R.id.passwordTextInputLayout)
        signUpRedirectText = view.findViewById(R.id.signUpRedirectText)
        forgotPassword = view.findViewById(R.id.forgotPassword)
        loginButton = view.findViewById(R.id.loginButton)
    }
    private fun buttonClickListener() {
        view = requireView()
        memberInfoHelper = MemberInfoHelper()
        loginButton.setOnClickListener {
            if(loginEmail.text.isNotEmpty() && loginPassword.text.isNotEmpty()) {
                /** Show progress indicators **/
                val builder = AlertDialog.Builder(requireContext())
                val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
                builder.setView(dialogView)
                dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                //Prepare Map Data
                connectToServerHelper = ConnectToServerHelper(loginEmail.text.toString(),  loginPassword.text.toString())
                val requestMap = mapOf(
                    Model.cEmail to  loginEmail.text.toString(),
                    Model.cPassword to  loginPassword.text.toString()
                )
                val call = myApiService.memberLogin(requestMap)
                call.enqueue(object : Callback<Model.MemberResponse> {
                    override fun onResponse(call: Call<Model.MemberResponse>, response: Response<Model.MemberResponse>) {
                        if (response.isSuccessful) {
                            val infoList = response.body()
                            if (infoList != null) {
                                memberInfoHelper.createMemberInfoMap(infoList.cName, infoList.cNickName, infoList.cEmail,
                                    infoList.cPhone, infoList.cAddr, infoList.cCountry, infoList.cBirthday, infoList.cSex, infoList.cImage)
                            }
                            RegisterHelper.memberLoginFlag = true
                            saveUserLogInData(requireContext(), loginEmail.text.toString(), loginPassword.text.toString()) //Save User Login Data
                            connectToServerHelper.getMemberFavorite(view, requireContext()) {
                                connectToServerHelper.getOrder(view, requireContext(), null)
                                connectToServerHelper.getCartData(view, requireContext()) {
                                    fragmentShift.setNavigationBarViewCartBadge(requireActivity(), R.id.bottomNavigation)
                                    dialog.dismiss()
                                    CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.loginSuccess))
                                    fragmentShift.returnBackStackFragment(requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG)
                                }
                            }
                            //成功
                        } else {
                            //失敗
                            dialog.dismiss()
                            //val responseStatusCode = response.code()
                            CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.loginFailed))
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
            } else {
                emailTextInputLayout.error  = getString(R.string.emailEmpty)
                passwordTextInputLayout.error  = getString(R.string.passwordEmpty)
            }
        }
    }
    private fun TextChangedListener() {
        /**  Text Changed Listener **/
        passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
        loginEmail.addTextChangedListener(object : TextWatcher {
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
        loginPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    passwordTextInputLayout.error  = getString(R.string.passwordEmpty)
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
                } else if(RegisterHelper.containsSpecialCharacter(s.toString())) {
                    passwordTextInputLayout.error = getString(R.string.illegalCharacters)
                }
                else {
                    passwordTextInputLayout.error  = null
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
            }
        })
    }
    private fun textViewClickListener() {
        signUpRedirectText.setOnClickListener(textViewOnClickListener())
        forgotPassword.setOnClickListener(textViewOnClickListener())
    }
    private fun textViewOnClickListener() : View.OnClickListener?  {
        return View.OnClickListener {
            val view = it as? View
            val viewId = view?.id
            when(viewId) {
                R.id.signUpRedirectText -> {
                    val fragmentManager = requireActivity().supportFragmentManager
                    val transaction = fragmentManager.beginTransaction()
                    val signUpFragment = fragmentManager.findFragmentByTag(SIGNUP_FRAGMENT_SHIFT_TAG)
                    if(signUpFragment != null) {
                        fragmentManager.popBackStack(SIGNUP_FRAGMENT_SHIFT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        transaction.commit()
                    } else {
                        fragmentShift.goToNextFragment(SignUpFragment(), requireActivity(), LOGIN_FRAGMENT_SHIFT_TAG, LOGIN_FRAGMENT_SHIFT_TAG)
                    }
                }
                R.id.forgotPassword -> {
                    registerHelper.forgotPasswordCheckEmail(requireView(), requireContext(), requireActivity(), null)
                }
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun textViewTouchListener() {
        signUpRedirectText.setOnTouchListener(textViewOnTouchListener())
        forgotPassword.setOnTouchListener(textViewOnTouchListener())
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun textViewOnTouchListener(): View.OnTouchListener? {
        return View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    when(v.id) {
                        R.id.signUpRedirectText -> {
                            signUpRedirectText.setTextColor(Color.GRAY)
                        }
                        R.id.forgotPassword -> {
                            forgotPassword.setTextColor(Color.GRAY)
                        }
                    }
                    // 延遲 2 秒後恢復原本的顏色
                    colorhandler.postDelayed({
                        when(v.id) {
                            R.id.signUpRedirectText -> {
                                signUpRedirectText.setTextColor(requireContext().getColor(R.color.textColor))
                            }
                            R.id.forgotPassword -> {
                                forgotPassword.setTextColor(requireContext().getColor(R.color.textColor))
                            }
                        }
                    }, 600) // 0.6 秒後執行
                }
                MotionEvent.ACTION_UP -> {
                    // 取消延遲執行，防止在 2 秒內放開時顏色恢復的操作執行
                    colorhandler.removeCallbacksAndMessages(null)
                    when(v.id) {
                        R.id.signUpRedirectText -> {
                            signUpRedirectText.setTextColor(requireContext().getColor(R.color.textColor))
                        }
                        R.id.forgotPassword -> {
                            forgotPassword.setTextColor(requireContext().getColor(R.color.textColor))
                        }
                    }
                }
            }
            false
        }
    }


}