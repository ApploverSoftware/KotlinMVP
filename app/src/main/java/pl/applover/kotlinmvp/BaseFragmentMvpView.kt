package pl.applover.kotlinmvp

import android.content.Context

/**
 * Created by sp0rk on 23.05.17.
 */

interface BaseFragmentMvpView : BaseMvpView {
    fun getContext(): Context?
}