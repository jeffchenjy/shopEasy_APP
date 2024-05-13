package com.example.shopping_app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ApiHelper.ConnectToServerHelper.Companion.clearAll
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.cartMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.checkCartList
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.itemInfoMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.memberFavoriteMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.truncateString
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.EncryptionUtils
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesEmailKey
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.FragmentShift.Companion.CART_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.FragmentShift.Companion.SHOW_ITEM_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.Model
import com.example.shopping_app.NetworkUtils
import com.example.shopping_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ShowItemFragment: Fragment() {
    private lateinit var encryptionUtils: EncryptionUtils
    private lateinit var connectToServerHelper: ConnectToServerHelper
    private lateinit var fragmentShift: FragmentShift
    private lateinit var thisView: View
    private lateinit var includedLayout: View
    private lateinit var toolbar: Toolbar
    private lateinit var itemImageView: ImageView
    private lateinit var itemNameTextView: TextView
    private lateinit var showPriceTextView: TextView
    private lateinit var showAuthorTextView: TextView
    private lateinit var showCompanyTextView: TextView
    private lateinit var showDescriptionTextView: TextView
    private lateinit var companyTextView: TextView
    private lateinit var quantityText: TextView
    /* Layout */
    private lateinit var authorLayout: LinearLayout
    private lateinit var favorLayout: LinearLayout
    private lateinit var cartLayout: LinearLayout
    /* ImageButton */
    private lateinit var favorImageButton: ImageButton
    private lateinit var cartImageButton: ImageButton
    /* Button */
    private lateinit var inputCartButton: Button
    private lateinit var buyButton: Button
    /* String */
    private val maxLength: Int = 16
    private var itemString: String? = null
    private var imageUrl: String? = null
    private var memberEmail: String? = null
    private var itemID: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        itemString = arguments?.getString("itemString")
        imageUrl = arguments?.getString("imageUrl")

        return inflater.inflate(R.layout.fragment_show_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        findView(view)
        setUpToolbar()
        init()
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.main_toolbar)
        itemImageView = view.findViewById(R.id.itemImageView)
        itemNameTextView = view.findViewById(R.id.itemNameTextView)
        showPriceTextView = view.findViewById(R.id.showPriceTextView)
        showAuthorTextView = view.findViewById(R.id.showAuthorTextView)
        showCompanyTextView = view.findViewById(R.id.showCompanyTextView)
        showDescriptionTextView = view.findViewById(R.id.showDescriptionTextView)
        /* Title TextView */
        authorLayout = view.findViewById(R.id.authorLayout)
        companyTextView = view.findViewById(R.id.companyTextView)
        /* BottomSheet */
        includedLayout = view.findViewById(R.id.includedLayout)
        favorLayout = includedLayout.findViewById(R.id.favorLayout)
        cartLayout = includedLayout.findViewById(R.id.cartLayout)
        favorImageButton = includedLayout.findViewById(R.id.favorImageButton)
        cartImageButton = includedLayout.findViewById(R.id.cartImageButton)
        quantityText = includedLayout.findViewById(R.id.quantityText)
        inputCartButton = includedLayout.findViewById(R.id.inputCartButton)
        buyButton = includedLayout.findViewById(R.id.buyButton)
    }
    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        if (itemString != null) {
            val truncatedText = truncateString(itemString!!, maxLength)
            (requireActivity() as AppCompatActivity).supportActionBar?.title = truncatedText
        } else (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.showItemTitle)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            fragmentShift.returnBackStackFragment(requireActivity(), SHOW_ITEM_FRAGMENT_SHIFT_TAG)
        }
    }
    private fun init() {
        fragmentShift.setNavigationBarViewVisibility(requireActivity(), R.id.bottomNavigation, View.GONE)
        thisView = requireView()
        encryptionUtils = EncryptionUtils(requireContext())
        connectToServerHelper = ConnectToServerHelper()
        memberEmail = encryptionUtils.getDecryptedData(SharedPreferencesEmailKey)
        if (itemString != null && imageUrl != null) {
            itemNameTextView.text = itemString
            itemID = itemInfoMap[itemString]?.get(Model.cID)
            /* Get Item Image */
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_error_outline) // 設置占位符，當圖片加載時顯示
                .error(R.drawable.ic_error_outline) // 設置加載錯誤時顯示的圖片
                .into(itemImageView)
            if(itemInfoMap[itemString]?.get(Model.cSort) != "Book") {
                companyTextView.text = getString(R.string.compTitle)
                authorLayout.visibility = View.GONE
            } else {
                companyTextView.text = getString(R.string.companyTitle)
                authorLayout.visibility = View.VISIBLE
                showAuthorTextView.text = itemInfoMap[itemString]?.get(Model.cAuthor)
            }
            showPriceTextView.text = itemInfoMap[itemString]?.get(Model.cPrice)
            showCompanyTextView.text = itemInfoMap[itemString]?.get(Model.cCompany)
            showDescriptionTextView.text = itemInfoMap[itemString]?.get(Model.cDescription)
            /* Check Member Favorite */
            if(itemID in memberFavoriteMap.keys) {
                favorImageButton.setImageResource(R.drawable.ic_collection)
            }
        }
        bottomButtonAction()
        changeQuantityText()
    }
    private fun bottomButtonAction() {
        favorLayout.setOnClickListener(bottomSheetOnClickListener())
        favorImageButton.setOnClickListener(bottomSheetOnClickListener())
        cartImageButton.setOnClickListener(bottomSheetOnClickListener())
        inputCartButton.setOnClickListener(bottomSheetOnClickListener())
        buyButton.setOnClickListener(bottomSheetOnClickListener())
    }
    private fun bottomSheetOnClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val view = it
            val viewId = view?.id
            when(viewId) {
                R.id.favorLayout -> {
                    addFavorite()
                }
                R.id.favorImageButton -> {
                    addFavorite()
                }
                R.id.cartImageButton -> {
                    fragmentShift.goToNextFragment(CartFragment(), requireActivity(), CART_FRAGMENT_SHIFT_TAG, CART_FRAGMENT_SHIFT_TAG)
                }
                R.id.inputCartButton -> {
                    if(memberEmail != null) {
                        if(!checkCartList(view, requireContext(), itemID.toString())) {
                            connectToServerHelper.addTOCartFunction(thisView, requireContext(),
                                itemID, itemString, showPriceTextView.text.toString(), 1) {
                                if(isAdded) {
                                    fragmentShift.setNavigationBarViewCartBadge(requireActivity(), R.id.bottomNavigation)
                                    changeQuantityText()
                                    showSuccessDialog()
                                }
                            }
                        }
                    } else {
                        CustomSnackbar.showSnackbar(requireView(), requireContext(), getString(R.string.noLogin))
                    }

                }
                R.id.buyButton -> {

                }
            }
        }
    }
    private fun addFavorite() {
        if(memberEmail != null) {
            if(itemID in memberFavoriteMap.keys) {
                CustomSnackbar.showSnackbar(requireView(), requireContext(), getString(R.string.alreadyIntoFavorite))
            } else {
                changeFavorImage()
                connectToServerHelper.addFavoriteMerchandise(requireView(), requireContext(), itemID.toString()) {
                    changeQuantityText()
                }
            }
        } else {
            CustomSnackbar.showSnackbar(requireView(), requireContext(), getString(R.string.noLogin))
        }
    }
    private fun changeFavorImage() {
        if(isAdded) {
            favorImageButton.setImageResource(R.drawable.ic_collection)
            CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.intoFavorite))
        }
    }
    private fun changeQuantityText() {
        if (cartMap.isNotEmpty()) {
            var quantity = 0
            for(itemID in cartMap.keys) {
                quantity += cartMap[itemID]?.get(Model.quantity)?.toInt() ?: 0
            }
            quantityText.text = quantity.toString()
        } else {
            quantityText.text = "0"
        }
    }
    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(resources.getString(R.string.addedToCart))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }
    override fun onResume() {
        super.onResume()
        if(!NetworkUtils.isNetworkOnline(requireContext())) {
            clearAll()
        }
    }
}

