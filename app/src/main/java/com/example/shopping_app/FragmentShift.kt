package com.example.shopping_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.getQuantity
import com.google.android.material.navigation.NavigationBarView

class FragmentShift {
    companion object {
        const val MEMBER_FRAGMENT_SHIFT_TAG = "memberFragment"
        const val LOGIN_FRAGMENT_SHIFT_TAG = "loginFragment"
        const val SIGNUP_FRAGMENT_SHIFT_TAG = "signFragment"
        const val SHOW_ITEM_FRAGMENT_SHIFT_TAG = "showItemFragment"
        const val CART_FRAGMENT_SHIFT_TAG = "cartFragment"
        const val CART_CHECKOUT_FRAGMENT_SHIFT_TAG = "cartCheckoutFragment"
        const val CART_FINAL_CHECK_FRAGMENT_SHIFT_TAG = "cartFinalCheckFragment"
        const val MEMBER_ORDER_LIST_FRAGMENT_SHIFT_TAG = "MemberOrderListFragment"
        const val ORDER_INFO_FRAGMENT_SHIFT_TAG = "OrderInfoFragment"
        const val LANGUAGE_FRAGMENT_SHIFT_TAG = "LanguageFragment"
    }
    fun goToNextFragment(fragment: Fragment, activity: FragmentActivity, tag: String, backStackName: String) {
        activity.supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out_right  // popExit
            )
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(backStackName)
            .commit()
    }
    fun goToPreviousFragment(fragment: Fragment, activity: FragmentActivity, tag: String, backStackName: String) {
        activity.supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,  // enter
                R.anim.slide_out_right,  // exit
                R.anim.fade_out,   // popEnter
                R.anim.slide_in_right  // popExit
            )
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(backStackName)
            .commit()
    }

    fun returnBackStackFragment(activity: FragmentActivity, popBackStackName: String) {
        val fragmentManager = activity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentManager.popBackStack(popBackStackName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        fragmentTransaction.commit()
    }

    fun changeToShowItemFragment(fragment: Fragment, activity: FragmentActivity, itemString: String, imageUrl: String) {
        val goToFragment = fragment.apply {
            arguments = Bundle().apply {
                putString("itemString", itemString)
                putString("imageUrl", imageUrl)
            }
        }
        goToNextFragment(goToFragment, activity, SHOW_ITEM_FRAGMENT_SHIFT_TAG, SHOW_ITEM_FRAGMENT_SHIFT_TAG)
    }
    fun changeToOrderInfoFragment(fragment: Fragment, activity: FragmentActivity, orderID: String) {
        val goToFragment = fragment.apply {
            arguments = Bundle().apply {
                putString("orderID", orderID)
            }
        }
        goToNextFragment(goToFragment, activity, ORDER_INFO_FRAGMENT_SHIFT_TAG, ORDER_INFO_FRAGMENT_SHIFT_TAG)
    }


    fun setNavigationBarViewVisibility(activity: FragmentActivity, id: Int, visible: Int) {
        val bottomNavigation = activity.findViewById<NavigationBarView>(id)
        bottomNavigation.visibility = visible
    }
    fun setNavigationBarViewCartBadge(activity: FragmentActivity, id: Int) {
        val bottomNavigation = activity.findViewById<NavigationBarView>(id)
        val badge = bottomNavigation.getOrCreateBadge(R.id.barCart)
        getQuantity()
        if(ItemInfoMap.cartQuantity != 0) {
            badge.isVisible = true
            badge.number = ItemInfoMap.cartQuantity
        } else {
            badge.isVisible = false
            badge.clearNumber()
        }
    }
}