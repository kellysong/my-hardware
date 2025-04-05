/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.sinpo.xnfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sinpo.xnfc.adapter.CardListAdapter;
import com.sinpo.xnfc.bean.CardInfo;
import com.sinpo.xnfc.card.CardManager;

import org.xml.sax.XMLReader;

import androidx.appcompat.app.AppCompatActivity;


import java.util.HashMap;
import java.util.Map;

/**
 * 设置系统调度 -> 系统调用onNewIntent(Intent intent) -> 获取Tag -> 获取读写通道 -> 进行读写 -> 最后取消系统调度
 */
public class NFCardActivity extends AppCompatActivity implements OnClickListener,
        Html.ImageGetter, Html.TagHandler {
    private static final String TAG = "SIMPLE_LOGGER";
    private NfcAdapter nfcAdapter; //NFC适配器
    private PendingIntent pendingIntent; //传达意图
    private Resources res;
    //    private TextView board;
    private TextView mHint;
    private LinearLayout defaultBg;
    private ScrollView sv_context;
    private ListView listView;
    private Spanned cardData;

    private TextView cardName, cardNo, cardBalance;
    private Button btnNfc;
    /**
     * 是否点击Activity进来的，默认0，不是点击Activity进来，1选中应用进来
     */
    private int isActivityOpen = 0;

    private ImageView mBack;
    private TextView iv_title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        initData();
    }


    private void initView() {
        setContentView(R.layout.nfcard_activity);
        mBack = findViewById(R.id.iv_back);
        iv_title = findViewById(R.id.iv_title);
        defaultBg = (LinearLayout) findViewById(R.id.ll_default_bg);
        mHint = (TextView) findViewById(R.id.tv_hint);
        sv_context = (ScrollView) findViewById(R.id.sv_context);
        listView = findViewById(R.id.lv_cardList);
        cardName = findViewById(R.id.tv_cardName);
        cardNo = findViewById(R.id.tv_cardNo);
        cardBalance = findViewById(R.id.tv_cardBalance);
        btnNfc = findViewById(R.id.btnNfc);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int nfcColor = extras.getInt("nfcColor", -1);
            if (nfcColor != -1) {
                setStatusBar(nfcColor);
                RelativeLayout titleBar = findViewById(R.id.ll_title_bar);
                titleBar.setBackgroundColor(nfcColor);
            }
            isActivityOpen = extras.getInt("isActivityOpen", 0);
            iv_title.setText(extras.getString("title", "普卡余额查询"));
            Log.i(TAG, "nfcColor:" + nfcColor + ",isActivityOpen:" + isActivityOpen);
        }
        CardInfo cardInfo = getCardInfo(this);
        initCardInfoList(cardInfo);
    }

    /**
     * 设置StatusBar颜色
     *
     * @param color 颜色值
     */
    protected void setStatusBar(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    private void initListener() {
        //		final TextView board = (TextView) decor.findViewById(R.id.board);
//		this.board = board;
//		board.setMovementMethod(LinkMovementMethod.getInstance());
//		board.setFocusable(false);
//		board.setClickable(false);
//		board.setLongClickable(false);
        findViewById(R.id.btnCopy).setOnClickListener(this);
        btnNfc.setOnClickListener(this);
        findViewById(R.id.btnExit).setOnClickListener(this);
        mBack.setOnClickListener(this);
    }

    private void initData() {
        final Resources res = getResources();
        this.res = res;
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            btnNfc.setEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
        onNewIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.clear) {
            showData(null);
            return true;
        } else if (i == R.id.help) {
            showHelp(R.string.info_help);
            return true;
        } else if (i == R.id.about) {
            showHelp(R.string.info_about);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);// 取消调度

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null)
            // 这行代码是添加调度，效果是读标签的时候不会弹出候选程序，直接用本程序处理
            nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                    CardManager.FILTERS, CardManager.TECHLISTS);

//		refreshStatus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {
            final Parcelable p = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);//获取tag
            Log.e(TAG, "NFCTAG:" + intent.getAction());
            showData((p != null) ? CardManager.load(p, res) : null);
        } catch (Exception e) {
            Log.e(TAG, "获取tag异常", e);
        }
    }

    @Override
    public void onClick(final View v) {
        int i = v.getId();
        if (i == R.id.btnCopy) {
            copyData();
        } else if (i == R.id.btnNfc) {

            startActivityForResult(new Intent(
                    android.provider.Settings.ACTION_NFC_SETTINGS), 0);
        } else if (i == R.id.btnExit) {
            finish();
        } else if (i == R.id.iv_back) {
            //如果是点击消息(或者nfc感应)跳转进来的，且（该运行的进程里没有该应用进程 或 应用首页的Activity不存在任务栈里面）
            try {
                Class<?> clz = getLaunchMainClass();
                boolean proessRunning = ServiceHelper.isProessRunning(getApplicationContext(), this.getPackageName());
                boolean exsitMianActivity = ServiceHelper.isExsitMianActivity(this, clz);
                if (isActivityOpen == 0 && (!proessRunning || !exsitMianActivity)) {
                    Intent intent = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected Class<?> getLaunchMainClass() {
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshStatus();
            }
        }, 800);
    }

    private void refreshStatus() {
        final Resources r = this.res;

        final String tip;
        if (nfcAdapter == null)
            tip = r.getString(R.string.tip_nfc_notfound);
        else if (nfcAdapter.isEnabled())
            tip = r.getString(R.string.tip_nfc_enabled);
        else
            tip = r.getString(R.string.tip_nfc_disabled);
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
        showHint();

    }

    private void copyData() {
        final CharSequence text = cardData;
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, res.getString(R.string.msg_nocard), Toast.LENGTH_SHORT).show();
            return;
        }

        ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                .setText(text);

        final String msg = res.getString(R.string.msg_copied);
        final Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 显示数据
     *
     * @param data
     */
    private void showData(String data) {
        if (data == null || data.length() == 0) {
            showHint();
            return;
        }
        CardInfo cardInfo = CardManager.getCardInfo();

        initCardInfoList(cardInfo);

        defaultBg.setVisibility(View.GONE);
        sv_context.setVisibility(View.VISIBLE);
//		board.setText(Html.fromHtml(data));

        cardData = Html.fromHtml(data);
        saveCardInfo(this,cardInfo);
    }

    private void initCardInfoList(CardInfo cardInfo) {
        if (cardInfo == null){
            return;
        }
        Log.i(TAG, cardInfo.toString());
        cardName.setText(cardInfo.getCardName());
        cardNo.setText("卡号:" + cardInfo.getCardNo());
        cardBalance.setText(Html.fromHtml("余额:<font color='red'>" + cardInfo.getCardBalance() + "</font>"));
        CardListAdapter cardListAdapter = new CardListAdapter(this, R.layout.item_card_list, cardInfo.getConsumeRecords());
        listView.setAdapter(cardListAdapter);
    }

    private void showHelp(int id) {
        mHint.setText(Html.fromHtml(res.getString(id), this, this));
    }

    /**
     * 提示信息，不支持nfc时，或者说支持，但被禁用
     */
    private void showHint() {
        final Resources res = this.res;
        final String hint;

        if (nfcAdapter == null)
            hint = res.getString(R.string.msg_nonfc);
        else if (nfcAdapter.isEnabled())
            hint = res.getString(R.string.msg_nocard);
        else
            hint = res.getString(R.string.msg_nfcdisabled);
        mHint.setText(hint);

    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output,
                          XMLReader xmlReader) {
        if (!opening && "version".equals(tag)) {
            try {
                output.append(getPackageManager().getPackageInfo(
                        getPackageName(), 0).versionName);
            } catch (NameNotFoundException e) {
            }
        }
    }

    @Override
    public Drawable getDrawable(String source) {
        final Resources r = getResources();

        final Drawable ret;
        final String[] params = source.split(",");
        if ("icon_main".equals(params[0])) {
            ret = r.getDrawable(R.drawable.ic_app_main);
        } else {
            ret = null;
        }

        if (ret != null) {
            final float f = r.getDisplayMetrics().densityDpi / 72f;
            final int w = (int) (Util.parseInt(params[1], 10, 16) * f + 0.5f);
            final int h = (int) (Util.parseInt(params[2], 10, 16) * f + 0.5f);
            ret.setBounds(0, 0, w, h);
        }

        return ret;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void saveCardInfo(Context context, CardInfo data) {
        SharedPreferences sp = context.getSharedPreferences("nfc_card", Context.MODE_PRIVATE);//数据自己可用
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("recentReadData", new Gson().toJson(data));
        edit.commit();
    }

    public static CardInfo getCardInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences("nfc_card", Context.MODE_PRIVATE);
        String data = sp.getString("recentReadData", null);
        if (data == null){
            return null;
        }
        CardInfo cardInfo = new Gson().fromJson(data, CardInfo.class);
        return cardInfo;
    }

}
