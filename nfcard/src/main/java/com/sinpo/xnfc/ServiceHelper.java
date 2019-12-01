package com.sinpo.xnfc;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台service组件助手
 *
 * @author Kelly
 * @version 1.0.0
 * @filename ServiceHelper.java
 * @time 2019/3/30 13:40
 * @copyright(C) 2019 song
 */
public class ServiceHelper {
    /**
     * 判断某个service是否正在运行
     *
     * @param context
     * @param runService
     *            要验证的service组件的类名
     * @return
     */
    public static boolean isServiceRunning(Context context,
                                           Class<? extends Service> runService) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) am
                .getRunningServices(1024);
        for (int i = 0; i < runningService.size(); ++i) {
            if (runService.getName().equals(
                    runningService.get(i).service.getClassName().toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回app运行状态
     *
     * @param context
     *            一个context
     * @param packageName
     *            要判断应用的包名
     * @return int 1:前台 2:后台 0:不存在
     */
    public static int isAppAlive(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> listInfos = activityManager
                .getRunningTasks(20);
        // 判断程序是否在栈顶
        if (listInfos.get(0).topActivity.getPackageName().equals(packageName)) {
            return 1;
        } else {
            // 判断程序是否在栈里
            for (ActivityManager.RunningTaskInfo info : listInfos) {
                if (info.topActivity.getPackageName().equals(packageName)) {
                    return 2;
                }
            }
            return 0;// 栈里找不到，返回3
        }
    }




    /**
     * 判断进程是否运行
     *
     * @param context
     * @param proessName
     * @return
     */
    public static boolean isProessRunning(Context context, String proessName) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : lists) {
            if (info.processName.equals(proessName)) {
                isRunning = true;
            }
        }
        return isRunning;
    }

    /**
     * 判断某一个类是否存在任务栈里面
     *
     * @return
     */
    public static boolean isExsitMianActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        ComponentName cmpName = intent.resolveActivity(context
                .getPackageManager());
        boolean flag = false;
        if (cmpName != null) { // 说明系统中存在这个activity
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                if (taskInfo.baseActivity.equals(cmpName)) { // 说明它已经启动了
                    flag = true;
                    break; // 跳出循环，优化效率
                }
            }
        }
        return flag;
    }

}
