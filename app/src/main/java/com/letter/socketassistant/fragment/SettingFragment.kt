package com.letter.socketassistant.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.letter.socketassistant.R

/**
 * 设置Fragment
 * @author Letter(nevermindzzt@gmail.com)
 * @since 1.0.4
 */
class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey)
    }
}
