package net.oschina.gitapp.common;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kymjs.rxvolley.client.HttpCallback;

import net.oschina.gitapp.R;
import net.oschina.gitapp.api.GitOSCApi;
import net.oschina.gitapp.bean.Update;
import net.oschina.gitapp.dialog.LightProgressDialog;
import net.oschina.gitapp.utils.IO;
import net.oschina.gitapp.utils.JsonUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * 应用程序更新工具包
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.1
 * @created 2012-6-29
 */
@SuppressLint("HandlerLeak")
public class UpdateManager {

    private static final int DOWN_NOSDCARD = 0;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;

    private static final int DIALOG_TYPE_LATEST = 0;
    private static final int DIALOG_TYPE_FAIL = 1;

    private static UpdateManager updateManager;

    private Context mContext;
    //通知对话框
    private Dialog noticeDialog;
    //下载对话框
    private AlertDialog downloadDialog;
    //'已经是最新' 或者 '无法获取最新版本' 的对话框
    private Dialog latestOrFailDialog;
    //进度条
    private ProgressBar mProgress;
    //显示下载数值
    private TextView mProgressText;
    //查询动画
    private ProgressDialog mProDialog;
    //进度值
    private int progress;
    //下载线程
    private Thread downLoadThread;
    //终止标记
    private boolean interceptFlag;
    //提示语
    private String updateMsg = "";
    //返回的安装包url
    private String apkUrl = "";
    //下载包保存路径
    private String savePath = "";
    //apk保存完整路径
    private String apkFilePath = "";
    //临时下载文件路径
    private String tmpFilePath = "";
    //下载文件大小
    private String apkFileSize;
    //已下载文件大小
    private String tmpFileSize;

    private int curVersionCode;
    private Update mUpdate;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    mProgressText.setText(tmpFileSize + "/" + apkFileSize);
                    break;
                case DOWN_OVER:
                    downloadDialog.dismiss();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        installAndroidQApk();
                    } else {
                        installApk();
                    }
                    break;
                case DOWN_NOSDCARD:
                    downloadDialog.dismiss();
                    Toast.makeText(mContext, "无法下载安装文件，请检查SD卡是否挂载", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        ;
    };

    public static UpdateManager getUpdateManager() {
        if (updateManager == null) {
            updateManager = new UpdateManager();
        }
        updateManager.interceptFlag = false;
        return updateManager;
    }

    /**
     * 显示'已经是最新'或者'无法获取版本信息'对话框
     */
    private void showLatestOrFailDialog(int dialogType) {
        if (latestOrFailDialog != null) {
            //关闭并释放之前的对话框
            latestOrFailDialog.dismiss();
            latestOrFailDialog = null;
        }
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("系统提示");
        if (dialogType == DIALOG_TYPE_LATEST) {
            builder.setMessage("您当前已经是最新版本");
        } else if (dialogType == DIALOG_TYPE_FAIL) {
            builder.setMessage("无法获取版本更新信息");
        }
        builder.setPositiveButton("确定", null);
        latestOrFailDialog = builder.create();
        latestOrFailDialog.show();
    }

    /**
     * 获取当前客户端版本信息
     */
    private void getCurrentVersion() {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext
                    .getPackageName(), 0);
            curVersionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }

    public interface OnPermissionCallback {
        void onPermissionCallback();
    }

    /**
     * 检查App更新
     *
     * @param context
     * @param isShowMsg 是否显示提示消息
     */
    public void checkAppUpdate(Context context, final OnPermissionCallback callback, final boolean isShowMsg) {
        this.mContext = context;
        getCurrentVersion();
        final AlertDialog check = LightProgressDialog.create(context, "正在检测，请稍候...");
        check.setCanceledOnTouchOutside(false);
        GitOSCApi.getUpdateInfo(new HttpCallback() {
            @Override
            public void onSuccess(Map<String, String> headers, byte[] t) {
                super.onSuccess(headers, t);
                Update update = JsonUtils.toBean(Update.class, t);
                if (update != null) {
                    mUpdate = update;
                    if (curVersionCode < mUpdate.getNum_version()) {
                        apkUrl = mUpdate.getDownload_url();
                        updateMsg = mUpdate.getDescription();
                        showNoticeDialog(callback);
                    } else {
                        if (isShowMsg) {
                            showLatestOrFailDialog(DIALOG_TYPE_LATEST);
                        }
                    }
                }
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                UIHelper.toastMessage(mContext, "网络异常");
            }

            @Override
            public void onPreStart() {
                super.onPreStart();
                if (isShowMsg) {
                    check.show();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                check.dismiss();
            }
        });
    }

    /**
     * 显示版本更新通知对话框
     */
    private void showNoticeDialog(final OnPermissionCallback callback) {
        AlertDialog.Builder builder = new Builder(mContext, R.style.App_Theme_Dialog_Alert);
        builder.setTitle("软件版本更新");
        builder.setMessage(updateMsg);
        builder.setPositiveButton("立即更新", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                callback.onPermissionCallback();
                //showDownloadDialog();
            }
        });
        builder.setNegativeButton("以后再说", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();
    }

    public void showNotPermissionDialog() {
        AlertDialog.Builder builder = new Builder(mContext, R.style.App_Theme_Dialog_Alert);
        builder.setTitle("温馨提示");
        builder.setMessage("需要开启开源中国对您手机的存储权限才能下载安装，是否现在开启");
        builder.setPositiveButton("去开启", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mContext.startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
            }
        });
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 显示下载对话框
     */
    public void showDownloadDialog() {
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("正在下载新版本");

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.update_progress, null);
        mProgress = v.findViewById(R.id.update_progress);
        mProgressText = v.findViewById(R.id.update_progress_text);

        builder.setView(v);
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                interceptFlag = true;
            }
        });
        downloadDialog = builder.create();
        downloadDialog.setCanceledOnTouchOutside(false);
        downloadDialog.show();

        downloadApk();
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                downloadAndroidQ();
            } else {
                downloadCustom();
            }
        }
    };


    private String mFileName;
    private String mFilePath;

    private void downloadAndroidQ() {
        new Thread() {
            @RequiresApi(api = 29)
            @Override
            public void run() {
                HttpURLConnection httpConnection = null;
                InputStream is = null;
                OutputStream fos = null;
                try {

                    mFileName = System.currentTimeMillis() + mUpdate.getVersion() + ".apk";
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, mFileName);
                    values.put(MediaStore.Downloads.MIME_TYPE, "application/vnd.android.package-archive");
                    values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/");

                    Uri external = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    ContentResolver resolver = mContext.getContentResolver();

                    Uri insertUri = resolver.insert(external, values);
                    if (insertUri != null) {
                        mFilePath = insertUri.toString();
                    }
                    URL url = new URL(apkUrl);
                    httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.connect();
                    int length = httpConnection.getContentLength();

                    is = httpConnection.getInputStream();
                    fos = resolver.openOutputStream(insertUri);


                    //显示文件大小格式：2个小数点显示
                    DecimalFormat df = new DecimalFormat("0.00");
                    //进度条下面显示的总文件大小
                    apkFileSize = df.format((float) length / 1024 / 1024) + "MB";

                    int count = 0;
                    byte buf[] = new byte[1024];

                    do {
                        int numread = is.read(buf);
                        count += numread;
                        //进度条下面显示的当前下载文件大小
                        tmpFileSize = df.format((float) count / 1024 / 1024) + "MB";
                        //当前进度值
                        progress = (int) (((float) count / length) * 100);
                        //更新进度
                        mHandler.sendEmptyMessage(DOWN_UPDATE);
                        if (numread <= 0) {
                            //下载完成 - 将临时下载文件转成APK文件
                            mHandler.sendEmptyMessage(DOWN_OVER);
                            break;
                        }
                        fos.write(buf, 0, numread);
                    } while (!interceptFlag);//点击取消就停止下载

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (httpConnection != null) {
                        httpConnection.disconnect();
                    }
                    IO.close(is, fos);
                }
            }


        }.start();
    }

    private void downloadCustom() {
        try {
            String apkName = "OSChinaApp_" + mUpdate.getVersion() + ".apk";
            String tmpApk = "OSChinaApp_" + mUpdate.getVersion() + ".tmp";
            //判断是否挂载了SD卡
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                savePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/OSChina/Update/";
                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                apkFilePath = savePath + apkName;
                tmpFilePath = savePath + tmpApk;
            }

            //没有挂载SD卡，无法下载文件
            if (apkFilePath == null || apkFilePath == "") {
                mHandler.sendEmptyMessage(DOWN_NOSDCARD);
                return;
            }

            File ApkFile = new File(apkFilePath);

            //是否已下载更新文件
            if (ApkFile.exists()) {
                downloadDialog.dismiss();
                installApk();
                return;
            }

            //输出临时下载文件
            File tmpFile = new File(tmpFilePath);
            FileOutputStream fos = new FileOutputStream(tmpFile);

            URL url = new URL(apkUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int length = conn.getContentLength();
            InputStream is = conn.getInputStream();

            //显示文件大小格式：2个小数点显示
            DecimalFormat df = new DecimalFormat("0.00");
            //进度条下面显示的总文件大小
            apkFileSize = df.format((float) length / 1024 / 1024) + "MB";

            int count = 0;
            byte buf[] = new byte[1024];

            do {
                int numread = is.read(buf);
                count += numread;
                //进度条下面显示的当前下载文件大小
                tmpFileSize = df.format((float) count / 1024 / 1024) + "MB";
                //当前进度值
                progress = (int) (((float) count / length) * 100);
                //更新进度
                mHandler.sendEmptyMessage(DOWN_UPDATE);
                if (numread <= 0) {
                    //下载完成 - 将临时下载文件转成APK文件
                    if (tmpFile.renameTo(ApkFile)) {
                        //通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                    }
                    break;
                }
                fos.write(buf, 0, numread);
            } while (!interceptFlag);//点击取消就停止下载

            fos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载apk
     */
    private void downloadApk() {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    private void installAndroidQApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(mFilePath), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mContext.startActivity(intent);
    }

    /**
     * 安装apk
     */
    private void installApk() {
        File file = new File(apkFilePath);
        if (!file.exists())
            return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(mContext, "net.oschina.gitapp.provider", file);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        mContext.startActivity(intent);
    }
}
