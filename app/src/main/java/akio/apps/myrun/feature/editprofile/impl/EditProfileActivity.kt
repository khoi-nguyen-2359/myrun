package akio.apps.myrun.feature.editprofile.impl

import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps._base.ui.getNoneEmptyTextOrNull
import akio.apps._base.ui.getTextAsString
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._base.utils.PhotoSelectionDelegate
import akio.apps.myrun._base.utils.circleCenterCrop
import akio.apps.myrun._di.createViewModelInjectionDelegate
import akio.apps.myrun.data.userprofile.model.Gender
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.databinding.ActivityEditProfileBinding
import akio.apps.myrun.feature.cropavatar.CropAvatarActivity
import akio.apps.myrun.feature.editprofile.EditProfileViewModel
import akio.apps.myrun.feature.signin.impl.OtpDialogFragment
import akio.apps.myrun.feature.signin.impl.SignInActivity
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.PhoneAuthCredential
import java.io.File
import java.text.DecimalFormat

class EditProfileActivity : AppCompatActivity(R.layout.activity_edit_profile),
    PhotoSelectionDelegate.EventListener, OtpDialogFragment.EventListener {

    private var croppedPhotoFile: File? = null
    private val photoSelectionDelegate = PhotoSelectionDelegate(
        this,
        null,
        PhotoSelectionDelegate.RequestCodes(
            RC_TAKE_PHOTO_PERMISSIONS,
            RC_PICK_PHOTO_PERMISSIONS,
            RC_TAKE_PHOTO,
            RC_PICK_PHOTO
        ),
        this
    )

    private val isOnboarding: Boolean by lazy { intent.getBooleanExtra(EXT_IS_ONBOARDING, false) }

    private val bodyDimensFormat = DecimalFormat("#.#")

    private val viewModelInjectionDelegate by lazy { createViewModelInjectionDelegate() }

    private val editProfileVM by lazy { viewModelInjectionDelegate.getViewModel<EditProfileViewModel>() }

    private val viewBinding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }

    private val dialogDelegate by lazy { DialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
        observe(editProfileVM.isInProgress, dialogDelegate::toggleProgressDialog)
        observe(editProfileVM.isUpdatingPhoneNumber, dialogDelegate::toggleProgressDialog)
        observeEvent(editProfileVM.isUpdatePhoneNumberSuccess, ::onUpdatePhoneNumberSuccess)
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
        observeEvent(editProfileVM.phoneNumberReauthenticateError) {
            requestReauthenticate(RC_REAUTHENTICATE_FOR_PHONE_UPDATE)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onUpdatePhoneNumberSuccess(unit: Unit) {
        Snackbar.make(
            viewBinding.saveButton,
            R.string.edit_user_profile_mobile_number_update_success_message,
            Snackbar.LENGTH_LONG
        )
            .show()
        (supportFragmentManager.findFragmentByTag(TAG_OTP_DIALOG) as? OtpDialogFragment)?.dismiss()
    }

    private fun onStravaTokenExchanged() {
        setResult(Activity.RESULT_OK)
        Snackbar.make(
            viewBinding.saveButton,
            getString(R.string.edit_user_profile_link_running_app_success_message),
            Snackbar.LENGTH_LONG
        )
            .show()
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        when (fragment) {
            is OtpDialogFragment -> fragment.eventListener = this
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        croppedPhotoFile?.delete()
    }

    private fun fillCurrentUserProfile(userProfile: UserProfile) = viewBinding.apply {
        nameEditText.setText(userProfile.name)
        phoneBox.setFullNumber(userProfile.phone)
        genderTextView.text = userProfile.gender?.name?.capitalize()
        weightEditText.setText(userProfile.weight?.let { bodyDimensFormat.format(it) })
        heightEditText.setText(userProfile.height?.let { bodyDimensFormat.format(it) })

        userProfile.photo?.let { userPhoto ->
            Glide.with(this@EditProfileActivity)
                .load(userPhoto)
                .override(resources.getDimensionPixelSize(R.dimen.profile_avatar_size))
                .circleCenterCrop()
                .into(avatarImageView)
        }
    }

    private fun initViews() = viewBinding.apply {
        setContentView(root)

        avatarImageView.setOnClickListener {
            photoSelectionDelegate.showPhotoSelectionDialog(
                getString(R.string.photo_selection_dialog_title)
            )
        }

        genderTextView.setOnClickListener { openGenderPicker() }

        saveButton.setOnClickListener {
            val userProfileUpdateData = createUpdateData()
            editProfileVM.updateProfile(userProfileUpdateData)
        }

        if (isOnboarding) {
            phoneBox.isEnabled = false
        }
    }

    private fun requestReauthenticate(requestCode: Int) {
        Toast.makeText(
            this,
            R.string.edit_user_profile_session_expired_error_message,
            Toast.LENGTH_LONG
        )
            .show()
        val signInIntent = SignInActivity.launchIntent(this)
        startActivityForResult(signInIntent, requestCode)
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
                Gender.parse(genderTextView.getNoneEmptyTextOrNull()),
                heightEditText.getNoneEmptyTextOrNull()
                    ?.toFloat(),
                weightEditText.getNoneEmptyTextOrNull()
                    ?.toFloat(),
                croppedPhotoFile?.let { Uri.fromFile(it) },
                phoneBox.getFullNumber()
            )
        }
    }

    private fun openGenderPicker() {
        val genderList = resources.getStringArray(R.array.gender_list)
        val dialog = AlertDialog.Builder(this)
            .setItems(genderList) { _, which ->
                viewBinding.genderTextView.text = genderList[which]
            }
            .create()
        dialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        photoSelectionDelegate.onRequestPermissionsResult(requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoSelectionDelegate.onActivityResult(requestCode, resultCode, data)
        checkPhoneUpdateResult(requestCode, resultCode)
        checkReauthenticateResult(requestCode, resultCode)
        checkCropPhotoResult(requestCode, resultCode, data)
    }

    private fun checkCropPhotoResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != RC_CROP_AVATAR || resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        croppedPhotoFile =
            data.getSerializableExtra(CropAvatarActivity.RESULT_CROPPED_AVATAR_FILE) as File

        Glide.with(this)
            .load(croppedPhotoFile)
            .override(resources.getDimensionPixelSize(R.dimen.profile_avatar_size))
            .circleCenterCrop()
            .into(viewBinding.avatarImageView)
    }

    private fun checkReauthenticateResult(requestCode: Int, resultCode: Int) {
        if (requestCode == RC_REAUTHENTICATE_FOR_PROFILE_UPDATE && resultCode == RESULT_OK) {
            editProfileVM.updateProfile(createUpdateData())
        }

        if (requestCode == RC_REAUTHENTICATE_FOR_PHONE_UPDATE && resultCode == RESULT_OK) {
            viewBinding.phoneBox.getFullNumber()
                ?.let { phoneNumber ->
                    openOtp(phoneNumber)
                }
        }
    }

    private fun checkPhoneUpdateResult(requestCode: Int, resultCode: Int) {
        if (requestCode == RC_UPDATE_PHONE && resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    override fun onPhotoSelectionReady(photoContentUri: Uri) {
        startActivityForResult(
            CropAvatarActivity.launchIntent(this, photoContentUri),
            RC_CROP_AVATAR
        )
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
        const val RC_REAUTHENTICATE_FOR_PROFILE_UPDATE = 6
        const val RC_CROP_AVATAR = 7
        const val RC_REAUTHENTICATE_FOR_PHONE_UPDATE = 8

        const val TAG_OTP_DIALOG = "TAG_OTP_DIALOG"

        const val EXT_IS_ONBOARDING = "EXT_IS_ONBOARDING"

        fun launchIntentForOnboarding(context: Context): Intent {
            val intent = Intent(context, EditProfileActivity::class.java)
            intent.putExtra(EXT_IS_ONBOARDING, true)
            return intent
        }

        fun launchIntentForEditing(context: Context): Intent {
            val intent = Intent(context, EditProfileActivity::class.java)
            return intent
        }
    }
}
