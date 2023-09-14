My Run
---
<p align="center">
    <img src="https://user-images.githubusercontent.com/5378704/151899259-acce479b-fe2d-4802-a6a5-ace19c97b603.png"/>
</p>
<a href='https://play.google.com/store/apps/details?id=akio.apps.myrun'>
    <p align="center">
        <img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' style="width:250px;height:100px;"/>
    </p>
</a>

Status
---
[![Develop](https://github.com/khoi-nguyen-2359/myrun/actions/workflows/develop_pr_checks.yml/badge.svg)](https://github.com/khoi-nguyen-2359/myrun/actions/workflows/develop_pr_checks.yml)
[![Release](https://github.com/khoi-nguyen-2359/myrun/actions/workflows/release_pr_checks.yml/badge.svg)](https://github.com/khoi-nguyen-2359/myrun/actions/workflows/release_pr_checks.yml)
[![Publish](https://github.com/khoi-nguyen-2359/myrun/actions/workflows/publish_on_release.yml/badge.svg)](https://github.com/khoi-nguyen-2359/myrun/actions/workflows/publish_on_release.yml)

Highlights
---
- Utilize persistent storage and WorkManager to make offline UX.
- Use Android foreground service for long run tracking.
- Modularize the app vertically and horizontally.
- Integrate Firebase for backend: Authentication, Firestore, Storage, Cloud functions.
- Build CI/CD pipeline using Github Action (code quality, unit test checks, Play store publish workflow).

Techstack
---
- Kotlin
- Coroutines
- Dagger 2
- Jetpack: Room, DataStore, WorkManager, Compose.
- Firebase: Firestore, Storage, Authentication, Cloud function, Crashlytics.
- Android foreground service.
- Multi-module
- Google Map, Map Box.

Why I'm making this
---
- Learning purpose: where I can change the project's architecture back and forth 😎
- Sample of my work and my interests (beside my [resume 🧾](https://docs.google.com/document/d/1Qs8YTNrCz8lqp6FTVQ3VL7DRZCcARBSn))

Wiki
---
1. [Build setup](https://github.com/khoi-nguyen-2359/myrun/wiki/Build-setup)
2. [Project modules](https://github.com/khoi-nguyen-2359/myrun/wiki/Project-modules)
3. [Workflows](https://github.com/khoi-nguyen-2359/myrun/wiki/Workflows)

Learning stuff
---
1. Modularization on Android
- [Build a modular Android app architecture (Google I/O'19)](https://www.youtube.com/watch?v=PZBg5DIzNww)
- [ANDROID AT SCALE @SQUARE](https://www.droidcon.com/2019/11/15/android-at-scale-square/)

2. WorkManager
- [WorkManager: Beyond the basics (Android Dev Summit '19)](https://www.youtube.com/watch?v=Bz0z694SrEE)

3. Structured concurrency
- [Coroutine Context and Scope](https://elizarov.medium.com/coroutine-context-and-scope-c8b255d59055)
