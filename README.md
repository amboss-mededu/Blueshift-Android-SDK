# Blueshift-Android-SDK
Blueshift Android SDK - description goes here.

# Contents
  * [Prerequisites] (#prerequisites)
  * [Setup] (#setup)
    * [Permissions] (#permissions)
  
<a name="prerequisites"></a>
# Prerequisites
  * Android API 14 (Android 4.0 - ICS) or above.
  * API key provided by Blueshift

<a name="setup"></a>
# Setup

<a name="permissions"></a>
## Permissions

Add the following permissions to your `AndroidManifest.xml`

```xml
<!-- For GCM notification -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />

<!-- Important: Replace 'com.blueshift.sampleapp' with your app's package name -->
<permission
    android:name="com.blueshift.sampleapp.permission.C2D_MESSAGE"
    android:protectionLevel="signature" />

<!-- Important: Replace 'com.blueshift.sampleapp' with your app's package name -->
<uses-permission android:name="com.blueshift.sampleapp.permission.C2D_MESSAGE" />

<!-- For downloading and reading images shown in notification -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- For scheduling network operations effectively -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- For analytical purpose -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## Rich push notification ##

Add the following block inside `<application>` tag. This part is required to receive GCM push messages in your app.

```xml
<receiver
    android:name="com.blueshift.gcm.GCMBroadcastReceiver"
    android:permission="com.google.android.c2dm.permission.SEND">
    <intent-filter>
        <action android:name="com.google.android.c2dm.intent.RECEIVE" />
        <action android:name="com.google.android.c2dm.intent.REGISTRATION" />

        <!-- Important: Replace 'com.blueshift.sampleapp' with your app's package name -->
        <category android:name="com.blueshift.sampleapp" />
    </intent-filter>
</receiver>

<service android:name="com.blueshift.gcm.GCMIntentService" />

<!-- Important: Replace 'com.blueshift.sampleapp' with your app's package name -->
<receiver android:name="com.blueshift.rich_push.RichPushBroadcastReceiver">
    <intent-filter>
        <action android:name="com.blueshift.sampleapp.RICH_PUSH_RECEIVED" />

        <category android:name="com.blueshift.sampleapp" />
    </intent-filter>
</receiver>

<meta-data
    android:name="com.blueshift.gcm_sender_id"
    android:value="id:YOUR_GCM_SENDER_ID" />
```

Add the following block inside `<application>` tag to enable deeplinking from notification to product/cart/offer pages.

```xml
<receiver android:name="com.blueshift.rich_push.RichPushActionReceiver">
    <!-- Important: Replace 'com.blueshift.sampleapp' with your app's package name -->
    <intent-filter>
        <action android:name="com.blueshift.sampleapp.ACTION_VIEW" />
        <action android:name="com.blueshift.sampleapp.ACTION_BUY" />
        <action android:name="com.blueshift.sampleapp.ACTION_OPEN_CART" />
        <action android:name="com.blueshift.sampleapp.ACTION_OPEN_OFFER_PAGE" />

        <category android:name="com.blueshift.sampleapp" />
    </intent-filter>
</receiver>
```

If you wish to override the notifications received by the SDK, then add the following block inside <application> tag and replace the "name" attribute with your `BroadcastReceiver`'s name.

```xml
<receiver android:name=".YourCustomPushReceiver">
    <intent-filter>
        <action android:name="com.blueshift.sampleapp.ACTION_PUSH_RECEIVED" />
    </intent-filter>
</receiver>
```

## Batch Events And Install Tracking ##
Add this receiver inside `<application>` tag to enable batch events support. This is important. If this is missing in AndroidManifest.xml, the event tracking will not happen properly.

```xml
<receiver android:name="com.blueshift.batch.AlarmReceiver"/>
```

Add the following block inside `<application>` tag to enable app install tracking by SDK.

```xml
<receiver
    android:name="com.blueshift.receiver.AppInstallReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="com.android.vending.INSTALL_REFERRER" />

        <data android:scheme="package" />
    </intent-filter>
</receiver>
```

## Initializing SDK ##

To initialize the SDK, add the following code inside `onCreate()` of your `Application` file.

```java
Configuration configuration = new Configuration();
configuration.setAppIcon(R.mipmap.ic_launcher);                 // provide app icon resource id

// For deep-linking from notifications
configuration.setProductPage(ProductActivity.class);            // provide product activity class
configuration.setCartPage(CartActivity.class);                  // provide cart activity class
configuration.setOfferDisplayPage(OfferDisplayActivity.class);  // provide offers activity class

// This time is used as batch interval. 30 min if not set.
// It is recommended to use one of the following for API < 19 devices.
// AlarmManager.INTERVAL_FIFTEEN_MINUTES
// AlarmManager.INTERVAL_HALF_HOUR
// AlarmManager.INTERVAL_HOUR
// AlarmManager.INTERVAL_HALF_DAY
// AlarmManager.INTERVAL_DAY
configuration.setBatchInterval(timeInMilliSeconds);             

configuration.setApiKey("YOUR_BLUESHIFT_API_KEY");

Blueshift.getInstance(this).initialize(configuration);
```

## Set user info ##
The `UserInfo` class helps sending the user related details to Blueshift. If the values are set after sign in, then they will be used for building the events params next time onwards. Here is an example of setting retailer customer id and email after user sign in.

```java
// sign in complete
UserInfo userInfo = UserInfo.getInstance(this);
userInfo.setRetailerCustomerId(retailerCustomerId);
userInfo.setEmail(email);
// It is important to save the instance once an updation is made on UserInfo
userInfo.save(context);
```
