package pl.applover.kotlinmvp

import android.content.Context

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 12/04/2018.
 */
interface BaseActivityMvpView {
    fun showError(title: String? = null, msg: String? = null, completion: (() -> Unit)? = null)
}