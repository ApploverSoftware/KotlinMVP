package pl.applover.kotlinmvp

import android.content.Intent
import android.support.v4.app.Fragment

/**
 * Created by sp0rk on 24.05.17.
 */
interface FragmentNavigator {
    fun display(fragment: Fragment, into: Int? = null, push: Boolean = true, animIn: Int? = null, animOut: Int? = null, tag: String? = null)
    fun proceedToActivity(intent: Intent)
    fun goBack()
}