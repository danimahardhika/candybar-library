# CandyBar <img src="https://drive.google.com/uc?id=0B0f4ypHfNKm5b0w5SklmMldvajg" width="30">
[![](https://jitpack.io/v/danimahardhika/candybar-library.svg)](https://jitpack.io/#danimahardhika/candybar-library) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
<br>Android Icon Pack Material Dashboard
<p><a href="https://play.google.com/store/apps/details?id=com.material.dashboard.candybar.demo">
<img alt="Get it on Google Play" src="https://camo.githubusercontent.com/bdaf711a93d64d0bb5e5abfc346a8b84ea47f164/68747470733a2f2f706c61792e676f6f676c652e636f6d2f696e746c2f656e5f75732f6261646765732f696d616765732f67656e657269632f656e2d706c61792d62616467652e706e67" height="60" data-canonical-src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" style="max-width:100%;">
</a></p>

# Gradle Dependency
**Requirements**
<ul>
<li>Latest version of Android Studio</li>
<li>Android-SDK Build tools v25</li>
<li>API 25 SDK Platform</li>
<li>Latest version of Android Support Library</li>
<li>Java SE Development Kit 8</li>
</ul>
<p>Take a look on this <a href="https://raw.githubusercontent.com/danimahardhika/candybar-library/master/screenshots/requirements.jpg">screenshot</a> for requirements
<p>The minimum API level supported by this library is API 15</p>
Add JitPack repository to root ```build.gradle```
```groovy
allprojects {
    repositories {
        //...
        maven { url "https://jitpack.io" }
    }
}
```
Add the dependency
```groovy
dependencies {
    //...
    compile 'com.github.danimahardhika.candybar-library:core:1.2.0'
}
```
# Previews
<img src="https://raw.githubusercontent.com/danimahardhika/candybar-library/master/screenshots/home.jpg" width="215">
<img src="https://raw.githubusercontent.com/danimahardhika/candybar-library/master/screenshots/navigation_drawer.jpg" width="215">
<img src="https://raw.githubusercontent.com/danimahardhika/candybar-library/master/screenshots/changelog.jpg" width="215">
<img src="https://raw.githubusercontent.com/danimahardhika/candybar-library/master/screenshots/icon_request.jpg" width="215">
<img src="https://raw.githubusercontent.com/danimahardhika/candybar-library/master/screenshots/cloud_wallpapers.jpg" width="215">
<img src="https://raw.githubusercontent.com/danimahardhika/candybar-library/master/screenshots/icons.jpg" width="215">
<img src="https://raw.githubusercontent.com/danimahardhika/candybar-library/master/screenshots/settings.jpg" width="215">
<img src="https://raw.githubusercontent.com/danimahardhika/candybar-library/master/screenshots/about.jpg" width="215">

# How to Use
Take a look inside <a href="https://github.com/danimahardhika/candybar-library/wiki" target="_blank">Wiki Site</a>.

# Why CandyBar?
<ul>
<li>Very Light &#8594; Compared to other dashboard, CandyBar has smallest size, only around 2 MB.</li>
<li>Premium Icon Request &#8594; CandyBar is the <b>FIRST</b> dashboard that has premium icon request feature (pay to request).</li>
<li>Customization &#8594; CandyBar has a lot of options and easy to customize.</li>
<li>CandyBar tools.</li>
</ul>

# Features
<ul>
<li>Very Light: only 2 MB</li>
<li>License Checker</li>
<li>Apply: 19 launchers</li>
<li>Icon Picker: see all icons included with sections and search</li>
<li>Icon Request</li>
    <ul>
    <li>Regular Request: free to request</li>
    <li>Premium Request: pay to request</li>
    </ul>
<li>Cloud Based Wallpaper</li>
    <ul>
    <li>Preview wallpaper</li>
    <li>Apply wallpaper: enable or disable wallpaper scroll</li>
    <li>Download wallpaper</li>
    </ul>
<li>Settings</li>
    <ul>
    <li>Clear Cache</li>
    <li>Swith to Dark Theme</li>
    <li>Restore Purchases: restore premium request after reinstalling</li>
    </ul>
<li>Frequently Asked Questions: with search</li>
<li>About</li>
<li>Donation</li>
<li>Changelog: show changelog every update</li>
<li>Muzei Live Wallpaper: smart art source loader</li>
</ul>

**NOTE:** This is just Icon Pack Dashboard, not Icon Pack template or Icon Pack tutorial. You need to add required XML for Icon Pack by yourself.

# Support Development
Support development by making donation through demo app at Google Play.

# Open Source Libraries Used
<ul>
<li><a href="https://github.com/evant/gradle-retrolambda">Retrolambda</a></li>
<li><a href="https://github.com/bluelinelabs/LoganSquare">LoganSquare</a></li>
<li><a href="https://github.com/nostra13/Android-Universal-Image-Loader">Universal Image Loader</a></li>
<li><a href="https://github.com/afollestad/material-dialogs">Material Dialog</a></li>
<li><a href="https://github.com/takahirom/PreLollipopTransition">PreLollipopTransition</a></li>
<li><a href="https://github.com/chrisjenx/Calligraphy">Calligraphy</a></li>
<li><a href="https://github.com/anjlab/android-inapp-billing-v3">Android In-App Billing v3</a></li>
<li><a href="https://github.com/lopspower/CircularImageView">Circular ImageView</a></li>
<li><a href="https://github.com/plusCubed/recycler-fast-scroll">Recycler Fast Scroll </a></li>
<li><a href="https://github.com/balysv/material-ripple">Material Ripple Layout</a></li>
</ul>

# License
```
Copyright (c) 2014-2016 Dani Mahardhika

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
