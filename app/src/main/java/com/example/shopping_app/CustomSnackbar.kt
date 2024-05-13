package com.example.shopping_app

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

class CustomSnackbar {
    companion object {
        fun showSnackbar(view: View?, context: Context, msg: String) {
            val layoutInflater = LayoutInflater.from(context)
            val snackbar = Snackbar.make(view!!, "", Snackbar.LENGTH_SHORT)
            val snackbarView = snackbar.view as Snackbar.SnackbarLayout
            val customSnackbarView = layoutInflater.inflate(R.layout.custom_snackbar_layout, null)
            val messageTextView = customSnackbarView.findViewById<TextView>(R.id.messageTextView)
            messageTextView.text = msg
            // 清除 Snackbar 原有的背景，使自訂佈局顯示正常
            snackbarView.setBackgroundColor(Color.TRANSPARENT)
            // 將自定義佈局設置給 Snackbar
            snackbarView.addView(customSnackbarView, 0)
            snackbar.show()
        }

        // 將 dp 單位轉換為像素
        private fun dpToPx(context: Context, dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }
    }
}