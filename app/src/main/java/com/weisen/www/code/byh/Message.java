package com.weisen.www.code.byh;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class Message {
    public String getVersionName(Context context){
        try{
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            String version = packInfo.versionName;
            return version;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
    public String getSystemVersionName(){//系统版本
        return android.os.Build.MODEL;
    }
    public String getAndroidVersionName(){//安卓版本
        return android.os.Build.VERSION.RELEASE;
    }
}