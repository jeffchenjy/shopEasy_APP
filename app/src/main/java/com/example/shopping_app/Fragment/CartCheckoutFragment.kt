package com.example.shopping_app.Fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shopping_app.ApiHelper.MemberInfoHelper.Companion.memberInfoMap
import com.example.shopping_app.ApiHelper.RegisterHelper
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.FragmentShift.Companion.CART_CHECKOUT_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.Model
import com.example.shopping_app.R
import com.google.android.material.textfield.TextInputLayout

class CartCheckoutFragment: Fragment() {
    private lateinit var fragmentShift: FragmentShift
    private lateinit var toolbar: Toolbar
    /* RadioGroup */
    private lateinit var shippingMethodRadioGroup: RadioGroup
    private lateinit var paymentRadioGroup: RadioGroup
    private lateinit var deliveryTimeRadioGroup: RadioGroup

    /* TextInputLayout */
    private lateinit var usernameTextInputLayout: TextInputLayout
    private lateinit var phoneTextInputLayout: TextInputLayout
    private lateinit var emailTextInputLayout: TextInputLayout
    private lateinit var recipientTextInputLayout: TextInputLayout
    private lateinit var recipientPhoneTextInputLayout: TextInputLayout
    private lateinit var recipientEmailTextInputLayout: TextInputLayout
    private lateinit var shippingAddressTextInputLayout: TextInputLayout
    /* EditText */
    private lateinit var usernameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var recipientEditText: EditText
    private lateinit var recipientPhoneEditText: EditText
    private lateinit var recipientEmailEditText: EditText
    private lateinit var shippingAddressEditText: EditText
    /* Check Box */
    private lateinit var recipientCheckBox: CheckBox
    private lateinit var shippingAddressCheckBox: CheckBox
    /* Get RadioButton Text */
    private var shippingMethod: String? = null
    private var payment: String? = null
    private var deliveryTime: String? = null
    /* SwipeRefreshLayout */
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    /* Button */
    private lateinit var checkoutPreviousButton: Button
    private lateinit var checkoutNextButton: Button
    private lateinit var checkoutNextButtonCardView: CardView
    /* checkInfoChange Loop */
    private val handler = Handler(Looper.myLooper()!!)
    private val checkInfoChange = Runnable {
        if(shippingMethod != null && payment != null && deliveryTime != null &&
            (usernameEditText.text.length > 3 && !RegisterHelper.containsSpecialCharacter(usernameEditText.text.toString()) && usernameEditText.text.isNotEmpty()) &&
            (recipientEditText.text.length > 3 && !RegisterHelper.containsSpecialCharacter(recipientEditText.text.toString()) && recipientEditText.text.isNotEmpty()) &&
            (phoneEditText.text.length >=10 && !RegisterHelper.containsCharacter(phoneEditText.text.toString()) && phoneEditText.text.isNotEmpty()) &&
            (recipientPhoneEditText.text.length >=10 && !RegisterHelper.containsCharacter(recipientPhoneEditText.text.toString()) && recipientPhoneEditText.text.isNotEmpty()) &&
            //(Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString()).matches() && emailEditText.text.isNotEmpty()) &&
            (Patterns.EMAIL_ADDRESS.matcher(recipientEmailEditText.text.toString()).matches() && recipientEmailEditText.text.isNotEmpty()) &&
            shippingAddressEditText.text.isNotEmpty()) {
            checkoutNextButton.isEnabled = true
            checkoutNextButtonCardView.visibility = View.GONE

        } else {
            checkoutNextButton.isEnabled = false
            checkoutNextButtonCardView.visibility = View.VISIBLE
        }
        if(recipientEditText.text.toString() != usernameEditText.text.toString()) {
            recipientCheckBox.setOnCheckedChangeListener(null)
            recipientCheckBox.isChecked = false
            checkBoxCheckedFunction()
        } else if(recipientPhoneEditText.text.toString() != phoneEditText.text.toString()) {
            recipientCheckBox.setOnCheckedChangeListener(null)
            recipientCheckBox.isChecked = false
            checkBoxCheckedFunction()
        } else if(recipientEmailEditText.text.toString() != emailEditText.text.toString()) {
            recipientCheckBox.setOnCheckedChangeListener(null)
            recipientCheckBox.isChecked = false
            checkBoxCheckedFunction()
        }
        if(shippingAddressEditText.text.toString() != memberInfoMap[Model.cAddr].toString()) {
            shippingAddressCheckBox.setOnCheckedChangeListener(null)
            shippingAddressCheckBox.isChecked = false
            checkBoxCheckedFunction()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart_checkout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        findView(view)
        setUpToolbar()
        init()
        radioGroupCheckedChangeListener(view)
        checkBoxCheckedFunction()
        textChangedListenerFunction()
        setEditorActionListener()
        swipeSetOnRefresh()
        checkoutButton()
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        /* Related to RadioGroup */
        shippingMethodRadioGroup = view.findViewById(R.id.shippingMethodRadioGroup)
        paymentRadioGroup = view.findViewById(R.id.paymentRadioGroup)
        deliveryTimeRadioGroup = view.findViewById(R.id.deliveryTimeRadioGroup)
        /* CheckBox */
        recipientCheckBox = view.findViewById(R.id.recipientCheckBox)
        shippingAddressCheckBox = view.findViewById(R.id.shippingAddressCheckBox)
        /* Related to EditText */
        usernameTextInputLayout = view.findViewById(R.id.usernameTextInputLayout)
        phoneTextInputLayout = view.findViewById(R.id.phoneTextInputLayout)
        emailTextInputLayout = view.findViewById(R.id.emailTextInputLayout)
        recipientTextInputLayout = view.findViewById(R.id.recipientTextInputLayout)
        recipientPhoneTextInputLayout = view.findViewById(R.id.recipientPhoneTextInputLayout)
        recipientEmailTextInputLayout = view.findViewById(R.id.recipientEmailTextInputLayout)
        shippingAddressTextInputLayout = view.findViewById(R.id.shippingAddressTextInputLayout)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        recipientEditText = view.findViewById(R.id.recipientEditText)
        recipientPhoneEditText = view.findViewById(R.id.recipientPhoneEditText)
        recipientEmailEditText = view.findViewById(R.id.recipientEmailEditText)
        shippingAddressEditText = view.findViewById(R.id.shippingAddressEditText)
        /* Button */
        checkoutPreviousButton = view.findViewById(R.id.checkoutPreviousButton)
        checkoutNextButton = view.findViewById(R.id.checkoutNextButton)
        checkoutNextButtonCardView = view.findViewById(R.id.checkoutNextButtonCardView)
    }
    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.checkout)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            fragmentShift.returnBackStackFragment(requireActivity(),
               CART_CHECKOUT_FRAGMENT_SHIFT_TAG
            )
        }
    }
    private fun init() {
        if(memberInfoMap.isNotEmpty()) {
            usernameEditText.setText(memberInfoMap[Model.cName].toString())
            emailEditText.setText(memberInfoMap[Model.cEmail].toString())
            if(memberInfoMap.containsKey(Model.cPhone)) {
                phoneEditText.setText(memberInfoMap[Model.cPhone].toString())
            }
        }
        emailEditText.isEnabled = false
        checkoutNextButton.isEnabled = false
        checkoutNextButtonCardView.visibility = View.VISIBLE
    }
    private fun radioGroupCheckedChangeListener(view: View) {
        val radioGroupChangeListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (group.id) {
                R.id.shippingMethodRadioGroup -> {
                    // 更新 shippingMethod 為選中的 RadioButton 的text
                    shippingMethod = view.findViewById<RadioButton>(checkedId).text.toString()
                }
                R.id.paymentRadioGroup -> {
                    // 更新 payment 為選中 RadioButton 的text
                    payment = view.findViewById<RadioButton>(checkedId).text.toString()
                }
                R.id.deliveryTimeRadioGroup -> {
                    // 更新 deliveryTime 為選中 RadioButton 的text
                    deliveryTime = view.findViewById<RadioButton>(checkedId).text.toString()
                }
            }
            // 清除所有 RadioButton 的背景
            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as RadioButton
                radioButton.setBackgroundColor(Color.TRANSPARENT)
            }
            // 設置 RadioButton 的背景颜色
            val selectedRadioButton: RadioButton = view.findViewById(checkedId)
            selectedRadioButton.setBackgroundColor(requireContext().getColor(R.color.radioButtonBackground))
            handler.post(checkInfoChange)
        }
        shippingMethodRadioGroup.setOnCheckedChangeListener(radioGroupChangeListener)
        paymentRadioGroup.setOnCheckedChangeListener(radioGroupChangeListener)
        deliveryTimeRadioGroup.setOnCheckedChangeListener(radioGroupChangeListener)
    }
    private fun checkBoxCheckedFunction() {
        recipientCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(usernameEditText.text.isNotEmpty()) {
                if (isChecked) {
                    recipientEditText.setText(usernameEditText.text.toString())
                    recipientEmailEditText.setText(emailEditText.text.toString())
                    if(phoneEditText.text.isNotEmpty()) {
                        recipientPhoneEditText.setText(phoneEditText.text.toString())
                    }
                } else {
                    recipientEditText.setText("")
                    recipientEmailEditText.setText("")
                    recipientPhoneEditText.setText("")
                }
                handler.post(checkInfoChange)
            }
        }

        if(memberInfoMap[Model.cAddr].toString() != "Unknown") {
            shippingAddressCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    shippingAddressEditText.setText(memberInfoMap[Model.cAddr].toString())
                } else {
                    shippingAddressEditText.setText("")
                }
                handler.post(checkInfoChange)
            }
        } else {
            shippingAddressCheckBox.isEnabled = false
            shippingAddressCheckBox.setTextColor(Color.GRAY)
        }

    }

    private fun textChangedListenerFunction() {
        val nameTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { // No need to implement
            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                val name = s.toString()
                val editText = when (s.hashCode()) {
                    usernameEditText.text.hashCode() -> {
                        usernameEditText
                    }
                    recipientEditText.text.hashCode() -> {
                        recipientEditText
                    }
                    else -> return
                }
                // Assuming TextInputEditText is directly nested inside TextInputLayout
                val textInputLayout = editText.parent.parent as? TextInputLayout
                textInputLayout?.apply {
                    when {
                        s.isNullOrEmpty() -> {
                            textInputLayout.error = null
                            error = getString(R.string.usernameEmpty)
                        }
                        RegisterHelper.containsSpecialCharacter(name) -> {
                            error = null
                            textInputLayout.error = getString(R.string.illegalCharacters)
                        }
                        charCount in 1..2 -> {
                            error = null
                            textInputLayout.error = getString(R.string.errorUsernameMessage)
                        }
                        else -> {
                            error = null
                            textInputLayout.error = null
                        }
                    }
                }
                handler.post(checkInfoChange)
            }
        }
        usernameEditText.addTextChangedListener(nameTextWatcher)
        recipientEditText.addTextChangedListener(nameTextWatcher)
        val phoneTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { // No need to implement
            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                val phoneNumber = s.toString()
                val editText = when (s.hashCode()) {
                    phoneEditText.text.hashCode() -> {
                        phoneEditText
                    }
                    recipientPhoneEditText.text.hashCode() -> {
                        recipientPhoneEditText
                    }
                    else -> return
                }
                // Assuming TextInputEditText is directly nested inside TextInputLayout
                val textInputLayout = editText.parent.parent as? TextInputLayout
                textInputLayout?.apply {
                    when {
                        s.isNullOrEmpty() -> {
                            textInputLayout.error = null
                            error = getString(R.string.phoneEmpty)
                        }
                        RegisterHelper.containsCharacter(phoneNumber) -> {
                            error = null
                            textInputLayout.error = getString(R.string.illegalCharacters)
                        }
                        charCount in 1..9 -> {
                            error = null
                            textInputLayout.error = getString(R.string.errorPhoneMessage)
                        }
                        else -> {
                            error = null
                            textInputLayout.error = null
                        }
                    }
                }
                handler.post(checkInfoChange)
            }
        }
        phoneEditText.addTextChangedListener(phoneTextWatcher)
        recipientPhoneEditText.addTextChangedListener(phoneTextWatcher)
        val emailTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { // No need to implement
            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                val email = s.toString()
                val editText = when (s.hashCode()) {
//                    emailEditText.text.hashCode() -> {
//                        emailEditText
//                    }
                    recipientEmailEditText.text.hashCode() -> {
                        recipientEmailEditText
                    }
                    else -> return
                }
                // Assuming TextInputEditText is directly nested inside TextInputLayout
                val textInputLayout = editText.parent.parent as? TextInputLayout
                textInputLayout?.apply {
                    when {
                        s.isNullOrEmpty() -> {
                            textInputLayout.error = null
                            error = getString(R.string.emailEmpty)
                        }
                        (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && charCount > 0) -> {
                            error = null
                            textInputLayout.error = getString(R.string.errorEmailMessage)
                        }
                        else -> {
                            error = null
                            textInputLayout.error = null
                        }
                    }
                }
                handler.post(checkInfoChange)
            }
        }
        //emailEditText.addTextChangedListener(emailTextWatcher)
        recipientEmailEditText.addTextChangedListener(emailTextWatcher)

        shippingAddressEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                when {
                    s.isNullOrEmpty() -> {
                        shippingAddressTextInputLayout.error = getString(R.string.shippingAddressEmpty)
                        shippingAddressEditText.error = null
                    }
                    else -> {
                        shippingAddressTextInputLayout.error = null
                        shippingAddressEditText.error = null
                    }
                }
                handler.post(checkInfoChange)
            }
        })
    }
    private fun setEditorActionListener() {
        val editorAction = OnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 隐藏光標
                textView.clearFocus()
                // 關閉鍵盤
                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(textView.windowToken, 0)
                return@OnEditorActionListener true
            }
            false
        }
        usernameEditText.setOnEditorActionListener(editorAction)
        phoneEditText.setOnEditorActionListener(editorAction)
        //emailEditText.setOnEditorActionListener(editorAction)
        recipientEditText.setOnEditorActionListener(editorAction)
        recipientPhoneEditText.setOnEditorActionListener(editorAction)
        recipientEmailEditText.setOnEditorActionListener(editorAction)
        shippingAddressEditText.setOnEditorActionListener(editorAction)
    }
    private fun swipeSetOnRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            clear()
            init()
            radioGroupCheckedChangeListener(requireView())
            swipeRefreshLayout.isRefreshing = false
        }
    }
    private fun clear() {
        clearCheckRadioButton(shippingMethodRadioGroup)
        clearCheckRadioButton(paymentRadioGroup)
        clearCheckRadioButton(deliveryTimeRadioGroup)
        shippingMethod = null
        payment = null
        deliveryTime = null
        recipientCheckBox.isChecked = false
        shippingAddressCheckBox.isChecked = false
        recipientEditText.setText("")
        recipientEmailEditText.setText("")
        recipientPhoneEditText.setText("")
        shippingAddressEditText.setText("")
        recipientTextInputLayout.error = null
        recipientEmailTextInputLayout.error = null
        recipientPhoneTextInputLayout.error = null
        shippingAddressTextInputLayout.error = null
    }
    private fun clearCheckRadioButton(radioGroup: RadioGroup) {
        val checkedId = radioGroup.checkedRadioButtonId
        radioGroup.setOnCheckedChangeListener(null)
        if (checkedId != -1) {
            radioGroup.clearCheck()
            for (i in 0 until radioGroup.childCount) {
                val radioButton = radioGroup.getChildAt(i) as RadioButton
                radioButton.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }
    private fun checkoutButton() {
        checkoutPreviousButton.setOnClickListener(checkoutButtonClickListener())
        checkoutNextButton.setOnClickListener(checkoutButtonClickListener())
    }
    private fun checkoutButtonClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val view = it
            val viewId = view?.id
            when(viewId) {
                R.id.checkoutPreviousButton -> {
                    fragmentShift.returnBackStackFragment(requireActivity(), CART_CHECKOUT_FRAGMENT_SHIFT_TAG)
                }
                R.id.checkoutNextButton -> {
                    goToFinalCheckFragment()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
    fun goToFinalCheckFragment() {
        val goToFragment = CartFinalCheckFragment().apply {
            arguments = Bundle().apply {
                putString("shippingMethod", shippingMethod)
                putString("payment", payment)
                putString("deliveryTime", deliveryTime)
                putString("customerName", usernameEditText.text.toString())
                putString("customerPhone", phoneEditText.text.toString())
                putString("customerEmail", emailEditText.text.toString())
                putString("recipientName", recipientEditText.text.toString())
                putString("recipientPhone", recipientPhoneEditText.text.toString())
                putString("recipientEmail", recipientEmailEditText.text.toString())
                putString("shippingAddress", shippingAddressEditText.text.toString())
            }
        }
        fragmentShift.goToNextFragment(goToFragment, requireActivity(),
            FragmentShift.CART_FINAL_CHECK_FRAGMENT_SHIFT_TAG,
            FragmentShift.CART_FINAL_CHECK_FRAGMENT_SHIFT_TAG
        )
    }
}