package com.hbf.geofencedemo;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/10/22.
 */
public class GeofenceApplication extends Application{
    private final static String GEOFENCE = "geofence";
    private final static String NUM = "number";
    private int num = -1;

    public void record(long time, String label){
        num = getRecordNum();

        SharedPreferences sp = getSharedPreferences(GEOFENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(String.valueOf(num), label+":"+Long.toString(time));
        editor.commit();

        updateRecordNum(++num);
    }

    public void updateRecordNum(int num){
        SharedPreferences sp = getSharedPreferences(GEOFENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(NUM, num);
        editor.commit();
    }

    public String getRecord(int index){
        SharedPreferences sp = getSharedPreferences(GEOFENCE, MODE_PRIVATE);

        String str = "";
        if(sp != null) {
            str = sp.getString(String.valueOf(index), "");
        }

        return str;
    }

    public int getRecordNum(){
        SharedPreferences sp = getSharedPreferences(GEOFENCE, MODE_PRIVATE);

        int num = 0;
        if(sp != null) {
            num = sp.getInt(NUM, 0);
        }

        return num;
    }
}
