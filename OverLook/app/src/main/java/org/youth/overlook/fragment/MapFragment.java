package org.youth.overlook.fragment;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amap.api.cloud.model.AMapCloudException;
import com.amap.api.cloud.model.CloudItemDetail;
import com.amap.api.cloud.search.CloudResult;
import com.amap.api.cloud.search.CloudSearch;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;

import org.youth.overlook.R;
import org.youth.overlook.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements LocationSource, AMapLocationListener,
        CloudSearch.OnCloudSearchListener, AMap.OnMapClickListener {
    private Context myActivity;

    private MapFragment myFragment;
    private MapView mapView;
    private AMap aMap;
    private OnLocationChangedListener locationChangedListener;
    private LocationManagerProxy mAMapLocationManager;

    private String storeKey = "69321fe7c216c9390df5d264bc13cb6e";
    private String searchKey = "b9fe014c800e7cfe7e130df31a5ac04c";
    private String tableId = "552e0ab5e4b00684f0d987ae";
    private String queryString = "";
    private String cityName = "";
    private CloudSearch cloudSearch;
    private CloudSearch.Query myQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myFragment = this;
        myActivity = getActivity();

        cloudSearch = new CloudSearch(myActivity);
        //设置查询监听
        cloudSearch.setOnCloudSearchListener(myFragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, null);
        mapView = (MapView) rootView.findViewById(R.id.map);
        Log.d("jdbc", "mapview获取完成");
        mapView.onCreate(savedInstanceState);
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 定位按钮的回调方法
     *
     * @param onLocationChangedListener
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationChangedListener = onLocationChangedListener;
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this.getActivity());
            mAMapLocationManager.requestLocationData(
                    LocationProviderProxy.AMapNetwork, 60000, 10, this);
        }


    }

    @Override
    public void deactivate() {
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destroy();
        }
        mAMapLocationManager = null;
    }

    /**
     * 定位成功后
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        String desc = "";
        Bundle addressInfo = aMapLocation.getExtras();
        if (addressInfo != null) {
            desc = addressInfo.getString("desc");
        }
        Log.d("jdbc", "AMapLocationListener:  " + desc);
        if (locationChangedListener != null && aMapLocation != null) {
            locationChangedListener.onLocationChanged(aMapLocation);
        }

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        initAMap();
        Log.d("jdbc", "Resume:aMap初始化");
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onCloudSearched(CloudResult cloudResult, int i) {

    }

    @Override
    public void onCloudItemDetailSearched(CloudItemDetail cloudItemDetail, int i) {

    }

    /**
     * 初始化地图
     */
    private void initAMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setLocationSource(myFragment);

            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.location_marker));
            myLocationStyle.strokeWidth(0);
            aMap.setMyLocationStyle(myLocationStyle);

            aMap.getUiSettings().setMyLocationButtonEnabled(true);
            aMap.setMyLocationEnabled(true);
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

            aMap.setOnMapClickListener(this);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("jdbc", latLng.toString());
    }

    /**
     * 创建一条位置数据
     * @param params
     */
    private void insertLocation(Map<String, String> params){
        String path = "http://yuntuapi.amap.com/datamanage/data/create";
        HttpUtil.sendHttpClientPost(path, params, "utf-8");
    }

    /**
     * 更新位置信息
     * @param params
     */
    public void updateLocation(Map<String, String> params) {
        String path = "http://yuntuapi.amap.com/datamanage/data/update";
        HttpUtil.sendHttpClientPost(path, params, "uft-8");
    Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();
    }


    /**
     * 根据id删除位置信息，多个id间用半角逗号隔开
     * @param params
     */
    public void deleteLocation(Map<String, String> params) {
        String path = "http://yuntuapi.amap.com/datamanage/data/delete";
        HttpUtil.sendHttpClientPost(path, params, "uft-8");
    }

    /**
     * 查询所有活动成员位置
     */
    private void queryAllMembers() {
        try {
            myQuery = new CloudSearch.Query(tableId, queryString, new CloudSearch.SearchBound(cityName));
            myQuery.setPageSize(30);
            cloudSearch.searchCloudAsyn(myQuery);
        } catch (AMapCloudException e) {
            e.printStackTrace();
        }
    }
}
