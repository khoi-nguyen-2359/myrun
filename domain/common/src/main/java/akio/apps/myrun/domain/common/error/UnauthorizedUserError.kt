package akio.apps.myrun.domain.common.error

class UnauthorizedUserError : Throwable("User authentication state is not valid for current job")
