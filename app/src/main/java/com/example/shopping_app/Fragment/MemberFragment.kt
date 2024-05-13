package com.example.shopping_app.Fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ApiHelper.ConnectToServerHelper.Companion.clearAll
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.memberFavoriteMap
import com.example.shopping_app.ApiHelper.MemberInfoHelper
import com.example.shopping_app.ApiHelper.MemberInfoHelper.Companion.memberInfoMap
import com.example.shopping_app.ApiHelper.RegisterHelper.Companion.memberLoginFlag
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.EncryptionUtils
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesEmailKey
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesPasswordKey
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.FragmentShift.Companion.MEMBER_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.FragmentShift.Companion.MEMBER_ORDER_LIST_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.Model
import com.example.shopping_app.MyApiManager
import com.example.shopping_app.MyApiManager.baseUrl
import com.example.shopping_app.NetworkUtils
import com.example.shopping_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class MemberFragment: Fragment() {
    private val myApiService = MyApiManager.myApiService
    private lateinit var memberInfoHelper: MemberInfoHelper
    private lateinit var encryptionUtils: EncryptionUtils
    private lateinit var connectToServerHelper: ConnectToServerHelper
    private lateinit var fragmentShift: FragmentShift
    /*  Member Empty */
    private lateinit var logInButton: Button
    private lateinit var signUpButton: Button
    /* Member */
    private lateinit var orderListButton: Button
    private lateinit var logoutButton: Button
    private lateinit var editProfileButton: Button
    private lateinit var editAccountButton: Button
    private lateinit var profileUsername: TextView
    private lateinit var profileEmail: TextView
    private lateinit var titleNickname: TextView
    /* Button Card View */
    private lateinit var editAccountButtonCardView: CardView
    private lateinit var editProfileButtonCardView: CardView
    private lateinit var memberImg: CircleImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        encryptionUtils = EncryptionUtils(requireContext())
        val resourceLayout = if(encryptionUtils.containsKey(SharedPreferencesEmailKey)) {
            R.layout.fragment_member
        } else {
            R.layout.fragment_member_empty
        }
        return inflater.inflate(resourceLayout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        memberInfoHelper = MemberInfoHelper()
        connectToServerHelper = ConnectToServerHelper()

        fragmentShift.setNavigationBarViewVisibility(
            requireActivity(),
            R.id.bottomNavigation,
            View.VISIBLE
        )

        findView(view)

        if(encryptionUtils.containsKey(SharedPreferencesEmailKey)) {
            memberButton()
            setMemberInfo()
        } else {
            logInViewButton()
        }
    }
    private fun findView(view: View) {
        if(encryptionUtils.containsKey(SharedPreferencesEmailKey)) {
            orderListButton = view.findViewById(R.id.orderListButton)
            editProfileButton = view.findViewById(R.id.editProfileButton)
            editAccountButton = view.findViewById(R.id.editAccountButton)
            logoutButton = view.findViewById(R.id.logoutButton)
            profileUsername = view.findViewById(R.id.profileUsername)
            profileEmail = view.findViewById(R.id.profileEmail)
            titleNickname = view.findViewById(R.id.titleNickname)
            editAccountButtonCardView = view.findViewById(R.id.editAccountButtonCardView)
            editProfileButtonCardView = view.findViewById(R.id.editProfileButtonCardView)
            memberImg = view.findViewById(R.id.memberImg)
        } else {
            logInButton = view.findViewById(R.id.logInButton)
            signUpButton = view.findViewById(R.id.signUpButton)
        }
    }
    private fun logInViewButton() {
        logInButton.setOnClickListener {
            fragmentShift.goToNextFragment(LoginFragment(), requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG, MEMBER_FRAGMENT_SHIFT_TAG)
        }
        signUpButton.setOnClickListener {
            fragmentShift.goToNextFragment(SignUpFragment(), requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG, MEMBER_FRAGMENT_SHIFT_TAG)
        }
    }
    private fun memberButton() {
        orderListButton.setOnClickListener(memberButtonClickListener())
        editAccountButton.setOnClickListener(memberButtonClickListener())
        editProfileButton.setOnClickListener(memberButtonClickListener())
        logoutButton.setOnClickListener(memberButtonClickListener())
    }
    private fun memberButtonClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val view = it
            val viewId = view?.id
            when(viewId) {
                R.id.orderListButton -> {
                    fragmentShift.goToNextFragment(MemberOrderListFragment(), requireActivity(), MEMBER_ORDER_LIST_FRAGMENT_SHIFT_TAG, MEMBER_ORDER_LIST_FRAGMENT_SHIFT_TAG)
                }
                R.id.editAccountButton -> {
                    fragmentShift.goToNextFragment(EditAccountFragment(), requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG, MEMBER_FRAGMENT_SHIFT_TAG)
                }
                R.id.editProfileButton -> {
                    fragmentShift.goToNextFragment(EditProfileFragment(), requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG, MEMBER_FRAGMENT_SHIFT_TAG)
                }
                R.id.logoutButton -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setIcon(R.drawable.ic_warning)
                        .setTitle(resources.getString(R.string.logout))
                        .setMessage(resources.getString(R.string.logoutMessage))
                        .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                            /* 清除與會員相關資料 */
                            encryptionUtils.removeData(SharedPreferencesEmailKey)
                            encryptionUtils.removeData(SharedPreferencesPasswordKey)
                            memberFavoriteMap.clear()
                            memberInfoMap.clear()
                            memberLoginFlag = false
                            ItemInfoMap.cartMap.clear()
                            ItemInfoMap.cartQuantity = 0
                            ItemInfoMap.memberOrders = emptyList()
                            fragmentShift.setNavigationBarViewCartBadge(requireActivity(), R.id.bottomNavigation)
                            fragmentShift.goToNextFragment(LoginFragment(), requireActivity(), MEMBER_FRAGMENT_SHIFT_TAG, MEMBER_FRAGMENT_SHIFT_TAG)
                            dialog.dismiss()
                        }
                        .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }
    private fun setMemberInfo() {
        setButtonEnabledStatus(true)
        if(memberInfoMap.isNotEmpty()) {
            if(memberInfoMap.containsKey(Model.cNickName) && memberInfoMap.containsKey(Model.cName) && memberInfoMap.containsKey(Model.cEmail)) {
                titleNickname.text = memberInfoMap[Model.cNickName].toString()
                profileUsername.text = memberInfoMap[Model.cName].toString()
                profileEmail.text = memberInfoMap[Model.cEmail].toString()
            }
            if(memberInfoMap.containsKey(Model.cImage)) {
                if(memberInfoMap[Model.cImage] != null) {
                    val image = memberInfoMap[Model.cImage].toString()
                    val imageUrl = baseUrl + image.substring(1, image.length)
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁碟緩存
                        .skipMemoryCache(true) // 禁用內存緩存
                        .placeholder(R.drawable.ic_person_circle_bg) // 設置占位符，當圖片加載時顯示
                        .error(R.drawable.ic_person_circle_bg) // 設置加載錯誤時顯示的圖片
                        .into(memberImg)
                }
            }
        } else {
            titleNickname.text = "Unknown"
            profileUsername.text = "Unknown"
            profileEmail.text = encryptionUtils.getDecryptedData(SharedPreferencesEmailKey)
            getMemberData()
        }
    }
    private fun setButtonEnabledStatus(status: Boolean) {
        editAccountButton.isEnabled = status
        editProfileButton.isEnabled = status
        if(status) {
            editAccountButtonCardView.visibility = View.GONE
            editProfileButtonCardView.visibility = View.GONE
        } else {
            editAccountButtonCardView.visibility = View.VISIBLE
            editProfileButtonCardView.visibility = View.VISIBLE
        }
    }

    private fun getMemberData() {
        /** Show progress indicators **/
        val builder = AlertDialog.Builder(requireContext())
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
        builder.setView(dialogView)
        val dialog: AlertDialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val userMail = encryptionUtils.getDecryptedData(SharedPreferencesEmailKey)
        val requestMap = mapOf(
            Model.cEmail to userMail!!
        )
        myApiService.getMemberData(requestMap).enqueue(object : Callback<Model.MemberResponse> {
            override fun onResponse(call: Call<Model.MemberResponse>, response: Response<Model.MemberResponse>) {
                if (response.isSuccessful) {
                    dialog.dismiss()
                    val infoList = response.body()
                    if (infoList != null) {
                        memberInfoHelper.createMemberInfoMap(infoList.cName, infoList.cNickName, infoList.cEmail,
                            infoList.cPhone, infoList.cAddr, infoList.cCountry, infoList.cBirthday, infoList.cSex, infoList.cImage)
                    }
                    connectToServerHelper.getMemberFavorite(requireView(), requireContext()) {
                        connectToServerHelper.getCartData(requireView(), requireContext(), null)
                        connectToServerHelper.getOrder(requireView(), requireContext()) {
                            fragmentShift.setNavigationBarViewCartBadge(requireActivity(), R.id.bottomNavigation)
                            setMemberInfo()
                        }
                    }
                } else {
                    dialog.dismiss()
                    // 失敗時的處理邏輯
                    val responseStatusCode = response.code()
                    CustomSnackbar.showSnackbar(view, requireContext(), responseStatusCode.toString())
                    setButtonEnabledStatus(false)
                }
            }
            override fun onFailure(call: Call<Model.MemberResponse>, t: Throwable) {
                dialog.dismiss()
                // 請求失敗時的處理邏輯
                val message: String = when (t) {
                    is SocketTimeoutException -> getString(R.string.connectTimeOut)
                    is IOException -> getString(R.string.connectFailed)
                    else -> getString(R.string.serverError)
                }
                CustomSnackbar.showSnackbar(view, requireContext(), message)
                call.cancel()
                setButtonEnabledStatus(false)
            }
        })

    }
    override fun onResume() {
        super.onResume()
        val bottomNavigation = requireActivity().findViewById<NavigationBarView>(R.id.bottomNavigation)
        if(!bottomNavigation.menu.findItem(R.id.barMember).isChecked) {
            bottomNavigation.menu.findItem(R.id.barMember).setIcon(R.drawable.ic_person)
            bottomNavigation.menu.findItem(R.id.barCart).setIcon(R.drawable.ic_cart_empty)
            bottomNavigation.menu.findItem(R.id.barMember).isChecked = true
            bottomNavigation.menu.findItem(R.id.barCart).isChecked = false
            bottomNavigation.selectedItemId = R.id.barMember
        }
        if(!NetworkUtils.isNetworkOnline(requireContext())) {
            clearAll()
            connectToServerHelper.reloadData(requireView(), requireContext()) {
                getMemberData()
            }
        }
    }
}