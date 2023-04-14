package com.pax.pay.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.pax.pay.utils.ToastUtils;
import com.pax.up.bjb.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashHandler implements UncaughtExceptionHandler {
    public static final String TAG = "CrashHandler";

    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/CrashLog/";
    private static final String FILE_NAME = "crash";
    private static final String FILE_NAME_SUFFIX = ".trace";

    private static CrashHandler instance;
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public void init(Context cxt) {
        this.mContext = cxt;
        // Returns the default exception handler that's executed when uncaught exception terminates a thread.
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // 如果用户没有处理则让系统默认的异常处理器来处理
        if (!handleExceptions(ex) && mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            killApp();
        }

    }

    private void killApp() {
        SystemClock.sleep(2000); //Waits a given number of milliseconds (of uptimeMillis) before returning
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private boolean handleExceptions(Throwable ex) {
        if (ex == null) {
            return false;
        }
        try {
            promptMessageForUser();
            dumpExceptionToSDCard(ex);
            uploadExceptionToServer();
        } catch (IOException e) {
            Log.e(TAG, "", e);
        }

        return true;
    }

    private void promptMessageForUser() {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                ToastUtils.showMessage(mContext, mContext.getString(R.string.app_crash_exit));
                Looper.loop();
            }
        }.start();
    }

    @SuppressLint("SimpleDateFormat")
    private void dumpExceptionToSDCard(Throwable ex) throws IOException {
        // 如果SD卡不存在或无法使用，则无法把异常信息写入SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }

        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        File file = new File(PATH + FILE_NAME + FILE_NAME_SUFFIX);

        try (
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))
        ) {
            pw.println(time);
            dumpPhoneInfo(pw);
            pw.println();
            ex.printStackTrace(pw);
            pw.println();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);

        // android版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);

        // 手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);

        // 手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);

        // cpu架构
        pw.print("CPU ABI: ");
        pw.println(Build.CPU_ABI);
    }

    private void uploadExceptionToServer() {

    }
}
