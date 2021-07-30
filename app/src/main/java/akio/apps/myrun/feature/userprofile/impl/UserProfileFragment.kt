package akio.apps.myrun.feature.userprofile.impl

import akio.apps._base.Resource
import akio.apps._base.lifecycle.viewLifecycleScope
import akio.apps._base.ui.SingleFragmentActivity
import akio.apps._base.ui.ViewBindingDelegate
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._base.utils.circleCenterCrop
import akio.apps.myrun._di.viewModel
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import akio.apps.myrun.data.externalapp.model.ProviderToken
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.databinding.FragmentUserProfileBinding
import akio.apps.myrun.feature.editprofile.impl.EditProfileActivity
import akio.apps.myrun.feature.googlefit.GoogleFitLinkingDelegate
import akio.apps.myrun.feature.splash.impl.SplashActivity
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import akio.apps.myrun.feature.userprofile._di.DaggerUserProfileFeatureComponent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import java.util.Locale
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private val dialogDelegate by lazy { DialogDelegate(requireContext()) }
    private val viewBinding by ViewBindingDelegate(FragmentUserProfileBinding::bind)

    private val userId: String? by lazy { arguments?.getString(ARG_USER_ID) }

    private val profileViewModel: UserProfileViewModel by viewModel {
        DaggerUserProfileFeatureComponent.factory().create(
            UserProfileViewModelImpl.Params(userId),
            requireActivity().application
        )
    }

    private val googleFitLinkingDelegate = GoogleFitLinkingDelegate()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
        profileViewModel.getUserProfileAlive().observe(viewLifecycleOwner, userProfileObserver)
        profileViewModel.isInlineLoading.observe(viewLifecycleOwner, inlineLoadingObserver)
        profileViewModel.isInProgress.observe(
            viewLifecycleOwner,
            dialogDelegate::toggleProgressDialog
        )
        profileViewModel.getProvidersAlive().observe(viewLifecycleOwner, providersObserver)
    }

    private fun initViews() {
        setupTopBar()
        viewBinding.apply {
            logoutButton.setOnClickListener { showLogoutAlert() }
            swipeRefreshLayout.isEnabled = false

            currentUserViewGroup.isVisible = profileViewModel.isCurrentUser()
        }
    }

    private fun setupTopBar() {
        viewBinding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit_profile -> {
                    openProfileDetails()
                    true
                }
                else -> false
            }
        }
        viewBinding.topAppBar.setNavigationOnClickListener {
            activity?.finish()
        }
    }

    private val providersObserver = Observer<Resource<ExternalProviders>> {
        it.data?.let(::showLinkedRunningApps)
    }

    private val inlineLoadingObserver = Observer<Boolean> {
        viewBinding.swipeRefreshLayout.isRefreshing = it
    }

    private val userProfileObserver = Observer<UserProfile> {
        fillUserProfile(it)
    }

    private fun showLinkedRunningApps(providers: ExternalProviders) {
        val linkedAppMap = mapOf<ExternalAppItemViewIds, ProviderToken<out ExternalAppToken>?>(
            ExternalAppItemViewIds.Strava to providers.strava
        )

        viewBinding.apply {
            linkedAppMap.forEach { (viewIds, token) ->
                val itemViewContainer = linkedAppsContainer.findViewById<View>(viewIds.containerId)
                itemViewContainer.findViewById<SwitchCompat>(viewIds.checkBoxId).run {
                    isEnabled = true
                    isChecked = token != null
                }

                if (token != null) {
                    itemViewContainer.setOnClickListener {
                        showUnlinkConfirmationDialog {
                            profileViewModel.unlinkProvider(token)
                        }
                    }
                } else {
                    itemViewContainer.setOnClickListener {
                        if (viewIds == ExternalAppItemViewIds.Strava) {
                            openStravaLinking()
                        }
                    }
                }
            }
        }
    }

    private fun openStravaLinking() {
        val loginIntent = LinkStravaDelegate.buildStravaLoginIntent(requireContext())
        startActivity(loginIntent)
    }

    private fun showUnlinkConfirmationDialog(onConfirmation: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.profile_app_unlink_dialog_message)
            .setPositiveButton(R.string.action_yes) { dialog, _ ->
                dialog.dismiss()
                onConfirmation()
            }
            .setNegativeButton(R.string.action_no) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .apply { setCanceledOnTouchOutside(false) }
            .show()
    }

    private fun showLogoutAlert() = viewLifecycleScope.launch {
        val uploadCount = profileViewModel.getActivityUploadCount()
        val alertMessage = buildSpannedString {
            append(getString(R.string.profile_logout_confirmation))
            if (uploadCount > 0) {
                appendLine()
                append(getText(R.string.profile_logout_pending_activity_upload_warning))
            }
        }
        AlertDialog.Builder(requireContext())
            .setMessage(alertMessage)
            .setNegativeButton(R.string.action_no, null)
            .setPositiveButton(R.string.action_yes) { _, _ ->
                logout()
            }
            .show()
    }

    private fun logout() = viewLifecycleScope.launch {
        dialogDelegate.showProgressDialog()
        profileViewModel.logout()
        dialogDelegate.dismissProgressDialog()
        startActivity(SplashActivity.clearTaskIntent(requireContext()))
    }

    private fun fillUserProfile(updatedUserProfile: UserProfile) {
        viewBinding.apply {
            Glide.with(requireContext())
                .load(updatedUserProfile.photo)
                .override(resources.getDimensionPixelSize(R.dimen.profile_avatar_size))
                .placeholder(R.drawable.common_avatar_placeholder_image)
                .circleCenterCrop()
                .into(avatarImage)

            userNameTextField.setValue(updatedUserProfile.name)
            genderTextField.setValue(
                updatedUserProfile.gender?.name?.replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(Locale.getDefault())
                    } else {
                        it.toString()
                    }
                }
            )
            heightTextField.setValue(updatedUserProfile.getHeightText())
            weightTextField.setValue(updatedUserProfile.getWeightText())
        }
    }

    private fun openProfileDetails() {
        val intent = EditProfileActivity.launchIntentForEditing(requireContext())
        startActivity(intent)
    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RC_ACTIVITY_RECOGNITION_PERMISSION ->
                googleFitLinkingDelegate.verifyActivityRecognitionPermission()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_FITNESS_DATA_PERMISSION -> googleFitLinkingDelegate.verifyFitnessDataPermission()
        }
    }

    companion object {
        const val RC_ACTIVITY_RECOGNITION_PERMISSION = 1
        const val RC_FITNESS_DATA_PERMISSION = 2

        /**
         * Pass user id to fetch data, null value is for current user.
         */
        private const val ARG_USER_ID = "ARG_USER_ID"

        fun intentForUserId(context: Context, userId: String) =
            SingleFragmentActivity.launchIntent<UserProfileFragment>(
                context,
                bundleOf(ARG_USER_ID to userId)
            )

        fun intentForCurrentUser(context: Context): Intent =
            SingleFragmentActivity.launchIntent<UserProfileFragment>(context)
    }

    enum class ExternalAppItemViewIds(
        @IdRes val containerId: Int,
        @IdRes val checkBoxId: Int
    ) {
        Strava(R.id.strava_item_view_container, R.id.is_strava_linked_check_box)
    }
}
