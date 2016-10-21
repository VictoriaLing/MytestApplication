package com.lysehun.mytestapplication;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;


public class MainActivity extends Activity {
    private static final String TAG = "dzt";
    boolean isFirstLoc = true;
    private Button refresh;
    double mylatitude=22.257223;
    double mylongitude=113.542895;
    private double x,y;
    private ContentResolver mContentResolver=null;
    private Uri mUri = null;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener ;
    private String eg="where are you?";
    private MapView mMapView=null;
    BaiduMap mBaiduMap=null;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        refresh=(Button)findViewById(R.id.refresh);
        mMapView=(MapView)findViewById(R.id.bmapView);
        mBaiduMap=mMapView.getMap();
        initContentObserver();
        myListener = new MyLocationListener();
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String num="15820579377";
                //String num="13143125912";
                String msg="where are you?";
                SendMSG(num,msg);
            }
        });
        Maker();
    }
    private void initContentObserver(){
        mUri = Uri.parse("content://sms");
        mContentResolver=this.getContentResolver();
        mContentResolver.registerContentObserver(mUri, true,new SMSContentObserver(new Handler()));
    }
    private class SMSContentObserver extends ContentObserver {


        public SMSContentObserver(Handler handler) {

            super(handler);

        }
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            String[] projection = new String[] {"_id","address","body","type"};
            Cursor cursor = mContentResolver.query(mUri, projection, null, null, "date desc");
            while (cursor.moveToNext()) {
                String address  = cursor.getString(cursor.getColumnIndex("address"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String type  = cursor.getString(cursor.getColumnIndex("type"));
                String tn="1";
                if(type.equals(tn)&&body.equals(eg)){
                    mLocationClient.start();
                    if (mLocationClient != null && mLocationClient.isStarted())
                        mLocationClient.requestLocation();

                    else
                        Log.d(TAG, "locClient is null or not started");
                }
                break;
            }

        }
    }
    private void SendMSG(String num, String msg) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(
                num,
                null,
                msg,
                null,
                null
        );
        Toast.makeText(getApplicationContext(), "刷新成功，正在获取位置", Toast.LENGTH_SHORT).show();
    }
    private void initLocation(){
        mBaiduMap.setMyLocationEnabled(true);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        mLocationClient.setLocOption(option);
    }
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            x=location.getLatitude();
           y=location.getLongitude();

            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng pt = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(pt).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }
    //添加标记
    private void Maker()
    {
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        LatLng point = new LatLng(mylatitude,mylongitude);
        BitmapDescriptor mMaker = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
        OverlayOptions option = new MarkerOptions().position(point).icon(mMaker);
        mBaiduMap.addOverlay(option);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}
