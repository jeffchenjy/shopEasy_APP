package com.example.shopping_app

import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator

class ProgressIndicatorAnimator {

    fun linearProgressIndicatorAnimator(recyclerView: RecyclerView, progressBar: LinearProgressIndicator) {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0
        // 加載數據完成後將進度設置為100
        ObjectAnimator.ofInt(progressBar, "progress", 100).apply {
            duration = Constants.DELAY_DURATION // 持續時間，這裡設置為1秒
            start()
        }
        // 延遲幾秒後顯示資訊
        Handler(Looper.getMainLooper()).postDelayed({
            showData(recyclerView, progressBar)
            animateRecyclerView(recyclerView)
        }, Constants.DELAY_DURATION)
    }
    private fun showData(recyclerView: RecyclerView, progressBar: LinearProgressIndicator) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
    private fun animateRecyclerView(recyclerView: RecyclerView) {
        val animator = ObjectAnimator.ofFloat(recyclerView, "translationY", 800f, 0f)
        animator.duration = 500 // 設置動畫持續時間
        animator.start()
    }
}