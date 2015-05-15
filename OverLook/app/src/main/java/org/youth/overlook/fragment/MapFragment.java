package org.youth.overlook.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.cloud.model.AMapCloudException;
import com.amap.api.cloud.model.CloudItem;
import com.amap.api.cloud.model.CloudItemDetail;
import com.amap.api.cloud.model.LatLonPoint;
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
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

import org.json.JSONException;
import org.json.JSONObject;
import org.youth.overlook.R;
import org.youth.overlook.activity.MainActivity;
import org.youth.overlook.bean.PoiOverlay;
import org.youth.overlook.utils.HttpUtil;
import org.youth.overlook.utils.PreferenceUtil;
import org.youth.overlook.utils.SQLUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.os.Handler;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements LocationSource, AMapLocationListener,
        CloudSearch.OnCloudSearchListener, AMap.OnInfoWindowClickListener {
    private final String ENCODE = "utf-8";

    private MainActivity myActivity;
    private MenuFragment myMenuFragment;
    private MapFragment myMapFragment;
    private MapView mapView;
    private AMap aMap;
    private OnLocationChangedListener locationChangedListener;
    private LocationManagerProxy mAMapLocationManager;

    private String yuntuKey = "69321fe7c216c9390df5d264bc13cb6e";
    private String searchKey = "b9fe014c800e7cfe7e130df31a5ac04c";
    private String tableId = "552e0ab5e4b00684f0d987ae";
    //    private String tableId = "552f2495e4b00684f0da821c";
    private String cityName;
    private CloudSearch cloudSearch;
    private CloudSearch.Query myQuery;

    private AMapLocation aMapLocation;
    private ProgressDialog progDialog;
    private PoiOverlay mPoiCloudOverlay;
    private List<CloudItem> mCloudItems;
    private MarkerOptions mMarerOptions;
    private String TAG = "jdbc";

    private PreferenceUtil preferenceUtil;

    public String actionname;
    public String actionid;
    public String phoneNumber;
    private String locationid;
    private String didjoin;

    private Handler handler = new Handler();

    public Map<String, Boolean> didQuery = new HashMap<String, Boolean>();

    private CloudItemDetail item;
    private ArrayList<CloudItem> items = new ArrayList<CloudItem>();

    public ActionBarTitleChangeListener actionBarTitleChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myMapFragment = this;
        myActivity = (MainActivity) getActivity();
        myMenuFragment = myActivity.myMenuFragment;

        preferenceUtil = new PreferenceUtil(myActivity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, null);
        mapView = (MapView) rootView.findViewById(R.id.map);

        mapView.onCreate(savedInstanceState);

        return rootView;
    }


    /**
     * 数值初始化工作
     */
    public void init() {
        cloudSearch = new CloudSearch(myActivity);
        //设置查询监听
        cloudSearch.setOnCloudSearchListener(myMapFragment);

        locationid = null;
        cityName = "";
//        TODO:退出活动后把preference中的actionid置为空
        phoneNumber = preferenceUtil.getValue("phoneNumber");
        actionid = preferenceUtil.getValue("actionid");//没有任何活动的时候是null
        actionname = preferenceUtil.getValue("actionname");//没有任何活动的时候是null

        if (actionid != null) {
            didQuery.put(actionid, false);
        }
        if (actionname != null) {
            actionBarTitleChangeListener.onChangeTitle(actionname);
        } else {
            actionBarTitleChangeListener.onChangeTitle("overlook");
        }
    }

    /**
     * 初始化地图
     */
    private void initAMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setLocationSource(myMapFragment);

            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                    .fromResource(R.drawable.location_marker));
            myLocationStyle.strokeWidth(0);
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.getUiSettings().setMyLocationButtonEnabled(true);
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
            aMap.setOnInfoWindowClickListener(this);
        }
        aMap.setMyLocationEnabled(true);//显示定位层并触发一次定位
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 定位按钮的回调方法
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        Log.d("testlocation", "activate");
        locationChangedListener = onLocationChangedListener;
        if (mAMapLocationManager != null) {
            deactivate();
        }
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this.getActivity());
            //立刻定位一次
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
    public void onLocationChanged(final AMapLocation aMapLocation) {
        //显示定位箭头
        this.aMapLocation = aMapLocation;
        if (locationChangedListener != null && aMapLocation != null) {
            locationChangedListener.onLocationChanged(aMapLocation);
        }
        if (actionid != null) {
            String desc = "";
            Bundle addressInfo = aMapLocation.getExtras();
            if (addressInfo != null) {
                desc = addressInfo.getString("desc");
            }
            Log.d("jdbc", "AMapLocationListener:  " + desc);

            Log.d("jdbc", "cityName--->" + cityName);
            if (cityName.length() == 0) {
                cityName = aMapLocation.getCity();
                queryLocationid();
            } else {
                cityName = aMapLocation.getCity();

                if (locationid == null) {
                    Log.d("jdbc", "即将插入云图");
                    JSONObject data = new JSONObject();
                    try {
                        data.put("_name", phoneNumber);
                        data.put("_location", aMapLocation.getLongitude() + "," + aMapLocation.getLatitude());
                        data.put("actionId", actionid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("key", yuntuKey);
                    params.put("tableid", tableId);
                    params.put("data", data.toString());
                    insertLocation(params);
                } else {
                    Log.d("jdbc", "即将更新云图");
                    /*locationid不为空时更新自己位置*/
                    JSONObject data = new JSONObject();
                    try {
                        data.put("_id", locationid);
                        data.put("_location", aMapLocation.getLongitude() + "," + aMapLocation.getLatitude());
                        data.put("phoneNumber", phoneNumber);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("key", yuntuKey);
                    params.put("tableid", tableId);
                    params.put("data", data.toString());
                    updateLocation(params);
                }
            }
        }
        dismissProgressDialog();
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
        refreshMap();//刷新Map
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
        didQuery.put(actionid, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 异步创建一条位置数据
     *
     * @param params
     */
    private void insertLocation(final Map<String, String> params) {
        Log.d("jdbc", "进入插入方法");

        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... param) {
                String path = "http://yuntuapi.amap.com/datamanage/data/create";
                String result = HttpUtil.sendHttpClientPost(path, params, ENCODE);
                Log.d("jdbc", "insertLocation------->" + result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.getString("status").equals("1")) {
                        locationid = jsonObject.getString("_id");
                        Log.d("jdbc", "成功插入数据，取得locationid");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                //插入之后查询云图
                queryAllMembers();
            }
        }.execute();
    }

    /**
     * 异步更新位置信息
     *
     * @param params
     */
    public void updateLocation(final Map<String, String> params) {
        Log.d("jdbc", "进入更新方法");
        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... param) {
                String path = "http://yuntuapi.amap.com/datamanage/data/update";
                String result = HttpUtil.sendHttpClientPost(path, params, ENCODE);
                Log.d("jdbc", "updateLocation------->" + result);
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                queryAllMembers();
            }
        }.execute();

    }


    /**
     * 根据id异步删除位置信息，多个id间用半角逗号隔开
     *
     * @param params
     */
    public void deleteLocation(final Map<String, String> params) {
        Log.d("jdbc", "进入删除方法");
        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... param) {
                String path = "http://yuntuapi.amap.com/datamanage/data/delete";
                String result = HttpUtil.sendHttpClientPost(path, params, ENCODE);
                Log.d("jdbc", "deleteLocation------->" + result);
                return null;
            }
        }.execute();

    }

    /**
     * 查询所有活动成员位置
     */
    private void queryMembers(Map<String, String> filter) {
        try {
            showProgressDialog("卫星已就绪\r\n寻找对象...");
            myQuery = new CloudSearch.Query(tableId, "", new CloudSearch.SearchBound(cityName));
            myQuery.setPageSize(30);
            String name = filter.get("_name");
            final String actionid = filter.get("actionid");
            if (name != null) {
                myQuery.addFilterString("_name", name);
            }
            if (actionid != null) {
                myQuery.addFilterString("actionId", actionid);
            }
            new AsyncTask<String, Integer, String>() {

                @Override
                protected String doInBackground(String... params) {
                    cloudSearch.searchCloudAsyn(myQuery);
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    dismissProgressDialog();
                }
            }.execute();
        } catch (AMapCloudException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询actionid下所有成员
     */
    public void queryAllMembers(){
        if (!cityName.isEmpty()) {
            Map<String, String> filter = new HashMap<String, String>();
            filter.put("actionid", actionid);
            queryMembers(filter);
        }
    }

    /**
     * 查询locationid
     */
    public void queryLocationid(){
        Map<String, String> filter = new HashMap<String, String>();
        filter.put("actionid", actionid);
        filter.put("_name", phoneNumber);
        Log.d("jdbc", "filter------->"+ filter);
        queryMembers(filter);
    }

    /**
     * searchCloudAsyn方法的回调方法
     *
     * @param result
     * @param rCode
     */
    @Override
    public void onCloudSearched(CloudResult result, int rCode) {

        /*rCode==0表示查询成功，其他为错误码，请查询高德API*/
        if (rCode == 0) {
            mCloudItems = result.getClouds();
            if (mCloudItems != null && mCloudItems.size() != 0) {
                /*若filterString包含“_name”，说明此次查询是用于查询locationid*/
                String filterString = result.getQuery().getFilterString();
                if (filterString.contains("_name")) {
                    locationid = mCloudItems.get(0).getID();
                    Log.d("jdbc", "查询时,获得locationid");
                } else {
                    aMap.clear();
                    CloudItem cloudItem = null;
                    for (CloudItem item : mCloudItems) {
                        LatLonPoint latLonPoint = item.getLatLonPoint();
                        if(item.getTitle().equals(phoneNumber)){
                            cloudItem = item;
                            mMarerOptions = new MarkerOptions().anchor(0.5f, 0.5f)
                                    .position(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude()))
                                    .draggable(false)
                                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.location_marker)));
                        }
                        Log.d(TAG, "_name " + item.getTitle() + "_address " + item.getSnippet() + "_distance " + item.getDistance());
                        Iterator iter = item.getCustomfield().entrySet()
                                .iterator();
                        while (iter.hasNext()) {
                            Map.Entry entry = (Map.Entry) iter.next();
                            Object key = entry.getKey();
                            Object val = entry.getValue();
                            Log.d(TAG, key + "   " + val);
                        }
                    }
                    if(cloudItem!=null) {
                        mCloudItems.remove(cloudItem);
                    }
                    mPoiCloudOverlay = new PoiOverlay(aMap, mCloudItems);
                    mPoiCloudOverlay.removeFromMap();
                    mPoiCloudOverlay.addToMap();
                    aMap.addMarker(mMarerOptions);
                }
            } else {
                Log.d("jdbc", "没有搜索到相关数据");
            }
            if (!didQuery.get(actionid)) {
                didQuery.put(actionid, true);// 表示此action已成功查询一次
                initAMap();
                queryAllMembers();
            }
        } else {
            Log.d("jdbc", "搜索失败");
        }
    }

    @Override
    public void onCloudItemDetailSearched(CloudItemDetail cloudItemDetail, int i) {

    }

    public void refreshMap() {
        showProgressDialog("卫星准备中...");
        init();
        /*查询用户是否已同意加入活动，若未同意，弹出对话框询问是否加入,并根据选择修改数据库*/
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... param) {
                SQLUtil sqlUtil = new SQLUtil();
                String sql=null;
                List<Object> params;
                actionid = preferenceUtil.getValue("actionid");
                if(actionid!=null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showProgressDialog("卫星已就绪\r\n任务确认中...");
                        }
                    });
                    //查询actionid是否存在
                    sql = "select actionid from actioninfo where actionid=?";
                    params = new ArrayList<Object>();
                    params.add(actionid);
                    try {
                        //若actionid不存在，清空preference
                        if (sqlUtil.queryResults(sql, params).size() == 0) {
                            preferenceUtil.putValues("actionid", null);
                            preferenceUtil.putValues("actionname", null);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                //重新获取actionid
                actionid = preferenceUtil.getValue("actionid");
                if (actionid != null) {
                    Log.d("jdbc", "查询是否已同意加入此action");
                    sql = "select didjoin from useraction where phonenumber=? and actionid=?";
                    params = new ArrayList<Object>();
                    params.add(phoneNumber);
                    params.add(actionid);
                    try {
                        List<Map<String, Object>> list = sqlUtil.queryResults(sql, params);
                        Log.d("jdbc", params.toString() + "   " + list.toString());
                        if (list.size() != 0 && String.valueOf(list.get(0).get("didjoin")).equals("1")) {
                            didjoin = "1";
                        } else {
                            didjoin = "";
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                Log.d("jdbc", "didjoi---->" + didjoin);
                if (actionid != null) {
                    if (didjoin.equals("1")) {
                        Log.d("jdbc", "切换action后的初始化");
                        init();
                        initAMap();
                    } else {
                        final SQLUtil sqlUtil = new SQLUtil();
                        new AlertDialog.Builder(getActivity())
                                .setMessage("是否加入" + actionname + "?")
                                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    /*点击“否”按钮，异步任务删除记录*/
                                        new AsyncTask<String, Integer, String>() {

                                            @Override
                                            protected String doInBackground(String... param) {
                                                String sql = "delete from useraction where phonenumber=? and actionid=?";
                                                List<Object> params = new ArrayList<Object>();
                                                params.add(phoneNumber);
                                                params.add(actionid);
                                                Log.d("jdbc", "点击否----->" + params.toString());
                                                try {
                                                    sqlUtil.updateByPrepareStatment(sql, params);
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(String s) {
                                                preferenceUtil.putValues("actionid", null);
                                                preferenceUtil.putValues("actionname", null);
                                                deactivate();
                                                init();
                                                initAMap();
                                            }
                                        }.execute();
                                    }
                                })
                                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    /*点击“是”按钮，异步任务更新记录，修改didjoin字段为'1'*/
                                        new AsyncTask<String, Integer, String>() {

                                            @Override
                                            protected String doInBackground(String... param) {
                                                String sql = "update useraction set didjoin='1' where phonenumber=? and actionid=?";
                                                List<Object> params = new ArrayList<Object>();
                                                params.add(phoneNumber);
                                                params.add(actionid);
                                                Log.d("jdbc", "点击是----->" + params.toString());
                                                try {
                                                    sqlUtil.updateByPrepareStatment(sql, params);
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(String s) {
                                                Log.d("jdbc", "切换action后的初始化");
                                                init();
                                                initAMap();
                                            }
                                        }.execute();

                                    }
                                })
                                .show();
                    }
                } else {
                    init();
                    initAMap();
                }
                //刷新actionlist
                myActivity.refreshActionList();
            }
        }.execute();
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog(String message) {
        if (progDialog == null)
            progDialog = new ProgressDialog(getActivity());
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage(message);
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dismissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d("jdbc", "onInfoWindowClick");
        marker.hideInfoWindow();
    }

    /**
     * 导航栏标题修改监听器
     */
    public interface ActionBarTitleChangeListener {
        public void onChangeTitle(String title);
    }
}