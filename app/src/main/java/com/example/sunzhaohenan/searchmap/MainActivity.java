package com.example.sunzhaohenan.searchmap;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.sunzhaohenan.searchmap.overlayutil.PoiOverlay;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    private MapView mMapView = null;
    private BaiduMap baiduMap=null;
    PoiSearch mPoiSearch;

    private static int id=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView= (MapView) findViewById(R.id.baidumapView);
        baiduMap=mMapView.getMap();


        //baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);

        Button button=(Button)findViewById(R.id.buttonChange);
        final int[] mapType={baiduMap.MAP_TYPE_NORMAL,
        baiduMap.MAP_TYPE_SATELLITE};

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                id++;
                if(id>=mapType.length)
                    id=0;
                baiduMap.setMapType(mapType[id ]);
            }
        });

        Button locButton=(Button)findViewById(R.id.location_button);
        locButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                // intent.setClass(MainActivity.this,Location.class);
                intent.setClass(MainActivity.this,Location.class);

                startActivity(intent);
            }
        });

        initPoiSearch();
    }
    private void initPoiSearch(){
        mPoiSearch= PoiSearch.newInstance();

        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){

            public void onGetPoiResult(PoiResult result){
                //获取POI检索结果

                Toast.makeText(MainActivity.this,"result:"+result.error,
                        Toast.LENGTH_SHORT).show();
            }

            public void onGetPoiDetailResult(PoiDetailResult result){
                //获取Place详情页检索结果
                Toast.makeText(MainActivity.this,"detail result:"+result.address,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                Toast.makeText(MainActivity.this,"okdf",
                        Toast.LENGTH_SHORT).show();

            }
        };

        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);

        Button searchButton=(Button)findViewById(R.id.search);
        OnClickListener clickListener=new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this,PoiSearchDemo.class);

                startActivity(intent);
            }
        };

        searchButton.setOnClickListener(clickListener);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mPoiSearch.destroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }


}
