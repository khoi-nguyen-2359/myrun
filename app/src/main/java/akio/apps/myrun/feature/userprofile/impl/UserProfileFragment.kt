package akio.apps.myrun.feature.userprofile.impl

import akio.apps._base.Resource
import akio.apps._base.lifecycle.viewLifecycleScope
import akio.apps._base.ui.SingleFragmentActivity
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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckedTextView
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private val dialogDelegate by lazy { DialogDelegate(requireContext()) }
    private val viewBinding by lazy { FragmentUserProfileBinding.bind(requireView()) }

    private val profileViewModel: UserProfileViewModel by viewModel()

    private val googleFitLinkingDelegate = GoogleFitLinkingDelegate()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initGoogleFitAppView()
        initObservers()
    }

    private fun initGoogleFitAppView() {
        viewBinding.isGoogleFitLinkedCheckBox.isChecked =
            googleFitLinkingDelegate.isGoogleFitLinked(requireActivity())
        viewBinding.googleFitItemViewContainer.setOnClickListener {
            if (googleFitLinkingDelegate.isGoogleFitLinked(requireActivity())) {
                showUnlinkConfirmationDialog {
                    viewLifecycleScope.launch {
                        disconnectGoogleFit()
                    }
                }
            } else {
                viewLifecycleScope.launch {
                    connectGoogleFit()
                }
            }
        }
    }

    private suspend fun connectGoogleFit() {
        dialogDelegate.toggleProgressDialog(true)
        googleFitLinkingDelegate.requestGoogleFitPermissions(
            requireActivity(),
            RC_ACTIVITY_RECOGNITION_PERMISSION,
            RC_FITNESS_DATA_PERMISSION,
            this@UserProfileFragment
        )
        viewBinding.isGoogleFitLinkedCheckBox.isChecked =
            googleFitLinkingDelegate.isGoogleFitLinked(requireActivity())
        dialogDelegate.toggleProgressDialog(false)
    }

    private suspend fun disconnectGoogleFit() {
        dialogDelegate.toggleProgressDialog(true)
        val isSuccessfullyDisconnected =
            googleFitLinkingDelegate.disconnectGoogleFit(requireActivity())
        dialogDelegate.toggleProgressDialog(false)
        if (!isSuccessfullyDisconnected) {
            dialogDelegate.showErrorAlert(getString(R.string.user_profile_error_app_disconnect))
        }
        viewBinding.isGoogleFitLinkedCheckBox.isChecked =
            !isSuccessfullyDisconnected
    }

    private fun initObservers() {
        profileViewModel.getUserProfileAlive()
            .observe(viewLifecycleOwner, userProfileObserver)
        profileViewModel.isInlineLoading.observe(viewLifecycleOwner, inlineLoadingObserver)
        profileViewModel.isInProgress.observe(
            viewLifecycleOwner,
            dialogDelegate::toggleProgressDialog
        )
        profileViewModel.getProvidersAlive()
            .observe(viewLifecycleOwner, providersObserver)
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
        val linkedAppMap = mapOf<ExternalAppItemViewIds, ProviderToken<out ExternalAppToken>?>(
            ExternalAppItemViewIds.Strava to providers.strava
        )

        viewBinding.apply {
            linkedAppMap.forEach { (viewIds, token) ->
                val itemViewContainer = linkedAppsContainer.findViewById<View>(viewIds.containerId)
                itemViewContainer.findViewById<CheckedTextView>(viewIds.checkBoxId).isChecked =
                    token != null
                if (token != null) {
                    itemViewContainer.setOnClickListener {
                        showUnlinkConfirmationDialog {
                            profileViewModel.unlinkProvider(token)
                        }
                    }
                } else {
                    itemViewContainer.setOnClickListener {
                        when (viewIds) {
                            ExternalAppItemViewIds.Strava -> openStravaLinking()
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
            Glide.with(requireContext())
                .load(updatedUserProfile.photo)
                .override(resources.getDimensionPixelSize(R.dimen.profile_avatar_size))
                .placeholder(R.drawable.common_avatar_placeholder_image)
                .circleCenterCrop()
                .into(avatarImage)

            userNameTextField.setValue(updatedUserProfile.name)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RC_ACTIVITY_RECOGNITION_PERMISSION -> googleFitLinkingDelegate.verifyActivityRecognitionPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_FITNESS_DATA_PERMISSION -> googleFitLinkingDelegate.verifyFitnessDataPermission()
        }
    }

    companion object {
        const val RC_ACTIVITY_RECOGNITION_PERMISSION = 1
        const val RC_FITNESS_DATA_PERMISSION = 2

        fun launchIntent(context: Context) =
            SingleFragmentActivity.launchIntent<UserProfileFragment>(context)
    }

    enum class ExternalAppItemViewIds(
        @IdRes val containerId: Int,
        @IdRes val checkBoxId: Int
    ) {
        Strava(R.id.strava_item_view_container, R.id.is_strava_linked_check_box),
        GoogleFit(R.id.google_fit_item_view_container, R.id.is_google_fit_linked_check_box)
    }
}
