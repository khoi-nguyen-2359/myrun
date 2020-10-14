package akio.apps.myrun.data.userprofile.model

enum class Gender {
	male, female, others;
	
	companion object {
		fun parse(value: String?): Gender? {
			return if (value.isNullOrEmpty()) {
				null
			} else {
				valueOf(value.toLowerCase())
			}
		}
	}
}

