fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
## Android
### android libtest
```
fastlane android libtest
```
Runs all library tests
### android libdeploy
```
fastlane android libdeploy
```
Deploy a new artifact to jcenter
### android apptest
```
fastlane android apptest
```
Runs all app tests
### android appbeta
```
fastlane android appbeta
```
Submit a new internal build to Google Play
### android appdeploy
```
fastlane android appdeploy
```
Deploy a new app version to Google Play

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
