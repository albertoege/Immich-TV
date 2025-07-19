package com.albertoeg.android.tv.immich.settings

import androidx.preference.Preference
import arrow.core.Either
import com.albertoeg.android.tv.immich.shared.prefs.AlbumDetailsSettingsScreen
import com.albertoeg.android.tv.immich.shared.prefs.PrefScreen

class AlbumDetailsSettingsFragment : SettingsScreenFragment(){
    override fun getFragment(): SettingsInnerFragment {
        return AlbumDetailsInnerSettingsFragment()
    }
}
class AlbumDetailsInnerSettingsFragment : SettingsScreenFragment.SettingsInnerFragment() {

    override fun getLayout(): Either<Int, PrefScreen> {
        val bundle = requireArguments()
        val albumId = bundle.getString("albumId")!!
        val albumName = bundle.getString("albumName")!!
        return Either.Right(AlbumDetailsSettingsScreen(albumId, albumName))
    }

    override fun handlePreferenceClick(preference: Preference?): Boolean {
        return false
    }
}