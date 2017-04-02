package com.hbf.geofencedemo;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class GeofenceService extends Service implements AMapLocationListener {
    // 声明一个单次定位的客户端，获取当前位置的坐标，用于设置围栏的中心点坐标
    private AMapLocationClient onceClient = null;
    // 声明一个持续定位的客户端，用于添加地理围栏
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    // 用于接收地理围栏提醒的pendingIntent
    private PendingIntent mPendingIntent = null;
    public static final String GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofencedemo.broadcast";

    public GeofenceService() {
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        IntentFilter fliter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        fliter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver, fliter);
        Intent intent = new Intent(GEOFENCE_BROADCAST_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
                intent, 0);
        onceClient = new AMapLocationClient(getApplicationContext());
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapLocationClientOption();

        // 设置定位模式高精度，添加地理围栏最好设置成高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位监听
        locationClient.setLocationListener(this);

        AMapLocationClientOption onceOption = new AMapLocationClientOption();
        onceOption.setOnceLocation(true);
        onceClient.setLocationOption(onceOption);
        onceClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation loc) {
                if (loc != null) {
                    if (loc.getErrorCode() == 0) {
                        if (null != locationClient) {
                            float radius = 200;
                            String strRadius = "200";
                            if (!TextUtils.isEmpty(strRadius)) {
                                radius = Float.valueOf(strRadius);
                            }
                            // 添加地理围栏，
                            // 第一个参数：围栏ID,可以自定义ID,示例中为了方便只使用一个ID;第二个：纬度；第三个：精度；
                            // 第四个：半径；第五个：过期时间，单位毫秒，-1代表不过期；第六个：接收触发消息的PendingIntent
                            locationClient.addGeoFenceAlert("fenceId",
                                    loc.getLatitude(), loc.getLongitude(),
                                    radius, -1, mPendingIntent);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "获取当前位置失败!",
                                Toast.LENGTH_SHORT).show();

                        Message msg = mHandler.obtainMessage();
                        msg.obj = loc;
                        msg.what = -1;
                        mHandler.sendMessage(msg);
                    }
                }
            }
        });

        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent("com.hbf.geofencedemo.service");
        this.startService(intent);

        super.onDestroy();
    }

    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收广播
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                Bundle bundle = intent.getExtras();
                // 根据广播的event来确定是在区域内还是在区域外
                int status = bundle.getInt("event");
                String geoFenceId = bundle.getString("fenceid");
                if (status == 1) {
                    // 进入围栏区域
                    // 可以自定义提醒方式,示例中使用的是文字方式
                    mHandler.sendEmptyMessage(1);
                } else if (status == 2) {
                    // 离开围栏区域
                    // 可以自定义提醒方式,示例中使用的是文字方式
                    mHandler.sendEmptyMessage(2);
                }
            }
        }
    };

    Handler mHandler = new Handler() {
        public void dispatchMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    ((GeofenceApplication)getApplication()).record(SystemClock.elapsedRealtime(), "进入围栏区域");
                    Toast.makeText(getApplicationContext(), "进入围栏区域", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    ((GeofenceApplication)getApplication()).record(SystemClock.elapsedRealtime(), "离开围栏区域");
                    Toast.makeText(getApplicationContext(), "离开围栏区域", Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    // 获取当前位置失败
                    AMapLocation loc = (AMapLocation) msg.obj;
                    Toast.makeText(getApplicationContext(), Utils.getLocationStr(loc), Toast.LENGTH_SHORT).show();
                    //btFence.setText(getResources().getString(R.string.addFence));
                    break;
                default:
                    break;
            }
        };
    };

    public class GeofenceBinder extends Binder{
        public void addGeofence(){
            // 启动单次定位，获取当前位置
            onceClient.startLocation();

            // 设置定位参数
            locationClient.setLocationOption(locationOption);
            // 启动定位,地理围栏依赖于持续定位
            locationClient.startLocation();
        }

        public void removeGeofence(){
            // 移除围栏
            locationClient.removeGeoFenceAlert(mPendingIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return  new GeofenceBinder();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

    }
}
