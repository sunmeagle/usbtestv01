package com.example.administrator.usbtestv01.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.nio.ByteBuffer;

public class MyService extends Service {
    public MyService() {
    }
    private byte[] bytes = new byte[1024];
    private static int TIMEOUT = 1000;
    private boolean forceClaim = true;

    private UsbDeviceConnection mDeviceConnection;
    private UsbEndpoint mEndpointOut;
    private  UsbEndpoint mEndpointIn;

    private String mSerial;
    private  UsbDevice mUsbDevice ;
    private  UsbManager mUsbManager ;

    private UsbInterface mInterface ;

    private String Tag = "houyafei";

    private boolean isRead = true ;

    public static final  String SEND_MSG_ACTION= "com.example.administrator.usbtestv01.services_SEND_MSG_ACTION";
    public static final String   DATA_NAME = "DATANAME" ;
    public String getDevicemsg() {
        return devicemsg;
    }

    private String devicemsg = "";

    public  void  initUsbService(UsbDevice UsbDevice) {
        mUsbDevice = UsbDevice;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
       // mUsbDevice = getIntent("");
        Log.i(Tag, "----------------onCreate()");
        //System.out.println("----------------onCreate()");


    }

    /**
     * 返回初始化的Usbdevice
     * @return
     */
     public UsbDevice getUsbDevices(){
         return mUsbDevice ;
     }

    /**
     * 获得通信接口
     */
    public void getInterfaces() {
        Log.i(Tag,"----------------onCreate()--2..0");
        int interfaceCount = mUsbDevice.getInterfaceCount();
        for (int i = 0; i < interfaceCount; i++) {
            Log.i(Tag, mUsbDevice.getInterface(i).toString());
            devicemsg = devicemsg+mUsbDevice.getInterface(i).toString()+"\n";
            //得到端口号
            int endpointCount = mUsbDevice.getInterface(i).getEndpointCount();
            for (int j = 0; j < endpointCount; j++) {
                UsbEndpoint endpoint = mUsbDevice.getInterface(i).getEndpoint(j);
                Log.i(Tag,endpoint.toString());
                devicemsg = devicemsg+endpoint.toString()+"\n";
            }

        }
        Log.i(Tag,"----------------onCreate()--2"+devicemsg);
        //System.out.println("----------------getInterfaces"+devicemsg);
        //System.out.println(devicemsg);
    }

    public void adbDevice( final UsbDeviceConnection connection,
                           UsbInterface intf) {

        mDeviceConnection = connection;
        mSerial = connection.getSerial();

        UsbEndpoint epOut = null;
        UsbEndpoint epIn = null;
        // look for our bulk endpoints
        for (int i = 0; i < intf.getEndpointCount(); i++) {
            UsbEndpoint ep = intf.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    epOut = ep;
                } else {
                    epIn = ep;
                }
            }
        }
        if (epOut == null || epIn == null) {
            throw new IllegalArgumentException("not all endpoints found");
        }
        mEndpointOut = epOut;
        mEndpointIn = epIn;



        new Thread(){
            @Override
            public void run() {
                super.run();
                int len = connection.bulkTransfer(mEndpointOut, bytes, bytes.length, 0);
                Log.i(Tag, "---是否接受数据成功"+len);
                System.out.print(bytes);
                for (byte data:bytes){
                    Log.i(Tag, "---"+String.valueOf(data ));
                }


            }
        }.start();


    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public String getData(){

        final Intent intent = new Intent(MyService.SEND_MSG_ACTION);
        //final byte[] bytes = new byte[512] ;

        UsbInterface intf = mUsbDevice.getInterface(0);
        final UsbEndpoint outEndpoint = intf.getEndpoint(0);
        final UsbEndpoint inEndpoint = intf.getEndpoint(0);
        final UsbDeviceConnection connection = mUsbManager.openDevice(mUsbDevice);
        connection.claimInterface(intf, forceClaim);

       // adbDevice(connection, intf);

        new Thread(){
            @Override
            public void run() {



                while(isRead){
                    int outMax = outEndpoint.getMaxPacketSize();
                    int inMax = inEndpoint.getMaxPacketSize();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);
                    UsbRequest usbRequest = new UsbRequest();
                    usbRequest.initialize(connection, inEndpoint);
                    usbRequest.queue(byteBuffer, inMax);
                    if(connection.requestWait() == usbRequest){
                        byte[] retData = byteBuffer.array();
                        for(Byte byte1 : retData){
                            Log.e(Tag, "-------->: " + byte1);
                            //System.err.println(byte1);
                        }
                        Log.i(Tag,new String(retData));
                        final StringBuilder stringBuilder = new StringBuilder(retData.length);
                        for(byte byteChar : retData)
                            stringBuilder.append(String.format("%02X ", byteChar));
                        Log.i(Tag,""+stringBuilder);
                        intent.putExtra(DATA_NAME, retData);
                    }else{
                        Log.e(Tag, "--------" + "读取数据错误");
                    }
                    sendBroadcast(intent);
                }
            }
        }.start();

        return null;
    }

public void setReadenable(boolean status){
     isRead = !isRead  ;
}

    /**
     * 绑定Service需要的内部类
     */
    public class MyBinder extends Binder {
        public MyService getService(){
            return MyService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }
}
