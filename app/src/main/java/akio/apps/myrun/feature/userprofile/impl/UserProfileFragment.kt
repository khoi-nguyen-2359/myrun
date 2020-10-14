package akio.apps.myrun.feature.userprofile.impl

import akio.apps._base.data.Resource
import akio.apps._base.ui.inflate
import akio.apps._base.ui.setVisibleOrGone
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._base.utils.ImageLoaderUtils
import akio.apps.myrun._base.view.TextField
import akio.apps.myrun._di.createViewModelInjectionDelegate
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.databinding.FragmentUserProfileBinding
import akio.apps.myrun.feature.editprofile.impl.EditProfileActivity
import akio.apps.myrun.feature.splash.impl.SplashActivity
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import android.content.Intent
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private val dialogDelegate by lazy { DialogDelegate(requireContext()) }
    private val viewModelInjectionDelegate by lazy { createViewModelInjectionDelegate() }
    private val viewBinding by lazy { FragmentUserProfileBinding.bind(requireView()) }

    private val profileViewModel by lazy { viewModelInjectionDelegate.getViewModel<UserProfileViewModel>() }

    private val facebookCallbackManager by lazy { CallbackManager.Factory.create() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.apply {
            editButton.setOnClickListener { openProfileDetails() }
            logoutButton.setOnClickListener { logout() }
            swipeRefreshLayout.isEnabled = false
        }

        profileViewModel.getUserProfileAlive().observe(viewLifecycleOwner, userProfileObserver)
        profileViewModel.isInlineLoading.observe(viewLifecycleOwner, inlineLoadingObserver)
        profileViewModel.isFacebookAccountLinked().observe(viewLifecycleOwner, facebookLinkObserver)
        profileViewModel.isInProgress.observe(viewLifecycleOwner, dialogDelegate::toggleProgressDialog)
        profileViewModel.getProvidersAlive().observe(viewLifecycleOwner, providersObserver)
    }

    private val providersObserver = Observer<Resource<ExternalProviders>> {
        it.data?.let { showLinkedRunningApps(it) }
    }

    private val facebookLinkObserver = Observer<Boolean> {
        setupFacebookButton(it)
    }

    private val inlineLoadingObserver = Observer<Boolean> {
        viewBinding.swipeRefreshLayout.isRefreshing = it
    }

    private val userProfileObserver = Observer<UserProfile> {
        fillUserProfile(it)
    }

    private val fbCallback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            profileViewModel.linkFacebookAccount(result.accessToken.token)
        }

        override fun onCancel() {}

        override fun onError(error: FacebookException?) {
            dialogDelegate.showErrorAlert(error?.message)
        }
    }

    private fun showLinkedRunningApps(providers: ExternalProviders) {
        viewBinding.apply {
            val externalTokenList = providers.toList()
            linkedAppsContainer.setVisibleOrGone(externalTokenList.isNotEmpty())
            linkedAppsContainer.removeViews(1, linkedAppsContainer.childCount - 1)
            externalTokenList.forEach { providerToken ->
                val itemView = linkedAppsContainer.inflate(R.layout.item_linked_running_app) as TextField
                itemView.setValue(providerToken.runningApp.appName)
                linkedAppsContainer.addView(itemView)
                itemView.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.profile_app_unlink_dialog_message)
                        .setPositiveButton(R.string.action_yes) { dialog, which ->
                            dialog.dismiss()
                            profileViewModel.unlinkProvider(providerToken)
                        }
                        .show()
                }
            }
        }
    }

    private fun logout() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.profile_logout_confirmation)
            .setPositiveButton(R.string.action_yes) { _, _ ->
                profileViewModel.logout()
                startActivity(SplashActivity.clearTaskIntent(requireContext()))
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        LoginManager.getInstance().unregisterCallback(facebookCallbackManager)
    }

    private fun setupFacebookButton(isLinked: Boolean) {
        viewBinding.apply {
            if (isLinked) {
                facebookButton.setText(R.string.profile_linked_with_fb)
                facebookButton.isEnabled = false
            } else {
                facebookButton.isEnabled = true
                facebookButton.setText(R.string.profile_link_to_fb_button)
                LoginManager.getInstance().registerCallback(facebookCallbackManager, fbCallback)
            }
        }
    }

    private fun fillUserProfile(updatedUserProfile: UserProfile) {
        viewBinding.apply {
            updatedUserProfile.photo?.let {
                ImageLoaderUtils.loadCircleCropAvatar(avatarImage, it, resources.getDimensionPixelSize(R.dimen.profile_avatar_size))
            }

            setFieldValue(userNameTextView, R.string.profile_hint_name, updatedUserProfile.name)
            setFieldValue(emailTextView, R.string.profile_hint_email, updatedUserProfile.email)
            setFieldValue(phoneTextView, R.string.profile_hint_phone, updatedUserProfile.phone)
            setFieldValue(genderTextView, R.string.profile_hint_gender, updatedUserProfile.gender?.name?.capitalize())
            setFieldValue(heightTextView, R.string.profile_hint_height, updatedUserProfile.getHeightText())
            setFieldValue(weightTextView, R.string.profile_hint_weight, updatedUserProfile.getWeightText())
        }
    }

    private fun openProfileDetails() {
        val intent = EditProfileActivity.launchIntentForEditing(requireContext())
        startActivity(intent)
    }

    private fun setFieldValue(textField: TextView, labelRes: Int, value: String?) {
        val noneNullValue = value ?: ""
        val label = getString(labelRes)
        val spannedValue = SpannableString("$label\n$noneNullValue")
        val labelTfSpan = StyleSpan(BOLD)
        spannedValue.setSpan(labelTfSpan, 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val labelTextSpan = TextAppearanceSpan(requireContext(), R.style.TextFieldLabel)
        spannedValue.setSpan(labelTextSpan, 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        if (noneNullValue.isNotEmpty()) {
            val valueSpan = StyleSpan(NORMAL)
            spannedValue.setSpan(valueSpan, label.length + 1, spannedValue.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            val valueTextSpan = TextAppearanceSpan(requireContext(), R.style.TextFieldValue)
            spannedValue.setSpan(valueTextSpan, label.length + 1, spannedValue.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textField.setText(spannedValue)
    }

    companion object {
        fun instantiate() = UserProfileFragment()
    }
}