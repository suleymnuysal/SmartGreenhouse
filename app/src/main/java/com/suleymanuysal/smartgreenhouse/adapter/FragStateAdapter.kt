package com.suleymanuysal.smartgreenhouse.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragStateAdapter(fm:FragmentManager,lc:Lifecycle):FragmentStateAdapter(fm,lc) {

    private val fragmentList = ArrayList<Fragment>()

    fun addToFragmentList(fr:Fragment){
        fragmentList.add(fr)
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}