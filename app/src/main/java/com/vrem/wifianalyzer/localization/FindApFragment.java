
package com.vrem.wifianalyzer.localization;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.odometry.Coordinates;
import com.vrem.wifianalyzer.odometry.Odom;
import com.vrem.wifianalyzer.settings.Settings;
import com.vrem.wifianalyzer.wifi.model.WiFiData;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.scanner.Scanner;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FindApFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private PositionUpdates positionUpdates;

    private TextView tvX;
    private TextView tvY;

    private List<WiFiDetail> wiFiDetails = new ArrayList<>();

    private Odom mOdom;

    private Handler mHandler;

    private boolean mKeepRunningUI;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();

        View view = inflater.inflate(R.layout.findap_content, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.odomRefresh);
       // swipeRefreshLayout.setOnRefreshListener(new AccessPointsFragment.ListViewOnRefreshListener());

        positionUpdates = new PositionUpdates();

        Scanner scanner = MainContext.INSTANCE.getScanner();
        //scanner.register(odometryUpdates);

        tvX = (TextView) view.findViewById(R.id.textViewX_value);
        tvY = (TextView) view.findViewById(R.id.textViewY_value);

        mOdom = new Odom();

        mHandler = new Handler();
        ui_update.start();

        refresh();

        update(scanner.getWiFiData());


        return view;
    }

    private Thread ui_update = new Thread() {
        public void run() {
            mKeepRunningUI = true;
            while (mKeepRunningUI) {
                try {
                    Thread.sleep(150);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mHandler.post(new Runnable(){
                    public void run() {
                        Coordinates c = mOdom.getCoords();

                        tvX.setText(""+c.getX());
                        tvY.setText(""+c.getY());
                    }
                });
            }
        }
    };


    private void update(WiFiData wiFiData){
        Settings settings = MainContext.INSTANCE.getSettings();
        wiFiDetails = wiFiData.getWiFiDetails(settings.getWiFiBand(), settings.getSortBy(), settings.getGroupBy());

    }

    private void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        Scanner scanner = MainContext.INSTANCE.getScanner();
        scanner.update();
        swipeRefreshLayout.setRefreshing(false);
    }


    private String formatDouble(Double doubles) {
        DecimalFormat format = new DecimalFormat("####.##");
        String distanceStr = format.format(doubles);
        return distanceStr.equals(getString(R.string.zero)) ? getString(R.string.double_zero)
                : distanceStr;
    }


    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private class ListViewOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            refresh();
        }
    }

    @Override
    public void onDetach() {
        //mOdom.unregisterListener();
        ui_update = null;
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        //mOdom.unregisterListener();
        ui_update = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        //mOdom.unregisterListener();
        ui_update = null;
        super.onDestroy();
    }
}