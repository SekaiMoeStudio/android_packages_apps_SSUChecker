package me.dabao1955.ssuchecker;

import android.app.Service;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class PMHookService extends Service {
    private static final String TAG = "PMHookService";
    private final IPackageManager mPM;
    private final PackageManager mPackageManager;
    private final Handler mHandler;
    private final PMController mController;

    public PMHookService() {
        mPM = IPackageManager.Stub.asInterface(
                ServiceManager.getService("package"));
        mPackageManager = getSystemService(PackageManager.class);
        mHandler = new Handler();
        mController = new PMController(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerPackageInstallObserver();
    }

    private void registerPackageInstallObserver() {
        try {
            mPM.registerPackageInstallObserver(new IPackageInstallObserver.Stub() {
                @Override
                public void onPackageInstalled(String packageName, int returnCode) {
                    if (mController.shouldBlockPackage(packageName)) {
                        blockInstallation(packageName);
                    }
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register observer", e);
        }
    }

    private void blockInstallation(String packageName) {
        try {
            mPM.deletePackage(packageName, null, 0, 0);
            Intent intent = new Intent(ACTION_PACKAGE_BLOCKED);
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
            sendBroadcast(intent);
            Log.i(TAG, "Blocked package installation: " + packageName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to block package", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
