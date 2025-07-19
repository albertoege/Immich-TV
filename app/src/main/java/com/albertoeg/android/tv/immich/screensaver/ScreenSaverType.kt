package com.albertoeg.android.tv.immich.screensaver

import com.albertoeg.android.tv.immich.shared.prefs.EnumWithTitle

enum class ScreenSaverType : EnumWithTitle {
    ALBUMS {
        override fun getTitle(): String {
            return "Albums"
        }
    },
    RANDOM {
        override fun getTitle(): String {
            return "Random"
        }
    },
    RECENT {
        override fun getTitle(): String {
            return "Recent"
        }
    },
    SIMILAR_TIME_PERIOD {
        override fun getTitle(): String {
            return "Seasonal"
        }
    }
}