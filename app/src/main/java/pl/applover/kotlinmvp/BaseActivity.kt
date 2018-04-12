package pl.applover.kotlinmvp

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

/**
 * Created by janpawlov ( ͡° ͜ʖ ͡°) on 11/04/2018.
 */
abstract class BaseActivity<in V : BaseMvpView, P : BaseMvpPresenter<V>>: AppCompatActivity(), BaseMvpView {

    protected abstract var mPresenter: P

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this as V)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    override fun showError(title: String?, msg: String?, completion: (() -> Unit)?) {
        Toast.makeText(this, (title ?: "") + (msg ?: ""), Toast.LENGTH_SHORT).show()
        completion?.invoke()
    }
}