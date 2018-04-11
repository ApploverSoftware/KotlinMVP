package pl.applover.kotlinmvp

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
/**
 * Created by sp0rk on 23.05.17.
 */

abstract class BaseFragment<in V : BaseMvpView, P : BaseMvpPresenter<V>> : Fragment(), BaseMvpView {

    protected abstract var mPresenter: P
    protected var navigator: FragmentNavigator? = null
    lateinit protected var contextActivity: AppCompatActivity

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.attachView(this as V)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        contextActivity = getBaseActivity(this)

        if (contextActivity is FragmentNavigator)
            navigator = contextActivity as FragmentNavigator
        else
            throw ClassCastException(contextActivity.toString() + " must implement FragmentNavigator")
    }

    private fun getBaseActivity(fragment: Fragment?): AppCompatActivity {
        return when (fragment?.activity) {
            null -> getBaseActivity(parentFragment)
            else -> fragment.activity as? AppCompatActivity
                    ?: throw ClassCastException("$fragment$activity has to extend AppCompatActivity")
        }
    }

    override fun onDetach() {
        super.onDetach()
        navigator = null
    }

    override fun showError(title: String?, msg: String?, completion: (() -> Unit)?) {
        Toast.makeText(context, (title ?: "") + (msg ?: ""), Toast.LENGTH_SHORT).show()
        completion?.invoke()
    }
}


