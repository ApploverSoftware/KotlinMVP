# KotlinMVP
Provides classes to work in MVP architecture.
Extend your Activities/Fragments with provided classes, create proper Presenter and bind them with Contract Interface.

To use this library, add to your build.gradle:

```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

And this dependency:
```
    implementation ('com.github.ApploverSoftware:KotlinMVP:1.0.4'){
        exclude group: 'com.android.support'
    }
```

### First, create a contract that will bind your View with Presenter:

```
interface ExampleContract {

    interface View : BaseMvpView { //put View callback methods here
    }

    interface Presenter : BaseMvpPresenter<View> { //put Presenter methods here
    }
}

```
### Then, create the View and Presenter

```
class ExamplePresenter : BasePresenter<ExampleV>, ExampleContract.Presenter {...}
```

```
class ExampleFragment : BaseFragment<ExampleContract.View, ExampleContract.Presenter>, ExampleContract.View{...} //fragment
class ExampleActivity : BaseActivity<ExampleContract.View, ExampleContract.Presenter>, ExampleContract.View{...} //activity
```

### To simplify names, create TypeAliases.kt and declare aliases of your implemented View, Presenter and Contract Interface:

```
typealias ExampleV = ExampleContract.View
typealias ExampleP = ExampleContract.Presenter

typealias ExampleBF = BaseFragment<ExampleV, ExampleP> //Fragment

typealias ExampleBA = BaseActivity<ExampleV, ExampleP> //Activity

typealias ExampleBP = BasePresenter<ExampleV>
```

### Your class implementation would then look like:
```
class ExampleFragment : ExampleBF(), ExampleV {...} //View
class ExamplePresenter : ExampleBP(), ExampleP {...} //Presenter
```
