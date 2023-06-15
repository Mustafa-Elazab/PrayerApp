package com.mostafa.alaymiatask

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.pray.PrayFragment
import com.mostafa.alaymiatask.presentation.cycles.home_cycle.fragment.qibla.QiblaFragment


class TestAppFragmentFactory : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (loadFragmentClass(classLoader, className)) {
            PrayFragment::class.java -> PrayFragment()
            QiblaFragment::class.java -> QiblaFragment()

            else -> super.instantiate(classLoader, className)
        }
    }
}