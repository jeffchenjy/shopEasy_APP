package com.example.shopping_app

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ApiHelper.ConnectToServerHelper.Companion.clearAll
import com.example.shopping_app.ApiHelper.RegisterHelper.Companion.memberLoginFlag
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesEmailKey
import com.example.shopping_app.EncryptionUtils.Companion.SharedPreferencesPasswordKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout

class HomeFragment : Fragment() {
    private lateinit var connectToServerHelper: ConnectToServerHelper
    private lateinit var encryptionUtils: EncryptionUtils
    private lateinit var fragmentShift: FragmentShift
    /* toolbar */
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    /* ViewPager */
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        fragmentShift.setNavigationBarViewVisibility(requireActivity(), R.id.bottomNavigation, View.VISIBLE)
        findView(view)
        setUpToolbar()
        setupViewPager()
        setNavigationItemSelectedListener()

        encryptionUtils = EncryptionUtils(requireContext())
        connectToServerHelper = if(memberLoginFlag) {
            val memberEmail = encryptionUtils.getDecryptedData(SharedPreferencesEmailKey)
            val memberPassword = encryptionUtils.getDecryptedData(SharedPreferencesPasswordKey)
            ConnectToServerHelper(memberEmail, memberPassword)
        } else {
            ConnectToServerHelper()
        }
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.main_toolbar)
        drawerLayout = view.findViewById(R.id.drawerLayout)
        navigationView = view.findViewById(R.id.navigationView)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
    }
    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.mainTitle)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_menu)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            drawerLayout.let {
                if (it.isDrawerOpen(GravityCompat.START)) {
                    it.closeDrawer(GravityCompat.START)
                } else {
                    it.openDrawer(GravityCompat.START)
                }
            }
        }
    }
    private fun setupViewPager() {
        tabLayout.setupWithViewPager(viewPager)
        val fragmentList = listOf(HomePagerFragment(), BookPagerFragment(), ComputerPagerFragment(), AppliancePagerFragment())
        val tabTitles = listOf(getString(R.string.home), getString(R.string.book), getString(R.string.computer), getString(R.string.appliances))
        val adapter = MyPagerAdapter(childFragmentManager, fragmentList, tabTitles)
        viewPager.adapter = adapter
    }
    private fun setNavigationItemSelectedListener() {
        navigationView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when(item.itemId) {
                R.id.drawHome -> {

                }
                R.id.drawAbout -> {
                    aboutDialog()
                }
                R.id.drawCopyright -> {
                    copyrightDialog()
                }
                R.id.drawExit -> {
                    exitAppDialog()
                }
            }
            false
        }
    }
    private fun aboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_about)
            .setTitle(resources.getString(R.string.aboutAppTitle))
            .setMessage(resources.getString(R.string.aboutApp))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }
    private fun copyrightDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_copyright)
            .setTitle(resources.getString(R.string.copyrightTitle))
            .setMessage(resources.getString(R.string.copyright))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }
    private fun exitAppDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_exit)
            .setTitle(resources.getString(R.string.escTitle))
            .setMessage(resources.getString(R.string.esc))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                // Respond to positive button press
                val activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
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
    override fun onResume() {
        super.onResume()
        val bottomNavigation = requireActivity().findViewById<NavigationBarView>(R.id.bottomNavigation)
        if(!bottomNavigation.menu.findItem(R.id.barHome).isChecked) {
            bottomNavigation.menu.findItem(R.id.barHome).setIcon(R.drawable.ic_home)
            bottomNavigation.menu.findItem(R.id.barCart).setIcon(R.drawable.ic_cart_empty)
            bottomNavigation.menu.findItem(R.id.barHome).isChecked = true
            bottomNavigation.menu.findItem(R.id.barCart).isChecked = false
            bottomNavigation.selectedItemId = R.id.barHome
        }
        if(!NetworkUtils.isNetworkOnline(requireContext())) {
            clearAll()
        }
    }
}