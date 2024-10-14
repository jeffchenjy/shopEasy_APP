package com.example.shopping_app.Fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.LocaleHelper
import com.example.shopping_app.R

class LanguageFragment: Fragment() {
    private lateinit var fragmentShift: FragmentShift
    private lateinit var toolbar: Toolbar
    private lateinit var changeLanguageRadioGroup: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_language, container, false)
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
        setUpToolbar()
        setDefaultLanguageSelection(view)
        radioGroupCheckedChangeListener(view)
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        changeLanguageRadioGroup = view.findViewById(R.id.changeLanguageRadioGroup)
    }

    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.languageSettings)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            fragmentShift.returnBackStackFragment(requireActivity(),
                FragmentShift.LANGUAGE_FRAGMENT_SHIFT_TAG
            )
        }
    }
    private fun setDefaultLanguageSelection(view: View) {
        val currentLanguage = LocaleHelper.getCurrentLanguage(requireContext())
        when (currentLanguage) {
            "" -> {
                changeLanguageRadioGroup.check(R.id.changeJapaneseButton)
                val selectedRadioButton: RadioButton = view.findViewById(R.id.changeJapaneseButton)
                selectedRadioButton.setBackgroundColor(requireContext().getColor(R.color.radioButtonBackground))
            }
            "zh" -> {
                changeLanguageRadioGroup.check(R.id.changeChineseButton)
                val selectedRadioButton: RadioButton = view.findViewById(R.id.changeChineseButton)
                selectedRadioButton.setBackgroundColor(requireContext().getColor(R.color.radioButtonBackground))
            }
        }
        // 設置 RadioButton 的背景颜色

    }

    private fun radioGroupCheckedChangeListener(view: View) {
        val radioGroupChangeListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (group.id) {
                R.id.changeLanguageRadioGroup -> {
                    val languageName = view.findViewById<RadioButton>(checkedId).text.toString()
                    when(languageName) {
                        getString(R.string.japanese) -> LocaleHelper.setLocale(requireContext(), "")
                        getString(R.string.chinese) -> LocaleHelper.setLocale(requireContext(), "zh")
                    }
                    restartActivity()
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
        }
        changeLanguageRadioGroup.setOnCheckedChangeListener(radioGroupChangeListener)
    }
    private fun restartActivity() {
        val intent = requireActivity().intent
        requireActivity().finish()
        startActivity(intent)
    }
}