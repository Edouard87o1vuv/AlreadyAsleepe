package net.oschina.gitapp;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.text.TextUtils;

import com.kymjs.okhttp.OkHttpStack;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.http.RequestQueue;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.squareup.okhttp.OkHttpClient;

import net.oschina.gitapp.api.AsyncHttpHelp;
import net.oschina.gitapp.bean.Follow;
import net.oschina.gitapp.bean.User;
import net.oschina.gitapp.common.BroadcastController;
import net.oschina.gitapp.common.CyptoUtils;
import net.oschina.gitapp.common.MethodsCompat;
import net.oschina.gitapp.common.StringUtils;
import net.oschina.gitapp.share.SinaShare;
import net.oschina.gitapp.utils.CodeFileUtils;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

import static net.oschina.gitapp.common.Contanst.ACCOUNT_EMAIL;
import static net.oschina.gitapp.common.Contanst.ACCOUNT_PWD;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_BIO;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_BLOG;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_CAN_CREATE_GROUP;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_CAN_CREATE_PROJECT;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_CAN_CREATE_TEAM;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_CREATED_AT;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_EMAIL;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_IS_ADMIN;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_NAME;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_NEWPORTRAIT;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_PORTRAIT;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_PRIVATE_TOKEN;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_STATE;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_THEME_ID;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_UID;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_USERNAME;
import static net.oschina.gitapp.common.Contanst.PROP_KEY_WEIBO;
import static net.oschina.gitapp.common.Contanst.ROP_KEY_FOLLOWERS;
import static net.oschina.gitapp.common.Contanst.ROP_KEY_FOLLOWING;
import static net.oschina.gitapp.common.Contanst.ROP_KEY_STARRED;
import static net.oschina.gitapp.common.Contanst.ROP_KEY_WATCHED;

/**
 * ????????????????????????????????????????????????????????????????????????????????????
 *
 * @author ?????? (http://my.oschina.net/LittleDY)
 * @version 1.0
 * @created 2014-04-22
 */
public class AppContext extends Application {

    public static final int PAGE_SIZE = 20;// ??????????????????

    private boolean login = false; // ????????????
    private int loginUid = 0; // ???????????????id

    private static AppContext appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        CodeFileUtils.init();
        WbSdk.install(this,
                new AuthInfo(this, SinaShare.APP_KEY,
                        "http://www.sina.com", SinaShare.APP_SECRET));
        // ??????App?????????????????????
        File cacheFolder = getCacheDir();
        RxVolley.setRequestQueue(RequestQueue.newRequestQueue(cacheFolder, new
                OkHttpStack(new OkHttpClient())));
        init();
        appContext = this;
    }

    public static AppContext getInstance() {
        return appContext;
    }

    /**
     * ?????????Application
     */
    private void init() {
        // ??????????????????????????????
        User loginUser = getLoginInfo();
        if (null != loginUser && StringUtils.toInt(loginUser.getId()) > 0
                && !StringUtils.isEmpty(getProperty(PROP_KEY_PRIVATE_TOKEN))) {
            // ???????????????id?????????
            this.loginUid = StringUtils.toInt(loginUser.getId());
            this.login = true;
        }
    }



    public void setProperties(Properties ps) {
        AppConfig.getAppConfig(this).set(ps);
    }

    public Properties getProperties() {
        return AppConfig.getAppConfig(this).get();
    }

    public void setProperty(String key, String value) {
        AppConfig.getAppConfig(this).set(key, value);
    }

    public String getProperty(String key) {
        return AppConfig.getAppConfig(this).get(key);
    }

    public void removeProperty(String... key) {
        AppConfig.getAppConfig(this).remove(key);
    }

    /**
     * ????????????????????????App
     */
    public boolean isFirstStart() {
        boolean res = false;
        String perf_frist = getProperty(AppConfig.CONF_FRIST_START);
        // ?????????http
        if (TextUtils.isEmpty(perf_frist)) {
            res = true;
        }
        return res;
    }

    /**
     * ????????????????????????App
     */
    public void setFirstStart(boolean isFirstStart) {
        if(isFirstStart){
            removeProperty(AppConfig.CONF_FRIST_START);
            return;
        }
        setProperty(AppConfig.CONF_FRIST_START, String.valueOf(isFirstStart));
    }

    /**
     * ???????????????????????????
     */
    public void setConfigVoice(boolean b) {
        setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
    }

    /**
     * ????????????????????????
     */
    public boolean isCheckUp() {
        String perf_checkup = getProperty(AppConfig.CONF_CHECKUP);
        // ???????????????
        return StringUtils.isEmpty(perf_checkup) || StringUtils.toBool(perf_checkup);
    }

    /**
     * ????????????????????????
     */
    public void setConfigCheckUp(boolean b) {
        setProperty(AppConfig.CONF_CHECKUP, String.valueOf(b));
    }

    /**
     * ?????????????????????????????????????????????
     */
    public boolean isAudioNormal() {
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    /**
     * ?????????????????????????????????
     */
    public boolean isAppSound() {
        return isAudioNormal() && isVoice();
    }


    /**
     * ???????????????????????????
     */
    public void setConfigSensor(boolean openSensor) {
        setProperty(AppConfig.CONF_OPEN_SENSOR, String.valueOf(openSensor));
    }

    /**
     * ?????????????????????
     */
    public boolean isOpenSensor() {
        String perf_open_sensor = getProperty(AppConfig.CONF_OPEN_SENSOR);
        // ???????????????
        return StringUtils.toBool(perf_open_sensor);
    }

    /**
     * ??????????????????
     */
    public boolean isReceiveNotice() {
        String perf_notice = getProperty(AppConfig.CONF_RECEIVENOTICE);
        // ???????????????????????????
        return StringUtils.isEmpty(perf_notice) || StringUtils.toBool(perf_notice);
    }

    /**
     * ????????????????????????
     */
    public void setConfigReceiveNotice(boolean isReceiveNotice) {
        setProperty(AppConfig.CONF_RECEIVENOTICE, String.valueOf(isReceiveNotice));
    }

    /**
     * ?????????????????????
     */
    public boolean isVoice() {
        String perf_voice = getProperty(AppConfig.CONF_VOICE);
        return StringUtils.isEmpty(perf_voice) || StringUtils.toBool(perf_voice);
    }

    /**
     * ???????????????????????????????????????????????????
     */
    public static boolean isMethodsCompat(int VersionCode) {
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        return currentVersion >= VersionCode;
    }

    /**
     * ??????App????????????
     */
    public String getAppId() {
        String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
        if (StringUtils.isEmpty(uniqueID)) {
            uniqueID = UUID.randomUUID().toString();
            setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
        }
        return uniqueID;
    }

    /**
     * ??????App???????????????
     */
    public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }

    /**
     * ??????????????????
     */
    public User getLoginInfo() {
        User user = new User();
        user.setId(getProperty(PROP_KEY_UID));
        user.setUsername(getProperty(PROP_KEY_USERNAME));
        user.setName(getProperty(PROP_KEY_NAME));
        user.setBio(getProperty(PROP_KEY_BIO));
        user.setWeibo(getProperty(PROP_KEY_WEIBO));
        user.setBlog(getProperty(PROP_KEY_BLOG));
        user.setTheme_id(StringUtils.toInt(getProperty(PROP_KEY_THEME_ID), 1));
        user.setState(getProperty(PROP_KEY_STATE));
        user.setCreated_at(getProperty(PROP_KEY_CREATED_AT));
        user.setPortrait(getProperty(PROP_KEY_PORTRAIT));
        user.setNew_portrait(getProperty(PROP_KEY_NEWPORTRAIT));
        user.setIsAdmin(StringUtils.toBool(getProperty(PROP_KEY_IS_ADMIN)));
        user.setCanCreateGroup(StringUtils
                .toBool(getProperty(PROP_KEY_CAN_CREATE_GROUP)));
        user.setCanCreateProject(StringUtils
                .toBool(getProperty(PROP_KEY_CAN_CREATE_PROJECT)));
        user.setCanCreateTeam(StringUtils
                .toBool(getProperty(PROP_KEY_CAN_CREATE_TEAM)));
        Follow follow = new Follow();
        follow.setFollowers(StringUtils.toInt(getProperty(ROP_KEY_FOLLOWERS)));
        follow.setStarred(StringUtils.toInt(getProperty(ROP_KEY_STARRED)));
        follow.setFollowing(StringUtils.toInt(getProperty(ROP_KEY_FOLLOWING)));
        follow.setWatched(StringUtils.toInt(getProperty(ROP_KEY_WATCHED)));
        user.setFollow(follow);
        return user;
    }

    /**
     * ???????????????email???pwd
     */
    public void saveAccountInfo(String email, String pwd) {
        setProperty(ACCOUNT_EMAIL, email);
        setProperty(ACCOUNT_PWD, pwd);
    }

    /**
     * ???????????????????????????
     */
    @SuppressWarnings("serial")
    public void saveLoginInfo(final User user) {
        if (null == user) {
            return;
        }
        // ?????????????????????
        this.loginUid = StringUtils.toInt(user.getId());
        this.login = true;
        setProperties(new Properties() {
            {
                setProperty(PROP_KEY_UID, String.valueOf(user.getId()));
                setProperty(PROP_KEY_USERNAME, String.valueOf(user.getUsername()));
                setProperty(PROP_KEY_NAME, String.valueOf(user.getName()));
                setProperty(PROP_KEY_BIO, String.valueOf(user.getBio()));// ????????????
                setProperty(PROP_KEY_WEIBO, String.valueOf(user.getWeibo()));
                setProperty(PROP_KEY_BLOG, String.valueOf(user.getBlog()));
                setProperty(PROP_KEY_THEME_ID, String.valueOf(user.getTheme_id()));
                setProperty(PROP_KEY_STATE, String.valueOf(user.getState()));
                setProperty(PROP_KEY_CREATED_AT, String.valueOf(user.getCreated_at()));
                setProperty(PROP_KEY_PORTRAIT, String.valueOf(user.getPortrait()));// ????????????
                setProperty(PROP_KEY_NEWPORTRAIT, String.valueOf(user.getNew_portrait()));// ????????????
                setProperty(PROP_KEY_IS_ADMIN, String.valueOf(user.isIsAdmin()));
                setProperty(PROP_KEY_CAN_CREATE_GROUP, String.valueOf(user.isCanCreateGroup()));
                setProperty(PROP_KEY_CAN_CREATE_PROJECT, String.valueOf(user.isCanCreateProject()));
                setProperty(PROP_KEY_CAN_CREATE_TEAM, String.valueOf(user.isCanCreateTeam()));
                setProperty(ROP_KEY_FOLLOWERS, String.valueOf(user.getFollow().getFollowers()));
                setProperty(ROP_KEY_STARRED, String.valueOf(user.getFollow().getStarred()));
                setProperty(ROP_KEY_FOLLOWING, String.valueOf(user.getFollow().getFollowing()));
                setProperty(ROP_KEY_WATCHED, String.valueOf(user.getFollow().getWatched()));
            }
        });
    }

    /**
     * ????????????????????????????????????token???????????????
     */
    private void cleanLoginInfo() {
        this.loginUid = 0;
        this.login = false;
        removeProperty(PROP_KEY_PRIVATE_TOKEN, PROP_KEY_UID, PROP_KEY_USERNAME, PROP_KEY_EMAIL,
                PROP_KEY_NAME, PROP_KEY_BIO, PROP_KEY_WEIBO, PROP_KEY_BLOG,
                PROP_KEY_THEME_ID, PROP_KEY_STATE, PROP_KEY_CREATED_AT,
                PROP_KEY_PORTRAIT, PROP_KEY_IS_ADMIN,
                PROP_KEY_CAN_CREATE_GROUP, PROP_KEY_CAN_CREATE_PROJECT,
                PROP_KEY_CAN_CREATE_TEAM, ROP_KEY_FOLLOWERS, ROP_KEY_STARRED,
                ROP_KEY_FOLLOWING, ROP_KEY_WATCHED);
    }

    /**
     * ??????????????????
     */
    public boolean isLogin() {
        return login;
    }

    /**
     * ??????????????????id
     */
    public int getLoginUid() {
        return this.loginUid;
    }

    /**
     * ????????????
     */
    public void logout() {
        // ??????????????????????????????
        cleanLoginInfo();
        this.login = false;
        this.loginUid = 0;
        // ??????????????????
        BroadcastController.sendUserChangeBroadcase(this);
    }

    /**
     * ??????app??????
     */
    public void clearAppCache() {
        deleteDatabase("webview.db");
        deleteDatabase("webview.db-shm");
        deleteDatabase("webview.db-wal");
        deleteDatabase("webviewCache.db");
        deleteDatabase("webviewCache.db-shm");
        deleteDatabase("webviewCache.db-wal");
        // ??????????????????
        clearCacheFolder(getFilesDir(), System.currentTimeMillis());
        clearCacheFolder(getCacheDir(), System.currentTimeMillis());
        // 2.2????????????????????????????????????sd????????????
        if (isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
            clearCacheFolder(MethodsCompat.getExternalCacheDir(this),
                    System.currentTimeMillis());
        }
        // ????????????????????????????????????
        Properties props = getProperties();
        for (Object key : props.keySet()) {
            String _key = key.toString();
            if (_key.startsWith("temp"))
                removeProperty(_key);
        }
    }

    /**
     * ??????????????????
     *
     * @param dir     ??????
     * @param curTime ??????????????????
     */
    private int clearCacheFolder(File dir, long curTime) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, curTime);
                    }
                    if (child.lastModified() < curTime) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }

    public static String getToken() {
        String private_token = AppContext.getInstance().getProperty(AsyncHttpHelp.PRIVATE_TOKEN);
        private_token = CyptoUtils.decode(AsyncHttpHelp.GITOSC_PRIVATE_TOKEN, private_token);
        return private_token;
    }
}
