package com.suleymanuysal.smartgreenhouse.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.suleymanuysal.smartgreenhouse.R
import com.suleymanuysal.smartgreenhouse.adapter.FragStateAdapter
import com.suleymanuysal.smartgreenhouse.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var stateAdapter: FragStateAdapter
    private val tabTitles = arrayListOf("Monitoring","Control","Automation")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        stateAdapter = FragStateAdapter(supportFragmentManager,lifecycle)

        stateAdapter.addToFragmentList(IndicatorFragment())
        stateAdapter.addToFragmentList(ControlFragment())
        stateAdapter.addToFragmentList(AutomationFragment())

        binding.viewPager.adapter = stateAdapter

        TabLayoutMediator(binding.tabLayout,binding.viewPager,true,true){
            tab,position ->
            tab.text = tabTitles[position]
        }.attach()

    }
}