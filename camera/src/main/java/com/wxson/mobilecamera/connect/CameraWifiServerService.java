package com.wxson.mobilecamera.connect;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wxson.mobilecomm.connect.FileTransfer;
import com.wxson.mobilecamera.R;
import com.wxson.mobilecomm.connect.Md5Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by wxson on 2018/3/6.
 * Package com.wxson.remote_camera.connection.
 * 服务器端接收文件
 */

public class CameraWifiServerService extends IntentService {

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
    private StringTransferListener stringTransferListener;

    public class MyBinder extends Binder {
        public CameraWifiServerService getService() {
            return CameraWifiServerService.this;
        }
    }

    public CameraWifiServerService() {
        super("CameraWifiServerService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        clean();
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            int PORT = getResources().getInteger(R.integer.portNumberA);
            serverSocket.bind(new InetSocketAddress(PORT));
            //等待客户端来连接
            Socket clientSocket = serverSocket.accept();
            Log.i(TAG, "客户端IP地址 : " + clientSocket.getInetAddress().getHostAddress());
            inputStream = clientSocket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
            //根据object的类别处理
            Object inputObject = objectInputStream.readObject();
            switch (inputObject.getClass().getSimpleName()){
                case "byte[]" :
                    Log.i(TAG,"接收到byte[]类");
                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(inputStream));
                    String arrivedString = bufferedReader.readLine();
                    Log.i(TAG,"接收到字符串：" + arrivedString);
                    //触发监听器
                    stringTransferListener.onStringArrived(arrivedString, clientSocket.getInetAddress());
                    break;
                case "FileTransfer":
                    Log.i(TAG,"接收到FileTransfer类");
                    readFileFromStream(inputObject);
                    break;
                default:
                    Log.i(TAG,"接收到" + inputObject.getClass().getSimpleName() + "类");
                    break;
            }

            serverSocket.close();
            inputStream.close();
            objectInputStream.close();
            serverSocket = null;
            inputStream = null;
            objectInputStream = null;
        } catch (Exception e) {
            Log.e(TAG, "Stream接收 Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            clean();
            //再次启动服务，等待客户端下次连接
            startService(new Intent(this, CameraWifiServerService.class));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(TAG,"onStart");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clean();
    }

    public void setProgressChangListener(OnProgressChangListener progressChangListener) {
        this.progressChangListener = progressChangListener;
    }

    public void setStringTransferListener(StringTransferListener stringTransferListener){
        this.stringTransferListener = stringTransferListener;
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
        Log.i(TAG, "待接收的文件: " + fileTransfer);
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
                Log.i(TAG, "文件接收进度: " + progress);
                if (progressChangListener != null) {
                    progressChangListener.onProgressChanged(fileTransfer, progress);
                }
            }
            fileOutputStream.close();
            fileOutputStream = null;
            Log.i(TAG, "文件接收成功，文件的MD5码是：" + Md5Util.getMd5(file));
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
}
