package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Build;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
// CM FIX https://github.com/CyanogenMod/android_frameworks_base/commit/0ba4c710c6f8805175bde2dbd85d7d8788a15ee0
class ActiveServiceForegroundNotificationCancellationSubModule extends AndroidSubModule {

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.N;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookCancelForegroundNotificationLocked(lpparam);
    }

    private void hookCancelForegroundNotificationLocked(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookCancelForegroundNotificationLocked...");
        try {
            Class ams = XposedHelpers.findClass("com.android.server.am.ActiveServices",
                    lpparam.classLoader);
            // There is a mistake on N source code, maybe the developer is a Chinese?
            // It's funny.
            Set unHooks = XposedBridge.hookAllMethods(ams,
                    OSUtil.isOOrAbove()
                            ? "cancelForegroundNotificationLocked"
                            : "cancelForegroudNotificationLocked",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param)
                                throws Throwable {
                            super.beforeHookedMethod(param);
                            boolean opt = XAshmanManager.get().isServiceAvailable() && XAshmanManager.get()
                                    .isOptFeatureEnabled(XAshmanManager.OPT.FOREGROUND_NOTIFICATION.name());
                            if (opt) {
                                // Always allow to cancel, do not check active services.
                                Object serviceRecordObject = param.args[0];
                                if (serviceRecordObject != null) {
                                    try {
                                        XposedHelpers.callMethod(serviceRecordObject, "cancelNotification");
                                        param.setResult(null);
                                        if (BuildConfig.DEBUG) {
                                            String pkgName = (String) XposedHelpers.getObjectField(serviceRecordObject, "packageName");
                                            XposedLog.verbose("cancelNotification hooked for: " + pkgName);
                                        }
                                    } catch (Throwable e) {
                                        XposedLog.wtf("Fail invoke cancelNotification: " + Log.getStackTraceString(e));
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookCancelForegroundNotificationLocked OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookCancelForegroundNotificationLocked: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
