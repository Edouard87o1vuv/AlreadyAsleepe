package net.oschina.gitapp.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kymjs.core.bitmap.client.BitmapCore;
import com.kymjs.rxvolley.client.HttpCallback;

import net.oschina.gitapp.AppContext;
import net.oschina.gitapp.R;
import net.oschina.gitapp.api.GitOSCApi;
import net.oschina.gitapp.bean.LuckMsg;
import net.oschina.gitapp.bean.RandomProject;
import net.oschina.gitapp.common.StringUtils;
import net.oschina.gitapp.common.UIHelper;
import net.oschina.gitapp.dialog.ShareDialog;
import net.oschina.gitapp.ui.baseactivity.BaseActivity;
import net.oschina.gitapp.utils.JsonUtils;
import net.oschina.gitapp.utils.ShakeListener;
import net.oschina.gitapp.utils.ShakeListener.OnShakeListener;
import net.oschina.gitapp.utils.TypefaceUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShakeActivity extends BaseActivity implements OnClickListener {

    private final int DURATION_TIME = 600;

    private AppContext mAppContext;

    private ShakeListener mShakeListener = null;

    private Vibrator mVibrator;

    private TextView mLuckMsg;

    private RelativeLayout mImgUp;

    private RelativeLayout mImgDn;

    private LinearLayout mLoaging;

    private RelativeLayout mShakeResProject;// ????????????

    private ImageView mProjectFace;

    private TextView mProjectTitle;

    private TextView mProjectDescription;

    private TextView mProjectLanguage;

    private TextView mProjectWatchNums;

    private TextView mProjectStarNums;

    private TextView mProjectForkNums;

    private RelativeLayout mShakeResAward;// ????????????

    private ImageView mShakeResAwardImg;

    private TextView mShakeResAwardMsg;

    private RandomProject mProject;

    private SoundPool sndPool;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Integer> soundPoolMap = new HashMap<>();

    private Bitmap mBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);
        mAppContext = AppContext.getInstance();
        initView();
        // ???????????????????????????
        loadLuckMsg();
        mVibrator = (Vibrator) getApplication().getSystemService(
                VIBRATOR_SERVICE);

        loadSound();
        mShakeListener = new ShakeListener(this);
        // ?????????????????????
        mShakeListener.setOnShakeListener(new OnShakeListener() {
            public void onShake() {
                startAnim();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shake_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.shake_menu_share:
                showShare();
                break;
            case R.id.shake_menu_edit_shippingaddress:
                showShippingAddressActivity();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showShare() {
        if (mProject == null) {
            UIHelper.getDialog(ShakeActivity.this, "????????????", "???????????????????????????", "?????????").show();
        } else {
            String title = "????????????????????????";
            String url = GitOSCApi.NO_API_BASE_URL + mProject.getPathWithNamespace();
            String shareContent = "??????Git@OSC?????????????????????" + mProject.getOwner().getName() + "?????????" +
                    mProject.getName() + "???????????????????????????";
            //UIHelper.showShareOption(ShakeActivity.this, title, url, shareContent, mBitmap);
            new ShareDialog(this)
                    .init(ShakeActivity.this, title, url, shareContent, mBitmap)
                    .show();
        }
    }

    private void initView() {

        mLuckMsg = findViewById(R.id.shake_luck_msg);

        mImgUp = findViewById(R.id.shakeImgUp);
        mImgDn = findViewById(R.id.shakeImgDown);

        mLoaging = findViewById(R.id.shake_loading);

        mShakeResProject = findViewById(R.id.shakeres_paroject);

        mProjectFace = findViewById(R.id.iv_portrait);

        mProjectTitle = findViewById(R.id.tv_title);

        mProjectDescription = findViewById(R.id.tv_description);

        mProjectLanguage = findViewById(R.id.tv_lanuage);

        mProjectWatchNums = findViewById(R.id.tv_watch);

        mProjectStarNums = findViewById(R.id.tv_star);

        mProjectForkNums = findViewById(R.id.tv_fork);

        mShakeResAward = findViewById(R.id.shakeres_award);

        mShakeResAwardImg = findViewById(R.id.shake_award_img);

        mShakeResAwardMsg = findViewById(R.id.shake_award_msg);

        mShakeResProject.setOnClickListener(this);
    }

    private void loadSound() {

        sndPool = new SoundPool(2, AudioManager.STREAM_SYSTEM, 5);
        new Thread() {
            public void run() {
                try {
                    soundPoolMap.put(
                            0,
                            sndPool.load(
                                    getAssets().openFd(
                                            "sound/shake_sound_male.mp3"), 1));

                    soundPoolMap.put(1, sndPool.load(
                            getAssets().openFd("sound/shake_match.mp3"), 1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void startAnim() {
        AnimationSet animup = new AnimationSet(true);
        TranslateAnimation mytranslateanimup0 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                -0.5f);
        mytranslateanimup0.setDuration(DURATION_TIME);
        TranslateAnimation mytranslateanimup1 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                +0.5f);
        mytranslateanimup1.setDuration(DURATION_TIME);
        mytranslateanimup1.setStartOffset(DURATION_TIME);
        animup.addAnimation(mytranslateanimup0);
        animup.addAnimation(mytranslateanimup1);
        mImgUp.startAnimation(animup);

        AnimationSet animdn = new AnimationSet(true);
        TranslateAnimation mytranslateanimdn0 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                +0.5f);
        mytranslateanimdn0.setDuration(DURATION_TIME);
        TranslateAnimation mytranslateanimdn1 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                -0.5f);
        mytranslateanimdn1.setDuration(DURATION_TIME);
        mytranslateanimdn1.setStartOffset(DURATION_TIME);
        animdn.addAnimation(mytranslateanimdn0);
        animdn.addAnimation(mytranslateanimdn1);
        mImgDn.startAnimation(animdn);

        // ?????????????????????????????????????????????
        mytranslateanimdn0.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mShakeResProject.setVisibility(View.GONE);
                mShakeListener.stop();
                sndPool.play(soundPoolMap.get(0), (float) 0.2, (float) 0.2, 0, 0,
                        (float) 0.6);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadProject();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mShakeListener != null) {
            mShakeListener.stop();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.shakeres_paroject:
                if (mProject != null) {
                    UIHelper.showProjectDetail(ShakeActivity.this, null, mProject.getId());
                }
                break;

            default:
                break;
        }
    }

    private void beforeLoading() {
        mLoaging.setVisibility(View.VISIBLE);
        mShakeResProject.setVisibility(View.GONE);
        mShakeResAward.setVisibility(View.GONE);
    }

    private void afterLoading() {
        mLoaging.setVisibility(View.GONE);
        sndPool.play(soundPoolMap.get(1), (float) 0.2, (float) 0.2,
                0, 0, (float) 0.6);
        mVibrator.cancel();
        mShakeListener.start();
    }

    private void loadProject() {
        GitOSCApi.getRandomProject(new HttpCallback() {
            @Override
            public void onSuccess(Map<String, String> headers, byte[] t) {
                super.onSuccess(headers, t);
                mProject = JsonUtils.toBean(RandomProject.class, t);

                if (mProject != null && mProject.getRand_num() == 0) {
                    mShakeResProject.setBackgroundResource(R.color.white);
                    mShakeResProject.setVisibility(View.VISIBLE);
                    // ????????????????????????
                    mProjectTitle.setText(mProject.getOwner().getName() + "/" + mProject.getName());
                    mProjectDescription.setText(mProject.getDescription());

                    setTextWithSemantic(mProjectStarNums, mProject.getWatches_count().toString(),
                            R.string.sem_watch);
                    setTextWithSemantic(mProjectForkNums, mProject.getStars_count().toString(), R
                            .string.sem_star);
                    setTextWithSemantic(mProjectWatchNums, mProject.getForks_count().toString(),
                            R.string.sem_fork);

                    String language = mProject.getLanguage() != null ? mProject.getLanguage() : "";
                    if (mProject.getLanguage() != null) {
                        setTextWithSemantic(mProjectLanguage, language, R.string.sem_tag);
                    } else {
                        mProjectLanguage.setVisibility(View.GONE);
                    }

                    mProjectFace.setImageResource(R.drawable.widget_dface_loading);
                    String faceUrl = mProject.getOwner().getNew_portrait();
                    if (faceUrl.endsWith(".gif") || StringUtils.isEmpty(faceUrl)) {
                        mProjectFace.setImageResource(R.drawable.mini_avatar);
                    } else {
                        new BitmapCore.Builder().url(faceUrl).view(mProjectFace).doTask();
                    }

                    Handler handle = new Handler();
                    handle.postDelayed(() -> mBitmap = UIHelper.takeScreenShot(ShakeActivity.this), 500);

                } else if (mProject != null) {
                    mShakeListener.stop();
                    mShakeResAward.setVisibility(View.VISIBLE);
                    new BitmapCore.Builder().url(mProject.getImg()).view(mShakeResAwardImg)
                            .doTask();
                    mShakeResAwardMsg.setText(mProject.getMsg());

                    AlertDialog.Builder dialog = new Builder(ShakeActivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("????????????????????????????????????");
                    dialog.setMessage(Html.fromHtml("?????????" + mProject.getMsg() +
                            "<br><br>???????????????<br>????????????????????????????????????????????????????????????"));
                    dialog.setNegativeButton("????????????", (dialog1, which) -> {
                        mShakeListener.start();
                        showShippingAddressActivity();
                    });
                    dialog.setPositiveButton("??????", (dialog12, which) -> {
                        String url = "http://t.cn/RhLDd4k";
                        Bitmap bitmap = UIHelper.takeScreenShot(ShakeActivity.this);
//                            UIHelper.showShareOption(ShakeActivity.this, "??????????????????", url, mProject
//                                    .getMsg(), bitmap);
                        new ShareDialog(ShakeActivity.this)
                                .init(ShakeActivity.this, "??????????????????", url, mProject
                                        .getMsg(), bitmap)

                                .show();
                    });
                    dialog.show();
                }
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                UIHelper.toastMessage(ShakeActivity.this, "??????????????????????????????????????????????????????");
            }

            @Override
            public void onPreStart() {
                super.onPreStart();
                beforeLoading();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                afterLoading();
            }
        });
    }

    public void setTextWithSemantic(TextView tv, String text, int semanticRes) {
        String finalText = AppContext.getInstance().getResources().getString(semanticRes) + " " +
                text;
        tv.setText(finalText);
        TypefaceUtils.setSemantic(tv);
    }

    /**
     * ???????????????????????????????????????
     */
    private void showShippingAddressActivity() {
        if (!mAppContext.isLogin()) {
            UIHelper.showLoginActivity(ShakeActivity.this);
            return;
        }
        Intent intent = new Intent(ShakeActivity.this, ShippingAddressActivity.class);
        startActivity(intent);
    }

    private void loadLuckMsg() {

        GitOSCApi.getLuckMsg(new HttpCallback() {
            @Override
            public void onSuccess(Map<String, String> headers, byte[] t) {
                super.onSuccess(headers, t);
                LuckMsg luckMsg = JsonUtils.toBean(LuckMsg.class, t);
                if (luckMsg != null) {
                    mLuckMsg.setVisibility(View.VISIBLE);
                    mLuckMsg.setText(luckMsg.getMessage());
                }
            }

        });
    }
}