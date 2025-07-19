package com.albertoeg.android.tv.immich

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import androidx.preference.Preference
import arrow.core.Either
import com.albertoeg.android.tv.immich.settings.SettingsScreenFragment
import com.albertoeg.android.tv.immich.shared.prefs.ALL_ASSETS_SORTING
import com.albertoeg.android.tv.immich.shared.prefs.FILTER_CONTENT_TYPE
import com.albertoeg.android.tv.immich.shared.prefs.GenericAssetsSettingsScreen
import com.albertoeg.android.tv.immich.shared.prefs.NAVIGATION_MODE
import com.albertoeg.android.tv.immich.shared.prefs.PrefScreen
import com.albertoeg.android.tv.immich.shared.prefs.PreferenceManager


class GenericAssetsSettingsFragment : SettingsScreenFragment(){
    override fun getFragment(): SettingsInnerFragment {
        return GenericAssetsInnerSettingsFragment()
    }
}
class GenericAssetsInnerSettingsFragment : SettingsScreenFragment.SettingsInnerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<ListPreference>(NAVIGATION_MODE.key())!!.summary = NAVIGATION_MODE.getValue(PreferenceManager.sharedPreference).getTitle()
        findPreference<ListPreference>(ALL_ASSETS_SORTING.key())!!.summary = ALL_ASSETS_SORTING.getValue(PreferenceManager.sharedPreference).getTitle()
        findPreference<ListPreference>(FILTER_CONTENT_TYPE.key())!!.summary = FILTER_CONTENT_TYPE.getValue(PreferenceManager.sharedPreference).getTitle()
    }

    override fun getLayout(): Either<Int, PrefScreen> {
        return Either.Right(GenericAssetsSettingsScreen)
    }

    override fun handlePreferenceClick(preference: Preference?): Boolean {
        return false
    }

}