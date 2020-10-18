package akio.apps.myrun.feature.userprofile.impl

import akio.apps._base.data.Resource
import akio.apps._base.ui.SingleFragmentActivity
import akio.apps._base.ui.inflate
import akio.apps._base.ui.setVisibleOrGone
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._base.utils.circleCenterCrop
import akio.apps.myrun._base.view.TextField
import akio.apps.myrun._di.createViewModelInjectionDelegate
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.databinding.FragmentUserProfileBinding
import akio.apps.myrun.feature.editprofile.impl.EditProfileActivity
import akio.apps.myrun.feature.signin.impl.SignInActivity
import akio.apps.myrun.feature.splash.impl.SplashActivity
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
        profileViewModel.getUserProfileAlive().observe(viewLifecycleOwner, userProfileObserver)
        profileViewModel.isInlineLoading.observe(viewLifecycleOwner, inlineLoadingObserver)
        profileViewModel.isInProgress.observe(viewLifecycleOwner, dialogDelegate::toggleProgressDialog)
        profileViewModel.getProvidersAlive().observe(viewLifecycleOwner, providersObserver)
    }

    private fun initViews() {
        viewBinding.apply {
            editButton.setOnClickListener { openProfileDetails() }
            logoutButton.setOnClickListener { logout() }
            swipeRefreshLayout.isEnabled = false
        }
    }

    private val providersObserver = Observer<Resource<ExternalProviders>> {
        it.data?.let { showLinkedRunningApps(it) }
    }

    private val inlineLoadingObserver = Observer<Boolean> {
        viewBinding.swipeRefreshLayout.isRefreshing = it
    }

    private val userProfileObserver = Observer<UserProfile> {
        fillUserProfile(it)
    }

    private fun showLinkedRunningApps(providers: ExternalProviders) {
        viewBinding.apply {
            val externalTokenList = providers.toList()
            linkedAppsContainer.setVisibleOrGone(externalTokenList.isNotEmpty())
            linkedAppsContainer.removeViews(1, linkedAppsContainer.childCount - 1)
            externalTokenList.forEach { providerToken ->
                val itemView = linkedAppsContainer.inflate(R.layout.item_user_profile_linked_running_app) as TextField
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
            .setNegativeButton(R.string.action_no, null)
            .setPositiveButton(R.string.action_yes) { _, _ ->
                profileViewModel.logout()
                startActivity(SplashActivity.clearTaskIntent(requireContext()))
            }
            .show()
    }

    private fun fillUserProfile(updatedUserProfile: UserProfile) {
        viewBinding.apply {
            updatedUserProfile.photo?.let { userPhoto ->
                Glide.with(requireContext())
                    .load(userPhoto)
                    .override(resources.getDimensionPixelSize(R.dimen.profile_avatar_size))
                    .circleCenterCrop()
                    .into(avatarImage)
            }

            userNameTextField.setValue(updatedUserProfile.name)
            emailTextField.setValue(updatedUserProfile.email)
            phoneTextField.setValue(updatedUserProfile.phone)
            genderTextField.setValue(updatedUserProfile.gender?.name?.capitalize())
            heightTextField.setValue(updatedUserProfile.getHeightText())
            weightTextField.setValue(updatedUserProfile.getWeightText())
        }
    }

    private fun openProfileDetails() {
        val intent = EditProfileActivity.launchIntentForEditing(requireContext())
        startActivity(intent)
    }

    companion object {
        fun launchIntent(context: Context) = SingleFragmentActivity.launchIntent<UserProfileFragment>(context)
    }
}