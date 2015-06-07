package org.youth.overlook.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.cloud.model.AMapCloudException;
import com.amap.api.cloud.model.CloudItem;
import com.amap.api.cloud.model.CloudItemDetail;
import com.amap.api.cloud.search.CloudResult;
import com.amap.api.cloud.search.CloudSearch;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.youth.overlook.R;
import org.youth.overlook.activity.MainActivity;
import org.youth.overlook.bean.PoiOverlay;
import org.youth.overlook.utils.HttpUtil;
import org.youth.overlook.utils.InitUtil;
import org.youth.overlook.utils.PreferenceUtil;
import org.youth.overlook.utils.SQLUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

public class MapFragment extends Fragment implements LocationSource, AMapLocationListener,
        CloudSearch.OnCloudSearchListener, AMap.OnMapClickListener, AMap.InfoWindowAdapter
        , AMap.OnInfoWindowClickListener, AMap.OnMarkerClickListener, AMap.OnMapLongClickListener, GeocodeSearch.OnGeocodeSearchListener {
    private final String ENCODE = "utf-8";

    private MainActivity myActivity;
    private MenuFragment myMenuFragment;
    private MapFragment myMapFragment;
    private MapView mapView;
    public AMap aMap;
    private OnLocationChangedListener locationChangedListener;
    private LocationManagerProxy mAMapLocationManager;
    private GeocodeSearch geocoderSearch;

    public String yuntuKey = "69321fe7c216c9390df5d264bc13cb6e";
    private String searchKey = "b9fe014c800e7cfe7e130df31a5ac04c";
    public String tableId = "552e0ab5e4b00684f0d987ae";
    private String cityName;
    private CloudSearch cloudSearch;
    private CloudSearch.Query myQuery;

    private AMapLocation aMapLocation;
    private ProgressDialog progDialog;
    private PoiOverlay mPoiCloudOverlay;
    private List<CloudItem> mCloudItems;
    private MarkerOptions mMarerOptions;
    private MarkerOptions mMarerOptions2;
    private Marker myMarker;
    private Marker destinationMarker;
    private String TAG = "jdbc";
    private LatLng latLng;

    private PreferenceUtil preferenceUtil;

    public String actionname;
    public String actionid;
    public String phoneNumber;
    public String locationid;
    private String destinationid;
    private String didjoin;

    private Handler handler = new Handler();

    private CloudItemDetail item;
    private ArrayList<CloudItem> items = new ArrayList<CloudItem>();

    public ActionBarTitleChangeListener actionBarTitleChangeListener;
    private SQLUtil sqlUtil = new SQLUtil();
    private boolean didQuery;

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
        refreshMap();//刷新Map
        return rootView;
    }


    /**
     * 数值初始化工作
     */
    public void init() {
        cloudSearch = new CloudSearch(myActivity);
        //设置查询监听
        cloudSearch.setOnCloudSearchListener(myMapFragment);

        cityName = "";
//        TODO:退出活动后把preference中的actionid置为空
        phoneNumber = preferenceUtil.getValue("phonenumber");
        actionid = preferenceUtil.getValue("actionid");//没有任何活动的时候是null
        actionname = preferenceUtil.getValue("actionname");//没有任何活动的时候是null

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
            aMap.setOnMapClickListener(this);
            aMap.setOnMarkerClickListener(this);
            aMap.setInfoWindowAdapter(this);
            aMap.setOnInfoWindowClickListener(this);
            aMap.setOnMapLongClickListener(this);
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
        locationChangedListener = onLocationChangedListener;
        if (mAMapLocationManager != null) {
            deactivate();
        }
        if (mAMapLocationManager == null) {
            mAMapLocationManager = LocationManagerProxy.getInstance(this.getActivity());
            //立刻定位一次
            mAMapLocationManager.requestLocationData(
                    LocationProviderProxy.AMapNetwork, 300000, 10, this);
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
        Log.d("jdbc", "定位一次");
        aMap.clear();
        //刷新actionlist
        myActivity.refreshActionList();
        //显示定位箭头
        this.aMapLocation = aMapLocation;
        if (locationChangedListener != null && aMapLocation != null) {
            locationChangedListener.onLocationChanged(aMapLocation);
        }
        mMarerOptions = new MarkerOptions().anchor(0.5f, 0.5f)
                .position(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()))
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.location_marker)));
        aMap.addMarker(mMarerOptions);
        dismissProgressDialog();
        Log.d("jdbc", "didquery   " + didQuery);
        if (didQuery) {
            if (actionid != null) {
                String desc = "";
                Bundle addressInfo = aMapLocation.getExtras();
                if (addressInfo != null) {
                    desc = addressInfo.getString("desc");
                }
                Log.d("jdbc", "AMapLocationListener:  " + desc);
                cityName = aMapLocation.getCity();

                new AsyncTask<String, Integer, String>() {

                    @Override
                    protected String doInBackground(String... params) {
                        new InitUtil(myActivity, actionid);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        didQuery = true;
                        locationid = preferenceUtil.getValue("locationid");
                        destinationid = preferenceUtil.getValue("destinationid");
                        if (locationid == null || locationid.isEmpty()) {
                            Log.d("jdbc", "即将插入云图");
                            JSONObject data = new JSONObject();
                            try {
                                //userinfo表添加一个nickname字段，写入云图的_name字段
//                        data.put("_name","");
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
                            insertLocation(params, false);

                        } else {
                            Log.d("jdbc", "即将更新云图");
                    /*locationid不为空时更新自己位置*/
                            JSONObject data = new JSONObject();
                            try {
                                data.put("_id", locationid);
                                data.put("_location", aMapLocation.getLongitude() + "," + aMapLocation.getLatitude());
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
                }.execute();

            }
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onLocationChanged(aMapLocation);
                }
            }, 2000);
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
        deactivate();
    }

    /**
     * 异步创建一条位置数据
     *
     * @param params
     */
    private void insertLocation(final Map<String, String> params, final boolean flag) {
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
                        if (!flag) {
                            locationid = jsonObject.getString("_id");
                            preferenceUtil.putValues("locationid", locationid);
                            Log.d("jdbc", "成功插入数据，取得locationid");
                        } else {
                            destinationid = jsonObject.getString("_id");
                            preferenceUtil.putValues("destinationid", destinationid);
                            Log.d("jdbc", "成功插入数据，取得destinationid");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {

                new AsyncTask<String, Integer, String>() {
                    @Override
                    protected String doInBackground(String... params) {
                        if (!flag) {
                            //在useraction表中设置locationid字段
                            try {
                                String sql = "update useraction set locationid=? where actionid=? and phonenumber=?";
                                List<Object> params1 = new ArrayList<Object>();
                                params1.add(locationid);
                                params1.add(actionid);
                                params1.add(phoneNumber);
                                sqlUtil.updateByPrepareStatment(sql, params1);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else {
                            //actioninfo表中设置destinationid字段
                            try {
                                String sql = "update actioninfo set destinationid=? where actionid=?";
                                List<Object> params1 = new ArrayList<Object>();
                                params1.add(destinationid);
                                params1.add(actionid);
                                sqlUtil.updateByPrepareStatment(sql, params1);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                        }
                        return null;
                    }
                }.execute();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //插入之后查询云图
                        Toast.makeText(myActivity, "位置标记完成", Toast.LENGTH_SHORT).show();
                        queryMembers();
                    }
                }, 3000);
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
                //更新之后查询一次云图
                queryMembers();
                Toast.makeText(myActivity, "位置更新完成", Toast.LENGTH_SHORT).show();
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

            @Override
            protected void onPostExecute(String s) {
                Toast.makeText(myActivity, "位置删除完成", Toast.LENGTH_SHORT).show();
                queryMembers();
            }
        }.execute();

    }

    /**
     * 查询所有活动成员位置
     */
    private void queryMembers() {
        try {
            showProgressDialog("Christina已就绪\r\n搜寻对象...");
            myQuery = new CloudSearch.Query(tableId, "", new CloudSearch.SearchBound(cityName));
            myQuery.setPageSize(30);
            if (actionid != null) {
                myQuery.addFilterString("actionId", actionid);
            }
//            new AsyncTask<String, Integer, String>() {
//
//                @Override
//                protected String doInBackground(String... params) {
                    cloudSearch.searchCloudAsyn(myQuery);
//                    return null;
//                }
//
//                @Override
//                protected void onPostExecute(String s) {
                    dismissProgressDialog();
//                }
//            }.execute();
        } catch (AMapCloudException e) {
            e.printStackTrace();
        }
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
                Log.d("flag", "clear");
                aMap.clear();
                CloudItem cloudItem1 = null;
                CloudItem cloudItem2 = null;
                com.amap.api.cloud.model.LatLonPoint latLonPoint;
                for (CloudItem item : mCloudItems) {
                    latLonPoint = item.getLatLonPoint();
                    if (item.getTitle().equals(phoneNumber)) {
                        Log.d("flag", "显示arrow");
                        cloudItem1 = item;
                        mMarerOptions = new MarkerOptions().anchor(0.5f, 0.5f)
                                .position(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude()))
                                .draggable(false)
                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.location_marker)));
                    } else if (item.getTitle().equals("集结点")) {
                        Log.d("flag", "显示flag");
                        cloudItem2 = item;
                        mMarerOptions2 = new MarkerOptions().anchor(0f, 1.0f)
                                .position(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude()))
                                .draggable(false)
                                .title(item.getTitle())
                                .snippet(item.getSnippet())
                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.destination_marker)));
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
                if (cloudItem1 != null) {
                    mCloudItems.remove(cloudItem1);
                    aMap.addMarker(mMarerOptions);
                }
                if (cloudItem2 != null) {
                    mCloudItems.remove(cloudItem2);
                    aMap.addMarker(mMarerOptions2);
                }
                mPoiCloudOverlay = new PoiOverlay(aMap, mCloudItems);
                mPoiCloudOverlay.removeFromMap();
                mPoiCloudOverlay.addToMap();
            } else {
                Log.d("jdbc", "没有搜索到相关数据");
            }
            dismissProgressDialog();
        } else {
            Log.d("jdbc", "搜索失败");
        }
    }

    @Override
    public void onCloudItemDetailSearched(CloudItemDetail cloudItemDetail, int i) {

    }

    public void refreshMap() {
        showProgressDialog("Christina准备中...");
        didQuery = false;
        preferenceUtil.putValues("locationid", null);
        preferenceUtil.putValues("destinationid", null);
        deactivate();
        init();
        /*查询用户是否已同意加入活动，若未同意，弹出对话框询问是否加入,并根据选择修改数据库*/
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... param) {
                SQLUtil sqlUtil = new SQLUtil();
                String sql = null;
                List<Object> params;
                actionid = preferenceUtil.getValue("actionid");
                if (actionid != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showProgressDialog("Christina已就绪\r\n任务确认中...");
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
                Log.d("jdbc", "didjoin---->" + didjoin);
                Log.d("jdbc", "actionid---->" + actionid);
                if (actionid != null) {
                    searchId();
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
                    didQuery = true;
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
    public void showProgressDialog(String message) {
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
    public void dismissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 点击地图时隐藏InfoWindow
     *
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        if (myMarker != null) {
            myMarker.hideInfoWindow();
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * 定制InfoWindow内容窗口
     * getInfoWindow(Marker)返回null 时才会被调用
     *
     * @param marker
     * @return
     */
    @Override
    public View getInfoContents(Marker marker) {
        View infoContent = getActivity().getLayoutInflater().inflate(
                R.layout.custom_infowindow, null);
        String title = marker.getTitle();
        TextView tvTitle = ((TextView) infoContent.findViewById(R.id.infowindow_title));
        if (title != null) {
            SpannableString titleText = new SpannableString(title);
            tvTitle.setText(titleText);
        } else {
            tvTitle.setText("");
        }
        String snippet = marker.getSnippet();
        TextView tvSnippet = ((TextView) infoContent.findViewById(R.id.infowindow_snippet));
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
            tvSnippet.setText(snippetText);
        } else {
            tvSnippet.setText("");
        }
        return infoContent;
    }

    /**
     * 点击Marker时保存Marker对象
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        this.myMarker = marker;
        return true;
    }

    /**
     * infowindow点击事件
     * 拨打snippet中的号码
     *
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        final String phoneNumber = marker.getTitle();
        new AlertDialog.Builder(myActivity)
                .setMessage("即将拨打：" + phoneNumber)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.CALL");
                        intent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(intent);
                    }
                })
                .show();
    }

    /**
     * 长按地图确定一个destination
     *
     * @param latLng
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d("jdbc", "长点击");
        if (preferenceUtil.getValue("actionid") != null) {
            showProgressDialog("Christina逆编码转换中...");
            this.latLng = latLng;
            geocoderSearch = new GeocodeSearch(myActivity);
            geocoderSearch.setOnGeocodeSearchListener(myMapFragment);
            // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
            LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);
        }
    }

    /**
     * 逆地理编码
     *
     * @param regeocodeResult
     * @param i
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        dismissProgressDialog();
        if (i == 0) {
            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
                    && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                final String addressName = regeocodeResult.getRegeocodeAddress().getFormatAddress()
                        + "附近";
                new AlertDialog.Builder(myActivity)
                        .setMessage("设定" + addressName + "为集结点？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                new AsyncTask<String, Integer, String>() {
                                    @Override
                                    protected String doInBackground(String... params) {
                                        String sql = "select builder from actioninfo where actionid=?";
                                        List<Object> param = new ArrayList<Object>();
                                        param.add(preferenceUtil.getValue("actionid"));
                                        List<Map<String, Object>> list = null;
                                        try {
                                            list = sqlUtil.queryResults(sql, param);
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                        String builder = String.valueOf(list.get(0).get("builder"));
                                        return builder;
                                    }

                                    @Override
                                    protected void onPostExecute(String builder) {
                                        if (!builder.equals(phoneNumber)) {
                                            new AlertDialog.Builder(myActivity)
                                                    .setMessage("对不起，只有创建者可以设定集结点")
                                                    .setNegativeButton("确定", null)
                                                    .show();
                                        } else {
                                            JSONObject data = new JSONObject();
                                            if (destinationid == null || destinationid.isEmpty()) {
                                                //destinationid为null则把集结点插入云图，插入后会查询一次
                                                try {
                                                    //userinfo表添加一个nickname字段，写入云图的_name字段
                                                    //data.put("_name","");
                                                    data.put("_name", "集结点");
                                                    data.put("_address", addressName);
                                                    data.put("_location", latLng.longitude + "," + latLng.latitude);
                                                    data.put("actionId", actionid);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                Map<String, String> params = new HashMap<String, String>();
                                                params.put("key", yuntuKey);
                                                params.put("tableid", tableId);
                                                params.put("data", data.toString());
                                                insertLocation(params, true);
                                            } else {
                                                //destinationid不为空则更新
                                                try {
                                                    data.put("_id", destinationid);
                                                    data.put("_address", addressName);
                                                    data.put("_location", latLng.longitude + "," + latLng.latitude);
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
                                }.execute();
                            }
                        })
                        .show();
            } else {
                Toast.makeText(myActivity, "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
            }
        } else if (i == 27) {
            Toast.makeText(myActivity, "搜索失败,请检查网络连接！", Toast.LENGTH_SHORT).show();
        } else if (i == 32) {
            Toast.makeText(myActivity, "key验证无效！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(myActivity, "未知错误，请稍后重试!错误码为" + i, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    /**
     * 查询destinationid 和 locationid
     */
    public void searchId() {
        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... params) {
                new InitUtil(myActivity, actionid);
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                didQuery = true;
                locationid = preferenceUtil.getValue("locationid");
                destinationid = preferenceUtil.getValue("destinationid");
            }
        }.execute();
    }

    /**
     * 导航栏标题修改监听器
     */
    public interface ActionBarTitleChangeListener {
        public void onChangeTitle(String title);
    }
}