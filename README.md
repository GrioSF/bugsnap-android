# Bugsnap
[![CircleCI](https://circleci.com/gh/GrioSF/bugsnap-android/tree/master.svg?style=shield&circle-token=6dddb6e75b300271d4ea1a9aaf65f9cfe00348cc)](https://circleci.com/gh/GrioSF/bugsnap-android/tree/master)

An Android Library enabling users to report bugs directly from their mobile device.

### Get Started
1. Add `com.replace.me:bugsnap:x.x.x` as a dependency in your application's `build.gradle` file.
2. Locate (or create if needed) your `local.properties` file and add the following:
```
bugsnap.url="[Your JIRA URL]"
bugsnap.projectName="[Your JIRA project name]"
bugsnap.projectKey="[Your JIRA project key]"
bugsnap.jiraUsername="[Your JIRA username]"
bugsnap.jiraApiKey="[Your JIRA API key]"
```
3. Ensure your project is synced. Finally, in your `Application` subclass, add the following in the `onCreate()` method:
```
override fun onCreate() {                     
    super.onCreate()
    if (BuildConfig.DEBUG) {                  
        BugSnap.init(this,                    
            BuildConfig.BUGSNAP_URL,          
            BuildConfig.BUGSNAP_PROJECT_NAME, 
            BuildConfig.BUGSNAP_PROJECT_KEY,  
            BuildConfig.BUGSNAP_JIRA_USERNAME,
            BuildConfig.BUGSNAP_JIRA_API_KEY) 
    }                                         
}                                             
```

To begin the reporting flow, simply shake the device.

### Development
#### Distribution
  * Install fastlane by follow the [fastlane installation instructions](fastlane/README.md)
  * You need the Google Play Android App Bundle signing certificate which goes into the root folder (./upload.jks).
  * You also need to add a keystore.properties file to the root folder (./keystore.properties) along with the storePassword and keyPassword credentials needed to sign the .aab output file.
    * keystore.properties example:
      storePassword=abc
      keyPassword=xyz
  * You need the Google Play Service Account JSON Secret in interact with Google Play, add path to secret file to your environment (~/.bash_profile): `export JSON_SECRET=~/path_to_secret.json`
  * See distrbution options by running `fastlane list`

#### Version Management
  * The application version is managed via gradle. The current state is managed via the version.properties file but you must run gradle commands in order to update the versions properly.
  * Valid commands:
    * ./gradlew doBuildNumberGet
    * ./gradlew doBuildNumberIncrement
    * ./gradlew doVersionNumberGet
    * ./gradlew doMajorVersionIncrement
    * ./gradlew doMinorVersionIncrement
    * ./gradlew doFixVersionIncrement
