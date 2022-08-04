package akio.apps.myrun.data.authentication.api.error

class UnauthorizedUserError(message: String? = null) : Throwable(
    message ?: "User authentication state is not valid for current job"
)
