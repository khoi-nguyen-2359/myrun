package akio.apps._base.ui

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat

object SoftKeyboardHelper {
    fun hideKeyboard(view: View) {
        val inputMethodManager =
            ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(view: View) {
        val inputMethodManager =
            ContextCompat.getSystemService(view.context, InputMethodManager::class.java)
        inputMethodManager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}
