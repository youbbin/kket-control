package com.example.kketcontrol_youbin;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DeviceControl extends Activity {
    private final static String TAG = DeviceControl.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private List<BluetoothGattService> gattServices;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    public static final String CC41_WRITE_UUID="0000ffe1-0000-1000-8000-00805f9b34fb";


    // 서비스 라이프사이클을 관리하는 코드입니다.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // 시동 초기화에 성공하면 자동으로 장치에 연결합니다.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        // 지원되는 GATT 서비스/특성을 통해 반복하는 방법을 시연합니다.
        // 이 샘플에서는 확장 가능한 목록 보기에 바인딩된 데이터 구조를 채웁니다.
        // UI에서.
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        private void displayGattServices(List<BluetoothGattService> gattServices) {
            if (gattServices == null) return;
            String uuid = null;
            String unknownServiceString = "Unknown Service";
            String unknownCharaString = "Unknown Characteristic";
            ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
            ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
            mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

            // 사용 가능한 GATT 서비스를 루프합니다.
            for (BluetoothGattService gattService : gattServices) {
                HashMap<String, String> currentServiceData = new HashMap<String, String>();
                uuid = gattService.getUuid().toString();
                gattServiceData.add(currentServiceData);
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> charas =
                        new ArrayList<BluetoothGattCharacteristic>();

                // Loops through available Characteristics.
                // 사용 가능한 특성을 루프합니다.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    charas.add(gattCharacteristic);
                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
                    uuid = gattCharacteristic.getUuid().toString();
                }
                mGattCharacteristics.add(charas);
            }
        }

        //서비스에서 발생한 다양한 이벤트를 처리합니다.
        //작업_GATT_CONNECTED: GATT 서버에 연결되었습니다.
        //작업_GATT_DISCONNECT: GATT 서버에서 연결이 끊어졌습니다.
        //작업_GATT_SERVICES_DISCOVERED: GATT 서비스를 검색했습니다.
        //조치_DATA_ABLE: 디바이스에서 데이터를 수신했습니다. 읽기 또는 알림 작업의 결과일 수 있습니다.
        private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    // Show all the supported services and characteristics on the user interface.
                    //displayGattServices(mBluetoothLeService.getSupportedGattServices());
                    displayGattServices(mBluetoothLeService.getSupportedGattServices());
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }
            }
        };

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);


    }

    public void setGattCharacteristic(){
        for ( ArrayList<BluetoothGattCharacteristic> service : mGattCharacteristics){
            for(BluetoothGattCharacteristic characteristic : service){
                if(characteristic.getUuid().equals(UUID.fromString(CC41_WRITE_UUID))){
                    mNotifyCharacteristic=characteristic;
                }
            }
        }
    }
}
