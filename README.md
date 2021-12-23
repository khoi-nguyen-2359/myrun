<a href='https://play.google.com/store/apps/details?id=akio.apps.myrun'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' style="width:250px;height:100px;"/></a>

[![Develop PR Checks](https://github.com/khoi-nguyen-2359/myrun/actions/workflows/develop_pr_checks.yml/badge.svg)](https://github.com/khoi-nguyen-2359/myrun/actions/workflows/develop_pr_checks.yml)

# Android project builds
## **1. `Debug` build**<br/>
Debug build type is used for development only.<br/>
* Clones this file and replace with your content:<br/>

    | Sample file    | Destination | Content  |
    |----------------|-------------|----------|
    | `samples/sample_local.properties` | `local.properties` | Dependencies credentials |

* Build command: <br/>
    `Debug` build type has enough credentials setup, so it can be built with:
    ```
    ./gradlew assembleDebug
    ```
<br/>

## **2. `RC` build**<br/>
Rc build type enables code obfuscation but is still debuggable and logging.
* Prepare the `Debug` build first.
* Clones these files then replace with your content:<br/>

    | Sample file    | Destination | Content  |
    |----------------|-------------|----------|
    | `samples/sample_google-services.json` | `app/src/rc/google-services.json` | Google cloud services |
    | `samples/registration_credentials.xml` | `feature/registration/src/rc/res/values/credentials.xml` | Social sign in |
    | `samples/location_credentials.xml` | `data/location/src/rc/res/values/credentials.xml` | Location services |

* Signing key:<br/>
Add these key/value in `local.properties`:
    ```
    signing.rc.path=[keystore file path]
    signing.rc.password=[keystore password]
    signing.rc.alias=[key alias]
    ```

* Build command:<br/><br/>
    ```
    ./gradlew assembleRc
    ```
