package akio.apps.myrun.feature.editprofile.impl

import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps._base.ui.getNoneEmptyTextOrNull
import akio.apps._base.ui.getTextAsString
import akio.apps.myrun.R
import akio.apps.myrun.STRAVA_APP_ID
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._base.utils.ImageLoaderUtils
import akio.apps.myrun._base.utils.PhotoSelectionDelegate
import akio.apps.myrun._di.createViewModelInjectionDelegate
import akio.apps.myrun.data.externalapp.model.RunningApp
import akio.apps.myrun.data.userprofile.model.Gender
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.databinding.ActivityEditProfileBinding
import akio.apps.myrun.feature.cropavatar.CropAvatarActivity
import akio.apps.myrun.feature.editprofile.EditProfileViewModel
import akio.apps.myrun.feature.signin.impl.OtpDialogFragment
import akio.apps.myrun.feature.signin.impl.SignInActivity
import akio.apps.myrun.feature.signin.impl.SignInMethod
import akio.apps.myrun.feature.userprofile.impl.ProfileEditData
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.PhoneAuthCredential
import java.io.File
import java.text.DecimalFormat

class EditProfileActivity: AppCompatActivity(R.layout.activity_edit_profile), PhotoSelectionDelegate.EventListener, OtpDialogFragment.EventListener {

    private var croppedPhotoFile: File? = null
    private val photoSelectionDelegate = PhotoSelectionDelegate(
		this, null, PhotoSelectionDelegate.RequestCodes(
			RC_TAKE_PHOTO_PERMISSIONS, RC_PICK_PHOTO_PERMISSIONS, RC_TAKE_PHOTO, RC_PICK_PHOTO
		), this
	)

    private val stravaRedirectUri by lazy { getString(R.string.app_scheme) + "://localhost" }

    private val onboardingMethod: SignInMethod? by lazy { intent.getSerializableExtra(EXT_ONBOARDING_METHOD) as SignInMethod? }
    private val isOnboarding by lazy { onboardingMethod != null }

    private val bodyDimensFormat = DecimalFormat("#.#")

    private val viewModelInjectionDelegate by lazy { createViewModelInjectionDelegate() }

    private val editProfileVM by lazy { viewModelInjectionDelegate.getViewModel<EditProfileViewModel>() }

    private val viewBinding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }

    private val dialogDelegate by lazy { DialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()

        intent.data?.let { checkStravaLoginResult(it) }
    }

    private fun initObservers() {
        observe(editProfileVM.isInProgress, dialogDelegate::toggleProgressDialog)
        observe(editProfileVM.isUpdatingPhoneNumber, dialogDelegate::toggleProgressDialog)
        observe(editProfileVM.userProfile, ::fillCurrentUserProfile)

        observeEvent(editProfileVM.error, dialogDelegate::showExceptionAlert)
        observeEvent(editProfileVM.stravaTokenExchangedSuccess) {
            onStravaTokenExchanged()
        }
        observeEvent(editProfileVM.blankEditDisplayNameError) {
            dialogDelegate.showErrorAlert(getString(R.string.error_invalid_display_name))
        }
        observeEvent(editProfileVM.openOtp) { navigationInfo ->
            openOtp(navigationInfo.phoneNumber)
        }
        observeEvent(editProfileVM.updateProfileSuccess) {
            onUpdateProfileSuccess()
        }
        observeEvent(editProfileVM.recentLoginRequiredError) {
            requestReauthenticate()
        }
    }

    private fun onStravaTokenExchanged() {
        setResult(Activity.RESULT_OK)
        Toast.makeText(this@EditProfileActivity, getString(R.string.profile_link_running_app_success_message), Toast.LENGTH_LONG).show()
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        when (fragment) {
			is OtpDialogFragment -> fragment.eventListener = this
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.data?.let { checkStravaLoginResult(it) }
    }

    override fun onDestroy() {
        super.onDestroy()

        croppedPhotoFile?.delete()
    }

    override fun onBackPressed() {
        if (!isOnboarding) {
            super.onBackPressed()
        }
    }

    private fun checkStravaLoginResult(data: Uri) {
        if (!data.toString().startsWith(stravaRedirectUri))
            return

        data.getQueryParameter("error")?.let {
            dialogDelegate.showErrorAlert(it)
            return
        }

        data.getQueryParameter("code")?.let { loginCode ->
            editProfileVM.exchangeStravaToken(loginCode)
        }
    }

    private fun openExternalAppList() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.profile_link_running_app_dialog_title))
            .setItems(RunningApp.values().map { it.appName }.toTypedArray()) { dialog, which ->
                when (which) {
                    RunningApp.Strava.ordinal -> openStravaLogin()
                }
            }
            .create()
        dialog.show()
    }

    private fun openStravaLogin() {
        val intentUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", STRAVA_APP_ID)
            .appendQueryParameter("redirect_uri", stravaRedirectUri)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", "activity:write,read")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, intentUri)
        startActivity(intent)
    }

    private fun fillCurrentUserProfile(userProfile: UserProfile) = viewBinding.apply {
        nameEditText.setText(userProfile.name)
        emailEditText.setText(userProfile.email)
        phoneBox.setFullNumber(userProfile.phone)
        genderTextView.text = userProfile.gender?.name?.capitalize()
        weightEditText.setText(userProfile.weight?.let { bodyDimensFormat.format(it) })
        heightEditText.setText(userProfile.height?.let { bodyDimensFormat.format(it) })

        userProfile.photo?.let {
            ImageLoaderUtils.loadCircleCropAvatar(avatarImageView, it, resources.getDimensionPixelSize(R.dimen.profile_avatar_size))
        }
    }

    private fun initViews() = viewBinding.apply {
        setContentView(root)

        connectButton.setOnClickListener { openExternalAppList() }

        avatarImageView.setOnClickListener { photoSelectionDelegate.showPhotoSelectionDialog(getString(R.string.photo_selection_dialog_title)) }

        genderTextView.setOnClickListener { openGenderPicker() }

        saveButton.setOnClickListener {
            val userProfileUpdateData = createUpdateData()
            editProfileVM.updateProfile(userProfileUpdateData)
        }

        when (onboardingMethod) {
			SignInMethod.Facebook -> emailEditText.isEnabled = false
			SignInMethod.Phone -> phoneBox.isEnabled = false
        }
    }

    private fun requestReauthenticate() {
        Toast.makeText(this, R.string.user_profile_recent_login_required_error_message, Toast.LENGTH_LONG).show()
        val signInIntent = SignInActivity.launchIntent(this)
        startActivityForResult(signInIntent, RC_REAUTHENTICATE)
    }

    private fun onUpdateProfileSuccess() {
        (supportFragmentManager.findFragmentByTag(TAG_OTP_DIALOG) as? OtpDialogFragment)?.dismiss()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun openOtp(updatePhone: String) {
        OtpDialogFragment.instantiate(updatePhone)
            .show(supportFragmentManager, TAG_OTP_DIALOG)
    }

    private fun createUpdateData(): ProfileEditData {
        viewBinding.apply {
            return ProfileEditData(
				nameEditText.getTextAsString(),
				emailEditText.getNoneEmptyTextOrNull(),
				Gender.parse(genderTextView.getNoneEmptyTextOrNull()),
				heightEditText.getNoneEmptyTextOrNull()?.toFloat(),
				weightEditText.getNoneEmptyTextOrNull()?.toFloat(),
				croppedPhotoFile,
				phoneBox.getFullNumber()
			)
        }
    }

    private fun openGenderPicker() {
        val genderList = resources.getStringArray(R.array.gender_list)
        val dialog = AlertDialog.Builder(this)
            .setItems(genderList) { dialog, which ->
                viewBinding.genderTextView.text = genderList[which]
            }
            .create()
        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        photoSelectionDelegate.onRequestPermissionsResult(requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoSelectionDelegate.onActivityResult(requestCode, resultCode, data)
        checkPhoneUpdateResult(requestCode, resultCode, data)
        checkReauthenticateResult(requestCode, resultCode)
        checkCropPhotoResult(requestCode, resultCode, data)
    }

    private fun checkCropPhotoResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != RC_CROP_AVATAR || resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        croppedPhotoFile = data.getSerializableExtra(CropAvatarActivity.RESULT_CROPPED_AVATAR_FILE) as File

        ImageLoaderUtils.loadCircleCropAvatar(viewBinding.avatarImageView, croppedPhotoFile, resources.getDimensionPixelSize(R.dimen.profile_avatar_size))
    }

    private fun checkReauthenticateResult(requestCode: Int, resultCode: Int) {
        if (requestCode != RC_REAUTHENTICATE || resultCode != Activity.RESULT_OK)
            return

        editProfileVM.updateProfile(createUpdateData())
    }

    private fun checkPhoneUpdateResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_UPDATE_PHONE && resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    override fun onPhotoSelectionReady(photoContentUri: Uri) {
        startActivityForResult(CropAvatarActivity.launchIntent(this, photoContentUri), RC_CROP_AVATAR)
    }

    override fun onConfirmOtp(phoneAuthCredential: PhoneAuthCredential) {
        editProfileVM.updatePhoneNumber(phoneAuthCredential)
    }

    companion object {
        const val RC_TAKE_PHOTO_PERMISSIONS = 1
        const val RC_PICK_PHOTO_PERMISSIONS = 2
        const val RC_TAKE_PHOTO = 3
        const val RC_PICK_PHOTO = 4
        const val RC_UPDATE_PHONE = 5
        const val RC_REAUTHENTICATE = 6
        const val RC_CROP_AVATAR = 7

        const val TAG_OTP_DIALOG = "TAG_OTP_DIALOG"

        const val EXT_ONBOARDING_METHOD = "EXT_ONBOARDING_METHOD"

        fun launchIntentForOnboarding(context: Context, signInMethod: SignInMethod): Intent {
            val intent = Intent(context, EditProfileActivity::class.java)
            intent.putExtra(EXT_ONBOARDING_METHOD, signInMethod)
            return intent
        }

        fun launchIntentForEditing(context: Context): Intent {
            val intent = Intent(context, EditProfileActivity::class.java)
            return intent
        }
    }
}