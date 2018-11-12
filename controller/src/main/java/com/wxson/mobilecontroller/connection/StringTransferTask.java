package com.wxson.mobilecontroller.connection;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by wxson on 2018/3/24.
 * Package com.wxson.remote_camera.connection.
 */

public class StringTransferTask extends Thread {

    private Socket mSocket;
    private OutputStream mOutputStream;
    private ObjectOutputStream mObjectOutputStream;
    private String mHostAddress;
    private int mPort;
    private byte[] mOutputByteArray;
    private static final String TAG = "StringTransferTask";
    
    public StringTransferTask(String hostAddress, int port, String outputString) {
        super("StringTransferTask");
        mHostAddress = hostAddress;
        mPort = port;
        mOutputByteArray = (outputString + "\n").getBytes();    //必须加换行符，以便read line读出
        Log.e(TAG, outputString + "字符串发送");
    }

    @Override
    public void run() {
        mSocket = null;
        mOutputStream = null;
        mObjectOutputStream = null;
        Log.e(TAG, "字符串发送地址：" + mHostAddress + " 端口：" + mPort);
        try{
            mSocket = new Socket(mHostAddress, mPort);
            mOutputStream = mSocket.getOutputStream();
            mObjectOutputStream = new ObjectOutputStream(mOutputStream);
            mObjectOutputStream.writeObject(mOutputByteArray);
            mOutputStream.write(mOutputByteArray);

            clean();
            Log.e(TAG,  "字符串发送成功");
        }
        catch (IOException e){
            Log.e(TAG, "字符串发送失败");
            e.printStackTrace();
        }
        finally {
            clean();
        }
    }

    private void clean(){
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
                mOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mObjectOutputStream != null) {
            try {
                mObjectOutputStream.close();
                mObjectOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
