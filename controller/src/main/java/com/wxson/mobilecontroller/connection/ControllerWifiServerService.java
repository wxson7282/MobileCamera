package com.wxson.mobilecontroller.connection;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.MediaCodec;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wxson.mobilecomm.connect.ByteBufferTransfer;
import com.wxson.mobilecomm.connect.FileTransfer;
import com.wxson.mobilecontroller.FragmentBroadcastReceiver;
import com.wxson.mobilecontroller.IFragmentChangedListener;
import com.wxson.mobilecontroller.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

//import com.wxson.mobilecontroller.camera_manager.VideoDecodeTask;

/**
 * Created by wxson on 2018/3/6.
 * Package com.wxson.remote_camera.connection.
 * 服务器端接收文件
 */

public class ControllerWifiServerService extends IntentService implements Serializable, IFragmentChangedListener {

    private static final String TAG = "WifiServerService";
    public interface OnProgressChangListener {
        //当传输进度发生变化时
        void onProgressChanged(FileTransfer fileTransfer, int progress);
        //当传输结束时
        void onTransferFinished(File file);
    }

    public interface StringTransferListener {
        //当字符串收到时
        void onStringArrived(String arrivedString, InetAddress clientInetAddress);
    }

    private ServerSocket serverSocket;
    private InputStream inputStream;
    private ObjectInputStream objectInputStream;
    private FileOutputStream fileOutputStream;
    private OnProgressChangListener progressChangListener;
    private StringTransferListener mStringTransferListener;
    private static IInputDataReadyListener mInputDataReadyListener;

    // FirstByteBuffer标志
    // 0:FirstByteBuffer未到 1:FirstByteBuffer已到 decode未启动 2:FirstByteBuffer已到 decode已经启动
    private static IFirstByteBufferListener mFirstByteBufferListener;
    private boolean mCameraManagerFragmentShowed;
    private BroadcastReceiver mFragmentBroadcastReceiver;
    private int mFirstByteBufferFlag;
    public class MyBinder extends Binder {
        public ControllerWifiServerService getService() {
            return ControllerWifiServerService.this;
        }
    }

    public ControllerWifiServerService() {
        super("ControllerWifiServerService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG,"onBind");
        mFirstByteBufferFlag = 0;
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate");
        mCameraManagerFragmentShowed = false;
        registerFragmentBroadcastReceiver();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG,"onHandleIntent");
        clean();
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            int PORT = getResources().getInteger(R.integer.portNumberB);
            serverSocket.bind(new InetSocketAddress(PORT));
            //等待客户端来连接
            Socket clientSocket = serverSocket.accept();
            Log.e(TAG, "客户端IP地址 : " + clientSocket.getInetAddress().getHostAddress());
            inputStream = clientSocket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
            //根据object的类别处理
            Object inputObject = objectInputStream.readObject();
            switch (inputObject.getClass().getSimpleName()){
                case "byte[]" :
                    Log.e(TAG,"接收到byte[]类");
                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(inputStream));
                    String arrivedString = bufferedReader.readLine();
                    Log.e(TAG,"接收到字符串：" + arrivedString);
                    //触发监听器
                    mStringTransferListener.onStringArrived(arrivedString, clientSocket.getInetAddress());
                    break;
                case "FileTransfer":
                    Log.e(TAG,"接收到FileTransfer类");
                    readFileFromStream(inputObject);
                    break;
                case "ByteBufferTransfer":
                    Log.e(TAG,"接收到ByteBufferTransfer类");
                    ByteBufferTransfer byteBufferTransfer = (ByteBufferTransfer)inputObject;
                    //如果FirstByteBuffer未到
                    if (mFirstByteBufferFlag == 0){
                        Log.e(TAG,"FirstByteBuffer未到");
                        mFirstByteBufferFlag++;
                    }
                    //如果FirstByteBuffer已到 decode未启动 且CameraManagerFragment已经启动
                    if (mFirstByteBufferFlag == 1 && mCameraManagerFragmentShowed ){
                        if (mFirstByteBufferListener != null){
                            Log.e(TAG,"FirstByteBuffer已到 decode未启动 且CameraManagerFragment已经启动");
                            mFirstByteBufferFlag++;
                            mFirstByteBufferListener.onFirstByteBufferArrived(byteBufferTransfer.getCsd());
                        }
                    }
                    //如果FirstByteBuffer已到 decode已经启动
                    if (mFirstByteBufferFlag > 1){
                        //把ByteBuffer传给Decode
                        Log.e(TAG,"把ByteBuffer传给MediaCodec");
//                        ByteBufferTransfer byteBufferTransfer = (ByteBufferTransfer)inputObject;
                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        bufferInfo.flags = byteBufferTransfer.getBufferInfoFlags();
                        bufferInfo.offset = byteBufferTransfer.getBufferInfoOffset();
                        bufferInfo.presentationTimeUs = byteBufferTransfer.getBufferInfoPresentationTimeUs();
                        bufferInfo.size = byteBufferTransfer.getBufferInfoSize();
                        mInputDataReadyListener.onInputDataReady(byteBufferTransfer.getByteArray(), bufferInfo);
                    }
                    break;
                default:
                    Log.e(TAG,"接收到" + inputObject.getClass().getSimpleName() + "类");
                    break;
            }

            serverSocket.close();
            inputStream.close();
            objectInputStream.close();
            serverSocket = null;
            inputStream = null;
            objectInputStream = null;
        }
        catch (Exception e) {
            Log.e(TAG, "Stream接收 Exception: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            clean();
            //再次启动服务，等待客户端下次连接
            startService(new Intent(this, ControllerWifiServerService.class));
        }
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
//        mFirstByteBufferFlag = 0;
        Log.e(TAG,"onStart");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.e(TAG,"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        unregisterFragmentBroadcastReceiver();
        clean();
    }

    public void setProgressChangListener(OnProgressChangListener progressChangListener) {
        this.progressChangListener = progressChangListener;
    }

    public void setStringTransferListener(StringTransferListener stringTransferListener){
        this.mStringTransferListener = stringTransferListener;
    }

    private void clean() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (objectInputStream != null) {
            try {
                objectInputStream.close();
                objectInputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
                fileOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param inputObject The object from input stream.
     */
    private void readFileFromStream(Object inputObject){
        File file;
        FileTransfer fileTransfer = (FileTransfer)inputObject;
        Log.e(TAG, "待接收的文件: " + fileTransfer);
        String fileName = new File(fileTransfer.getFilePath()).getName();
        //将文件存储至指定位置
        file = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
        try{
            fileOutputStream = new FileOutputStream(file);
            byte buf[] = new byte[512];
            int len;
            long total = 0;
            int progress;
            while ((len = inputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, len);
                total += len;
                progress = (int) ((total * 100) / fileTransfer.getFileLength());
                Log.e(TAG, "文件接收进度: " + progress);
                if (progressChangListener != null) {
                    progressChangListener.onProgressChanged(fileTransfer, progress);
                }
            }
            fileOutputStream.close();
            fileOutputStream = null;
            Log.e(TAG, "文件接收成功，文件的MD5码是：" + Md5Util.getMd5(file));
        }
        catch (Exception e){
            Log.e(TAG, "文件接收 Exception: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            clean();
            if (progressChangListener != null) {
                progressChangListener.onTransferFinished(file);
            }
        }
    }

    /**
     *  输入数据监听器
     *  在VideoDecodeTask中注册该监听器
     */
    public interface IInputDataReadyListener{
        void onInputDataReady(byte[] inputData, MediaCodec.BufferInfo bufferInfo);
    }

    /**
     *  注入输入数据监听器
     * @param inputDataReadyListener 输入数据监听器
     */
    public static void setInputDataReadyListener(IInputDataReadyListener inputDataReadyListener){
        mInputDataReadyListener = inputDataReadyListener;
    }

    public interface IFirstByteBufferListener{
        void onFirstByteBufferArrived(byte[] csd);
    }

    public static void setFirstByteBufferListener(IFirstByteBufferListener firstByteBufferListener){
        mFirstByteBufferListener = firstByteBufferListener;
    }

    @Override
    public void onFragmentChanged(String fragmentName) {
        if (fragmentName != null && fragmentName.equals("CameraManagerFragment"))
            mCameraManagerFragmentShowed = true;
        else
            mCameraManagerFragmentShowed = false;
    }

    @Override
    public void registerFragmentBroadcastReceiver() {
        mFragmentBroadcastReceiver = new FragmentBroadcastReceiver(this);
        this.registerReceiver(mFragmentBroadcastReceiver, FragmentBroadcastReceiver.getIntentFilter());
    }

    @Override
    public void unregisterFragmentBroadcastReceiver() {
        this.unregisterReceiver(mFragmentBroadcastReceiver);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for(byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }
        return buf.toString();
    }

    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] toBytes(String str) {
        if(str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for(int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

}
