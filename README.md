# bugsnap
An Android Library enabling users to report bugs directly from their mobile device.

[![CircleCI](https://circleci.com/gh/GrioSF/bugsnap-android/tree/master.svg?style=svg&circle-token=6dddb6e75b300271d4ea1a9aaf65f9cfe00348cc)](https://circleci.com/gh/GrioSF/bugsnap-android/tree/master) - master

[![CircleCI](https://circleci.com/gh/GrioSF/bugsnap-android/tree/dev.svg?style=svg&circle-token=6dddb6e75b300271d4ea1a9aaf65f9cfe00348cc)](https://circleci.com/gh/GrioSF/bugsnap-android/tree/dev) - dev

# Distribution
  * Install fastlane by follow the [fastlane installation instructions](fastlane/README.md)
  * You need the Google Play Android App Bundle signing certificate which goes into the root folder (./upload.jks).
  * You also need to add a keystore.properties file to the root folder (./keystore.properties) along with the storePassword and keyPassword credentials needed to sign the .aab output file.
    * keystore.properties example:
      storePassword=abc
      keyPassword=xyz
  * You need the Google Play Service Account JSON Secret in interact with Google Play, add path to secret file to your environment (~/.bash_profile): `export JSON_SECRET=~/path_to_secret.json`
  * See distrbution options by running `fastlane list`

# Version Management
  * The application version is managed via gradle. The current state is managed via the version.properties file but you must run gradle commands in order to update the versions properly.
  * Valid commands:
    * ./gradlew doBuildNumberGet
    * ./gradlew doBuildNumberIncrement
    * ./gradlew doVersionNumberGet
    * ./gradlew doMajorVersionIncrement
    * ./gradlew doMinorVersionIncrement
    * ./gradlew doFixVersionIncrement
