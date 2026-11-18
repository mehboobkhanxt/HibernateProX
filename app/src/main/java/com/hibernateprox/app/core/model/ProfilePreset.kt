package com.hibernateprox.app.core.model

import androidx.annotation.StringRes
import com.hibernateprox.app.R

enum class ProfilePreset(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val key: String,
) {
    GAMING(R.string.profile_gaming, R.string.profile_gaming_desc, "gaming"),
    BATTERY_SAVER(R.string.profile_battery, R.string.profile_battery_desc, "battery"),
    WORK(R.string.profile_work, R.string.profile_work_desc, "work"),
    NIGHT(R.string.profile_night, R.string.profile_night_desc, "night"),
}
