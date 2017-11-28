# Performance Tracking
The Performance Tracking module enables applications to measure the time taken for executing user scenarios like searching products and displaying product details.
Each scenario/metric lists various method, network call measurements. Performance Tracking even provides api's for starting custom measurements.

## Table of Contents
* [Install Perf Tracking SDK](#install)
* [Customize Tracking](#customize)
* [Configure Tracking](#configure)
* [Enable Debug Logs](#debug)
* [Confirm Performance Tracking SDK Integration](#integration)
* [Migrate from 0.x to 1.x](#migration-guide-1.0)
* [Changelog](#changelog)

##  <a name="install"></a> Installation procedure
### REMS Performance Tracking Credentials
Your app must be registered in the [Relay Portal](https://rs-portal-web-prd-japaneast-wa.azurewebsites.net/) to use the App Performance Tracking feature.
To get your subscription key please refer to the [Relay - Guide to get the subscription Key](https://developers.rakuten.net/hc/en-us/articles/115008917068-Relay-Guide-to-get-the-subscription-Key).
To get your app id please refer to the [Relay - Guide to get the app id](https://developers.rakuten.net/hc/en-us/articles/115015498448-Guide-to-get-the-AppID).
For more information please visit our [Developer Portal](https://developers.rakuten.net/hc/en-us/categories/115001441467-Relay).

### #1 Add dependency to buildscript
```groovy
buildscript {
    repositories {
        maven { url 'http://artifactory.raksdtd.com/artifactory/libs-release' }
    }
    dependencies {
        classpath 'com.rakuten.tech.mobile.perf:plugin:0.2.0'
    }
}

apply plugin: 'com.rakuten.tech.mobile.perf'
```

### <a name="subscription-key"></a>#2 Provide Subscription key and App id
You must provide Configuration api's subscription key and app id as metadata in application manifest file.

```xml
<manifest>
    <application>
        <meta-data android:name="com.rakuten.tech.mobile.relay.SubscriptionKey"
                   android:value="subscriptionKey" />
        <meta-data android:name="com.rakuten.tech.mobile.relay.AppId"
                   android:value="appid" />
    </application>
</manifest>
```

### #3 Build Application
The SDK instruments the application at compile time (in build types other than `debug`). To configure application tracking see [Configure Tracking](#configure).
So when you build your app you will see a `transformClassesWithPerfTrackingFor<BuildType>` task

```bash
$ ./gradlew assembleRelease
:preBuild UP-TO-DATE
:preReleaseBuild UP-TO-DATE
:compileReleaseAidl
# etc...
:transformClassesWithPerfTrackingForRelease
:transformClassesWithDexForRelease
:transformResourcesWithMergeJavaResForRelease
:packageRelease
:assembleRelease

BUILD SUCCESSFUL
```

Now your application is ready to automatically track the launch metrics, network requests, view lifecycle methods, runnables, webview loads, onClick listeners, threads, volley's hurl stack and many more. To add custom measurement and structure them around metrics see [Customize Tracking](#customize).

You will see your measurements in the [Relay Portal](https://rs-portal-web-prd-japaneast-wa.azurewebsites.net/), navigate to your Service and click on the "App Performance" feature.  Note that there is a few hours of delay before the data is reflected in Relay. If you obfuscate your app you can upload the `mapping.txt` in the portal and the tracking data will be deobfuscated for you.

## <a name="customize"></a> Customize Tracking
### Metrics
The Performance Tracking SDK is build around the concepts of **Metrics** - they measure a single event from the user perspective. Examples of metrics are app launch time, a detail screen load time or search result load time.

#### Starting Metrics
To start a metric use the `Metric` API:

```java
@Override public void onCreate(Bundle savedInstanceState) {
    Metric.start("item");
}
```

Currently there can only be one active metric at any given point of time, so if you start another metric the first metric will be considered done.

```java
Metric.start("item");
Metric.start("search"); // at this point the `item` metric is considered done
```

**NOTE:** The launch metric is started automatically by the SDK.

```java
// Custem Metric metric name can be AlphaNumeric, -, _, . and <i>Space</i>.
Metric.start("my_custom_metric");
```

#### <a name="termination"></a> Automatic Metric Termination
Metrics terminate automatically according to a set of rules described below. That means developers are only required to start metrics and the SDK takes care of the rest.

**What makes a metric start:**

* The Launch metric is started automatically by the SDK
* Other metrics are started by the app by calling `Metric#start(String)`

**What makes a metric keep going:**

* Activity life cycle changes
* Fragment life cycle and visibility changes
* Loading a page in WebView

**NOTE:**

By default the current metric is prolonged by UI life cycle events (Activity, Fragment and Webview). In case you start a metric and there are no lifecycle events after starting it your metric will not be recorded (the minimum duration for a metric is 5 milliseconds).

So all metrics which are started after `Activity#OnCreate` or `Fragment#onCreateView` and end before the respective `onDestroy` lifecycle events need to be prolonged by calling `Metric#prolong`.

In parallel execution scenarios (e.g. multiple image download) the metric should be prolonged in each individual execution in order to measure the total download time.

**What makes a metric terminate:**

* User interactions like clicks, back button presses, etc.
* WebView finishes loading a page
* Metric is getting prolonged for more than 10 seconds
* A new metric is started

## <a name="debug"></a> Enable Debug Logs
```xml
  <manifest>
      <application>
          <meta-data android:name="com.rakuten.tech.mobile.perf.debug"
                     android:value="true" />
      </application>
  </manifest>
```

You can see logs by filtering with "Performance Tracking" tag.

## <a name="configure"></a> Configure Tracking
The SDK instruments the application at compile time. Instrumentation is disabled in `debug` build type, which means performance is not tracked in `debug` builds by default.
You can enable/disable the tracking at build time in different build types in your application's `build.gradle`.
If `enable` is `true` the application's code will be instrumented at compile time and performance will be tracked at runtime. If `enable` is `false` your application is compiled and runs just like it would without the SDK.

```
performanceTracking {
    release {
        enable = true
    }
    debug {
        enable = true
    }
    qa {
        enable = false
    }
}
```

## <a name="integration"></a> Confirm the Performance Tracking integration
### Check for Build
* Confirm `transformClassesWithPerfTrackingXXX` tasks are successful without any error during build process.
* If your build fails because of any error in `transformClassesWithPerfTrackingXXX` tasks please contact us through [Inquiry Form](https://developers.rakuten.net/hc/en-us/requests/new?ticket_form_id=399907).
* You can disable tracking as shown in [Configure Tracking](#configure).

### Run your App
On first run of your app after integrating Performance Tracking the module will fetch and store its configuration data, it **will not** send metric data yet. On subsequent runs the module will track performance and send metrics and measurements if the previously received configuration is valid and the enable percentage check succeeds.

### Check Configuration
You can verify this by enabling debug logs as shown in [Enable Debug Logs](#debug). You will see "Error loading configuration" log in failure scenario.

### Check Sending data to eventhub
* Performance Tracking data of your app will reflect in the relay portal after few hours.
* You can even verify this by enabling debug logs as shown in [Enable Debug Logs](#debug). You will see "SEND_METRIC" AND "SEND" in logs.

## <a name="migration-guide-1.0"></a> Migrating from 0.x to 1.x
If you have already integrated the Performance Tracking SDK in a 0.x version (0.1.0, 0.1.1 or 0.2.0) you need to follow these steps to migrate to 1.0.0 and newer.

* Subscription key meta in manifest changed from `com.rakuten.tech.mobile.perf.SubscriptionKey` to `com.rakuten.tech.mobile.relay.SubscriptionKey`. If you still use the former meta key in your manifest replace it with the latter, [refer to the section on configuring the subscription key](#subscription-key).

## <a name="changelog"></a> Changelog
### 1.1.0 (In Progress)
- **[REM-25012](https://jira.rakuten-it.com/jira/browse/REM-25012)**: Send AppID in tracking data

### 1.0.0 (In Progress)
- **[REM-23958](https://jira.rakuten-it.com/jira/browse/REM-23958)**: Send device Memory and Battery information in tracking data
- **[REM-24597](https://jira.rakuten-it.com/jira/browse/REM-24597)**: Use AppID instead of package name while requesting configuration

### 0.2.0 (2017-10-30)
- **[REM-23429](https://jira.rakuten-it.com/jira/browse/REM-23429)**: Always update metric's end time when metric prolong is called
- **[REM-23396](https://jira.rakuten-it.com/jira/browse/REM-23396)**: Add Metric.prolong() to public api and remove StandardMetric class
- **[REM-23143](https://jira.rakuten-it.com/jira/browse/REM-23143)**: Send OS name and OS version information in tracking data
- **[REM-22694](https://jira.rakuten-it.com/jira/browse/REM-22694)**: Send measurement start timestamp in tracking data
- Changes Subscription Key Manifest namespace from `com.rakuten.tech.mobile.perf` to `com.rakuten.tech.mobile.relay`

### 0.1.1
- **[REM-20886](https://jira.rakuten-it.com/jira/browse/REM-20886)**: Fixed Missing country and network operator info in tracking data
- **[REM-20957](https://jira.rakuten-it.com/jira/browse/REM-20957)**: Added build switch to turn Instrumentation on/off
- **[REM-21479](https://jira.rakuten-it.com/jira/browse/REM-21479)**: Fixed events are tracked even when instrumentation is disabled
- **[REM-21577](https://jira.rakuten-it.com/jira/browse/REM-21577)**: Changed location information to Prefecture
- **[REM-22770](https://jira.rakuten-it.com/jira/browse/REM-22770)**: Send location information to proper field
- **[REM-22634](https://jira.rakuten-it.com/jira/browse/REM-22634)**: Provide documentation on how to confirm Performance Tracking SDK integration

### 0.1.0
- MVP Release
