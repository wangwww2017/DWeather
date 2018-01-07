package com.toosame.weather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.toosame.weather.model.DisCity;
import com.toosame.weather.model.Districts;
import com.toosame.weather.model.DistrictsRoot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private List<Integer> cities;
    private List<String> cityKeyVal;
    private List<String> cityVal;
    private Gson jsonConverter = new Gson();

    private SharedPreferences userSettings;
    private String selectName;
    private String selectCode;

    private TextView tvTitle;
    private Button btBack;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> dataList = new ArrayList<>();
    private List<Districts> mProvinceList = new ArrayList<>();   // 省列表
    private List<DisCity> mCityList = new ArrayList<>();           // 市列表
    private List<DisCity> mCountyList = new ArrayList<>();       // 县级列表
    private Districts selectedProvince;      // 选中的省份
    private DisCity selectedCity;          // 选中的城市
    private DisCity selectedCountry;        //选中的县区
    private int currentLevel;               // 当前选中的级别
    private String adcode;
    private String cityName;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area, container, false);

        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        btBack = (Button) view.findViewById(R.id.bt_back);
        mListView = (ListView) view.findViewById(R.id.lv_list);
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(mAdapter);

        //userSettings =
        cities = new ArrayList<>();
        cities.add(R.raw.anhui);
        cities.add(R.raw.aomeng);
        cities.add(R.raw.beijin);
        cities.add(R.raw.chongqing);
        cities.add(R.raw.fujiang);
        cities.add(R.raw.gangsu);
        cities.add(R.raw.guangdong);
        cities.add(R.raw.guangxi);
        cities.add(R.raw.guizhou);
        cities.add(R.raw.hainang);
        cities.add(R.raw.hebei);
        cities.add(R.raw.heilongjiang);
        cities.add(R.raw.henang);
        cities.add(R.raw.hongkong);
        cities.add(R.raw.hubei);
        cities.add(R.raw.hunang);
        cities.add(R.raw.jiangsu);
        cities.add(R.raw.jiangxi);
        cities.add(R.raw.jiling);
        cities.add(R.raw.liaoning);
        cities.add(R.raw.neimenggu);
        cities.add(R.raw.ningxia);
        cities.add(R.raw.qinghai);
        cities.add(R.raw.shangdong);
        cities.add(R.raw.shanghai);
        cities.add(R.raw.shangxi);
        cities.add(R.raw.shanxi);
        cities.add(R.raw.sichuang);
        cities.add(R.raw.tianjin);
        cities.add(R.raw.xinjiang);
        cities.add(R.raw.xizan);
        cities.add(R.raw.yunnang);
        cities.add(R.raw.zhejiang);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //        tvTitle.setText("中国");

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    // 显示城市列表

                    selectedProvince = mProvinceList.get(position);
                    System.out.println(selectedProvince.getName());
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = mCityList.get(position);
                    queryCounties();
                    //显示县区列表

                } else if (currentLevel == LEVEL_COUNTY) {
                    // 传入 代码 查询天气
                    if ( mCountyList.size()>0){
                        selectedCountry = mCountyList.get(position);
                        adcode = selectedCountry.getAdcode();
                        cityName = selectedCountry.getName();

                    }else {
                        adcode = selectedCity.getAdcode();
                        cityName = selectedCity.getName();
                    }

                    if (getActivity() instanceof MainActivity){
                        MainActivity activity = (MainActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.queryWeather(adcode,cityName);
                    }
                }
            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();

    }

    private void queryProvinces() {
        tvTitle.setText("中国");
        btBack.setVisibility(View.GONE);
        for (int i : cities) {
            StringBuilder stringBuilder = new StringBuilder();
            InputStream inputStream = getResources().openRawResource(i);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                DistrictsRoot dis = jsonConverter.fromJson(stringBuilder.toString(),
                        DistrictsRoot.class);

                if (dis.getDistricts().size() > 0) {

                    for (Districts diss : dis.getDistricts()) {
                        mProvinceList.add(diss);
                    }
                    ///System.out.println(mProvinceList.toString());
                    //dataList.clear();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(mProvinceList.size()>0){
            dataList.clear();
            for (int i = 0; i < mProvinceList.size(); i++) {
                dataList.add(mProvinceList.get(i).getName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }

    }

    private void queryCities() {
        tvTitle.setText(selectedProvince.getName());
        btBack.setVisibility(View.VISIBLE);
        //mCityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId()
        // )).find(City.class);

        mCityList = selectedProvince.getDistricts();
        if(mCityList.size()>0){
            dataList.clear();
            for (int i = 0; i < mCityList.size(); i++) {
                dataList.add(mCityList.get(i).getName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }

    }

    private void queryCounties() {
        tvTitle.setText(selectedCity.getName());

        mCountyList = selectedCity.getDistricts();
        if ( mCountyList.size()>0){
            dataList.clear();
            for (int i = 0; i < mCountyList.size(); i++) {
                dataList.add(mCountyList.get(i).getName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {

            adcode = selectedCity.getAdcode();
            cityName = selectedCity.getName();

            if (getActivity() instanceof MainActivity){
                MainActivity activity = (MainActivity) getActivity();
                activity.drawerLayout.closeDrawers();
                activity.queryWeather(adcode,cityName);
            }
        }

    }


}
