package pl.applover.kotlinmvp

/**
 * Created by sp0rk on 23.05.17.
 */

interface BaseMvpPresenter<in V : BaseMvpView> {
    fun attachView(view: V)
    fun detachView()
}