package com.wxson.mobilecontroller.connection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wxson.mobilecontroller.R;

import java.util.Objects;

import static android.support.v4.util.Preconditions.checkNotNull;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConnectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConnectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectFragment extends Fragment implements IConnectionContract.IView {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView tvMyDeviceName;
    private TextView tvMyMacAddress;
    private TextView tvMyDeviceStatus;
    private TextView tvMyConnectStatus;
    private TextView tvFileList;
    private EditText etInstruction;
    private RecyclerView rvDeviceList;
    private Button btnDisconnect;
    private Button btnChooseFile;
    private Button btnSend;
    private ImageView ivConnectStatus;

    private IConnectionContract.IPresenterController mPresenter;
    private OnFragmentInteractionListener mListener;

    public ConnectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConnectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConnectFragment newInstance(String param1, String param2) {
        ConnectFragment fragment = new ConnectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //获取
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
        mPresenter.registerBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);
        tvMyDeviceName = (TextView)rootView.findViewById(R.id.tvMyDeviceName);
        tvMyMacAddress = (TextView)rootView.findViewById(R.id.tvMyMacAddress);
        tvMyDeviceStatus = (TextView)rootView.findViewById(R.id.tvMyDeviceStatus);
        tvMyConnectStatus = (TextView)rootView.findViewById(R.id.tvMyConnectStatus);
        tvFileList = (TextView)rootView.findViewById(R.id.tvFileList);
//        rvDeviceList = (RecyclerView)rootView.findViewById(R.id.rvDeviceList);
        etInstruction = (EditText)rootView.findViewById(R.id.etInstruction);
        btnDisconnect = (Button)rootView.findViewById(R.id.btnDisconnect);
        btnChooseFile = (Button)rootView.findViewById(R.id.btnChooseFile);
        btnSend = (Button)rootView.findViewById(R.id.btnSend);
        ivConnectStatus = (ImageView)rootView.findViewById(R.id.ivConnectStatus);
        rvDeviceList = (RecyclerView)rootView.findViewById(R.id.rvDeviceList);

        //生成按钮的监听器 匿名内部类
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.disconnect();
            }
        });

        //文件选择按钮的监听器
        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileList("");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

        //发送按钮的监听器
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Objects.equals(etInstruction.getText().toString(), ""))
                mPresenter.startStringTransferTask(etInstruction.getText().toString());
            }
        });

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String text) {
        if (mListener != null) {
            mListener.onFragmentInteraction(text);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unregisterBroadcastReceiver();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.startFileTransferTask(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.connect, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuDirectEnable:{
                mPresenter.startWifiSetting();
                return true;
            }
            case R.id.menuDirectDiscover:{
                mPresenter.startDiscoverPeers();
                return true;
            }
            default:
                return true;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String text);
    }

    //region Implementation of IConnectionContract.IView
    //********************** Implementation of IConnectionContract.IView **************************

    @SuppressLint("RestrictedApi")
    @Override
    public void setPresenter(@NonNull IConnectionContract.IPresenterController presenter) {
        mPresenter = checkNotNull(presenter);
    }

    private LoadingDialog loadingDialog;

    @Override
    public void showLoadingDialog(String message) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this.getActivity());
        }
        loadingDialog.show(message, true, false);
    }

    @Override
    public void dismissLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 在Presenter中数据回调的方法中, 先检查View.isActive()是否为true, 来保证对Fragment的操作安全
     * 本功能没有用到数据模块，该方法未使用
     * @return isActive
     */
    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showFileList(String fileName) {
        tvFileList.setText(fileName);
    }

    @Override
    public void setBtnDisconnect(boolean enabled) {
        btnDisconnect.setEnabled(enabled);
    }

    @Override
    public void setBtnChooseFile(boolean enabled) {
        btnChooseFile.setEnabled(enabled);
    }

    @Override
    public void setBtnSend(boolean enabled) {
        btnSend.setEnabled(enabled);
    }

    @Override
    public void showStatus(String status) {
        tvMyConnectStatus.setText(status);
    }

    @Override
    public void showConnectStatus(boolean connected) {
        if (connected)  ivConnectStatus.setImageResource(R.drawable.ic_connected);
        else ivConnectStatus.setImageResource(R.drawable.ic_disconnected);
    }

    @Override
    public void showMyDeviceName(String deviceName) {
        tvMyDeviceName.setText(deviceName);
    }

    @Override
    public void showMyDeviceAddress(String deviceAddress) {
        tvMyMacAddress.setText(deviceAddress);
    }

    @Override
    public void showMyDeviceStatus(String deviceStatus) {
        tvMyDeviceStatus.setText(deviceStatus);
    }

    @Override
    public RecyclerView getRvDeviceList() {
        return rvDeviceList;
    }
//********************** Implementation of IConnectionContract.IView **************************
    //endregion
}
