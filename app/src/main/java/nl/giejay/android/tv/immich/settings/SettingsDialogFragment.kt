package nl.giejay.android.tv.immich.settings

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment
import nl.giejay.android.tv.immich.R


class SettingsDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val args = SettingsDialogFragmentArgs.fromBundle(requireArguments())
        val settingsType = args.settingsType
        val fragment = when (settingsType) {
            "album_details" -> {
                val frag = AlbumDetailsSettingsFragment()
                frag.arguments = requireArguments()
                frag
            }
            "meta_data_item" -> {
                val frag = MetaDataItemCustomizerFragment()
                frag.arguments = requireArguments()
                frag
            }
            else -> {
                val frag = PrefSettingsFragment()
                frag.arguments = requireArguments()
                frag
            }
        }
        val v: View = inflater.inflate(R.layout.fragment_dialog, container, false)
        childFragmentManager.beginTransaction().add(R.id.fragment_settings_holder, fragment)
            .commit()
        return v
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // the content
        val root = RelativeLayout(activity)
//        root.layoutParams = ViewGroup.LayoutParams(
//            200,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        )

        // creating the fullscreen dialog
        val dialog = Dialog(requireContext())
        val dialogWindow: Window? = dialog.window
        dialog.setContentView(root)
        dialogWindow!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
//        val params: WindowManager.LayoutParams = dialogWindow.attributes
//        params.height = ViewGroup.LayoutParams.MATCH_PARENT
//        dialogWindow.attributes = params
        dialogWindow.setGravity(Gravity.END)
        return dialog
    }

}