package org.x2ools.xappsearchlib;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Process;

/**
 * @author zhoubinjia
 * @date 2017/8/21
 */
public class PermissionHelper {

    public static boolean checkOpsPermission(Context context, String permission) {
        try {
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            String opsName = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                opsName = AppOpsManager.permissionToOp(permission);
            }
            if (opsName == null) {
                return true;
            }
            int opsMode = appOpsManager.checkOpNoThrow(opsName, Process.myUid(), context.getPackageName());
            return opsMode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {
            return true;
        }
    }
}
