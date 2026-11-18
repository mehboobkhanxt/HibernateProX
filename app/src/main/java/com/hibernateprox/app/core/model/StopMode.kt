package com.hibernateprox.app.core.model

import androidx.annotation.StringRes
import com.hibernateprox.app.R

enum class StopMode(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val priorityRank: Int,
) {
    SHIZUKU(
        titleRes = R.string.mode_shizuku_name,
        descriptionRes = R.string.mode_shizuku_desc,
        priorityRank = 1,
    ),
    ROOT(
        titleRes = R.string.mode_root_name,
        descriptionRes = R.string.mode_root_desc,
        priorityRank = 2,
    ),
    ACCESSIBILITY(
        titleRes = R.string.mode_accessibility_name,
        descriptionRes = R.string.mode_accessibility_desc,
        priorityRank = 3,
    ),
    UNAVAILABLE(
        titleRes = R.string.mode_unavailable_name,
        descriptionRes = R.string.mode_unavailable_desc,
        priorityRank = 99,
    ),
}
