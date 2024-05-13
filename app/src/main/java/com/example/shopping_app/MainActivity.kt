package com.example.shopping_app

import android.app.ActivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.cartQuantity
import com.example.shopping_app.Fragment.CartFragment
import com.example.shopping_app.Fragment.FavoriteFragment
import com.example.shopping_app.Fragment.MemberFragment
import com.example.shopping_app.FragmentShift.Companion.CART_FRAGMENT_SHIFT_TAG
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation : NavigationBarView
    private lateinit var fragmentShift: FragmentShift
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragmentShift = FragmentShift()
        bottomNavigation = findViewById(R.id.bottomNavigation)
        replaceFragment(HomeFragment())
        bottomNavigation.menu.findItem(R.id.barHome).setIcon(R.drawable.ic_home)
        bottomNavigationSelect()

        var badge = bottomNavigation.getOrCreateBadge(R.id.barCart)
        if(cartQuantity != 0) {
            badge.isVisible = true
            badge.number = cartQuantity
        } else {
            badge.isVisible = false
            badge.clearNumber()
        }
    }

    private fun bottomNavigationSelect() {
        bottomNavigation.setOnItemSelectedListener{item ->
            when(item.itemId) {
                R.id.barHome -> {
                    replaceFragment(HomeFragment())
                    switchIcon(item.itemId)
                }
                R.id.barCollection -> {
                    replaceFragment(FavoriteFragment())
                    switchIcon(item.itemId)
                }
                R.id.barCart -> {
                    fragmentShift.goToNextFragment(CartFragment(), this,  CART_FRAGMENT_SHIFT_TAG,  CART_FRAGMENT_SHIFT_TAG)
                    switchIcon(item.itemId)
                }
                R.id.barMember -> {
                    replaceFragment(MemberFragment())
                    switchIcon(item.itemId)
                }
            }
            true
        }
    }
    private fun switchIcon(itemId: Int) {
        when(itemId) {
            R.id.barHome -> {
                bottomNavigation.menu.findItem(R.id.barHome).setIcon(R.drawable.ic_home)
                bottomNavigation.menu.findItem(R.id.barCollection).setIcon(R.drawable.ic_collection_empty)
                bottomNavigation.menu.findItem(R.id.barCart).setIcon(R.drawable.ic_cart_empty)
                bottomNavigation.menu.findItem(R.id.barMember).setIcon(R.drawable.ic_person_empty)
            }
            R.id.barCollection -> {
                bottomNavigation.menu.findItem(R.id.barHome).setIcon(R.drawable.ic_home_empty)
                bottomNavigation.menu.findItem(R.id.barCollection).setIcon(R.drawable.ic_collection)
                bottomNavigation.menu.findItem(R.id.barCart).setIcon(R.drawable.ic_cart_empty)
                bottomNavigation.menu.findItem(R.id.barMember).setIcon(R.drawable.ic_person_empty)
            }
            R.id.barCart -> {
                bottomNavigation.menu.findItem(R.id.barHome).setIcon(R.drawable.ic_home_empty)
                bottomNavigation.menu.findItem(R.id.barCollection).setIcon(R.drawable.ic_collection_empty)
                bottomNavigation.menu.findItem(R.id.barCart).setIcon(R.drawable.ic_cart)
                bottomNavigation.menu.findItem(R.id.barMember).setIcon(R.drawable.ic_person_empty)
            }
            R.id.barMember -> {
                bottomNavigation.menu.findItem(R.id.barHome).setIcon(R.drawable.ic_home_empty)
                bottomNavigation.menu.findItem(R.id.barCollection).setIcon(R.drawable.ic_collection_empty)
                bottomNavigation.menu.findItem(R.id.barCart).setIcon(R.drawable.ic_cart_empty)
                bottomNavigation.menu.findItem(R.id.barMember).setIcon(R.drawable.ic_person)
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
        } else {
            MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_exit)
                .setTitle(resources.getString(R.string.escTitle))
                .setMessage(resources.getString(R.string.esc))
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                    val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
                    val tasks = activityManager.appTasks
                    for (task in tasks) {
                        task.finishAndRemoveTask()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}