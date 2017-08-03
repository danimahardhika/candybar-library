package com.dm.material.dashboard.candybar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.license.LicenseHelper;
import com.danimahardhika.android.helpers.permission.PermissionCode;
import com.dm.material.dashboard.candybar.applications.CandyBarApplication;
import com.dm.material.dashboard.candybar.databases.Database;
import com.dm.material.dashboard.candybar.fragments.AboutFragment;
import com.dm.material.dashboard.candybar.fragments.dialog.IntentChooserFragment;
import com.dm.material.dashboard.candybar.helpers.ConfigurationHelper;
import com.dm.material.dashboard.candybar.helpers.IconsHelper;
import com.dm.material.dashboard.candybar.helpers.LicenseCallbackHelper;
import com.dm.material.dashboard.candybar.helpers.LocaleHelper;
import com.dm.material.dashboard.candybar.helpers.NavigationViewHelper;
import com.dm.material.dashboard.candybar.helpers.TypefaceHelper;
import com.dm.material.dashboard.candybar.helpers.WallpaperHelper;
import com.dm.material.dashboard.candybar.items.Home;
import com.dm.material.dashboard.candybar.items.Icon;
import com.dm.material.dashboard.candybar.preferences.Preferences;
import com.dm.material.dashboard.candybar.receivers.CandyBarBroadcastReceiver;
import com.dm.material.dashboard.candybar.services.CandyBarWallpapersService;
import com.dm.material.dashboard.candybar.tasks.IconRequestTask;
import com.dm.material.dashboard.candybar.utils.LogUtil;
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
import com.dm.material.dashboard.candybar.fragments.dialog.ChangelogFragment;
import com.dm.material.dashboard.candybar.fragments.dialog.InAppBillingFragment;
import com.dm.material.dashboard.candybar.helpers.InAppBillingHelper;
import com.dm.material.dashboard.candybar.helpers.IntentHelper;
import com.dm.material.dashboard.candybar.helpers.RequestHelper;
import com.dm.material.dashboard.candybar.items.InAppBilling;
import com.dm.material.dashboard.candybar.items.Request;
import com.dm.material.dashboard.candybar.utils.ImageConfig;
import com.dm.material.dashboard.candybar.utils.listeners.InAppBillingListener;
import com.dm.material.dashboard.candybar.utils.listeners.RequestListener;
import com.dm.material.dashboard.candybar.utils.views.HeaderView;
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

public class CandyBarMainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback, RequestListener, InAppBillingListener,
        SearchListener, WallpapersListener {

    private TextView mToolbarTitle;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private String mFragmentTag;
    private int mPosition, mLastPosition;
    private BillingProcessor mBillingProcessor;
    private CandyBarBroadcastReceiver mReceiver;
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentManager mFragManager;
    private LicenseHelper mLicenseHelper;

    private boolean mIsMenuVisible = true;

    private InAppBillingHelper.Property mProperty;

    public static List<Request> sMissedApps;
    public static List<Icon> sSections;
    public static Home sHomeIcon;
    public static int sInstalledAppsCount;
    public static int sIconsCount;

    private static final String TAG_HOME = "home";
    private static final String TAG_APPLY = "apply";
    private static final String TAG_ICONS = "icons";
    private static final String TAG_REQUEST = "request";
    private static final String TAG_WALLPAPERS = "wallpapers";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_FAQS = "faqs";
    private static final String TAG_ABOUT = "about";

    /**
     * @deprecated use {{@link #initMainActivity(Bundle, InAppBillingHelper.Property)}} instead
     */
    @Deprecated
    public void initMainActivity(@Nullable Bundle savedInstanceState, boolean licenseChecker, byte[] salt,
                                 String licenseKey, String[] donationProductsId,
                                 String[] premiumRequestProductsId, int[] premiumRequestProductsCount) {
        InAppBillingHelper.Property property = new InAppBillingHelper.Property(licenseChecker, salt, licenseKey,
                donationProductsId, premiumRequestProductsId, premiumRequestProductsCount);
        initMainActivity(savedInstanceState, property);

    }

    public void initMainActivity(@Nullable Bundle savedInstanceState, InAppBillingHelper.Property property) {
        super.setTheme(Preferences.get(this).isDarkTheme() ?
                R.style.AppThemeDark : R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ColorHelper.setupStatusBarIconColor(this);
        ColorHelper.setNavigationBarColor(this, ContextCompat.getColor(this,
                Preferences.get(this).isDarkTheme() ?
                        R.color.navigationBarDark : R.color.navigationBar));
        registerBroadcastReceiver();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        toolbar.setPopupTheme(Preferences.get(this).isDarkTheme() ?
                R.style.AppThemeDark : R.style.AppTheme);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mProperty = property;
        mFragManager = getSupportFragmentManager();

        initNavigationView(toolbar);
        initNavigationViewHeader();
        initInAppBilling();

        mPosition = mLastPosition = 0;
        if (savedInstanceState != null) {
            mPosition = mLastPosition = savedInstanceState.getInt("position", 0);
            onSearchExpanded(false);
        }

        IntentHelper.sAction = IntentHelper.getAction(getIntent());
        if (IntentHelper.sAction == IntentHelper.ACTION_DEFAULT) {
            setFragment(getFragment(mPosition));
        } else {
            setFragment(getActionFragment(IntentHelper.sAction));
        }

        checkWallpapers();
        IconRequestTask.start(this, AsyncTask.THREAD_POOL_EXECUTOR);
        IconsHelper.prepareIconsList(this);

        if (Preferences.get(this).isFirstRun() && mProperty.licenseChecker) {
            mLicenseHelper = new LicenseHelper(this);
            mLicenseHelper.run(mProperty.licenseKey, mProperty.salt, new LicenseCallbackHelper(this));
            return;
        }

        if (Preferences.get(this).isNewVersion())
            ChangelogFragment.showChangelog(mFragManager);

        if (mProperty.licenseChecker && !Preferences.get(this).isLicensed()) {
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
        LocaleHelper.setLocale(this);
        if (mIsMenuVisible) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        LocaleHelper.setLocale(newBase);
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        int action = IntentHelper.getAction(intent);
        if (action != IntentHelper.ACTION_DEFAULT)
            setFragment(getActionFragment(action));
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

        if (mLicenseHelper != null) {
            mLicenseHelper.destroy();
        }

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        CandyBarMainActivity.sMissedApps = null;
        CandyBarMainActivity.sHomeIcon = null;
        super.onDestroy();
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
            setFragment(getFragment(mPosition));
            return;
        }
        super.onBackPressed();
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
        if (requestCode == PermissionCode.STORAGE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
                return;
            }
            Toast.makeText(this, R.string.permission_storage_denied, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPiracyAppChecked(boolean isPiracyAppInstalled) {
        MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.navigation_view_request);
        if (menuItem != null) {
            menuItem.setVisible(getResources().getBoolean(
                    R.bool.enable_icon_request) || !isPiracyAppInstalled);
        }
    }

    @Override
    public void onRequestSelected(int count) {
        if (mFragmentTag.equals(TAG_REQUEST)) {
            String title = getResources().getString(R.string.navigation_view_request);
            if (count > 0) title += " ("+ count +")";
            mToolbarTitle.setText(title);
        }
    }

    @Override
    public void onBuyPremiumRequest() {
        if (Preferences.get(this).isPremiumRequest()) {
            RequestHelper.showPremiumRequestStillAvailable(this);
        } else {
            if (mBillingProcessor == null) return;

            if (mBillingProcessor.loadOwnedPurchasesFromGoogle()) {
                List<String> products = mBillingProcessor.listOwnedProducts();
                if (products != null) {
                    boolean isProductIdExist = false;
                    for (String product : products) {
                        for (String premiumRequestProductId : mProperty.premiumRequestProductsId) {
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
                    mProperty.licenseKey,
                    mProperty.premiumRequestProductsId,
                    mProperty.premiumRequestProductsCount);
        }
    }

    @Override
    public void onPremiumRequestBought() {
        if (mFragmentTag.equals(TAG_REQUEST)) {
            RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(TAG_REQUEST);
            if (fragment != null) fragment.refreshIconRequest();
        }
    }

    @Override
    public void onRequestBuilt(Intent intent, int type) {
        if (intent == null) {
            Toast.makeText(this, "Icon Request: Intent is null", Toast.LENGTH_LONG).show();
            return;
        }

        if (type == IntentChooserFragment.ICON_REQUEST) {
            if (RequestFragment.sSelectedRequests == null)
                return;

            if (Preferences.get(this).isPremiumRequest()) {
                if (mBillingProcessor == null) return;

                int count = Preferences.get(this).getPremiumRequestCount() - RequestFragment.sSelectedRequests.size();
                Preferences.get(this).setPremiumRequestCount(count);
                if (count == 0) {
                    if (mBillingProcessor.consumePurchase(Preferences
                            .get(this).getPremiumRequestProductId())) {
                        Preferences.get(this).setPremiumRequest(false);
                        Preferences.get(this).setPremiumRequestProductId("");
                    } else {
                        RequestHelper.showPremiumRequestConsumeFailed(this);
                        return;
                    }
                }
            }

            if (mFragmentTag.equals(TAG_REQUEST)) {
                RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(TAG_REQUEST);
                if (fragment != null) fragment.refreshIconRequest();
            }
        }

        try {
            startActivity(intent);
        } catch (IllegalArgumentException e) {
            startActivity(Intent.createChooser(intent,
                    getResources().getString(R.string.email_client)));
        }
        CandyBarApplication.sRequestProperty = null;
        CandyBarApplication.sZipPath = null;
    }

    @Override
    public void onInAppBillingInitialized(boolean success) {
        if (!success) mBillingProcessor = null;
    }

    @Override
    public void onRestorePurchases() {
        if (mBillingProcessor == null) return;

        if (mBillingProcessor.loadOwnedPurchasesFromGoogle()) {
            List<String> productsId = mBillingProcessor.listOwnedProducts();
            if (productsId != null) {
                SettingsFragment fragment = (SettingsFragment) mFragManager.findFragmentByTag(TAG_SETTINGS);
                if (fragment != null) fragment.restorePurchases(productsId,
                        mProperty.premiumRequestProductsId, mProperty.premiumRequestProductsCount);
            }
        }
    }

    @Override
    public void onInAppBillingSelected(int type, InAppBilling product) {
        Preferences.get(this).setInAppBillingType(type);
        if (type == InAppBillingHelper.PREMIUM_REQUEST) {
            Preferences.get(this).setPremiumRequestCount(product.getProductCount());
            Preferences.get(this).setPremiumRequestTotal(product.getProductCount());
        }

        if (mBillingProcessor != null) mBillingProcessor.purchase(this, product.getProductId());
    }

    @Override
    public void onInAppBillingConsume(int type, String productId) {
        if (mBillingProcessor == null) return;

        if (mBillingProcessor.consumePurchase(productId)) {
            if (type == InAppBillingHelper.DONATE) {
                new MaterialDialog.Builder(this)
                        .typeface(
                                TypefaceHelper.getMedium(this),
                                TypefaceHelper.getRegular(this))
                        .title(R.string.navigation_view_donate)
                        .content(R.string.donation_success)
                        .positiveText(R.string.close)
                        .show();
            }
        }
    }

    @Override
    public void onInAppBillingRequest() {
        if (mFragmentTag.equals(TAG_REQUEST)) {
            RequestFragment fragment = (RequestFragment) mFragManager.findFragmentByTag(TAG_REQUEST);
            if (fragment != null) fragment.prepareRequest(mBillingProcessor);
        }
    }

    @Override
    public void onWallpapersChecked(@Nullable Intent intent) {
        if (intent != null) {
            String packageName = intent.getStringExtra("packageName");
            LogUtil.d("Broadcast received from service with packageName: " +packageName);

            if (packageName == null)
                return;

            if (!packageName.equals(getPackageName())) {
                LogUtil.d("Received broadcast from different packageName, expected: " +getPackageName());
                return;
            }

            int size = intent.getIntExtra("size", 0);
            int offlineSize = Database.get(this).getWallpapersCount();
            Preferences.get(this).setAvailableWallpapersCount(size);

            if (size > offlineSize) {
                if (mFragmentTag.equals(TAG_HOME)) {
                    HomeFragment fragment = (HomeFragment) mFragManager.findFragmentByTag(TAG_HOME);
                    if (fragment != null) fragment.resetWallpapersCount();
                }

                int accent = ColorHelper.getAttributeColor(this, R.attr.colorAccent);
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
    public void onSearchExpanded(boolean expand) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mIsMenuVisible = !expand;

        if (expand) {
            int color = ColorHelper.getAttributeColor(this, R.attr.toolbar_icon);
            toolbar.setNavigationIcon(DrawableHelper.getTintedDrawable(
                    this, R.drawable.ic_toolbar_back, color));
            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        } else {
            SoftKeyboardHelper.closeKeyboard(this);
            ColorHelper.setStatusBarColor(this, Color.TRANSPARENT, true);
            if (CandyBarApplication.getConfiguration().getNavigationIcon() == CandyBarApplication.NavigationIcon.DEFAULT) {
                mDrawerToggle.setDrawerArrowDrawable(new DrawerArrowDrawable(this));
            } else {
                toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(this,
                        CandyBarApplication.getConfiguration().getNavigationIcon()));
            }

            toolbar.setNavigationOnClickListener(view ->
                    mDrawerLayout.openDrawer(GravityCompat.START));
        }

        mDrawerLayout.setDrawerLockMode(expand ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED :
                DrawerLayout.LOCK_MODE_UNLOCKED);
        supportInvalidateOptionsMenu();
    }

    public void showSupportDevelopmentDialog() {
        InAppBillingFragment.showInAppBillingDialog(mFragManager,
                mBillingProcessor,
                InAppBillingHelper.DONATE,
                mProperty.licenseKey,
                mProperty.donationProductsId,
                null);
    }

    private void initNavigationView(Toolbar toolbar) {
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
                selectPosition(mPosition);
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(ConfigurationHelper.getNavigationIcon(this,
                CandyBarApplication.getConfiguration().getNavigationIcon()));
        toolbar.setNavigationOnClickListener(view ->
                mDrawerLayout.openDrawer(GravityCompat.START));

        if (CandyBarApplication.getConfiguration().getNavigationIcon() == CandyBarApplication.NavigationIcon.DEFAULT) {
            DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this);
            drawerArrowDrawable.setColor(ColorHelper.getAttributeColor(this, R.attr.toolbar_icon));
            drawerArrowDrawable.setSpinEnabled(true);
            mDrawerToggle.setDrawerArrowDrawable(drawerArrowDrawable);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        NavigationViewHelper.initApply(mNavigationView);
        NavigationViewHelper.initIconRequest(mNavigationView);
        NavigationViewHelper.initWallpapers(mNavigationView);

        ColorStateList itemStateList = ContextCompat.getColorStateList(this,
                Preferences.get(this).isDarkTheme() ?
                        R.color.navigation_view_item_highlight_dark :
                        R.color.navigation_view_item_highlight);
        mNavigationView.setItemTextColor(itemStateList);
        mNavigationView.setItemIconTintList(itemStateList);
        Drawable background = ContextCompat.getDrawable(this,
                Preferences.get(this).isDarkTheme() ?
                        R.drawable.navigation_view_item_background_dark :
                        R.drawable.navigation_view_item_background);
        mNavigationView.setItemBackground(background);
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

            item.setChecked(true);
            mDrawerLayout.closeDrawers();
            return true;
        });

        NavigationViewHelper.hideScrollBar(mNavigationView);
    }

    private void initNavigationViewHeader() {
        if (CandyBarApplication.getConfiguration().getNavigationViewHeader() == CandyBarApplication.NavigationViewHeader.NONE) {
            mNavigationView.removeHeaderView(mNavigationView.getHeaderView(0));
            return;
        }

        String imageUrl = getResources().getString(R.string.navigation_view_header);
        String titleText = getResources().getString(R.string.navigation_view_header_title);
        View header = mNavigationView.getHeaderView(0);
        HeaderView image = (HeaderView) header.findViewById(R.id.header_image);
        LinearLayout container = (LinearLayout) header.findViewById(R.id.header_title_container);
        TextView title = (TextView )header.findViewById(R.id.header_title);
        TextView version = (TextView) header.findViewById(R.id.header_version);

        if (CandyBarApplication.getConfiguration().getNavigationViewHeader() == CandyBarApplication.NavigationViewHeader.MINI) {
            image.setRatio(16, 9);
        }

        if (titleText.length() == 0) {
            container.setVisibility(View.GONE);
        } else {
            title.setText(titleText);
            try {
                String versionText = "v" + getPackageManager()
                        .getPackageInfo(getPackageName(), 0).versionName;
                version.setText(versionText);
            } catch (Exception ignored) {}
        }

        if (ColorHelper.isValidColor(imageUrl)) {
            image.setBackgroundColor(Color.parseColor(imageUrl));
            return;
        }

        if (!URLUtil.isValidUrl(imageUrl)) {
            imageUrl = "drawable://" + DrawableHelper.getResourceId(this, imageUrl);
        }

        ImageLoader.getInstance().displayImage(imageUrl, new ImageViewAware(image),
                ImageConfig.getDefaultImageOptions(true), new ImageSize(720, 720), null, null);
    }

    private void initInAppBilling() {
        boolean donation = getResources().getBoolean(R.bool.enable_donation);
        if (donation || Preferences.get(this).isPremiumRequestEnabled()) {
            if (mBillingProcessor != null) return;

            if (BillingProcessor.isIabServiceAvailable(this)) {
                mBillingProcessor = new BillingProcessor(this.getApplicationContext(),
                        mProperty.licenseKey, new InAppBillingHelper(this));
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
        if (Preferences.get(this).isConnectedToNetwork()) {
            Intent intent = new Intent(this, CandyBarWallpapersService.class);
            startService(intent);
            return;
        }

        int size = Preferences.get(this).getAvailableWallpapersCount();
        if (size > 0) {
            onWallpapersChecked(new Intent()
                    .putExtra("size", size)
                    .putExtra("packageName", getPackageName()));
        }
    }

    private void clearBackStack() {
        if (mFragManager.getBackStackEntryCount() > 0) {
            mFragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            onSearchExpanded(false);
        }
    }

    public void selectPosition(int position) {
        if (position == 3) {
            if (!getResources().getBoolean(R.bool.enable_icon_request) &&
                    getResources().getBoolean(R.bool.enable_premium_request)) {
                if (!Preferences.get(this).isPremiumRequestEnabled())
                    return;

                if (!Preferences.get(this).isPremiumRequest()) {
                    mPosition = mLastPosition;
                    mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                    onBuyPremiumRequest();
                    return;
                }
            }
        }

        if (position == 4) {
            if (WallpaperHelper.getWallpaperType(this)
                    == WallpaperHelper.EXTERNAL_APP) {
                mPosition = mLastPosition;
                mNavigationView.getMenu().getItem(mPosition).setChecked(true);
                WallpaperHelper.launchExternalApp(CandyBarMainActivity.this);
                return;
            }
        }

        if (position != mLastPosition) {
            mLastPosition = mPosition = position;
            setFragment(getFragment(position));
        }
    }

    private void setFragment(Fragment fragment) {
        clearBackStack();

        FragmentTransaction ft = mFragManager.beginTransaction()
                .replace(R.id.container, fragment, mFragmentTag);
        try {
            ft.commit();
        } catch (Exception e) {
            ft.commitAllowingStateLoss();
        }

        Menu menu = mNavigationView.getMenu();
        menu.getItem(mPosition).setChecked(true);
        mToolbarTitle.setText(menu.getItem(mPosition).getTitle());
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
        } else if (position == 7) {
            mFragmentTag = TAG_ABOUT;
            return new AboutFragment();
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
                if (WallpaperHelper.getWallpaperType(this) == WallpaperHelper.CLOUD_WALLPAPERS) {
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
