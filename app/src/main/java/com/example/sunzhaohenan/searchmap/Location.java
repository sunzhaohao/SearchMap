package com.example.sunzhaohenan.searchmap;

import android.content.Intent;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;


public class Location extends AppCompatActivity implements SensorEventListener,BaiduMap.OnMapClickListener{

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();

    private LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;

    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;
    private SensorManager mSensorManager;

    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;

    MapView mMapView;
    BaiduMap mBaiduMap;
    PoiSearch mPoiSearch;

    // UI相关
    OnCheckedChangeListener radioButtonListener;
    Button requestLocButton;
    Button searchButton;
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    private float direction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        requestLocButton = (Button) findViewById(R.id.button1);
        //获取传感器管理服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mCurrentMode = LocationMode.NORMAL;
        requestLocButton.setText("普通");
        OnClickListener btnClickListener = new OnClickListener() {
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("跟随");
                        mCurrentMode = LocationMode.FOLLOWING;

                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        break;
                    case COMPASS:
                        requestLocButton.setText("普通");
                        mCurrentMode = LocationMode.NORMAL;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        MapStatus.Builder builder1 = new MapStatus.Builder();
                        builder1.overlook(0);
                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
                        break;
                    case FOLLOWING:
                        requestLocButton.setText("罗盘");
                        mCurrentMode = LocationMode.COMPASS;
                        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                                mCurrentMode, true, mCurrentMarker));
                        break;
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);

        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
        radioButtonListener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.defaulticon) {
                    // 传入null则，恢复默认图标
                    mCurrentMarker = null;
                    mBaiduMap
                            .setMyLocationConfigeration(new MyLocationConfiguration(
                                    mCurrentMode, true, null));
                }
                if (checkedId == R.id.customicon) {
                    // 修改为自定义marker
                    mCurrentMarker = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_geo);
                    mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                            mCurrentMode, true, mCurrentMarker,
                            accuracyCircleFillColor, accuracyCircleStrokeColor));
                }
            }
        };
        group.setOnCheckedChangeListener(radioButtonListener);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        mLocClient.setLocOption(option);
        mLocClient.start();

        //mBaiduMap.setOnMapClickListener(this);

        initPoiSearch();
    }

    private void initPoiSearch(){
        mPoiSearch=PoiSearch.newInstance();
        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){

            public void onGetPoiResult(PoiResult result){
                //获取POI检索结果
                Toast.makeText(Location.this,"result:"+result.getAllAddr(),
                        Toast.LENGTH_SHORT).show();
            }

            public void onGetPoiDetailResult(PoiDetailResult result){
                //获取Place详情页检索结果
                Toast.makeText(Location.this,"detail result:"+result.address,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                Toast.makeText(Location.this,"okdf",
                        Toast.LENGTH_SHORT).show();

            }
        };

        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);

        searchButton=(Button)findViewById(R.id.searchPoi_button);
        OnClickListener clickListener=new OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLocationData locData=mBaiduMap.getLocationData();
                Intent intent=new Intent();
                intent.setClass(Location.this,PoiSearchDemo.class);


                intent.putExtra(getString(R.string.locLon),locData.longitude);
                intent.putExtra(getString(R.string.locLat),locData.latitude);
                intent.putExtra(getString(R.string.locDir),locData.direction);
                intent.putExtra(getString(R.string.locAcc),locData.accuracy);
                startActivity(intent);
            }
        };

        searchButton.setOnClickListener(clickListener);

    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);

        }
        lastX = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Toast.makeText(Location.this,"经度："+latLng.longitude+",纬度："+latLng.latitude,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        Toast.makeText(Location.this,"Poi物体："+mapPoi.getName()+", Uid:"+mapPoi.getUid(),
                Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {


        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();

            mBaiduMap.setMyLocationData(locData);

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;

        mPoiSearch.destroy();
        super.onDestroy();
    }
}
