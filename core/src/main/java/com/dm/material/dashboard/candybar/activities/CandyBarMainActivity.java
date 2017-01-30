package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.helpers.LicenseHelper;
import com.dm.material.dashboard.candybar.helpers.ReportBugsHelper;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.receivers.CandyBarBroadcastReceiver;
import com.dm.material.dashboard.candybar.services.CandyBarWallpapersService;
import com.dm.material.dashboard.candybar.utils.listeners.SearchListener;
import com.dm.material.dashboard.candybar.utils.listeners.WallpapersListener;
import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.fragments.ApplyFragment;
import com.dm.material.dashboard.candybar.fragments.FAQsFragment;
import com.dm.material.dashboard.candybar.fragments.HomeFragment;
import com.dm.material.dashboard.candybar.fragments.IconsBaseFragment;
import com.dm.material.dashboard.candybar.fragments.RequestFragment;
import com.dm.material.dashboard.candybar.fragments.SettingsFragment;
import com.dm.material.dashboard.candybar.fragments.WallpapersFragment;
import com.dm.material.dashboard.candybar.fragments.dialog.AboutFragment;
import com.dm.material.dashboard.candybar.fragments.dialog.ChangelogFragment;
import com.dm.material.dashboard.candybar.fragments.dialog.InAppBillingFragment;
import com.dm.material.dashboard.candybar.fragments.dialog.IntentChooserFragment;
import com.dm.material.dashboard.candybar.fragments.dialog.LicensesFragment;
import com.dm.material.dashboard.candybar.helpers.ColorHelper;
import com.dm.material.dashboard.candybar.helpers.DrawableHelper;
import com.dm.material.dashboard.candybar.helpers.InAppBillingHelper;
import com.dm.material.dashboard.candybar.helpers.IntentHelper;
import com.dm.material.dashboard.candybar.helpers.PermissionHelper;
import com.dm.material.dashboard.candybar.helpers.RequestHelper;
import com.dm.material.dashboard.candybar.helpers.SoftKeyboardHelper;
import com.dm.material.dashboard.candybar.helpers.ViewHelper;
import com.dm.material.dashboard.candybar.items.InAppBilling;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.utils.Animator;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.listeners.InAppBillingListener;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class CandyBarMainActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener,
        ActivityCompat.OnRequestPermissionsResultCallback, RequestListener, InAppBillingListener,
        SearchListener, WallpapersListener {

    private TextView mToolbarTitle;
    private AppBarLayout mAppBar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private FloatingActionButton mFab;

    private String mFragmentTag;
    private int mPosition, mLastPosition;
    private BillingProcessor mBillingProcessor;
    private CandyBarBroadcastReceiver mReceiver;
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentManager mFragManager;

    private boolean mIsMenuVisible = true;
    private boolean mIsToolbarTitleVisible = false;
    private boolean mIsTitleContainerVisible = true;

    private String mLicenseKey;
    private String[] mDonationProductsId;
    private String[] mPremiumRequestProductsId;
    private int[] mPremiumRequestProductsCount;

    public static List<ResolveInfo> sInstalledApps;

    private static final String TAG_HOME = "home";
    private static final String TAG_APPLY = "apply";
    private static final String TAG_ICONS = "icons";
    private static final String TAG_REQUEST = "request";
    private static final String TAG_WALLPAPERS = "wallpapers";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_FAQS = "faqs";

    public void initMainActivity(@Nullable Bundle savedInstanceState, boolean licenseChecker, byte[] salt,
                                 String licenseKey, String[] donationProductsId,
                                 String[] premiumRequestProductsId, int[] premiumRequestProductsCount) {
        super.setTheme(Preferences.getPreferences(this).isDarkTheme() ?
                R.style.AppThemeDark : R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerBroadcastReceiver();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mAppBar = (AppBarLayout) findViewById(R.id.appbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        ViewHelper.resetNavigationBarTranslucent(this, getResources().getConfiguration().orientation);
        SoftKeyboardHelper helper = new SoftKeyboardHelper(this, findViewById(R.id.container));
        helper.enable();

        mLicenseKey = licenseKey;
        mDonationProductsId = donationProductsId;
        mPremiumRequestProductsId = premiumRequestProductsId;
        mPremiumRequestProductsCount = premiumRequestProductsCount;
        mFragManager = getSupportFragmentManager();

        toolbar.setPopupTheme(Preferences.getPreferences(this).isDarkTheme() ?
                R.style.AppThemeDark : R.style.AppTheme);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Animator.startAlphaAnimation(mToolbarTitle, 0, View.INVISIBLE);
        mAppBar.addOnOffsetChangedListener(this);

        initNavigationView(toolbar);
        initNavigationViewHeader();
        initInAppBilling();

        mPosition = mLastPosition = 0;
        if (savedInstanceState != null) {
            mPosition = mLastPosition = savedInstanceState.getInt("position", 0);
        }
        initTheme();
        initRateReviewFab();

        IntentHelper.sAction = IntentHelper.getAction(getIntent());
        if (IntentHelper.sAction == IntentHelper.ACTION_DEFAULT) {
            setFragment(getFragment(mPosition), false);
        } else {
            setFragment(getActionFragment(IntentHelper.sAction), false);
        }

        initHomeImage();
        checkWallpapers();

        if (Preferences.getPreferences(this).isFirstRun()) {
            if (licenseChecker) {
                LicenseHelper.getLicenseChecker(this).checkLicense(mLicenseKey, salt);
                return;
            }
        }

        if (Preferences.getPreferences(this).isNewVersion())
            ChangelogFragment.showChangelog(mFragManager);

        if (licenseChecker) {
            if (!Preferences.getPreferences(this).isLicensed())
                finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mIsMenuVisible) mDrawerToggle.onConfigurationChanged(newConfig);
        resetNavigationView(newConfig.orientation);
        ViewHelper.resetNavigationBarTranslucent(this, newConfig.orientation);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        int action = IntentHelper.getAction(intent);
        if (action != IntentHelper.ACTION_DEFAULT)
            setFragment(getActionFragment(action), true);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RequestHelper.checkPiracyApp(this);
        IntentHelper.sAction = IntentHelper.getAction(getIntent());
    }

    @Override
    protected void onDestroy() {
        if (mBillingProcessor != null) {
            mBillingProcessor.release();
            mBillingProcessor = null;
        }
        if (mReceiver != null) unregisterReceiver(mReceiver);
        sInstalledApps = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ViewHelper.disableAppBarDrag(mAppBar);
        if (!mIsMenuVisible) return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_changelog) {
            ChangelogFragment.showChangelog(mFragManager);
            return true;
        } else if (id == R.id.menu_licenses) {
            LicensesFragment.showLicensesDialog(mFragManager);
            return true;
        } else if (id == R.id.menu_report_bugs) {
            ReportBugsHelper.checkForBugs(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", mPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            clearBackStack();
            return;
        }

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (!mFragmentTag.equals(TAG_HOME)) {
            mPosition = mLastPosition = 0;
            setFragment(getFragment(mPosition), true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBar, int offset) {
        int maxScroll = appBar.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        mIsToolbarTitleVisible = ViewHelper.handleToolbarTitleVisibility(this, percentage,
                mIsToolbarTitleVisible, mToolbarTitle);
        mIsTitleContainerVisible = ViewHelper.handleTitleContainerVisibility(percentage,
                mIsTitleContainerVisible, findViewById(R.id.home_title_container), mFab);

        if (mFragmentTag.equals(TAG_HOME)) {
            HomeFragment fragment = (HomeFragment) mFragManager.findFragmentByTag(TAG_HOME);
            if (fragment != null) fragment.showOptionsMenu(mIsToolbarTitleVisible);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.PERMISSION_STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
                return;
            }
            PermissionHelper.showPermissionStorageDenied(this);
        }
    }

    @Override
    public void OnSelected(int count) {
        if (mFragmentTag.equals(TAG_REQUEST)) {
            String title = getResources().getString(R.string.navigation_view_request);
            if (count > 0) title += " ("+ count +")";
            mToolbarTitle.setText(title);
        }
    }

    @Override
    public void OnBuyPremiumRequest() {
        if (Preferences.getPreferences(this).isPremiumRequest()) {
            RequestHelper.showPremiumRequestStillAvailable(this);
        } else {
            if (mBillingProcessor == null) return;

            if (mBillingProcessor.loadOwnedPurchasesFromGoogle()) {
                List<String> products = mBillingProcessor.listOwnedProducts();
                if (products != null) {
                    boolean isProductIdExist = false;
                    for (String product : products) {
                        for (String premiumRequestProductId : mPremiumRequestProductsId) {
                            if (premiumRequestProductId.equals(product)) {
                                isProductIdExist = true;
                                break;
                            }
                        }
                    }

                    if (isProductIdExist) {
                        RequestHelper.showPremiumRequestExist(this);
                        return;
                    }
                }
            }

            InAppBillingFragment.showInAppBillingDialog(getSupportFragmentManager(),
                    mBillingProcessor,
                    InAppBillingHelper.PREMIUM_REQUEST,
                    mLicenseKey,
                    mPremiumRequestProductsId,
                    mPremiumRequestProductsCount);
        }
    }

    @Override
    public void OnPremiumRequestBought() {
        if (mFragmentTag.equals(TAG_REQUEST)) {
            RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(TAG_REQUEST);
            if (fragment != null) fragment.premiumRequestBought();
        }
    }

    @Override
    public void OnRequestBuilt(Request request) {
        if (Preferences.getPreferences(this).isPremiumRequest()) {
            if (mBillingProcessor == null) return;

            int count = Preferences.getPreferences(this).getPremiumRequestCount()
                    - request.getRequestCount();
            Preferences.getPreferences(this).setPremiumRequestCount(count);
            if (count == 0) {
                if (mBillingProcessor.consumePurchase(Preferences
                        .getPreferences(this).getPremiumRequestProductId())) {
                    Preferences.getPreferences(this).setPremiumRequest(false);
                    Preferences.getPreferences(this).setPremiumRequestProductId("");
                } else {
                    RequestHelper.showPremiumRequestConsumeFailed(this);
                    return;
                }
            }
            if (mFragmentTag.equals(TAG_REQUEST)) {
                RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(TAG_REQUEST);
                if (fragment != null) fragment.premiumRequestBought();
            }
        }
        IntentChooserFragment.showIntentChooserDialog(mFragManager, request);
    }

    @Override
    public void OnInAppBillingInitialized(boolean success) {
        if (!success) mBillingProcessor = null;
    }

    @Override
    public void OnRestorePurchases() {
        if (mBillingProcessor == null) return;

        if (mBillingProcessor.loadOwnedPurchasesFromGoogle()) {
            List<String> productsId = mBillingProcessor.listOwnedProducts();
            if (productsId != null) {
                SettingsFragment fragment = (SettingsFragment) mFragManager.findFragmentByTag(TAG_SETTINGS);
                if (fragment != null) fragment.restorePurchases(productsId,
                        mPremiumRequestProductsId, mPremiumRequestProductsCount);
            }
        }
    }

    @Override
    public void OnInAppBillingSelected(int type, InAppBilling product) {
        Preferences.getPreferences(this).setInAppBillingType(type);
        if (type == InAppBillingHelper.PREMIUM_REQUEST)
            Preferences.getPreferences(this).setPremiumRequestCount(product.getProductCount());
        if (mBillingProcessor != null) mBillingProcessor.purchase(this, product.getProductId());
    }

    @Override
    public void OnInAppBillingConsume(int type, String productId) {
        if (mBillingProcessor == null) return;

        if (mBillingProcessor.consumePurchase(productId)) {
            if (type == InAppBillingHelper.DONATE) {
                new MaterialDialog.Builder(this)
                        .title(R.string.navigation_view_donate)
                        .content(R.string.donation_success)
                        .positiveText(R.string.close)
                        .show();
            }
        }
    }

    @Override
    public void OnInAppBillingRequest() {
        if (mFragmentTag.equals(TAG_REQUEST)) {
            RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(TAG_REQUEST);
            if (fragment != null) fragment.OnInAppBillingSent(mBillingProcessor);
        }
    }

    @Override
    public void OnWallpapersChecked(@Nullable Intent intent) {
        if (intent != null) {
            int size = intent.getIntExtra("size", 0);
            Database database = new Database(this);
            int offlineSize = database.getWallpapersCount();
            Preferences.getPreferences(this).setAvailableWallpapersCount(size);

            if (size > offlineSize) {
                int accent = ColorHelper.getAttributeColor(this, R.attr.color_accent_secondary);
                LinearLayout container = (LinearLayout) mNavigationView.getMenu().getItem(4).getActionView();
                if (container != null) {
                    TextView counter = (TextView) container.findViewById(R.id.counter);
                    if (counter == null) return;

                    ViewCompat.setBackground(counter, DrawableHelper.getTintedDrawable(this,
                            R.drawable.ic_toolbar_circle, accent));
                    counter.setTextColor(ColorHelper.getTitleTextColor(accent));
                    int newItem = (size - offlineSize);
                    counter.setText(String.valueOf(newItem > 99 ? "99+" : newItem));
                    container.setVisibility(View.VISIBLE);
                    return;
                }
            }
        }

        LinearLayout container = (LinearLayout) mNavigationView.getMenu().getItem(4).getActionView();
        if (container != null) container.setVisibility(View.GONE);
    }

    @Override
    public void OnSearchExpanded(boolean expand) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar);
        mIsMenuVisible = !expand;

        if (expand) {
            int color = ColorHelper.getAttributeColor(this, R.attr.search_toolbar);
            ViewHelper.changeSearchViewActionModeColor(this,
                    collapsingToolbar, R.attr.colorPrimary, R.attr.search_toolbar);
            ColorHelper.setStatusBarColor(this, color);

            int iconColor = ColorHelper.getAttributeColor(this, R.attr.search_toolbar_icon);
            toolbar.setNavigationIcon(DrawableHelper.getTintedDrawable(
                    this, R.drawable.ic_toolbar_back, iconColor));
            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        } else {
            SoftKeyboardHelper.closeKeyboard(this);
            ColorHelper.setTransparentStatusBar(this, Color.TRANSPARENT);
            collapsingToolbar.setContentScrim(new ColorDrawable(
                    ColorHelper.getAttributeColor(this, R.attr.colorPrimary)));
            mDrawerToggle.setDrawerArrowDrawable(new DrawerArrowDrawable(this));
            toolbar.setNavigationOnClickListener(view ->
                    mDrawerLayout.openDrawer(GravityCompat.START));
        }

        mDrawerLayout.setDrawerLockMode(expand ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED :
                DrawerLayout.LOCK_MODE_UNLOCKED);
        supportInvalidateOptionsMenu();
    }

    private void initTheme() {
        if (mPosition == 0)
            ColorHelper.setTransparentStatusBar(this, Color.parseColor("#22000000"));
        ColorHelper.setStatusBarIconColor(this);
        getWindow().getDecorView().setBackgroundColor(
                ColorHelper.getAttributeColor(this, R.attr.main_background));
    }

    private void resetNavigationView(int orientation) {
        int index = mNavigationView.getMenu().size() - 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mNavigationView.getMenu().getItem(index).setVisible(true);
                mNavigationView.getMenu().getItem(index).setEnabled(false);
                return;
            }
        }
        mNavigationView.getMenu().getItem(index).setVisible(false);
    }

    private void initNavigationView(Toolbar toolbar) {
        resetNavigationView(getResources().getConfiguration().orientation);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.txt_open, R.string.txt_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                SoftKeyboardHelper.closeKeyboard(CandyBarMainActivity.this);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (mPosition == 3) {
                    boolean iconRequest = getResources().getBoolean(R.bool.enable_icon_request);
                    boolean premiumRequest = getResources().getBoolean(R.bool.enable_premium_request);
                    if (!iconRequest && premiumRequest) {
                        if (!Preferences.getPreferences(CandyBarMainActivity.this).isPremiumRequestEnabled())
                            return;

                        if (!Preferences.getPreferences(CandyBarMainActivity.this).isPremiumRequest()) {
                            mPosition = mLastPosition;
                            mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                            OnBuyPremiumRequest();
                            return;
                        }
                    }
                }

                if (mPosition == 7) {
                    mPosition = mLastPosition;
                    mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                    AboutFragment.showAbout(mFragManager);
                    return;
                }

                if (mPosition == 8) {
                    mPosition = mLastPosition;
                    mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                    InAppBillingFragment.showInAppBillingDialog(mFragManager,
                            mBillingProcessor,
                            InAppBillingHelper.DONATE,
                            mLicenseKey,
                            mDonationProductsId,
                            null);
                    return;
                }

                if (mPosition != mLastPosition) {
                    mLastPosition = mPosition;
                    setFragment(getFragment(mPosition), true);
                }
            }
        };

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mNavigationView.getMenu().getItem(3).setVisible(
                getResources().getBoolean(R.bool.enable_icon_request) ||
                        Preferences.getPreferences(this).isPremiumRequestEnabled());
        String url = getResources().getString(R.string.wallpaper_json);
        if (!URLUtil.isValidUrl(url)) {
            mNavigationView.getMenu().getItem(4).setVisible(false);
        }
        mNavigationView.getMenu().getItem(mNavigationView.getMenu().size() - 2).setVisible(
                getResources().getBoolean(R.bool.enable_donation));
        ColorStateList colorStateList = ContextCompat.getColorStateList(this,
                Preferences.getPreferences(this).isDarkTheme() ?
                        R.color.navigation_view_item_highlight_dark :
                        R.color.navigation_view_item_highlight);
        mNavigationView.setItemTextColor(colorStateList);
        mNavigationView.setItemIconTintList(colorStateList);
        mNavigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_view_home) mPosition = 0;
            else if (id == R.id.navigation_view_apply) mPosition = 1;
            else if (id == R.id.navigation_view_icons) mPosition = 2;
            else if (id == R.id.navigation_view_request) mPosition = 3;
            else if (id == R.id.navigation_view_wallpapers) mPosition = 4;
            else if (id == R.id.navigation_view_settings) mPosition = 5;
            else if (id == R.id.navigation_view_faqs) mPosition = 6;
            else if (id == R.id.navigation_view_about) mPosition = 7;
            else if (id == R.id.navigation_view_donate) mPosition = 8;

            item.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
        });
    }

    private void initNavigationViewHeader() {
        String imageUrl = getResources().getString(R.string.navigation_view_header);
        String titleText = getResources().getString(R.string.navigation_view_header_title);
        View header = mNavigationView.getHeaderView(0);
        ImageView image = (ImageView) header.findViewById(R.id.header_image);
        LinearLayout container = (LinearLayout) header.findViewById(R.id.header_title_container);
        TextView title = (TextView )header.findViewById(R.id.header_title);
        TextView version = (TextView) header.findViewById(R.id.header_version);
        if (!URLUtil.isValidUrl(imageUrl)) {
            imageUrl = "drawable://" + DrawableHelper.getResourceId(this, imageUrl);
        }

        ImageLoader.getInstance().displayImage(imageUrl, new ImageViewAware(image),
                ImageConfig.getDefaultImageOptions(true), new ImageSize(720, 720), null, null);

        if (titleText.length() == 0) {
            container.setVisibility(View.GONE);
            return;
        }
        title.setText(titleText);
        try {
            String versionText = "v" + getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            version.setText(versionText);
        } catch (Exception ignored) {}
    }

    private void initHomeImage() {
        String image = getResources().getString(R.string.home_image);
        ImageView homeImage = (ImageView) findViewById(R.id.home_image);
        if (!URLUtil.isValidUrl(image)) {
            image = "drawable://" + DrawableHelper.getResourceId(this, image);
        }

        ImageLoader.getInstance().displayImage(image, homeImage,
                ImageConfig.getDefaultImageOptions(true));

        String subtitle = getResources().getString(R.string.home_subtitle);
        if (subtitle.length() == 0) {
            TextView textView = (TextView) findViewById(R.id.home_subtitle);
            textView.setVisibility(View.GONE);
        }
    }

    private void initRateReviewFab() {
        int color = ColorHelper.getTitleTextColor(ColorHelper
                .getAttributeColor(this, R.attr.colorAccent));
        Drawable icon = DrawableHelper.getTintedDrawable(this,
                R.drawable.ic_fab_star, color);
        mFab.setImageDrawable(icon);
        mFab.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://play.google.com/store/apps/details?id=" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            startActivity(intent);
        });
    }

    private void initInAppBilling() {
        boolean donation = getResources().getBoolean(R.bool.enable_donation);
        if (donation || Preferences.getPreferences(this).isPremiumRequestEnabled()) {
            if (mBillingProcessor != null) return;

            if (BillingProcessor.isIabServiceAvailable(this)) {
                mBillingProcessor = new BillingProcessor(this,
                        mLicenseKey, new InAppBillingHelper(this));
            }
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(CandyBarBroadcastReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mReceiver = new CandyBarBroadcastReceiver();
        registerReceiver(mReceiver, filter);
    }

    private void checkWallpapers() {
        if (Preferences.getPreferences(this).isConnectedToNetwork()) {
            Intent intent = new Intent(this, CandyBarWallpapersService.class);
            startService(intent);
            return;
        }

        int size = Preferences.getPreferences(this).getAvailableWallpapersCount();
        if (size > 0) {
            OnWallpapersChecked(new Intent().putExtra("size", size));
        }
    }

    private void expandToolbar(boolean expand) {
        mAppBar.setExpanded(expand, true);
        if (expand) mFab.show();
        else mFab.hide();
        mFab.setVisibility(expand ? View.VISIBLE : View.GONE);
    }

    private void clearBackStack() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            mFragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            OnSearchExpanded(false);
        }
    }

    private void setFragment(Fragment fragment, boolean animate) {
        clearBackStack();

        FragmentTransaction ft = mFragManager.beginTransaction()
                .replace(R.id.container, fragment, mFragmentTag);
        if (animate) ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        try {
            ft.commit();
        } catch (Exception e) {
            ft.commitAllowingStateLoss();
        }

        Menu menu = mNavigationView.getMenu();
        mToolbarTitle.setText(menu.getItem(mPosition).getTitle());
        expandToolbar(mPosition == 0);
        menu.getItem(mPosition).setChecked(true);
    }

    private Fragment getFragment(int position) {
        mFragmentTag = TAG_HOME;
        if (position == 0) {
            mFragmentTag = TAG_HOME;
            return new HomeFragment();
        } else if (position == 1) {
            mFragmentTag = TAG_APPLY;
            return new ApplyFragment();
        } else if (position == 2) {
            mFragmentTag = TAG_ICONS;
            return new IconsBaseFragment();
        } else if (position == 3) {
            mFragmentTag = TAG_REQUEST;
            return new RequestFragment();
        } else if (position == 4) {
            mFragmentTag = TAG_WALLPAPERS;
            return new WallpapersFragment();
        } else if (position == 5) {
            mFragmentTag = TAG_SETTINGS;
            return new SettingsFragment();
        } else if (position == 6) {
            mFragmentTag = TAG_FAQS;
            return new FAQsFragment();
        }
        return new HomeFragment();
    }

    private Fragment getActionFragment(int action) {
        switch (action) {
            case IntentHelper.ICON_PICKER :
            case IntentHelper.IMAGE_PICKER :
                mPosition = mLastPosition = 2;
                mFragmentTag = TAG_ICONS;
                return new IconsBaseFragment();
            case IntentHelper.WALLPAPER_PICKER :
                String url = getResources().getString(R.string.wallpaper_json);
                if (URLUtil.isValidUrl(url)) {
                    mPosition = mLastPosition = 4;
                    mFragmentTag = TAG_WALLPAPERS;
                    return new WallpapersFragment();
                }
            default :
                mPosition = mLastPosition = 0;
                mFragmentTag = TAG_HOME;
                return new HomeFragment();
        }
    }

}
