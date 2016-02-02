package com.miscell.lucky;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {
    private static final Intent sSettingsIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

    private TextView mAccessibleLabel;
    private TextView mNotificationLabel;
    private TextView mLabelText;
    private CheckBox mSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAccessibleLabel = (TextView) findViewById(R.id.label_accessible);
        mNotificationLabel = (TextView) findViewById(R.id.label_notification);
        mSwitch = (CheckBox) findViewById(R.id.cb_switch);
        mLabelText = (TextView) findViewById(R.id.label_text);
        updateGoalSwitchState();
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LuckyApplication.getInstance().setEnable(isChecked);
                updateGoalSwitchState();
            }
        });

        if (Build.VERSION.SDK_INT >= 18) {
            mNotificationLabel.setVisibility(View.VISIBLE);
            findViewById(R.id.button_notification).setVisibility(View.VISIBLE);
        } else {
            mNotificationLabel.setVisibility(View.GONE);
            findViewById(R.id.button_notification).setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.version_text)).setText("版本:" + getVersionName());

//        imageView1.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.i("test", "# fired");
//                unlockScreen();
//
//            }
//        }, 5000L);
    }

    private String getVersionName() {
        String versionName = "";

        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeLabelStatus();
    }

    private void updateGoalSwitchState() {
        boolean enable = LuckyApplication.getInstance().isEnable();
        mSwitch.setChecked(enable);
        mSwitch.setTextColor(getTextColor(enable));
    }

    private int getTextColor(boolean enable) {
        return getResources().getColor(enable
                ? R.color.text_enable
                : R.color.text_disable);
    }

    private void changeLabelStatus() {
        boolean isAccessibilityEnabled = isAccessibleEnabled();
        mAccessibleLabel.setTextColor(getTextColor(isAccessibilityEnabled));
        mAccessibleLabel.setText(isAccessibleEnabled() ? "辅助功能已打开" : "辅助功能未打开");
        mLabelText.setText(isAccessibilityEnabled ? "好了~你可以去做其他事情了，我会自动给你抢红包的" : "请打开开关开始抢红包");

        if (Build.VERSION.SDK_INT >= 18) {
            boolean isNotificationEnabled = isNotificationEnabled();
            mNotificationLabel.setTextColor(getTextColor(isNotificationEnabled));
            mNotificationLabel.setText(isNotificationEnabled ? "接收通知已打开" : "接收通知未打开");

            if (isAccessibilityEnabled && isNotificationEnabled) {
                mLabelText.setText("好了~你可以去做其他事情了，我会自动给你抢红包的");
            } else {
                mLabelText.setText("请把两个开关都打开开始抢红包");
            }
        }
    }

    public void onNotificationEnableButtonClicked(View view) {
        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    public void onSettingsClicked(View view) {
        startActivity(sSettingsIntent);
    }

    private boolean isAccessibleEnabled() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo info : runningServices) {
            if (info.getId().equals(getPackageName() + "/.MonitorService")) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotificationEnabled() {
        ContentResolver contentResolver = getContentResolver();
        String enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");

        if (!TextUtils.isEmpty(enabledListeners)) {
            return enabledListeners.contains(getPackageName() + "/" + getPackageName() + ".NotificationService");
        } else {
            return false;
        }
    }

    private void showEnableAccessibilityDialog() {
        final ConfirmDialog dialog = new ConfirmDialog(this);
        dialog.setTitle("重要!").setMessage("您需要打开\"有红包\"的辅助功能选项才能抢微信红包")
                .setPositiveButton("打开", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(sSettingsIntent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", null);
        dialog.show();
    }
}
