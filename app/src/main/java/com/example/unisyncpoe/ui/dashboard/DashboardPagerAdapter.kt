package com.example.unisyncpoe.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.unisyncpoe.ui.fragments.*

class DashboardPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 5
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TimetableFragment()
            1 -> AnnouncementsFragment()
            2 -> AssignmentsFragment()
            3 -> AttendanceFragment()
            4 -> NetworkFragment()
            else -> TimetableFragment()
        }
    }
}

