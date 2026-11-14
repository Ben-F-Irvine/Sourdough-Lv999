package com.sourdough.companion.data

/** Keys for the key/value settings table. */
object SettingKeys {
    /** Id of the schedule listed first and marked "Default". */
    const val DEFAULT_TEMPLATE = "default_template_id"

    /** Set once the cold-bulk schedule has been added, so it isn't re-added after being deleted. */
    const val SEEDED_COLD_BULK = "seeded_cold_bulk"
}
