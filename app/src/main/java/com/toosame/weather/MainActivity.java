package com.toosame.weather;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.toosame.weather.model.Casts;
import com.toosame.weather.model.Forecasts;
import com.toosame.weather.model.Lives;
import com.toosame.weather.model.TimeWeather;
import com.toosame.weather.model.Weather;
import com.toosame.weather.utils.HttpClient;
import com.toosame.weather.utils.IHttpCallback;
import com.toosame.weather.utils.WeatherUtils;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.toosame.weather.SplashActivity.ADCODE;
import static com.toosame.weather.SplashActivity.CITYNAME;

public class MainActivity extends AppCompatActivity {

    private String cityName;
    private String adcode;

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private LinearLayout relativeLayout;
    private LinearLayout linearLayout;
    private TextView headerLabel;
    private TextView temperatureLabel;
    private TextView posttimeLabel;
    private TextView windLabel;
    private TextView windDirectionLabel;
    private TextView weatherLabel;
    private ImageView weatherImage;
    private Button switchBtn;
    private Button homeBtn;
    public DrawerLayout drawerLayout;
    private LinearLayout forecast_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21)
        {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);

        init();
    }

    private void init(){
//        Log.v("SHA1",sHA1(this));
//        Log.v("SHA1","dasdas");
        initView();
        initData();

    }

    private void initView(){
        relativeLayout = (LinearLayout) findViewById(R.id.activity_main);
        linearLayout = (LinearLayout)findViewById(R.id.main_weather_info_layout);
        forecast_layout = (LinearLayout)findViewById(R.id.forecast_layout);
        headerLabel = (TextView)findViewById(R.id.main_header_label);
        weatherImage = (ImageView)findViewById(R.id.main_weather_image);
        weatherLabel = (TextView)findViewById(R.id.main_weather_info);
        windDirectionLabel = (TextView)findViewById(R.id.main_weather_direction);
        windLabel = (TextView)findViewById(R.id.main_weather_wind);
        posttimeLabel = (TextView)findViewById(R.id.main_weather_posttime);
        temperatureLabel = (TextView)findViewById(R.id.main_wearher_temperature);

        drawerLayout = (DrawerLayout) findViewById(R.id.draw_layout);

        homeBtn = (Button)findViewById(R.id.home_btn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //初始化定位
                mLocationClient = new AMapLocationClient(getApplicationContext());
                mLocationOption = new AMapLocationClientOption();
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

                //获取一次定位结果：
                // 该方法默认为false。
                mLocationOption.setOnceLocation(true);

                //获取最近3s内精度最高的一次定位结果：
                // 设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。
                // 如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
                mLocationOption.setOnceLocationLatest(true);

                //设置是否返回地址信息（默认返回地址信息）
                mLocationOption.setNeedAddress(true);


                //设置是否强制刷新WIFI提高精度，默认为true，强制刷新。
                mLocationOption.setWifiActiveScan(false);

                //设置是否允许模拟位置,默认为false，不允许模拟位置
                mLocationOption.setMockEnable(false);

                //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
                mLocationOption.setHttpTimeOut(20000);

                //关闭缓存机制
                mLocationOption.setLocationCacheEnable(true);

                //给定位客户端对象设置定位参数
                mLocationClient.setLocationOption(mLocationOption);
                //启动定位
                mLocationClient.startLocation();
                //设置定位回调监听
                mLocationClient.setLocationListener(new AMapLocationListener() {
                    @Override
                    public void onLocationChanged(AMapLocation aMapLocation) {
                        if (aMapLocation != null) {
                            if (aMapLocation.getErrorCode() == 0) {
                                //定位成功
                                String cityname = aMapLocation.getCity() + " "
                                        + aMapLocation.getDistrict();

                                String adcode = aMapLocation.getAdCode();


                                // 停止定位后，本地定位服务并不会被销毁
                                mLocationClient.stopLocation();

                                //销毁定位客户端，同时销毁本地定位服务。
                                mLocationClient.onDestroy();
                                queryWeather(adcode,cityname);
                            }else {
                                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。


                                //                        Log.e("AmapError","location Error, ErrCode:"
                                //                                + aMapLocation.getErrorCode() + ", errInfo:"
                                //                                + aMapLocation.getErrorInfo());
                                Toast.makeText(MainActivity.this, "我们找不到你，无法提供天气信息；错误代码:" + aMapLocation.getErrorCode(), Toast.LENGTH_SHORT).show();

                            }
                        }else {

                            Toast.makeText(MainActivity.this, "请给我位置权限，或者手动输入你所在城市:", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });

        switchBtn = (Button)findViewById(R.id.change_btn);

        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SelectCityActivity.class);
                startActivity(intent);
            }
        });

        Calendar now = Calendar.getInstance();
        if(now.get(Calendar.HOUR_OF_DAY) > 18 || now.get(Calendar.HOUR_OF_DAY) < 7){
            relativeLayout.setBackgroundResource(R.drawable.dark);
        }else {
            relativeLayout.setBackgroundResource(R.drawable.light);
        }
    }

    private void initData(){
        Intent intent = getIntent();
        cityName = intent.getStringExtra(CITYNAME);
        adcode = intent.getStringExtra(ADCODE);
        headerLabel.setText(cityName);

        HttpClient.query(adcode, HttpClient.WEATHER_TYPE_BASE, Weather.class, new IHttpCallback() {
            @Override
            public <T> void onSuccess(T result, boolean isSuccess) {
                if(isSuccess){
                     Weather weather = (Weather)result;
                    if (weather.getInfo().equals("OK") && weather.getCount().equals("1")){
                        final Lives info = weather.getLives().get(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                temperatureLabel.setText(info.getTemperature());
                                posttimeLabel.setText(format(info.getReporttime()) + "更新");
                                windDirectionLabel.setText(info.getWinddirection());
                                windLabel.setText("湿度" + info.getHumidity() + "%");

                                if (WeatherUtils.WeatherKV.containsKey(info.getWeather())){
                                    weatherLabel.setText(info.getWeather());
                                    weatherImage.setImageResource(WeatherUtils.WeatherKV.get(info.getWeather()));
                                }else {
                                    temperatureLabel.setText("N/A");
                                }
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                temperatureLabel.setText("服务不可用");
                                Toast.makeText(MainActivity.this, "服务不可用", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            temperatureLabel.setText("无法提供天气信息");
                            Toast.makeText(MainActivity.this, "无法提供天气信息", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        HttpClient.query(adcode, HttpClient.WEATHER_TYPE_ALL, TimeWeather.class, new IHttpCallback() {
            @Override
            public <T> void onSuccess(T result, boolean isSuccess) {
                if (isSuccess){
                    final TimeWeather timeWeather = (TimeWeather)result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (timeWeather.getInfo().equals("OK") && timeWeather.getCount().equals("1")){
                                forecast_layout.removeAllViews();
                                for (Forecasts forecasts : timeWeather.getForecasts()){
                                    for (Casts casts : forecasts.getCasts()){
                                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.weather_itme,forecast_layout ,false);
                                        TextView date = (TextView)view.findViewById(R.id.item_date);
                                        TextView max = (TextView)view.findViewById(R.id.item_max);
                                        TextView min = (TextView)view.findViewById(R.id.itme_min);
                                        TextView currentWeather = (TextView)view.findViewById(R.id.item_weather);
                                        TextView week = (TextView)view.findViewById(R.id.item_week);

                                        date.setText(getDay(casts.getDate()));
                                        max.setText(casts.getDaytemp() + "°");
                                        min.setText(casts.getNighttemp() + "°");
                                        currentWeather.setText(casts.getDayweather());
                                        week.setText(getWeek(casts.getWeek()));

                                        forecast_layout .addView(view);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public void queryWeather(String adcode,String cityName){
        headerLabel.setText(cityName);

        HttpClient.query(adcode, HttpClient.WEATHER_TYPE_BASE, Weather.class, new IHttpCallback() {
            @Override
            public <T> void onSuccess(T result, boolean isSuccess) {
                if(isSuccess){
                    Weather weather = (Weather)result;
                    if (weather.getInfo().equals("OK") && weather.getCount().equals("1")){
                        final Lives info = weather.getLives().get(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                temperatureLabel.setText(info.getTemperature());
                                posttimeLabel.setText(format(info.getReporttime()) + "更新");
                                windDirectionLabel.setText(info.getWinddirection());
                                windLabel.setText("湿度" + info.getHumidity() + "%");

                                if (WeatherUtils.WeatherKV.containsKey(info.getWeather())){
                                    weatherLabel.setText(info.getWeather());
                                    weatherImage.setImageResource(WeatherUtils.WeatherKV.get(info.getWeather()));
                                }else {
                                    temperatureLabel.setText("N/A");
                                }
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                temperatureLabel.setText("服务不可用");
                                Toast.makeText(MainActivity.this, "服务不可用", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            temperatureLabel.setText("无法提供天气信息");
                            Toast.makeText(MainActivity.this, "无法提供天气信息", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        HttpClient.query(adcode, HttpClient.WEATHER_TYPE_ALL, TimeWeather.class, new IHttpCallback() {
            @Override
            public <T> void onSuccess(T result, boolean isSuccess) {
                if (isSuccess){
                    final TimeWeather timeWeather = (TimeWeather)result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (timeWeather.getInfo().equals("OK") && timeWeather.getCount().equals("1")){
                                forecast_layout.removeAllViews();
                                for (Forecasts forecasts : timeWeather.getForecasts()){
                                    for (Casts casts : forecasts.getCasts()){
                                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.weather_itme,forecast_layout ,false);
                                        TextView date = (TextView)view.findViewById(R.id.item_date);
                                        TextView max = (TextView)view.findViewById(R.id.item_max);
                                        TextView min = (TextView)view.findViewById(R.id.itme_min);
                                        TextView currentWeather = (TextView)view.findViewById(R.id.item_weather);
                                        TextView week = (TextView)view.findViewById(R.id.item_week);

                                        date.setText(getDay(casts.getDate()));
                                        max.setText(casts.getDaytemp() + "°");
                                        min.setText(casts.getNighttemp() + "°");
                                        currentWeather.setText(casts.getDayweather());
                                        week.setText(getWeek(casts.getWeek()));

                                        forecast_layout.addView(view);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private String format(String posttime){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(posttime);

            return new SimpleDateFormat("HH:MM").format(date);
        } catch (ParseException e) {
            return "刚刚更新";
        }
    }

    private String getDay(String date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date toDate = dateFormat.parse(date);

            return new SimpleDateFormat("dd").format(toDate);
        } catch (ParseException e) {
            return "N/A";
        }
    }

    private String getWeek(String week){
        switch (week)
        {
            case "1":
                return "星期一";
            case "2":
                return "星期二";
            case "3":
                return "星期三";
            case "4":
                return "星期四";
            case "5":
                return "星期五";
            case "6":
                return "星期六";
            default:
                return "星期日";
        }
    }
    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length()-1);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
    //获取手机状态栏高
//    public static int getStatusBarHeight(Context context)
//    {
//        int result = 0;
//        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0)
//        {
//            result = context.getResources().getDimensionPixelSize(resourceId);
//        }
//        return result;
//    }
}
