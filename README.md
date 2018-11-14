# MobileCamera
现在具有wifi遥控功能的照相机已经很多，Canon和Nikon都有。最近两三年发布的新相机几乎都支持WIFI传图和遥控功能。本文介绍用wifi p2p方法实现两台android手机遥控拍摄的方案。
有关wifi直连(p2p)的公开技术资料已经有很多，此处不再赘述有关细节，仅对方案要点作一些说明，实现细节可以参照[源代码](https://download.csdn.net/download/wxson/10744796)。

使用两台手机，一台作为相机，安装camera app，在wifi p2p group中，担当group owner。
         另一台作为遥控器，安装controller app，在wifi p2p group中，担当group client。
![系统构成](https://img-blog.csdn.net/20181014202407117?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3d4c29u/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
在两台手机间建立wifi直连后，可以进行双向通信，为了实现遥控拍摄，需要通过wifi p2p传递以下信息：
 - 连接状态，在两台手机上显示连接成功与否。当收到对方手机信息时，在己方手机上显示连接成功符号。
 - 相机端向遥控端回传镜头取得的浏览图像，这实际上就是相机端取得的视频流，传送到遥控端用于取景。
 - 遥控端发出的拍摄指令，用来按下相机端的快门。
 - 如果需要，还可以把相机端拍摄的照片回传到遥控端，目前程序中没有实现，应该难度不大。
 
 这些传送信息，用三种形式表示：
第一种是字符串，用于传送连接状态、拍摄指令。
connected表示已经连接
disconnected表示断开连接
capture表示拍摄
第二种是用ByteBufferTransfer类封装的图像数据，包括承载图像的字节数组byte[]、以及从相机端取得图像的各种相关信息。
第三种是文件，可以用于传送已经拍摄的照片。

程序由三部分组成：camera app、controller app和comm lib。系统架构采用mvp官方模板。

一、camera app管理控制相机的打开、关闭、拍摄、预览视频取得、编码压缩等机能，并且以group owner的身份建立wifi p2p连接group。
![camer app的主架构](https://img-blog.csdn.net/20181015161706380?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3d4c29u/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
  
二、controller app以group client身份同相机端建立wifi p2p连接，取得相机端传送的字节流，解码成视频，显示在遥控端手机上。需要时，通过wifi p2p连接向相机端发出拍摄指令。
![controller app的主架构](https://img-blog.csdn.net/20181015213912101?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3d4c29u/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
三、comm lib是一些共通的类和方法。

由于在程序中使用了一些回调和监听，代码的可读性不太好。为了便于理解，在此简单介绍最核心的部分。

**ByteBufferTransfer**
该类包含了图像数据的字节数组，以及视频编解码相关的信息MediaCodec.BufferInfo，每一个ByteBufferTransfer的实例，代表视频中的一帧图像。为了能够通过wifi p2p传送，ByteBufferTransfer必须实现Serializable接口。

**ByteBufferTransferTask**
这是专用于传送ByteBufferTransfer实例的后台异步任务。

**DirectBroadcastReceiver**
本系统中有关wifi p2p的核心部分是DirectBroadcastReceiver，在相机端和遥控端都要用到，这是一个广播接收器，用于处理系统产生的wifi p2p事件，有以下四种：
 - WIFI_P2P_STATE_CHANGED_ACTION
    Wifi P2P状态变更时发生，用于指示 Wifi P2P 是否可用。
 - WIFI_P2P_PEERS_CHANGED_ACTION
    Wifi P2P节点list变化时发生，此时需要用WifiP2pManager.requestPeers方法请求获取Wifi P2P节点list，请求的结果由onPeersAvailable监听器获得。
 - WIFI_P2P_CONNECTION_CHANGED_ACTION
     WIFI_P2P节点连接状态变化时发生，此时需要用intent.getParcelableExtra方法取得EXTRA_NETWORK_INFO情报，如果处于连接状态，可以进一步取得WifiP2pInfo，WifiP2pInfo包含群组是否建立、谁是群主、群主的地址等信息。
 - WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
    本机设备信息变化时发生，此时需要重新获取本机设备信息。
 
**CameraWifiServerService、ControllerWifiServerService**
在相机端和遥控端各有一个IntentService，类名是CameraWifiServerService和ControllerWifiServerService，用来处理通过wifi p2p接受到的信息，分别对字符串、图像字节数组、文件做出相应处理。

**mStateCallback** 
这是在MainPresenter中定义的CameraDevice.StateCallback的实现，是openCamera必须的参数之一。在相机端打开照相机时，执行createCameraPreviewSession方法，在该方法中实现以下动作：

 - 建立相机预览用的Surface
 - 创建作为预览的CaptureRequest.Builder
 - 将textureView的surface作为CaptureRequest.Builder的目标
 - 根据视频编码类型创建编码器
 - 实例化MediaCodecCallback
 - 格式化编码器
 - 启动编码器
 - 创建CameraCaptureSession，该对象负责管理处理预览请求、拍照请求和传输请求。

视频流编解码使用的是H264 Vga(640x480)格式，如果改为H265格式，应该没有什么难度， android体系对于H265视频编解码已经有了成熟的支持，问题是三年前生产的手机大部分不支持H265编码。

**MediaCodecCallback**
这是在相机端实现的MediaCodec.Callback。当视频编码器完成一帧图像的编码时，通过onOutputBufferAvailable取得ByteBuffer数据，从中提取字节数组，注入ByteBufferTransfer的实例中，然后启动ByteBufferTransferTask，把ByteBufferTransfer的实例通过wifi p2p连接传送到对方。

**DecoderCallback**
这是在遥控端实现的MediaCodec.Callback。通过onInputBufferAvailable，当视频解码器的输入缓冲区空闲时，把通过wifi p2p接收到的字节数组注入解码器，启动视频解码进程。

MediaCodecCallback和DecoderCallback都是用回调实现的视频编解码器，优点是代码简洁、效率高，缺点是安卓SDK 21以前的版本不支持。

**MediaCodecAction**
该类用于视频解码器的准备、启动和释放。在准备时用mediaCodec.configure方法把遥控端手机的TextureView的Surface与视频解码器的输出绑定，在遥控端就可以看到相机端取得的图像。

把以上这些模块结合到android架构中，实现手机的遥控拍摄。

需要说明的是，由于各个手机品牌对于wifi p2p的定制深度不一，这个系统在一些手机上并不能正常运行。准确原因笔者尚未找到，也许是程序的BUG，希望高手大侠指点迷津。

camera app可以运行的安卓SDK版本是23-27。
controller app可以运行的安卓SDK版本是21-27。

源程序请参照   [用wifi直连(p2p)实现遥控照相的源代码
](https://download.csdn.net/download/wxson/10744796)
欢迎指摘、建议、问题。
