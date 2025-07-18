package nl.giejay.android.tv.immich.shared.prefs

enum class NavigationMode: EnumWithTitle {
    PHOTO_BY_PHOTO {
        override fun getTitle(): String = "Photo by Photo"
    },
    DAY_BY_DAY {
        override fun getTitle(): String = "Day by Day"
    },
    WEEK_BY_WEEK {
        override fun getTitle(): String = "Week by Week"
    },
    MONTH_BY_MONTH {
        override fun getTitle(): String = "Month by Month"
    },
    YEAR_BY_YEAR {
        override fun getTitle(): String = "Year by Year"
    };

    companion object {
        fun valueOfSafe(name: String, default: NavigationMode): NavigationMode{
            return entries.find { it.toString() == name } ?: default
        }
    }
}