package akio.apps._base.error

class UnauthorizedUserError : Throwable("User authentication state is not valid for current job")
