package akio.apps.myrun.feature.editprofile.impl

import akio.apps._base.ui.getNoneEmptyTextOrNull
import akio.apps._base.ui.getTextAsString
import akio.apps.base.feature.lifecycle.observe
import akio.apps.base.feature.lifecycle.observeEvent
import akio.apps.base.feature.viewmodel.viewModel
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.PhotoSelectionDelegate
import akio.apps.myrun._base.utils.circleCenterCrop
import akio.apps.myrun.data.userprofile.model.Gender
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.databinding.ActivityEditProfileBinding
import akio.apps.myrun.feature.cropavatar.CropAvatarActivity
import akio.apps.myrun.feature.editprofile.EditProfileViewModel
import akio.apps.myrun.feature.editprofile._di.DaggerEditProfileFeatureComponent
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.bumptech.glide.Glide
import java.io.File
import java.text.DecimalFormat
import java.util.Locale

class EditProfileActivity :
    AppCompatActivity(R.layout.activity_edit_profile),
    PhotoSelectionDelegate.EventListener {

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

    private val bodyDimensFormat = DecimalFormat("#.#")

    private val editProfileVM: EditProfileViewModel by viewModel {
        DaggerEditProfileFeatureComponent.factory().create()
    }

    private val viewBinding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }

    private val dialogDelegate by lazy { akio.apps.myrun.feature.base.DialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
        observe(editProfileVM.isInProgress, dialogDelegate::toggleProgressDialog)
        observe(editProfileVM.userProfile, ::fillCurrentUserProfile)

        observeEvent(editProfileVM.error, dialogDelegate::showExceptionAlert)
        observeEvent(editProfileVM.blankEditDisplayNameError) {
            dialogDelegate.showErrorAlert(getString(R.string.error_invalid_display_name))
        }
        observeEvent(editProfileVM.updateProfileSuccess) {
            onUpdateProfileSuccess()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        croppedPhotoFile?.delete()
    }

    private fun fillCurrentUserProfile(userProfile: UserProfile) = viewBinding.apply {
        nameEditText.setText(userProfile.name)
        genderTextView.text = userProfile.gender?.name?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
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

        viewBinding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_save_profile -> {
                    saveProfileUpdate()
                    true
                }
                else -> false
            }
        }

        viewBinding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    @Suppress("DEPRECATION")
    private fun saveProfileUpdate() {
        val userProfileUpdateData = createUpdateData()
        val connMan = getSystemService<ConnectivityManager>()
        if (userProfileUpdateData.avatarUri != null &&
            connMan?.activeNetworkInfo?.isConnectedOrConnecting != true
        ) {
            Toast.makeText(
                this,
                R.string.edit_user_profile_connection_unavailable_error,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        editProfileVM.updateProfile(userProfileUpdateData)
    }

    private fun onUpdateProfileSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
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

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoSelectionDelegate.onActivityResult(requestCode, resultCode, data)
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
    }

    override fun onPhotoSelectionReady(photoContentUri: Uri) {
        @Suppress("DEPRECATION")
        startActivityForResult(
            CropAvatarActivity.launchIntent(this, photoContentUri),
            RC_CROP_AVATAR
        )
    }

    companion object {
        const val RC_TAKE_PHOTO_PERMISSIONS = 1
        const val RC_PICK_PHOTO_PERMISSIONS = 2
        const val RC_TAKE_PHOTO = 3
        const val RC_PICK_PHOTO = 4
        const val RC_REAUTHENTICATE_FOR_PROFILE_UPDATE = 6
        const val RC_CROP_AVATAR = 7

        fun launchIntentForEditing(context: Context): Intent =
            Intent(context, EditProfileActivity::class.java)
    }
}
