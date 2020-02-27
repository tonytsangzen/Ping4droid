package com.rokid.networktest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.SharedElementCallback;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;

import java.io.IOException;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView mInfo;
    private Button mButton;
    private int mSelectNetworkType = ConnectivityManager.TYPE_MOBILE;
    private boolean mConnectStatus;

    final String TARGET_HOST = "www.baidu.com";

    private static String getInternetIp() throws Exception{
        try {
            // 打开连接

            Document doc = Jsoup.connect("http://chaipip.com/").get();
            Elements eles = doc.select("#ip");
            return eles.attr("value");
        }catch (IOException e) {
            e.printStackTrace();
        }

        return InetAddress.getLocalHost().getHostAddress();
    }

    public Handler  handler =new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            Log.d("NETWORKTEST:", msg.obj.toString());

            mInfo.setText(msg.obj.toString());
            if(mConnectStatus)
                mInfo.setTextColor(Color.GREEN);
            else
                mInfo.setTextColor(Color.RED);

            super.handleMessage(msg);
        }

    };

    private View.OnClickListener onButtonStartClick = new View.OnClickListener(){
        public void onClick(View v) {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

            for (Network net : connMgr.getAllNetworks()) {
                NetworkInfo networkInfo = connMgr.getNetworkInfo(net);

                if ( networkInfo.getType()== mSelectNetworkType) {
                    connMgr.bindProcessToNetwork(net);

                    if(mSelectNetworkType == ConnectivityManager.TYPE_WIFI){
                        mButton.setText("切换到4G");
                        mSelectNetworkType = ConnectivityManager.TYPE_MOBILE;
                    }else{
                        mButton.setText("切换到WIFI");
                        mSelectNetworkType = ConnectivityManager.TYPE_WIFI;
                    }

                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfo = findViewById(R.id.textView);
        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(onButtonStartClick);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Message msg = new Message();
                        msg.obj = "HOST NAME：" + TARGET_HOST + "\n";

                        InetAddress ip = InetAddress.getByName(TARGET_HOST);
                        //InetAddress ip = InetAddress.getByAddress(new byte[] {114,114,114,114});
                        msg.obj += "IP Address:" + ip.toString() + "\n";

                        mConnectStatus = ip.isReachable(1000);
                        msg.obj += "Status: " + (mConnectStatus?"Connected":"Disconnected") + "\n";

                        //String myip = getInternetIp();
                        //msg.obj += "My IP: " + myip + "\n";

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                        msg.obj += "Time: " + simpleDateFormat.format(System.currentTimeMillis());

                        handler.sendMessage(msg);
                        if(mConnectStatus)
                            Thread.sleep(1000);
                    }catch (Exception e){
                        mConnectStatus = false;
                        Message msg = new Message();
                        msg.obj = e.toString();
                        handler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }
}
