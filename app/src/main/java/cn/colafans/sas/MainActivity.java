
package cn.colafans.sas;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private List<AppInfo> mAppList;
    private AppListAdapter appListAdapter;
    private AppConfigAdapter appConfigAdapter;
    private Context mContext;
    private RecyclerView rvAppList, rvAppConfig;
    private ExecutorService executorService;
    private TextView tvHint;
    private int clickCount = 0;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = () -> clickCount = 0;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        rvAppList = findViewById(R.id.rv_app_list);
        executorService = Executors.newSingleThreadExecutor();
        tvHint = findViewById(R.id.tv_hint);
        initData();
        tvHint.setOnClickListener(v -> {
            clickCount++;
            if (clickCount == 5) {
                Toast.makeText(mContext, "aaaa", Toast.LENGTH_SHORT).show();
                try {
                    Settings.Global.putString(mContext.getContentResolver(), "policy_control", "immersive.full=*");
                } catch (Exception e) {
                    Log.e(TAG, "onCreate: ", e);
                }

                //TODO settings pop/activity
                clickCount = 0;
            }
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 5000);
        });
        // 设置点击事件以显示 PopupWindow
        tvHint.setOnLongClickListener(v -> {
            showPopupWindow();
            return true;
        });
    }

    private void showPopupWindow() {
        // 创建 PopupWindow 的内容视图
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_layout, null);

        // 创建 PopupWindow
        popupWindow = new PopupWindow(popupView,
                (int) mContext.getResources().getDimension(R.dimen.dp_400),
                RecyclerView.LayoutParams.MATCH_PARENT,
                true);

        // 设置 PopupWindow 的动画效果
        popupWindow.setAnimationStyle(R.style.PopupWindowAnimation);
        int[] location = new int[2];
        tvHint.getLocationOnScreen(location);
        int x = location[0] + tvHint.getWidth();
        int y = location[1];
        // 显示 PopupWindow
        popupWindow.showAtLocation(findViewById(R.id.main),
                Gravity.NO_GRAVITY,
                x,
                y);
        rvAppConfig = popupView.findViewById(R.id.rv_app_config);
        appConfigAdapter = new AppConfigAdapter(mContext, mAppList);
        rvAppConfig.setLayoutManager(new LinearLayoutManager(mContext));
        rvAppConfig.setAdapter(appConfigAdapter);
        appConfigAdapter.setOnItemCheckedChangeListener((buttonView, position, isChecked) -> {
            AppInfo appInfo = mAppList.get(position);
            appInfo.isFullScreen = buttonView.isChecked();
            String config = Settings.Global.getString(mContext.getContentResolver(), "policy_control");
            if (appInfo.isFullScreen) {
                config = config + "," + appInfo.pkgName;
            } else {
                config = config.replace(appInfo.pkgName, "");
            }
            Settings.Global.putString(mContext.getContentResolver(), "policy_control", config);

        });
        // 设置关闭按钮的点击事件
        Switch fullScreen = popupView.findViewById(R.id.sb_full_screen);
        fullScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    //Settings.Global.putString(mContext.getContentResolver(), "policy_control", "immersive.full=*");
                    Settings.Global.putString(mContext.getContentResolver(), "policy_control", "immersive.full=cn.colafans.sas,com.android.settings");
                } else {
                    Settings.Global.putString(mContext.getContentResolver(), "policy_control", "null");
                }
            }
        });
    }

    private void initData() {
        mAppList = new ArrayList<>();
        appListAdapter = new AppListAdapter(mContext, mAppList);
        rvAppList.setLayoutManager(new GridLayoutManager(mContext, 6, RecyclerView.VERTICAL, false));
        rvAppList.setAdapter(appListAdapter);
        appListAdapter.setOnItemClickListener((view, position) -> startApp(position));

        executorService.submit(() -> {
            final List<AppInfo> tempList = getAllCommonAppData();
            runOnUiThread(() -> notifyAppDataSetChanged(tempList));
        });
    }

    private void notifyAppDataSetChanged(List<AppInfo> data) {
        mAppList.addAll(data);
        appListAdapter.notifyDataSetChanged();
        if (appConfigAdapter != null) {
            appConfigAdapter.notifyDataSetChanged();
        }
    }

    private List<AppInfo> getAllCommonAppData() {
        List<AppInfo> appDatas = new ArrayList<>();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> allApps = pm.queryIntentActivities(mainIntent, 0);

        for (ResolveInfo res : allApps) {
            Log.d(TAG, "App Info: packageName = " + res.activityInfo.packageName
                    + ", name = " + res.activityInfo.name
                    + ", label = " + res.loadLabel(pm).toString());

            if (!isAppExists(res) && !isSgnwApp(res)) {
                AppInfo appItem = createAppInfo(res, pm);
                appDatas.add(appItem);
            }
        }
        return appDatas;
    }

    private boolean isSgnwApp(ResolveInfo res) {
        if (res.activityInfo.packageName.startsWith("com.sgmw.")) {
            return true;
        }
        return false;
    }

    private boolean isAppExists(ResolveInfo res) {
        for (AppInfo bean : mAppList) {
            if (bean.pkgName.equals(res.activityInfo.packageName) && bean.clzName.equals(res.activityInfo.name)) {
                return true;
            }
        }
        return false;
    }

    private AppInfo createAppInfo(ResolveInfo res, PackageManager pm) {
        AppInfo appItem = new AppInfo();
        appItem.pkgName = res.activityInfo.packageName;
        appItem.clzName = res.activityInfo.name;
        appItem.appName = res.loadLabel(pm).toString();
        appItem.allappIcon = String.valueOf(res.getIconResource());
        try {
            appItem.icon = pm.getApplicationIcon(appItem.pkgName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to get application icon", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error", e);
        }
        return appItem;
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        //enterPictureInPictureMode();
    }


    private void startApp(int position) {
        if (position >= 0 && position < mAppList.size()) {
            AppInfo appInfo = mAppList.get(position);
            Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(appInfo.pkgName);
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Log.e(TAG, "No launch intent found for package: " + appInfo.pkgName);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}