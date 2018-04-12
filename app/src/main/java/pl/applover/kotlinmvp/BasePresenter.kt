package pl.applover.kotlinmvp

/**
 * Created by sp0rk on 23.05.17.
 */

abstract class BasePresenter<V : BaseMvpView> : BaseMvpPresenter<V> {

    protected var mView: V? = null

    override fun attachView(view: V) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}