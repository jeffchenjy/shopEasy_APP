package com.example.shopping_app.ApiHelper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.EncryptionUtils
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesEmailKey
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesPasswordKey
import com.example.shopping_app.Fragment.LoginFragment
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.R
import com.example.shopping_app.RequestCallback
import com.google.android.material.textfield.TextInputLayout



class RegisterHelper {
    val connectToServerHelper = ConnectToServerHelper()
    companion object {
        var memberLoginFlag: Boolean = false
        fun containsSpecialCharacter(input: String): Boolean {
            val regex = Regex("[^A-Za-z0-9@!?\\u4e00-\\u9fa5]")
            return regex.find(input) != null
        }
        fun containsCharacter(input: String): Boolean {
            val regex = Regex("[^0-9#,+*/-]")
            return regex.find(input) != null
        }
        fun containsNumberCharacter(input: String): Boolean {
            val regex = Regex("[^0-9]")
            return regex.find(input) != null
        }
        fun saveUserLogInData(context: Context, email: String, password: String) {
            val encryptionUtils = EncryptionUtils(context)
            encryptionUtils.saveEncryptedData(SharedPreferencesEmailKey, email)
            encryptionUtils.saveEncryptedData(SharedPreferencesPasswordKey, password)
        }
    }
    fun forgotPasswordCheckEmail(view: View, context: Context, activity: FragmentActivity, callback: RequestCallback?) {
        val builder = AlertDialog.Builder(context)
        val dialogView: View =  activity.layoutInflater.inflate(R.layout.dialog_passwd_forgot, null)
        val emailTextInputLayout = dialogView.findViewById<TextInputLayout>(R.id.emailTextInputLayout)
        val emailBox = dialogView.findViewById<EditText>(R.id.emailBox)
        builder.setView(dialogView)
        val dialog: AlertDialog = builder.create()
        dialogView.findViewById<View>(R.id.btnReset).setOnClickListener{
            if(emailBox.text.isNotEmpty()) {
                dialog.dismiss()
                /** Show progress indicators **/
                val indicatorsBuilder = AlertDialog.Builder(context)
                val indicatorsDialogView: View = activity.layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
                indicatorsBuilder.setView(indicatorsDialogView)
                val indicatorsDialog: AlertDialog = indicatorsBuilder.create()
                indicatorsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                indicatorsDialog.show()
                val userEmail = emailBox.text.toString()
                connectToServerHelper.changePassword(view, context, userEmail, null) { isSuccess ->
                    indicatorsDialog.dismiss()
                    if (isSuccess) {
                        forgotPasswordChangePassword(view, context, activity, userEmail) { isSuccess ->
                            if(isSuccess) {
                                callback?.invoke(true)
                            } else {
                                callback?.invoke(false)
                            }

                        }
                    } else {
                        CustomSnackbar.showSnackbar(view, context, "メンバーが存在しません。もう一度電子メールを入力してください。")
                    }
                }
            } else {
                emailTextInputLayout.error  = context.getString(R.string.emailEmpty)
            }
        }
        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
            callback?.invoke(false)
        }
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        dialog.show()
        emailBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    emailTextInputLayout.error  = context.getString(R.string.emailEmpty)
                } else if(!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches() && charCount > 0) {
                    emailTextInputLayout.error = context.getString(R.string.errorEmailMessage)
                } else {
                    emailTextInputLayout.error  = null
                }
            }
        })
    }

    private fun forgotPasswordChangePassword(view: View, context: Context, activity: FragmentActivity, email: String, callback: RequestCallback) {
        val builder = AlertDialog.Builder(context)
        val dialogView: View = activity.layoutInflater.inflate(R.layout.dialog_passwd_change, null)
        val passwordBox = dialogView.findViewById<EditText>(R.id.passwordBox)
        val passwordTextInputLayout =  dialogView.findViewById<TextInputLayout>(R.id.passwordTextInputLayout)
        builder.setView(dialogView)
        val dialog: AlertDialog = builder.create()
        dialogView.findViewById<View>(R.id.btnCheck).setOnClickListener{
            if(passwordBox.text.isNotEmpty()) {
                /** Show progress indicators **/
                val indicatorsBuilder = AlertDialog.Builder(context)
                val indicatorsDialogView: View = activity.layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
                indicatorsBuilder.setView(indicatorsDialogView)
                val indicatorsDialog: AlertDialog = indicatorsBuilder.create()
                indicatorsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                indicatorsDialog.show()
                val newPassword = passwordBox.text.toString()
                connectToServerHelper.changePassword(view, context, email, newPassword) { isSuccess ->
                    indicatorsDialog.dismiss()
                    if (isSuccess) {
                        dialog.dismiss()
                        val encryptionUtils = EncryptionUtils(context)
                        if(encryptionUtils.containsKey(SharedPreferencesEmailKey)) {
                            encryptionUtils.removeData(SharedPreferencesEmailKey)
                            encryptionUtils.removeData(SharedPreferencesPasswordKey)
                            ItemInfoMap.memberFavoriteMap.clear()
                            MemberInfoHelper.memberInfoMap.clear()
                            memberLoginFlag = false
                            ItemInfoMap.cartMap.clear()
                            ItemInfoMap.memberOrders = emptyList()
                            ItemInfoMap.cartQuantity = 0
                        }
                        CustomSnackbar.showSnackbar(view, context, context.getString(R.string.passwordUpdateSuccess))
                        callback.invoke(true)
                    } else {
                        CustomSnackbar.showSnackbar(view, context, context.getString(R.string.passwordUpdateFailed))
                    }
                }
            } else {
                passwordTextInputLayout.error = context.getString(R.string.passwordEmpty)
            }

        }
        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
            callback.invoke(false)
        }
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        dialog.show()
        passwordBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    passwordTextInputLayout.error = context.getString(R.string.passwordEmpty)
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
                } else if(containsSpecialCharacter(s.toString())) {
                    passwordTextInputLayout.error = context.getString(R.string.illegalCharacters)
                } else if(charCount in 1..7) {
                    passwordTextInputLayout.error = context.getString(R.string.errorPasswordMessage)
                } else {
                    passwordTextInputLayout.error  = null
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
            }
        })

    }

}
