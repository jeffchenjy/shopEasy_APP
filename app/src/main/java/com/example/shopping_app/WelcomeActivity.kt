package com.example.shopping_app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ApiHelper.MemberInfoHelper
import com.example.shopping_app.ApiHelper.RegisterHelper
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesEmailKey
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesPasswordKey
import android.Manifest
import com.example.shopping_app.Constants.REQUEST_CODE_STORAGE_PERMISSION

class WelcomeActivity: AppCompatActivity() {
    private val GOTO_MainActivity = 0
    private lateinit var encryptionUtils: EncryptionUtils
    private lateinit var connectToServerHelper: ConnectToServerHelper
    private lateinit var memberInfoHelper: MemberInfoHelper
    private lateinit var view: View
    private var memberEmail: String? = null
    private var memberPassword: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        initial()
        checkMemberLogin()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE_PERMISSION)
        }
        connectToServer()

    }
    private fun initial() {
        view = findViewById(R.id.fragment_container)
        encryptionUtils = EncryptionUtils(this)
        connectToServerHelper = ConnectToServerHelper()
        memberInfoHelper = MemberInfoHelper()
    }
    private fun checkMemberLogin() {
        if (encryptionUtils.containsKey(SharedPreferencesEmailKey) && encryptionUtils.containsKey(SharedPreferencesPasswordKey)) {
            if(encryptionUtils.getDecryptedData(SharedPreferencesEmailKey) != null &&
                encryptionUtils.getDecryptedData(SharedPreferencesPasswordKey) != null) {
                memberEmail = encryptionUtils.getDecryptedData(SharedPreferencesEmailKey)!!
                memberPassword = encryptionUtils.getDecryptedData(SharedPreferencesPasswordKey)!!
                connectToServerHelper = ConnectToServerHelper(memberEmail, memberPassword)
                RegisterHelper.memberLoginFlag = true
            }
        }

    }
    private fun connectToServer() {
        if(NetworkUtils.isNetworkOnline(this)) {
            connectToServerHelper.reloadData(view, this) {
                mHandler.sendEmptyMessageDelayed(GOTO_MainActivity, 500) //跳轉
            }
        } else {
            CustomSnackbar.showSnackbar(view, this@WelcomeActivity, getString(R.string.connectFailed))
            ItemInfoMap.getDataFlag = false
            mHandler.sendEmptyMessageDelayed(GOTO_MainActivity, 10) //跳轉
        }
    }
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GOTO_MainActivity -> {
                    startMainActivity()
                }
            }
        }
    }

    private fun startMainActivity() {
        val Main_intent = Intent(this, MainActivity::class.java)
        startActivity(Main_intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}