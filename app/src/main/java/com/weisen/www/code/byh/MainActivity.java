package com.weisen.www.code.byh;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.EditText;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.content.Intent;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static SharedPreferences sp;
    public static WebView webview;//webview插件
    public ValueCallback<Uri> uploadMessage;
    public ValueCallback<Uri[]> uploadMessageAboveL;
    public final static int FILE_CHOOSER_RESULT_CODE = 10000;
    public static String ip = "0.0.0.0";
    public static String address = "";
    public static String network = "";
    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    public static int REQUEST_PERMISSION_CODE = 1;
    public static String orderId = "";
    public static String ServiceVersion = "0.0.0";
    public static boolean ipSwitch = false;
    public static boolean verSwitch = false;
    public static String varApi = ":18080/distribution/weisen/getAppNewVersionByType/2";
    public static String verText = "";
    public static String verAddress = "";
    public static String update = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();//授权操作
        init();//初始化
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    //授权操作
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.allSetting){
            startSettings();
        }else if (id == R.id.gotoAddress){
            goTo("http://"+sp.getString("server", "")+"/"+sp.getString("project", "")+"/"+sp.getString("modular", ""));
        }else if (id == R.id.clearCache){
            new AlertDialog.Builder(this)
                    .setMessage("删除缓存，以及所有登陆信息？")
                    .setPositiveButton("取消", null)
                    .setNegativeButton("确定", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            webview.clearFormData();
                            webview.clearHistory();
                            webview.clearCache(true);
                            webview.clearMatches();
                            webview.clearSslPreferences();
                            CookieManager.getInstance().removeAllCookie();//删除缓存
                            webview.removeAllViews();
                            Intent intent = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            System.exit(0);
                        }
                    })
                    .show();
        }else if (id == R.id.version){
            try{
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                }else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            IPutils.GetNetIp();
                            Version.getVersion("http://"+sp.getString("server", "")+varApi);
                        }
                    }).start();
                    while (!ipSwitch || !verSwitch){
                        Thread.sleep(50);
                    }
                    ipSwitch = false;
                    verSwitch = ipSwitch;
                    Message m = new Message();
                    message("系统信息", "服务器：" + sp.getString("server", "") +
                            "\n项目名：" + sp.getString("project", "") +
                            "\n模块名：" + sp.getString("modular", "") +
                            "\n系统版本：" + m.getSystemVersionName() +
                            "\n安卓版本：" + m.getAndroidVersionName() +
                            "\n软件版本：" + m.getVersionName(this) +
                            "\n最新版本：" + ServiceVersion +
                            "\nWifi地址：" + getWifi() +
                            "\n运营商：" + network +
                            "\n公网地址：" + ip +
                            "\n所在地：" + address +
                            "\n经纬度：" + getLocation()
                    );
                }
            }catch (Exception e){
                message("提示","系统错误,请重试。");
            }
        }else if (id == R.id.updata){
            getVersion(false);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public String getWifi() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            return "没有打开";
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }
    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
    //获取定位信息
    public String getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            return null;
        }
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = new Location("");
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // 查找到服务信息
            location = getLastKnownLocation();
        }else{
            LocationListener locationListener = new LocationListener() {
                // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
                // Provider被enable时触发此函数，比如GPS被打开
                @Override
                public void onProviderEnabled(String provider) {
                }
                // Provider被disable时触发此函数，比如GPS被关闭
                @Override
                public void onProviderDisabled(String provider) {
                }
                //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                @Override
                public void onLocationChanged(Location location) {
                }
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000, 0,locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return "\n北纬N <"+location.getLatitude() + ">\n东经E <"+location.getLongitude()+">";
    }

    private Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            return null;
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getAllProviders();
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    //设置地址
    private void startSettings() {
        final EditText inputServer = new EditText(this);
        inputServer.setHint("请输入管理密码");
        new AlertDialog.Builder(this)
                .setTitle("进入高级设置")
                .setIcon(android.R.drawable.ic_search_category_default)
                .setView(inputServer)
                .setPositiveButton("取消", null)
                .setNegativeButton("确认", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (inputServer.getText().toString().equals("135498")){
                            final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog, null);
                            ((EditText) view.findViewById(R.id.serverId)).setText(sp.getString("server", ""));
                            ((EditText) view.findViewById(R.id.projectId)).setText(sp.getString("project", ""));
                            ((EditText) view.findViewById(R.id.modularId)).setText(sp.getString("modular", ""));
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("服务器设置")
                                    .setIcon(android.R.drawable.ic_search_category_default)
                                    .setView(view)
                                    .setPositiveButton("取消", null)
                                    .setNegativeButton("保存", new DialogInterface.OnClickListener(){
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            SharedPreferences sp= getSharedPreferences("settings", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString("server", ((EditText)view.findViewById(R.id.serverId)).getText().toString());
                                            editor.putString("project", ((EditText)view.findViewById(R.id.projectId)).getText().toString());
                                            editor.putString("modular", ((EditText)view.findViewById(R.id.modularId)).getText().toString());
                                            editor.commit();
                                            goTo("http://"+sp.getString("server", "")+"/"+sp.getString("project", "")+"/"+sp.getString("modular", ""));
                                        }
                                    })
                                    .show();
                        }else{
                            message("提示","密码错误");
                        }
                    }
                })
                .show();
    }

    //首次启动设置
    private void oneStartSettings(){
        final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog, null);
        SharedPreferences sp= getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("server", "upick.booymp.com");
        editor.putString("project", "#");
        editor.putString("modular", "index");
        editor.commit();
    }
    public void init() {
        sp = getSharedPreferences("settings",Context.MODE_PRIVATE);//获取设置文件
        if (sp.getString("server", "").equals(""))oneStartSettings();
        webview = findViewById(R.id.webViewId);
        webview.setWebContentsDebuggingEnabled(true);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);//支持js
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(false);//缩放
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//缓存
        webSettings.setAllowFileAccess(true);     //设置可以访问文件
        webSettings.setAllowContentAccess(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setBlockNetworkImage(false);//解决图片不显示
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webview.addJavascriptInterface(this,"weisen");//js调用的名字
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webview,true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webview.setWebChromeClient(new WebChromeClient() {
            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> valueCallback) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }
            // For Android  >= 3.0
            public void openFileChooser(ValueCallback valueCallback, String acceptType) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }
            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                openImageChooserActivity();
            }
            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    return false;
                }
                return true;
            }
        });
        webview.setOnLongClickListener(v -> {
            final WebView.HitTestResult hitTestResult = webview.getHitTestResult();
            // 如果是图片类型或者是带有图片链接的类型
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(this)
                        .setMessage("没有文件读写权限，无法保存文件，请先授权后在进行保存。")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                            }
                        })
                        .show();
            }else {
                if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                        hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                    // 弹出保存图片的对话框
                    new AlertDialog.Builder(webview.getContext())
                            .setItems(new String[]{"保存图像到本地"}, (dialog, which) -> {
                                String pic = hitTestResult.getExtra();//获取图片
                                if (which == 0) new Thread(() -> saveImage(pic)).start();
                            })
                            .show();
                    return true;
                }
            }
            return false;//保持长按可以复制文字
        });
        goTo("http://"+sp.getString("server", "")+"/"+sp.getString("project", "")+"/"+sp.getString("modular", ""));
        getVersion(true);
    }

    public void getVersion(boolean one){
        try {
            new Thread(new Runnable(){
                @Override
                public void run() {
                    Version.getVersion("http://"+sp.getString("server", "")+varApi);
                }
            }).start();
            while (!verSwitch){
                Thread.sleep(50);
            }
            verSwitch = false;
            //检测版本操作
            String localVer = new Message().getVersionName(this);
            if (compareVersion(localVer,ServiceVersion) < 0){
                if (update.equals("1")) {
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage("发现新版本<" + ServiceVersion + ">，请下载更新。\n" + verText)
                            .setNegativeButton("更新", (dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction("android.intent.action.VIEW");
                                Uri content_url = Uri.parse(verAddress);
                                intent.setData(content_url);
                                startActivity(intent);
                                System.exit(0);
                            })
                            .setOnKeyListener((dialog, keyCode, event) -> {
                                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                                    return true;
                                }
                                else {
                                    return false;//默认返回false
                                }
                            })
                            .show();

                }else{
                    new AlertDialog.Builder(this)
                            .setMessage("发现新版本<" + ServiceVersion + ">，是否更新？\n" + verText)
                            .setPositiveButton("取消", null)
                            .setNegativeButton("更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    Uri content_url = Uri.parse(verAddress);
                                    intent.setData(content_url);
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
            }
            if (one)return;
            if (compareVersion(localVer,ServiceVersion) == 0){
                message("提示","当前是最新版本");
            }
        } catch (Exception e) {
            message("提示","更新服务器链接失败");
        }
    }
    public static int compareVersion(String v1, String v2) {//版本号大小对比
        if (v1.equals(v2)) {
            return 0;
        }
        String[] version1Array = v1.split("[.]");
        String[] version2Array = v2.split("[.]");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;

        while (index < minLen
                && (diff = Long.parseLong(version1Array[index])
                - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return 1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return -1;
                }
            }
            return 0;
        } else {
            return diff > 0 ? 1 : -1;
        }
    }
    public void saveImage(String data) {
        try {
            Bitmap bitmap = webData2bitmap(data);
            if (bitmap != null) save2Album(bitmap, new SimpleDateFormat("SXS_yyyyMMddHHmmss", Locale.getDefault()).format(new Date()) + ".jpg");
        } catch (Exception e) {
        }
    }

    public Bitmap webData2bitmap(String data) {
        byte[] imageBytes = Base64.decode(data.split(",")[1], Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private void save2Album(Bitmap bitmap, String fileName){
        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), fileName);
        FileOutputStream fos = null;
        try {
            if (!file.exists()) file.createNewFile();
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            runOnUiThread(() -> {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            });
        } catch (Exception e) {
        } finally {
            try {
                fos.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static void goTo(String url){
        webview.loadUrl(url);
    }
    @Override
    protected void onDestroy() {//退出会经过的方法
        if (webview != null) {
            webview.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webview.clearHistory();
            ((ViewGroup) webview.getParent()).removeView(webview);
            webview.destroy();
            webview = null;
        }
        super.onDestroy();
    }
    @Override
    protected void onPause() {//每次退出，暂停等都经过的方法
        super.onPause();
    }
    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webview.canGoBack()) {
                webview.goBack();//返回上一页面
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void message(String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }
    //js接口处
    @JavascriptInterface
    public void wxpay(String prepayId,String nonceStr,String timeStamp,String sign,String orderId){
        if (!prepayId.trim().equals("")&&!nonceStr.trim().equals("")&&!timeStamp.trim().equals("")&&!sign.trim().equals("")&&!orderId.trim().equals("")){
            IWXAPI api = WXAPIFactory.createWXAPI(this, null);
            api.registerApp("wx66cb03334f396adf");
            PayReq req = new PayReq();
            req.appId           = "wx66cb03334f396adf";//你的微信appid
            req.partnerId       = "1529301071";//商户号
            req.prepayId        = prepayId;//预支付交易会话ID
            req.nonceStr        = nonceStr;//随机字符串
            req.timeStamp       = timeStamp;//时间戳
            req.packageValue    = "Sign=WXPay";//扩展字段,这里固定填写Sign=WXPay
            req.sign            = sign;//签名
            api.sendReq(req);
            this.orderId = orderId;
        }
    }

    //js接口处
    @JavascriptInterface
    public String getlocation(){//获取位置信息
        return getLocation();
    }
}