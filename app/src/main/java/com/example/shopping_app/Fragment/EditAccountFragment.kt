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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.MemberInfoHelper
import com.example.shopping_app.ApiHelper.MemberInfoHelper.Companion.memberInfoMap
import com.example.shopping_app.ApiHelper.RegisterHelper
import com.example.shopping_app.ApiHelper.RegisterHelper.Companion.containsSpecialCharacter
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.EncryptionUtils
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesEmailKey
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesPasswordKey
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.FragmentShift.Companion.MEMBER_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.Model
import com.example.shopping_app.MyApiManager
import com.example.shopping_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class EditAccountFragment: Fragment() {
    private val myApiService = MyApiManager.myApiService
    private lateinit var encryptionUtils: EncryptionUtils
    private lateinit var memberInfoHelper: MemberInfoHelper
    private lateinit var fragmentShift: FragmentShift
    private lateinit var registerHelper: RegisterHelper
    /*  About ToolBar */
    private lateinit var toolbar: Toolbar
    /* EditText */
    private lateinit var editUsername: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    /* TextInputLayout */
    private lateinit var emailTextInputLayout : TextInputLayout
    private lateinit var usernameTextInputLayout : TextInputLayout
    private lateinit var passwordTextInputLayout : TextInputLayout
    /* TextView */
    private lateinit var forgotPassword: TextView
    private var colorhandler = Handler(Looper.myLooper()!!)
    /* Button */
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var savebuttonCardView: CardView
    /* String */
    private lateinit var newUsername: String
    private lateinit var newEmail: String
    private lateinit var newPassword: String
    private lateinit var currentUserPassword: String
    private lateinit var currentUserName: String
    private lateinit var currentUserEmail: String
    /* checkEditText Loop */
    private val handler = Handler(Looper.myLooper()!!)
    private val checkEditTexts = Runnable {
        if((editEmail.text.toString() == currentUserEmail || editEmail.text.isEmpty()) &&
            (editUsername.text.toString() == currentUserName || editUsername.text.isEmpty())
            && editPassword.text.isEmpty()) {
            saveButton.isEnabled = false
            savebuttonCardView.visibility = View.VISIBLE
        } else {
            saveButton.isEnabled = true
            savebuttonCardView.visibility = View.GONE
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        fragmentShift.setNavigationBarViewVisibility(
            requireActivity(),
            R.id.bottomNavigation,
            View.GONE
        )
        findView(view)
        setToolbar()
        initData()
        buttonClickListener()
        textChangedListener()
        textViewTouchListener()
        forgotPassword.setOnClickListener {
            registerHelper.forgotPasswordCheckEmail(view, requireContext(), requireActivity()) { isSuccess ->
                if (isSuccess) {
                    needReLogin {
                        /* 清除與會員相關資料 */
                        encryptionUtils.removeData(SharedPreferencesEmailKey)
                        encryptionUtils.removeData(SharedPreferencesPasswordKey)
                        ItemInfoMap.memberFavoriteMap.clear()
                        memberInfoMap.clear()
                        RegisterHelper.memberLoginFlag = false
                        ItemInfoMap.cartMap.clear()
                        ItemInfoMap.cartQuantity = 0
                        ItemInfoMap.memberOrders = emptyList()
                        fragmentShift.setNavigationBarViewCartBadge(requireActivity(), R.id.bottomNavigation)
                        fragmentShift.returnBackStackFragment(requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG)
                    }
                }
            }
        }

    }
    private fun findView(view: View){
        toolbar = view.findViewById(R.id.toolbar)
        editUsername = view.findViewById(R.id.editUsername)
        editEmail = view.findViewById(R.id.editEmail)
        editPassword = view.findViewById(R.id.editPassword)
        emailTextInputLayout = view.findViewById(R.id.emailTextInputLayout)
        usernameTextInputLayout = view.findViewById(R.id.usernameTextInputLayout)
        passwordTextInputLayout = view.findViewById(R.id.passwordTextInputLayout)
        forgotPassword = view.findViewById(R.id.forgotPassword)
        saveButton = view.findViewById(R.id.saveButton)
        deleteButton = view.findViewById(R.id.deleteButton)
        savebuttonCardView = view.findViewById(R.id.savebuttonCardView)
    }
    private fun setToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.editAccount)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            fragmentShift.returnBackStackFragment(requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG)
        }
    }
    private fun initData() {
        editUsername.setText(memberInfoMap[Model.cName].toString())
        editEmail.setText(memberInfoMap[Model.cEmail].toString())
        currentUserEmail = memberInfoMap[Model.cEmail].toString()
        currentUserName = memberInfoMap[Model.cName].toString()
        encryptionUtils = EncryptionUtils(requireContext())
        memberInfoHelper = MemberInfoHelper()
        registerHelper = RegisterHelper()
        currentUserPassword = encryptionUtils.getDecryptedData(SharedPreferencesPasswordKey).toString()
    }
    private fun buttonClickListener() {
        saveButton.setOnClickListener(buttonOnClickListener())
        deleteButton.setOnClickListener(buttonOnClickListener())
    }
    private fun buttonOnClickListener() : View.OnClickListener {
        return View.OnClickListener {
            val view = it as? View
            val viewId = view?.id
            when(viewId) {
                R.id.saveButton -> {
                    saveButtonAction()
                }
                R.id.deleteButton -> {
                    deleteDialog()
                }
            }
        }
    }
    private fun textChangedListener() {
        /**  Text Changed Listener **/
        passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
        editEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    emailTextInputLayout.error  = null
                    editEmail.error = getString(R.string.emailEmpty)
                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() && charCount > 0) {
                    emailTextInputLayout.error = getString(R.string.errorEmailMessage)
                } else if(email == (currentUserEmail)) {
                    emailTextInputLayout.error = null
                } else {
                    emailTextInputLayout.error  = null
                }
                handler.post(checkEditTexts)
            }
        })
        editUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                val username = s.toString()
                if (s.isNullOrEmpty()) {
                    usernameTextInputLayout.error  = null
                    editUsername.error = getString(R.string.usernameEmpty)
                } else if(containsSpecialCharacter(s.toString())) {
                    usernameTextInputLayout.error = getString(R.string.illegalCharacters)
                } else if(charCount in 1..2) {
                    usernameTextInputLayout.error  = getString(R.string.errorUsernameMessage)
                } else if(username == currentUserName) {
                    usernameTextInputLayout.error  = null
                } else {
                    usernameTextInputLayout.error  = null
                }
                handler.post(checkEditTexts)
            }
        })
        editPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    passwordTextInputLayout.error = null
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
                } else if(containsSpecialCharacter(s.toString())) {
                    passwordTextInputLayout.error = getString(R.string.illegalCharacters)
                } else if(charCount in 1..7) {
                    passwordTextInputLayout.error = getString(R.string.errorPasswordMessage)
                } else {
                    passwordTextInputLayout.error  = null
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
                handler.post(checkEditTexts)
            }
        })
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun textViewTouchListener() {
        forgotPassword.setOnTouchListener(textViewOnTouchListener())
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun textViewOnTouchListener(): View.OnTouchListener? {
        return View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    when(v.id) {
                        R.id.forgotPassword -> {
                            forgotPassword.setTextColor(Color.GRAY)
                        }
                    }
                    // 延遲 2 秒後恢復原本的顏色
                    colorhandler.postDelayed({
                        when(v.id) {
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
                        R.id.forgotPassword -> {
                            forgotPassword.setTextColor(requireContext().getColor(R.color.textColor))
                        }
                    }
                }
            }
            false
        }
    }
    private fun saveButtonAction() {
        newEmail = editEmail.text.toString()
        newUsername = editUsername.text.toString()
        newPassword = if(editPassword.text.isEmpty()) {
            currentUserPassword
        } else {
            editPassword.text.toString()
        }
        if(Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() && newUsername.length >= 3 && newPassword.length >= 8) {
            /** Check Password **/
            val builder_check = AlertDialog.Builder(requireContext())
            val dialogView_check: View = layoutInflater.inflate(R.layout.dialog_passwd_verify, null)
            val passwordBox = dialogView_check.findViewById<EditText>(R.id.passwordBox)
            builder_check.setView(dialogView_check)
            val dialog_check: AlertDialog = builder_check.create()
            dialogView_check.findViewById<View>(R.id.btnCheck).setOnClickListener(View.OnClickListener {
                val userPassword = passwordBox.text.toString()
                if (userPassword.equals(currentUserPassword)) {
                    dialog_check.dismiss()
                    prepareDataAndRequest()
                } else {
                    passwordBox.error = getString(R.string.passwordError)
                }
            })
            dialogView_check.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog_check.dismiss() }
            if (dialog_check.window != null) {
                dialog_check.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog_check.show()
        }
    }
    private fun prepareDataAndRequest() {
        /** Show progress indicators **/
        val builder = AlertDialog.Builder(requireContext())
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
        builder.setView(dialogView)
        val dialog: AlertDialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        // Prepare Data
        val requestMap = mapOf(
            Model.cEmail to newEmail,
            Model.cName to newUsername,
            Model.cPassword to newPassword
        )
        val call  = myApiService.editMemberInfo(requestMap)
        call.enqueue(object : Callback<Model.MemberResponse> {
            override fun onResponse(
                call: Call<Model.MemberResponse>,
                response: Response<Model.MemberResponse>
            ) {
                if (response.isSuccessful) {
                    dialog.dismiss()
                    CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.dataUpdateSuccess))
                    encryptionUtils.saveEncryptedData(SharedPreferencesEmailKey, newEmail)
                    encryptionUtils.saveEncryptedData(SharedPreferencesPasswordKey, newPassword)
                    memberInfoMap.clear()
                    val infoList = response.body()
                    if (infoList != null) {
                        memberInfoHelper.createMemberInfoMap(infoList.cName, infoList.cNickName, infoList.cEmail,
                            infoList.cPhone, infoList.cAddr, infoList.cCountry, infoList.cBirthday, infoList.cSex, infoList.cImage)
                    }
                    fragmentShift.returnBackStackFragment(requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG)
                } else {
                    dialog.dismiss()
                    //val responseStatusCode = response.code()
                    CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.dataUpdateFailed))
                }
            }
            override fun onFailure(call: Call<Model.MemberResponse>, t: Throwable) {
                dialog.dismiss()
                val message: String = when (t) {
                    is SocketTimeoutException -> getString(R.string.connectTimeOut)
                    is IOException -> getString(R.string.connectFailed)
                    else -> getString(R.string.serverError)
                }
                CustomSnackbar.showSnackbar(view, requireContext(), message)
                call.cancel()
            }

        })
    }
    private fun deleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_warning)
            .setTitle(resources.getString(R.string.deleteAccount))
            .setMessage(resources.getString(R.string.userAccountDelete))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
                /** Check Password **/
                val builder_check = AlertDialog.Builder(requireContext())
                val dialogView_check: View = layoutInflater.inflate(R.layout.dialog_passwd_verify, null)
                val passwordBox = dialogView_check.findViewById<EditText>(R.id.passwordBox)
                builder_check.setView(dialogView_check)
                val dialog_check: AlertDialog = builder_check.create()
                dialogView_check.findViewById<View>(R.id.btnCheck).setOnClickListener(View.OnClickListener {
                    val userPassword = passwordBox.text.toString()
                    if (userPassword.equals(currentUserPassword)) {
                        dialog_check.dismiss()
                        deleteMemberFunction()
                    } else {
                        passwordBox.error = getString(R.string.passwordError)
                    }
                })
                dialogView_check.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog_check.dismiss() }
                if (dialog_check.window != null) {
                    dialog_check.window!!.setBackgroundDrawable(ColorDrawable(0))
                }
                dialog_check.show()
            }
            .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun deleteMemberFunction() {
        val requestMap = mapOf(
            Model.cEmail to  encryptionUtils.getDecryptedData(SharedPreferencesEmailKey).toString()
        )
       val call = myApiService.deleteMember(requestMap)
        call.enqueue(object: Callback<Model.ResponseMessage> {
            override fun onResponse(
                call: Call<Model.ResponseMessage>,
                response: Response<Model.ResponseMessage>
            ) {
                if (response.isSuccessful) {
                    val message = response.body()?.message
                    CustomSnackbar.showSnackbar(view, requireContext(), message.toString())
                    encryptionUtils.removeData(SharedPreferencesEmailKey)
                    encryptionUtils.removeData(SharedPreferencesPasswordKey)
                    RegisterHelper.memberLoginFlag = false
                    fragmentShift.returnBackStackFragment(requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG)
                } else {
                    val responseStatusCode = response.code()
                    CustomSnackbar.showSnackbar(view, requireContext(), responseStatusCode.toString())
                }
            }
            override fun onFailure(call: Call<Model.ResponseMessage>, t: Throwable) {
                val message: String = when (t) {
                    is SocketTimeoutException -> getString(R.string.connectTimeOut)
                    is IOException -> getString(R.string.connectFailed)
                    else -> getString(R.string.serverError)
                }
                CustomSnackbar.showSnackbar(view, requireContext(), message)
                call.cancel()
            }

        })
    }

    private fun needReLogin(callback: (() -> Unit)?) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.needReLogin))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
                callback?.invoke()
            }
            .setCancelable(false) // 禁用對話框外部點擊事件
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}