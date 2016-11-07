package com.dm.material.dashboard.candybar.helpers;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dm.material.dashboard.candybar.R;

public class LauncherHelper {

    private static final int UNKNOWN = -1;
    private static final int ACTION = 1;
    private static final int ADW = 2;
    private static final int APEX = 3;
    private static final int ATOM = 4;
    private static final int AVIATE = 5;
    private static final int CMTHEME = 6;
    private static final int GO = 7;
    private static final int HOLO = 8;
    private static final int HOLOHD = 9;
    private static final int KK = 10;
    private static final int LGHOME = 11;
    private static final int LGHOME3 = 12;
    private static final int LUCID = 13;
    private static final int MINI = 14;
    private static final int NEXT = 15;
    private static final int NOVA = 16;
    private static final int S = 17;
    private static final int SMART = 18;
    private static final int SOLO = 19;
    private static final int ZENUI = 20;

    private static final String ACTION_APPLY_ICON_THEME = "com.teslacoilsw.launcher.APPLY_ICON_THEME";
    private static final String NOVA_PACKAGE = "com.teslacoilsw.launcher";
    private static final String EXTRA_ICON_THEME_PACKAGE = "com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE";
    private static final String EXTRA_ICON_THEME_TYPE = "com.teslacoilsw.launcher.extra.ICON_THEME_TYPE";
    private static final String APEX_ACTION_SET_THEME = "com.anddoes.launcher.SET_THEME";
    private static final String APEX_EXTRA_PACKAGE_NAME = "com.anddoes.launcher.THEME_PACKAGE_NAME";
    private static final String AVIATE_ACTION_SET_THEME = "com.tul.aviate.SET_THEME";
    private static final String AVIATE_EXTRA_PACKAGE_NAME = "THEME_PACKAGE";
    private static final String SOLO_ACTION_SET_THEME = "home.solo.launcher.free.APPLY_THEME";
    private static final String SOLO_EXTRA_THEME_NAME = "EXTRA_THEMENAME";
    private static final String SOLO_EXTRA_PACKAGE_NAME = "EXTRA_PACKAGENAME";
    private static final String KK_APPLY_ICON_THEME = "com.kk.launcher.APPLY_ICON_THEME";
    private static final String KK_EXTRA_PKG = "com.kk.launcher.theme.EXTRA_PKG";
    private static final String KK_EXTRA_NAME = "com.kk.launcher.theme.EXTRA_NAME";
    private static final String S_APPLY_ICON_THEME = "com.s.launcher.APPLY_ICON_THEME";
    private static final String S_EXTRA_PKG = "com.s.launcher.theme.EXTRA_PKG";
    private static final String S_EXTRA_NAME = "com.s.launcher.theme.EXTRA_NAME";
    private static final String LUCID_INTENT = "com.powerpoint45.action.APPLY_THEME";
    private static final String CMTHEME_INTENT = "org.cyanogenmod.theme.chooser.ChooserActivity";
    private static final String ASUS_APPLY_ICON_PACK = "com.asus.launcher.intent.action.APPLY_ICONPACK";

    private static int getLauncherId(String packageName) {
        if (packageName == null) return UNKNOWN;
        switch (packageName) {
            case "com.actionlauncher.playstore":
            case "com.chrislacy.actionlauncher.pro":
                return ACTION;
            case "org.adw.launcher":
            case "org.adwfreak.launcher":
                return ADW;
            case "com.anddoes.launcher":
            case "com.anddoes.launcher.pro":
                return APEX;
            case "com.dlto.atom.launcher":
                return ATOM;
            case "com.tul.aviate":
                return AVIATE;
            case "org.cyanogenmod.theme.chooser":
                return CMTHEME;
            case "com.gau.go.launcherex":
                return GO;
            case "com.mobint.hololauncher":
                return HOLO;
            case "com.mobint.hololauncher.hd":
                return HOLOHD;
            case "com.kk.launcher":
                return KK;
            case "com.lge.launcher2":
                return LGHOME;
            case "com.lge.launcher3":
                return LGHOME3;
            case "com.powerpoint45.launcher":
                return LUCID;
            case "com.jiubang.go.mini.launcher":
                return MINI;
            case "com.gtp.nextlauncher":
            case "com.gtp.nextlauncher.trial":
                return NEXT;
            case "com.teslacoilsw.launcher":
            case "com.teslacoilsw.launcher.prime":
                return NOVA;
            case "com.s.launcher":
                return S;
            case "ginlemon.flowerfree":
            case "ginlemon.flowerpro":
            case "ginlemon.flowerpro.special":
                return SMART;
            case "home.solo.launcher.free":
                return SOLO;
            case "com.asus.launcher":
                return ZENUI;
            default:
                return UNKNOWN;
        }
    }

    public static void apply(@NonNull Context context, String packageName, String launcherName) {
        applyLauncher(context, packageName, launcherName, getLauncherId(packageName));
    }

    private static void applyLauncher(Context context, String packageName, String launcherName, int id) {
        switch (id) {
            case ACTION:
                final Intent action = context.getPackageManager().getLaunchIntentForPackage(
                        packageName);
                try {
                    action.putExtra("apply_icon_pack", context.getPackageName());
                    action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(action);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case ADW:
                final Intent adw = new Intent("org.adw.launcher.SET_THEME");
                try {
                    adw.putExtra("org.adw.launcher.theme.NAME", context.getPackageName());
                    adw.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(adw);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case APEX:
                final Intent apex = new Intent(APEX_ACTION_SET_THEME);
                try {
                    apex.putExtra(APEX_EXTRA_PACKAGE_NAME, context.getPackageName());
                    apex.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(apex);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case ATOM:
                final Intent launcher_atom = new Intent(
                        "com.dlto.atom.launcher.intent.action.ACTION_VIEW_THEME_SETTINGS");
                try {
                    launcher_atom.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    launcher_atom.putExtra("packageName", context.getPackageName());
                    context.startActivity(launcher_atom);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case AVIATE:
                final Intent launcher_aviate = new Intent(AVIATE_ACTION_SET_THEME);
                try {
                    launcher_aviate.putExtra(AVIATE_EXTRA_PACKAGE_NAME, context.getPackageName());
                    launcher_aviate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launcher_aviate);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case CMTHEME:
                final Intent cmtheme = new Intent("android.intent.action.MAIN");
                try {
                    cmtheme.setComponent(new ComponentName(packageName, CMTHEME_INTENT));
                    cmtheme.putExtra("pkgName", context.getPackageName());
                    context.startActivity(cmtheme);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, R.string.apply_cmtheme_not_available,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case GO:
                final Intent goex = context.getPackageManager().getLaunchIntentForPackage(
                        "com.gau.go.launcherex"),
                        go = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction");
                try {
                    go.putExtra("type",1);
                    go.putExtra("pkgname", context.getPackageName());
                    goex.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(go);
                    context.startActivity(goex);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case HOLO:
                applyManual(context, launcherName, packageName, "com.mobint.hololauncher.SettingsActivity");
                break;
            case HOLOHD:
                applyManual(context, launcherName, packageName, "com.mobint.hololauncher.SettingsActivity");
                break;
            case KK:
                final Intent launcher_kk = new Intent(KK_APPLY_ICON_THEME);
                try {
                    launcher_kk.putExtra(KK_EXTRA_PKG, context.getPackageName());
                    launcher_kk.putExtra(KK_EXTRA_NAME, context.getResources().getString(
                            R.string.app_name));
                    launcher_kk.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launcher_kk);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case LGHOME:
                applyLgHome(context, packageName, launcherName, "com.lge.launcher2.homesettings.HomeSettingsPrefActivity");
                break;
            case LGHOME3:
                applyLgHome(context, packageName, launcherName, "com.lge.launcher3.homesettings.HomeSettingsPrefActivity");
                break;
            case LUCID:
                Intent lucid = new Intent(LUCID_INTENT, null);
                try {
                    lucid.putExtra("icontheme", context.getPackageName());
                    context.startActivity(lucid);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case MINI:
                applyManual(context, launcherName, packageName, "com.jiubang.go.mini.launcher.setting.MiniLauncherSettingActivity");
                break;
            case NEXT:
                Intent next = context.getPackageManager().getLaunchIntentForPackage("com.gtp.nextlauncher");
                if (next == null) {
                    next = context.getPackageManager().getLaunchIntentForPackage("com.gtp.nextlauncher.trial");
                }
                final Intent next2 = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction");
                try {
                    next2.putExtra("type", 1);
                    next2.putExtra("pkgname", context.getPackageName());
                    next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(next2);
                    context.startActivity(next);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case NOVA:
                final Intent nova = new Intent(ACTION_APPLY_ICON_THEME);
                try {
                    nova.setPackage(NOVA_PACKAGE);
                    nova.putExtra(EXTRA_ICON_THEME_TYPE, "GO");
                    nova.putExtra(EXTRA_ICON_THEME_PACKAGE, context.getPackageName());
                    nova.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(nova);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case S:
                final Intent launcher_s = new Intent(S_APPLY_ICON_THEME);
                try {
                    launcher_s.putExtra(S_EXTRA_PKG, context.getPackageName());
                    launcher_s.putExtra(S_EXTRA_NAME, context.getResources().getString(
                            R.string.app_name));
                    launcher_s.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launcher_s);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case SMART:
                final Intent smart = new Intent("ginlemon.smartlauncher.setGSLTHEME");
                try {
                    smart.putExtra("package", context.getPackageName());
                    smart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(smart);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case SOLO:
                final Intent launcher_solo = context.getPackageManager().getLaunchIntentForPackage(
                        "home.solo.launcher.free"),
                        launcher_solo_act = new Intent(SOLO_ACTION_SET_THEME);
                try {
                    launcher_solo_act.putExtra(SOLO_EXTRA_THEME_NAME, context.getResources().getString(
                            R.string.app_name));
                    launcher_solo_act.putExtra(SOLO_EXTRA_PACKAGE_NAME, context.getPackageName());
                    launcher_solo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.sendBroadcast(launcher_solo_act);
                    context.startActivity(launcher_solo);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
            case ZENUI:
                final Intent asus = new Intent("com.asus.launcher");
                try {
                    asus.setAction(ASUS_APPLY_ICON_PACK);
                    asus.addCategory(Intent.CATEGORY_DEFAULT);
                    asus.putExtra("com.asus.launcher.iconpack.PACKAGE_NAME", context.getPackageName());
                    asus.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(asus);
                    ((AppCompatActivity) context).finish();
                } catch (ActivityNotFoundException | NullPointerException e) {
                    openGooglePlay(context, packageName, launcherName);
                }
                break;
        }
    }

    private static void applyManual(Context context, String launcherName, String packageName, String activity) {
        new MaterialDialog.Builder(context)
                .title(launcherName)
                .content(launcherName + " " +
                        context.getResources().getString(R.string.manual_apply) + " " +
                        context.getResources().getString(R.string.app_name) + " " +
                        context.getResources().getString(R.string.manual_apply2))
                .positiveText(context.getResources().getString(R.string.ok))
                .onPositive((dialog, which) -> {
                    try{
                        final Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setComponent(new ComponentName(packageName,
                                activity));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        ((AppCompatActivity) context).finish();
                    } catch (ActivityNotFoundException | NullPointerException e) {
                        openGooglePlay(context, packageName, launcherName);
                    } catch (SecurityException e) {
                        Toast.makeText(context, context.getResources().getString(R.string.unable_launch)
                                        + " " +launcherName+ " " +
                                        context.getResources().getString(R.string.unable_launch2),
                                Toast.LENGTH_LONG).show();
                    }
                })
                .negativeText(context.getResources().getString(R.string.cancel))
                .show();
    }

    private static void applyLgHome(Context context, String packageName, String launcherName, String activity) {
        new MaterialDialog.Builder(context)
                .title(launcherName)
                .content(launcherName + " " +
                        context.getResources().getString(R.string.manual_apply) +" "+
                        context.getResources().getString(R.string.app_name) +" "+
                        context.getResources().getString(R.string.manual_apply2) +"\n\n"+
                        context.getResources().getString(R.string.manual_apply_lg_home))
                .positiveText(context.getResources().getString(R.string.ok))
                .onPositive((dialog, which) -> {
                    try{
                        final Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setComponent(new ComponentName(packageName,
                                activity));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        ((AppCompatActivity) context).finish();
                    } catch (ActivityNotFoundException | NullPointerException e) {
                        Log.d("TEST", Log.getStackTraceString(e));
                        Toast.makeText(context, R.string.manual_apply_lg_home_not_available,
                                Toast.LENGTH_LONG).show();
                    } catch (SecurityException e) {
                        Toast.makeText(context, context.getResources().getString(R.string.unable_launch)
                                        + " " +launcherName+ " " +
                                        context.getResources().getString(R.string.unable_launch2),
                                Toast.LENGTH_LONG).show();
                    }
                })
                .negativeText(context.getResources().getString(R.string.cancel))
                .show();
    }

    private static void openGooglePlay(Context context, String packageName, String launcherName) {
        new MaterialDialog.Builder(context)
                .title(launcherName)
                .content(launcherName + " " + context.getResources().getString(R.string.launcher_not_installed))
                .positiveText(context.getString(R.string.install))
                .onPositive((dialog, which) -> {
                    try {
                        Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "https://play.google.com/store/apps/details?id=" + packageName));
                        context.startActivity(store);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, context.getResources().getString(
                                R.string.no_browser), Toast.LENGTH_LONG).show();
                    }
                })
                .negativeText(R.string.cancel)
                .show();
    }

    public static String getDefaultLauncher(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        return packageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
    }

}
