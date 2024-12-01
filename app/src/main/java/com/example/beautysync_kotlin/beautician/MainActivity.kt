package com.example.beautysync_kotlin.beautician

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.beautysync_kotlin.R
import com.example.beautysync_kotlin.beautician.fragments.Dashboard
import com.example.beautysync_kotlin.beautician.fragments.Me
import com.example.beautysync_kotlin.beautician.fragments.Orders
import com.example.beautysync_kotlin.both.Feed
import com.example.beautysync_kotlin.both.Search
import com.example.beautysync_kotlin.databinding.ActivityMain2Binding
import com.example.beautysync_kotlin.user.fragments.Home

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding

    val iconColorStatesMain = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
        Color.parseColor("#626348"), Color.parseColor("#626348")))
    val iconColorStatesGray = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
        Color.parseColor("#A2A1A1"), Color.parseColor("#A2A1A1")))


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    b()
                    binding.navView.menu[0].setIcon(R.drawable.dashboard_filled).iconTintList = iconColorStatesMain
                    binding.navView.menu[1].setIcon(R.drawable.search).iconTintList = iconColorStatesGray
                    binding.navView.menu[2].setIcon(R.drawable.feed).iconTintList = iconColorStatesGray
                    binding.navView.menu[3].setIcon(R.drawable.orders).iconTintList = iconColorStatesGray
                    binding.navView.menu[4].setIcon(R.drawable.me).iconTintList = iconColorStatesGray

                    binding.navView.menu[0].isChecked = true
                    binding.navView.menu[1].isChecked = false
                    binding.navView.menu[2].isChecked = false
                    binding.navView.menu[3].isChecked = false
                    binding.navView.menu[4].isChecked = false

                    moveToFragment(Dashboard())
                    return@setOnItemSelectedListener true
                }
                R.id.nav_search -> {
                    b()
                    binding.navView.menu[0].setIcon(R.drawable.dashboard).iconTintList = iconColorStatesGray
                    binding.navView.menu[1].setIcon(R.drawable.search_filled).iconTintList = iconColorStatesGray
                    binding.navView.menu[2].setIcon(R.drawable.feed).iconTintList = iconColorStatesGray
                    binding.navView.menu[3].setIcon(R.drawable.orders).iconTintList = iconColorStatesGray
                    binding.navView.menu[4].setIcon(R.drawable.me).iconTintList = iconColorStatesMain

                    binding.navView.menu[0].isChecked = false
                    binding.navView.menu[1].isChecked = true
                    binding.navView.menu[2].isChecked = false
                    binding.navView.menu[3].isChecked = false
                    binding.navView.menu[4].isChecked = false
                    moveToFragment(Search())
                    return@setOnItemSelectedListener true
                }
                R.id.nav_feed -> {
                    a()
                    binding.navView.menu[0].setIcon(R.drawable.dashboard).iconTintList = iconColorStatesGray
                    binding.navView.menu[1].setIcon(R.drawable.search).iconTintList = iconColorStatesGray
                    binding.navView.menu[2].setIcon(R.drawable.feed_filled).iconTintList = iconColorStatesGray
                    binding.navView.menu[3].setIcon(R.drawable.orders).iconTintList = iconColorStatesGray
                    binding.navView.menu[4].setIcon(R.drawable.me).iconTintList = iconColorStatesMain

                    binding.navView.menu[0].isChecked = false
                    binding.navView.menu[1].isChecked = false
                    binding.navView.menu[2].isChecked = true
                    binding.navView.menu[3].isChecked = false
                    binding.navView.menu[4].isChecked = false
                    moveToFragment(Feed())
                    return@setOnItemSelectedListener true
                }

                R.id.nav_orders -> {
                    b()
                    binding.navView.menu[0].setIcon(R.drawable.dashboard).iconTintList = iconColorStatesGray
                    binding.navView.menu[1].setIcon(R.drawable.search).iconTintList = iconColorStatesGray
                    binding.navView.menu[2].setIcon(R.drawable.feed).iconTintList = iconColorStatesGray
                    binding.navView.menu[3].setIcon(R.drawable.orders_filled).iconTintList = iconColorStatesGray
                    binding.navView.menu[4].setIcon(R.drawable.me).iconTintList = iconColorStatesMain

                    binding.navView.menu[0].isChecked = false
                    binding.navView.menu[1].isChecked = false
                    binding.navView.menu[2].isChecked = false
                    binding.navView.menu[3].isChecked = true
                    binding.navView.menu[4].isChecked = false
                    moveToFragment(Orders())
                    return@setOnItemSelectedListener true
                }

                R.id.nav_me -> {
                    b()
                    binding.navView.menu[0].setIcon(R.drawable.dashboard).iconTintList = iconColorStatesGray
                    binding.navView.menu[1].setIcon(R.drawable.search).iconTintList = iconColorStatesGray
                    binding.navView.menu[2].setIcon(R.drawable.feed).iconTintList = iconColorStatesGray
                    binding.navView.menu[3].setIcon(R.drawable.orders).iconTintList = iconColorStatesGray
                    binding.navView.menu[4].setIcon(R.drawable.me_filled).iconTintList = iconColorStatesMain

                    binding.navView.menu[0].isChecked = false
                    binding.navView.menu[1].isChecked = false
                    binding.navView.menu[2].isChecked = false
                    binding.navView.menu[3].isChecked = false
                    binding.navView.menu[4].isChecked = true
                    moveToFragment(Me())
                    return@setOnItemSelectedListener true
                }
            }

            false
        }

        when (intent.getStringExtra("where_to").toString()) {
            "login" -> {
                a()
                moveToFragment(Feed())
            }
            "home" -> {
                b()
                moveToFragment(Dashboard())
            }
            "orders" -> {
                b()
                moveToFragment(Orders())
            }
        }
    }

    private fun a() {
        val iconColorStates = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                Color.parseColor("#FFFFFFFF"),
                Color.parseColor("#FFFFFFFF")
            )
        )
        binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
        binding.navView.itemIconTintList = iconColorStates
        binding.navView.itemTextColor = iconColorStates
        binding.navView.setBackgroundColor(Color.TRANSPARENT)

    }

    private fun b() {
        val iconColorStates = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
            Color.parseColor("#A2A1A1"), Color.parseColor("#A0A268")))
        binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        binding.navView.itemIconTintList = iconColorStates
        binding.navView.itemTextColor = iconColorStates
        binding.navView.setBackgroundColor(Color.WHITE)

    }

    private fun moveToFragment(fragment: Fragment) {
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()
    }
}