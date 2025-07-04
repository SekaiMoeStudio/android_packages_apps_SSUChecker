package org.lineageos.pmhook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PMController {
    private static final String TAG = "PMController";
    private static final String PREFS_NAME = "org.lineageos.pmhook_preferences";
    private static final String PROP_BLOCKED_PACKAGES = "ro.pmhook.blocked_packages";
    private static final String KEY_BLOCKED_PACKAGES = "blocked_packages";
    private static final String KEY_ENABLED = "hook_enabled";
    private static final String KEY_LAST_UPDATE = "last_update";
    private static final boolean DEFAULT_ENABLED = true;
    private static final int MAX_BLOCKED_PACKAGES = 1000;
    private final Context mContext;
    private final SharedPreferences mPrefs;
    private final Set<String> mSystemBlockedPackages;
    public PMController(Context context) {
        mContext = context;
        mPrefs = context.createDeviceProtectedStorageContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String blockedPackagesStr = SystemProperties.get(PROP_BLOCKED_PACKAGES, "");
        mSystemBlockedPackages = new HashSet<>(
            Arrays.asList(TextUtils.split(blockedPackagesStr, ":")));
        if (!mPrefs.contains(KEY_ENABLED)) {
            mPrefs.edit().putBoolean(KEY_ENABLED, DEFAULT_ENABLED).apply();
        }
        Log.i(TAG, "PMController initialized with " + mSystemBlockedPackages.size() +
              " system blocked packages");
    }

    public boolean shouldBlockPackage(String packageName) {
        if (!mPrefs.getBoolean(KEY_ENABLED, DEFAULT_ENABLED)) {
            return mSystemBlockedPackages.contains(packageName);
        }
        if (mSystemBlockedPackages.contains(packageName)) {
            Log.i(TAG, "Package " + packageName + " is blocked by system configuration");
            return true;
        }
        Set<String> runtimeBlockedPackages = getBlockedPackages();
        boolean shouldBlock = runtimeBlockedPackages.contains(packageName);

        if (shouldBlock) {
            Log.i(TAG, "Package " + packageName + " is blocked by runtime configuration");
        }

        return shouldBlock;
    }

    private Set<String> getBlockedPackages() {
        return mPrefs.getStringSet(KEY_BLOCKED_PACKAGES, new HashSet<>());
    }

    public boolean addBlockedPackage(String packageName) {
        if (mSystemBlockedPackages.contains(packageName)) {
            Log.w(TAG, "Package " + packageName + " is already blocked by system configuration");
            return false;
        }
        Set<String> blockedPackages = new HashSet<>(getBlockedPackages());
        if (blockedPackages.size() >= MAX_BLOCKED_PACKAGES) {
            Log.w(TAG, "Maximum number of blocked packages reached");
            return false;
        }
        if (blockedPackages.add(packageName)) {
            boolean saved = mPrefs.edit()
                .putStringSet(KEY_BLOCKED_PACKAGES, blockedPackages)
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                .commit();
            if (saved) {
                Log.i(TAG, "Added " + packageName + " to blocked packages");
                return true;
            }
        }
        return false;
    }
    public boolean removeBlockedPackage(String packageName) {
        if (mSystemBlockedPackages.contains(packageName)) {
            Log.w(TAG, "Cannot remove system blocked package: " + packageName);
            return false;
        }
        Set<String> blockedPackages = new HashSet<>(getBlockedPackages());
        if (blockedPackages.remove(packageName)) {
            boolean saved = mPrefs.edit()
                .putStringSet(KEY_BLOCKED_PACKAGES, blockedPackages)
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                .commit();
            if (saved) {
                Log.i(TAG, "Removed " + packageName + " from blocked packages");
                return true;
            }
        }
        return false;
    }

    public void setEnabled(boolean enabled) {
        mPrefs.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply();
        Log.i(TAG, "SSUChecker " + (enabled ? "enabled" : "disabled") + 
              " (system blocks still effective)");
    }

    public boolean isEnabled() {
        return mPrefs.getBoolean(KEY_ENABLED, DEFAULT_ENABLED);
    }

    public Set<String> getAllBlockedPackages() {
        Set<String> allBlocked = new HashSet<>(mSystemBlockedPackages);
        allBlocked.addAll(getBlockedPackages());
        return allBlocked;
    }

    public boolean isSystemBlocked(String packageName) {
        return mSystemBlockedPackages.contains(packageName);
    }
}
