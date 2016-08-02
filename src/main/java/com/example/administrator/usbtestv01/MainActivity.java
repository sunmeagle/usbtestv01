package com.example.administrator.usbtestv01;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.usbtestv01.services.MyService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "houyafei";
    private Intent intent ;

    private UsbDevice mUsbDevice ;

    private MyService mUsbService ;

    private TextView text,textMsg ;

    //要显示的数据
    String str = "";


    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";

    public final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            text.append(device.toString());
                        }else{
                            Snackbar.make(text, "不可用", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                    else {
                        Snackbar.make(text, "没有权限", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            } else if(MyService.SEND_MSG_ACTION.equals(action)){
                //TODO
                Log.e(TAG,"-----------接收到广播事件了");
                showMsg(intent.getByteArrayExtra(MyService.DATA_NAME));
            }
        }
    };

    /**
     * 显示最终数据结果
     * @param data
     */
    private void showMsg(byte[] data) {

        //转换成16进制数据
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
          // str += stringBuilder;
            textMsg.post(new Runnable() {
                @Override
                public void run() {
                    textMsg.append(stringBuilder + "\n");
                }
            });

        }
    }

    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUsbService = ((MyService.MyBinder)service).getService();
            if(mUsbDevice!=null){
                mUsbService.initUsbService(mUsbDevice);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        text = (TextView) findViewById(R.id.text);
        textMsg = (TextView) findViewById(R.id.msg);
        text.setText("MSg");
        textMsg.setText("设备传过来的数据\n");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        Intent intent = this.getIntent();
        boolean isObtainUSBPermisson = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

        //注册监听
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(MyService.SEND_MSG_ACTION);
        registerReceiver(mUsbReceiver, filter);


        //搜索所有可用的设备
        searchDevices(mUsbManager);

        //如果没有打开权限则请求打开权限
        getPeimisson(mUsbManager, isObtainUSBPermisson, mPermissionIntent);


    }

    //如果没有打开权限则请求打开权限
    private void getPeimisson(UsbManager mUsbManager, boolean isObtainUSBPermisson, PendingIntent mPermissionIntent) {
        if(mUsbDevice!=null){
            if(!isObtainUSBPermisson){
                mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);

                //绑定 服务
               // bindService(new Intent(MainActivity.this,MyService.class),mServiceConn, Service.BIND_AUTO_CREATE);
            }
        }else{
            text.append("\n没有合适的设备接入\n");
            Snackbar.make(text, "没有合适的设备接入 ", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    /**
     * 搜索所有可用的设备
     * @param mUsbManager
     */
    private void searchDevices(UsbManager mUsbManager) {
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Log.i(TAG, "usb设备：" + String.valueOf(deviceList.size()));
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        ArrayList<String> USBDeviceList = new ArrayList<String>(); // 存放USB设备的数量
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            USBDeviceList.add(String.valueOf(device.getVendorId()));
            USBDeviceList.add(String.valueOf(device.getProductId()));

            // 在这里添加处理设备的代码
            if (device.getVendorId() == 2385 && device.getProductId() == 5734) {
                mUsbDevice = device;
                //绑定 服务
               // bindService(new Intent(MainActivity.this,MyService.class),mServiceConn, Service.BIND_AUTO_CREATE);
                text.append("\n找到设备1\n");
                Log.i(TAG, "找到设备");
            }
            // 在这里添加处理设备的代码
            if (device.getVendorId() == 1027 && device.getProductId() == 24597) {
                mUsbDevice = device;
                //绑定 服务
               // bindService(new Intent(MainActivity.this,MyService.class),mServiceConn, Service.BIND_AUTO_CREATE);
                text.append("\n找到设备2\n");
                Log.i(TAG, "找到设备");
            }if(device.getVendorId() == 2362 && device.getProductId() == 9488){
                mUsbDevice = device;
                //绑定 服务
                // bindService(new Intent(MainActivity.this,MyService.class),mServiceConn, Service.BIND_AUTO_CREATE);
                text.append("\n找到设备3\n");
                Log.i(TAG, "找到设备");
            }
            //text.append(device.toString());
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mUsbService!=null){
            unbindService(mServiceConn); //解绑Service
            mUsbService = null ;
        }

        unregisterReceiver(mUsbReceiver); //注销监听
        if(mUsbDevice!=null){
            mUsbDevice = null ;
        }
    }


    public void btnChange(View view){
        switch(view.getId()){
            case R.id.button:
                //
//绑定 服务
                bindService(new Intent(MainActivity.this, MyService.class), mServiceConn, Service.BIND_AUTO_CREATE);
                break;
            case R.id.button2:
                if(mUsbDevice!=null&&mUsbService!=null){
                    mUsbService.initUsbService(mUsbDevice);
                    Log.i(TAG, "--------------UsbDevices---->" + mUsbService.getUsbDevices().toString());
                    mUsbService.getInterfaces();
                    Log.i(TAG,"------------Interface------->");
                    mUsbService.getData();
                }else{
                    Log.i(TAG,"------------无数据------->");
                }
                break;
            case R.id.button3:
                if(mUsbDevice!=null&&mUsbService!=null){
                    mUsbService.setReadenable(false);
                }
                break;
        }
    }
}
