package com.example.pda.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.pda.BuildConfig;
import com.example.pda.LoginActivity;
import com.example.pda.R;
import com.example.pda.bean.UpdateBean;
import com.example.pda.bean.globalbean.MyOkHttpClient;
import com.example.pda.bean.globalbean.MyToast;
import com.example.pda.config.Version;
import com.google.gson.Gson;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;

public class ApkUpdateUtils {

    private Context mContext;

    //提示语
    private String updateMsg = "有最新的软件包哦，快下载吧~";

    //返回的安装包url
    private String apkUrl = "";

    private String requestUrl = "http://175.24.14.165";

    private String port = "8080";

    private Dialog noticeDialog;

    private Dialog downloadDialog;
    /* 下载包安装路径 */
    private static final String savePath = "/sdcard/pda";
//    private static final String savePath = "/data/com.example.pda";

    private String saveFileName = "";

    /* 进度条与通知ui刷新的handler和msg常量 */
    private ProgressBar mProgress;

    private ZLoadingDialog dialog;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private static final int CHECK_VERSION = 3;

    private int progress;

    private Thread downLoadThread;

    private boolean interceptFlag = false;

    private OkHttpClient okHttpClient = MyOkHttpClient.getOkHttpClient();

    private Toast toast = MyToast.getToast();

    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
                case CHECK_VERSION:
                    resolveRequest ((String)msg.obj);
                    break;
                default:
                    break;
            }
        };
    };

    public ApkUpdateUtils(Context context) {
        this.mContext = context;
    }

    //外部接口让主Activity调用
    public void checkUpdateInfo(){
        showNoticeDialog();
    }

    private void resolveRequest(String returnMessage) {
        UpdateBean updateBean = new Gson().fromJson(returnMessage, UpdateBean.class);
        if ("true".equals(updateBean.getUpdate())) {
            apkUrl = updateBean.getUrl();
            showNoticeDialog();
        }
        SharedPreferences setinfo = mContext.getSharedPreferences("GlobalData", Context.MODE_PRIVATE);
        setinfo.edit().putString("Version", updateBean.getUpdate()).commit();

    }

    public void checkVersion() {
        final Request request = new Request.Builder().url(requestUrl + ":" + port + "/pda/" + Version.getVersion()).get().build();
        dialog = new ZLoadingDialog(mContext);
        dialog.setLoadingBuilder(Z_TYPE.DOUBLE_CIRCLE)//设置类型
                .setLoadingColor(Color.BLACK)//颜色
                .setHintText("检查更新中")
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        mHandler.obtainMessage(CHECK_VERSION , response.body().string()).sendToTarget();
                    } else {

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        toast.setText("请求超时！");
                        toast.show();
                    }
                    if (e instanceof ConnectException) {
                        toast.setText("和服务器连接异常！");
                        toast.show();

                    }
                } finally {
                    dialog.cancel();
                }

            }
        }).start();
    }

    private void showNoticeDialog(){
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("下载", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();
    }

    private void showDownloadDialog(){
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress, null);
        mProgress = (ProgressBar)v.findViewById(R.id.progress);

        builder.setView(v);
        downloadDialog = builder.create();
        downloadDialog.show();

        downloadApk();
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();
//                File file = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(Environment.getExternalStorageDirectory(), "pda");
                if (!file.exists()){
                    file.mkdir();
                }
                String apkFile = apkUrl.substring(apkUrl.lastIndexOf("/") + 1, apkUrl.length());
                File ApkFile = new File(file, apkFile);
                if (!ApkFile.exists()) {
                    ApkFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[1024];

                do{
                    int numread = is.read(buf);
                    count += numread;
                    progress =(int)(((float)count / length) * 100);
                    //更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if(numread <= 0){
                        //下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf,0,numread);
                }while(!interceptFlag);//点击取消就停止下载.

                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }

        }
    };

    /**
     * 下载apk
     * @param url
     */

    private void downloadApk(){
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }
    /**
     * 安装apk
     * @param url
     */
    private void installApk(){
        File externalFilesDir = new File(Environment.getExternalStorageDirectory(),"pda") ;
        File apkFile = new File(externalFilesDir, apkUrl.substring(apkUrl.lastIndexOf("/") + 1, apkUrl.length()));
        Uri apkUri = FileProvider.getUriForFile(mContext,
                BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        mContext.startActivity(installIntent);
    }
}