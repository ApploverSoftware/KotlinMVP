package pl.applover.kotlinmvp

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 12/04/2018.
 */
interface BaseMvpView {
    fun showError(title: String? = null, msg: String? = null, completion: (() -> Unit)? = null)
}