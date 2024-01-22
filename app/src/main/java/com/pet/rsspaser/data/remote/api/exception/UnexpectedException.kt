package com.pet.rsspaser.data.remote.api.exception

import androidx.annotation.StringRes
import com.pet.rsspaser.R


/**
 * Not handled unexpected exception
 */
class UnexpectedException(
    cause: Throwable? = null,
    @StringRes val defaultRes: Int = R.string.error_some_thing_went_wrong_content
) : Exception(cause)
