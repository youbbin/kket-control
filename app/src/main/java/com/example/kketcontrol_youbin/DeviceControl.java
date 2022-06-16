package com.example.kketcontrol_youbin;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DeviceControl extends AppCompatActivity {
    private final static String TAG = DeviceControl.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    protected BluetoothLeService mBluetoothLeService;

    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;
    protected BluetoothGattCharacteristic mNotifyCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    public static final String CC41_WRITE_UUID = "0000FF01-0000-1000-8000-00805F9B34FB";
    private SeekBar seekbar_2700, seekbar_4000, seekbar_5000, seekbar_6500;
    private SeekBar[] seekbars;
    private Button button_set, button_on, button_off;
    private TextView textview_name, textview_address, textview_char;
    private TextView[] textNum;

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
            if(mBluetoothLeService.connect(mDeviceAddress)){
                Toast.makeText(getApplicationContext(), "연결되었습니다.", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(getApplicationContext(), "연결 실패", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

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
                Toast.makeText(getApplicationContext(), "통신 연결되었습니다.", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Toast.makeText(getApplicationContext(), "통신 연결 실패", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }
        }
    };


    // 지원되는 GATT 서비스/특성을 통해 반복하는 방법을 시연합니다.
    // 이 샘플에서는 확장 가능한 목록 보기에 바인딩된 데이터 구조를 채웁니다.
    // UI에서.
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // 사용 가능한 GATT 서비스를 루프합니다.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();

            gattServiceData.add(currentServiceData);
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            // 사용 가능한 특성을 루프합니다.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
            }
            mGattCharacteristics.add(charas);
        }
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        seekbar_2700 = (SeekBar) findViewById(R.id.seekbar_2700);
        seekbar_4000 = (SeekBar) findViewById(R.id.seekbar_4000);
        seekbar_5000 = (SeekBar) findViewById(R.id.seekbar_5000);
        seekbar_6500 = (SeekBar) findViewById(R.id.seekbar_6500);
        seekbars = new SeekBar[4];
        seekbars[0] = seekbar_2700;
        seekbars[1] = seekbar_4000;
        seekbars[2] = seekbar_5000;
        seekbars[3] = seekbar_6500;
        textNum = new TextView[4];
        textNum[0] = (TextView) findViewById(R.id.textview_num1);
        textNum[1] = (TextView) findViewById(R.id.textview_num2);
        textNum[2] = (TextView) findViewById(R.id.textview_num3);
        textNum[3] = (TextView) findViewById(R.id.textview_num4);
        button_set = (Button) findViewById(R.id.button_set);
        button_on = (Button) findViewById(R.id.button_on);
        button_off = (Button) findViewById(R.id.button_off);

        textview_name = (TextView) findViewById(R.id.textview_name);
        textview_address = (TextView) findViewById(R.id.textview_address);
        textview_char=(TextView) findViewById(R.id.textview_char);
        textview_name.setText("Name : " + mDeviceName);
        textview_address.setText("Address : " + mDeviceAddress);


        // On 버튼 이벤트
        button_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGattCharacteristic();
                //mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                if(mBluetoothLeService == null||mNotifyCharacteristic == null){
                    Toast.makeText(v.getContext(), "연결 오류 : Characteristic 미설정", Toast.LENGTH_SHORT).show();
                    return;
                }

                String on_packet = "0211FFFFFFFFFF03";
                byte[] data = hexStringToByteArray(on_packet);
                mBluetoothLeService.write(mNotifyCharacteristic, data);
                Toast.makeText(v.getContext(), "조명을 점등하였습니다.", Toast.LENGTH_SHORT).show();

                for (int i = 0; i < seekbars.length; i++) {
                    int index = i;
                    seekbars[index].setProgress(255);
                    textNum[index].setText("255");
                }
            }
        });

        // Off 버튼 이벤트
        button_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGattCharacteristic();
                mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                if (mBluetoothLeService == null||mNotifyCharacteristic == null) {
                    Toast.makeText(v.getContext(), "연결 오류 : Characteristic 미설정", Toast.LENGTH_SHORT).show();
                    return;
                }

                String on_packet = "0211FF0000000003";
                byte[] data = hexStringToByteArray(on_packet);
                mBluetoothLeService.write(mNotifyCharacteristic, data);
                Toast.makeText(v.getContext(), "조명을 소등하였습니다.", Toast.LENGTH_SHORT).show();

                for (int i = 0; i < seekbars.length; i++) {
                    int index = i;
                    seekbars[index].setProgress(0);
                    textNum[index].setText("000");
                }
            }
        });

        /* 시크바 이벤트 */
        for (int i = 0; i < seekbars.length; i++) {
            int index = i;
            seekbars[index].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textNum[index].setText(String.format("%03d", progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }
        for (int i = 0; i < seekbars.length; i++) {
            int index = i;
            button_set.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onClick(View v) {
                    setGattCharacteristic(); // 캐릭터 설정
                    if(mBluetoothLeService==null || mNotifyCharacteristic==null){
                        Toast.makeText(v.getContext(), "연결 오류 : Characteristic 미설정", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String ch_packet = "0211FF";
                    for(int j = 0; j < textNum.length; j++){
                        int jndex = j;
                        String power = (String) textNum[jndex].getText();
                        String hex = String.format("%02X", Integer.parseInt(power));
                        ch_packet +=hex;
                    }
                    ch_packet +="03";
                    byte[] data = hexStringToByteArray(ch_packet);
                    mBluetoothLeService.write(mNotifyCharacteristic, data);
                    Toast.makeText(v.getContext(), "채널별 제어", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /* String -> 16진수 Byte */
    private byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unbindService(mServiceConnection);
//        mBluetoothLeService = null;
//    }

    public void setGattCharacteristic() {
        for (ArrayList<BluetoothGattCharacteristic> service : mGattCharacteristics) {
            for (BluetoothGattCharacteristic characteristic : service) {
                if (characteristic.getUuid().equals(UUID.fromString(CC41_WRITE_UUID))) {
                    mNotifyCharacteristic = characteristic;
                    textview_char.setText(characteristic.getUuid().toString());
                    mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                }
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
