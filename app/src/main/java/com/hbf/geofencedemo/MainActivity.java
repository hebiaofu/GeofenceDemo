package com.hbf.geofencedemo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

/**
 * 地理围栏功能演示
 *
 * @创建时间： 2015年11月24日 下午5:49:52
 * @项目名称： AMapLocationDemo2.x
 * @author hongming.wang
 * @文件名称: GeoFence_Activity.java
 * @类型名称: GeoFence_Activity
 */
public class MainActivity extends CheckPermissionsActivity implements View.OnClickListener, ServiceConnection {
    private EditText etRadius;
    private TextView tvReult;
    private CheckBox cbAlertIn;
    private CheckBox cbAlertOut;
    private Button btFence;
    private Button btRecords;
    private GeofenceService.GeofenceBinder mBinder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title_geoFenceAlert);

        etRadius = (EditText) findViewById(R.id.et_radius);
        cbAlertIn = (CheckBox) findViewById(R.id.cb_alertIn);
        cbAlertOut = (CheckBox) findViewById(R.id.cb_alertOut);
        tvReult = (TextView) findViewById(R.id.tv_result);
        btFence = (Button) findViewById(R.id.bt_fence);
        btRecords = (Button) findViewById(R.id.bt_records);

        btFence.setOnClickListener(this);
        btRecords.setOnClickListener(this);

        Intent intent = new Intent(this, GeofenceService.class);
        startService(intent);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_fence) {
            if (btFence.getText().equals(
                    getResources().getString(R.string.addFence))) {
                btFence.setText(getResources().getString(R.string.removeFence));

                // 启动单次定位，获取当前位置
                // 设置定位参数
                // 启动定位,地理围栏依赖于持续定位
                mBinder.addGeofence();
            } else {

                btFence.setText(getResources().getString(R.string.addFence));
                // 移除围栏
                mBinder.removeGeofence();
            }
        }else if (v.getId() == R.id.bt_records){
            StringBuffer buffer = new StringBuffer();

            int num = ((GeofenceApplication)getApplication()).getRecordNum();
            for(int i = 0; i < num; i++){
                buffer.append(((GeofenceApplication)getApplication()).getRecord(i));
                buffer.append("\n");
            }

            tvReult.setText(buffer);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBinder = (GeofenceService.GeofenceBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
