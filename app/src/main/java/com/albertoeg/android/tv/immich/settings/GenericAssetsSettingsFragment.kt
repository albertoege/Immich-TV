package com.albertoeg.android.tv.immich

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import arrow.core.Either
import com.albertoeg.android.tv.immich.settings.SettingsScreenFragment
import com.albertoeg.android.tv.immich.shared.prefs.ALL_ASSETS_SORTING
import com.albertoeg.android.tv.immich.shared.prefs.FILTER_CONTENT_TYPE
import com.albertoeg.android.tv.immich.shared.prefs.GenericAssetsSettingsScreen
import com.albertoeg.android.tv.immich.shared.prefs.NAVIGATION_MODE
import com.albertoeg.android.tv.immich.shared.prefs.NavigationMode
import com.albertoeg.android.tv.immich.shared.prefs.PrefScreen
import com.albertoeg.android.tv.immich.shared.prefs.PreferenceManager
import java.util.Calendar


class GenericAssetsSettingsFragment : SettingsScreenFragment(){
    override fun getFragment(): SettingsInnerFragment {
        return GenericAssetsInnerSettingsFragment()
    }
}
class GenericAssetsInnerSettingsFragment : SettingsScreenFragment.SettingsInnerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Quitar el selector de modo de navegación ya que ahora se usa desde el menú
        // findPreference<ListPreference>(NAVIGATION_MODE.key())!!.summary = NAVIGATION_MODE.getValue(PreferenceManager.sharedPreference).getTitle()
        findPreference<ListPreference>(ALL_ASSETS_SORTING.key())!!.summary = ALL_ASSETS_SORTING.getValue(PreferenceManager.sharedPreference).getTitle()
        findPreference<ListPreference>(FILTER_CONTENT_TYPE.key())!!.summary = FILTER_CONTENT_TYPE.getValue(PreferenceManager.sharedPreference).getTitle()

        // Agregar opciones de navegación rápida por fechas
        setupDateNavigationOptions()
    }

    private fun setupDateNavigationOptions() {
        // Crear categoría para navegación rápida
        val dateNavCategory = PreferenceCategory(requireContext())
        dateNavCategory.title = "Navegación Rápida"
        preferenceScreen.addPreference(dateNavCategory)

        // Botón para ir a mes anterior
        val prevMonthPref = Preference(requireContext())
        prevMonthPref.title = "← Mes Anterior"
        prevMonthPref.summary = "Ir a fotos del mes anterior"
        prevMonthPref.setOnPreferenceClickListener {
            navigateByTimePeriod(NavigationMode.MONTH_BY_MONTH, -1)
            dismissDialog()
            true
        }
        dateNavCategory.addPreference(prevMonthPref)

        // Botón para ir a mes siguiente
        val nextMonthPref = Preference(requireContext())
        nextMonthPref.title = "Mes Siguiente →"
        nextMonthPref.summary = "Ir a fotos del mes siguiente"
        nextMonthPref.setOnPreferenceClickListener {
            navigateByTimePeriod(NavigationMode.MONTH_BY_MONTH, 1)
            dismissDialog()
            true
        }
        dateNavCategory.addPreference(nextMonthPref)

        // Botón para ir a año anterior
        val prevYearPref = Preference(requireContext())
        prevYearPref.title = "← Año Anterior"
        prevYearPref.summary = "Ir a fotos del año anterior"
        prevYearPref.setOnPreferenceClickListener {
            navigateByTimePeriod(NavigationMode.YEAR_BY_YEAR, -1)
            dismissDialog()
            true
        }
        dateNavCategory.addPreference(prevYearPref)

        // Botón para ir a año siguiente
        val nextYearPref = Preference(requireContext())
        nextYearPref.title = "Año Siguiente →"
        nextYearPref.summary = "Ir a fotos del año siguiente"
        nextYearPref.setOnPreferenceClickListener {
            navigateByTimePeriod(NavigationMode.YEAR_BY_YEAR, 1)
            dismissDialog()
            true
        }
        dateNavCategory.addPreference(nextYearPref)
    }

    private fun navigateByTimePeriod(period: NavigationMode, direction: Int) {
        // Buscar el fragmento de la cuadrícula de manera más robusta
        var gridFragment: VerticalCardGridFragment<*>? = null

        // 1. Intentar buscar en el fragmentManager principal
        activity?.supportFragmentManager?.fragments?.forEach { fragment ->
            if (fragment is VerticalCardGridFragment<*>) {
                gridFragment = fragment
                return@forEach
            }
            // También buscar en fragmentos hijos
            fragment.childFragmentManager.fragments.forEach { childFragment ->
                if (childFragment is VerticalCardGridFragment<*>) {
                    gridFragment = childFragment
                    return@forEach
                }
            }
        }

        // 2. Si no se encuentra, buscar en todos los fragmentos anidados
        if (gridFragment == null) {
            findVerticalCardGridFragment(activity?.supportFragmentManager)?.let {
                gridFragment = it
            }
        }

        // 3. Ejecutar la navegación si se encontró el fragmento
        gridFragment?.let { fragment ->
            // Llamar directamente a la función de navegación mejorada
            fragment.navigateByYearOrMonth(period, direction)
        } ?: run {
            // Si no se encuentra el fragmento, mostrar error
            Toast.makeText(
                requireContext(),
                "Error: No se pudo navegar por fechas",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun findVerticalCardGridFragment(fragmentManager: androidx.fragment.app.FragmentManager?): VerticalCardGridFragment<*>? {
        fragmentManager?.fragments?.forEach { fragment ->
            if (fragment is VerticalCardGridFragment<*>) {
                return fragment
            }
            // Buscar recursivamente en fragmentos hijos
            findVerticalCardGridFragment(fragment.childFragmentManager)?.let {
                return it
            }
        }
        return null
    }

    private fun dismissDialog() {
        // Cerrar el diálogo de configuración
        activity?.let { activity ->
            val navController = activity.supportFragmentManager.fragments.firstOrNull()
                ?.childFragmentManager?.fragments?.firstOrNull()
                ?.findNavController()
            navController?.popBackStack()
        }
    }

    override fun getLayout(): Either<Int, PrefScreen> {
        return Either.Right(GenericAssetsSettingsScreen)
    }

    override fun handlePreferenceClick(preference: Preference?): Boolean {
        return false
    }

}