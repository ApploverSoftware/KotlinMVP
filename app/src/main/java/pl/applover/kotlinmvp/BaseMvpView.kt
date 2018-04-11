package pl.applover.kotlinmvp

import android.content.Context

/**
 * Created by sp0rk on 23.05.17.
 */

interface BaseMvpView {
    fun getContext(): Context?
    fun showError(title: String? = null, msg: String? = null, completion: (() -> Unit)? = null)
}