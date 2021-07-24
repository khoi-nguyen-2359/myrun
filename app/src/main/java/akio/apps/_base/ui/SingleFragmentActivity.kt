package akio.apps._base.ui

import akio.apps.myrun.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class SingleFragmentActivity : AppCompatActivity(R.layout.activity_single_fragment) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            return
        }

        val className = intent.getStringExtra(EXT_FRAGMENT_CLASS_NAME)
            ?: return

        val instance = supportFragmentManager.fragmentFactory.instantiate(classLoader, className)

        intent.getBundleExtra(EXT_FRAGMENT_ARGUMENTS)
            ?.let { arguments -> instance.arguments = arguments }

        supportFragmentManager.beginTransaction()
            .replace(R.id.single_fragment_container, instance)
            .commit()
    }

    companion object {
        const val EXT_FRAGMENT_CLASS_NAME = "SingleFragmentActivity.EXT_FRAGMENT_CLASS_NAME"
        const val EXT_FRAGMENT_ARGUMENTS = "SingleFragmentActivity.EXT_FRAGMENT_ARGUMENTS"

        inline fun <reified T : Fragment> launchIntent(
            context: Context,
            arguments: Bundle? = null
        ): Intent = Intent(context, SingleFragmentActivity::class.java)
            .putExtra(EXT_FRAGMENT_CLASS_NAME, T::class.java.name)
            .putExtra(EXT_FRAGMENT_ARGUMENTS, arguments)
    }
}
