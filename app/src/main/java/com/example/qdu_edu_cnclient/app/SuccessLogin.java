package com.example.qdu_edu_cnclient.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import Service.Logout;
import Service.TimeSet;


public class SuccessLogin extends Activity {

    private static final String SIGNOUT_URL = "http://172.20.1.1/portal/logout.jsp?language=English&userip=null";

    private Button signOutButton;
    private Button startVpnBtn;
    private Button startShadowsocksBtn;
    private Button startSchoolNetBtn;

    private TextView ipView;
    private TextView timeView;

    private Handler signOutHandler;
    private Handler timeViewHandler;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_login);


        signOutButton = (Button)findViewById(R.id.signOut);
        startVpnBtn = (Button)findViewById(R.id.startVpnBtn);
        startShadowsocksBtn = (Button)findViewById(R.id.shadowsocksBtn);
        startSchoolNetBtn = (Button)findViewById(R.id.startSchoolNetBtn);

        ipView = (TextView)findViewById(R.id.ipView);
        timeView = (TextView)findViewById(R.id.timeView);

        signOutHandler = new SignOutHandler();
        timeViewHandler = new TimeViewHandler();

        //MyNetworkInfo myNetworkInfo = new MyNetworkInfo();
        //myNetworkInfo.networkAvailable(getApplicationContext());

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(SuccessLogin.this)
                        .setTitle("PortalSign 注销")
                        .setMessage("确认注销?")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // progressDialog = ProgressDialog.show(SuccessLogin.this, "请稍候", "正在注销...", false);
                                new SignOutThread().start();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }).show();
            }
        });

        startVpnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.net.vpn.SETTINGS");
                if (null == intent) {
                    Toast.makeText(SuccessLogin.this, "No VPN Settings Available.", Toast.LENGTH_LONG).show();
                } else {
                    startActivity(intent);
                }
            }
        });

        startShadowsocksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager pm = getPackageManager();

                Intent intent = pm.getLaunchIntentForPackage("com.github.shadowsocks");
                if (null == intent) {
                    Toast.makeText(SuccessLogin.this, "You never install shadowsocks.", Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(SuccessLogin.this)
                            .setTitle("Warning")
                            .setMessage("Are you sure to download a shadowsocks client ?")
                            .setPositiveButton("Do", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Uri uri = Uri.parse("https://github.com/whatwewant/qdu_edu_cn_client_for_android/raw/ssapp/ss.apk");
                                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(it);
                                }
                            })
                            .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            }).show();
                } else {
                    startActivity(intent);
                }
            }
        });

        startSchoolNetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager pm = getPackageManager();

                Intent intent = pm.getLaunchIntentForPackage("com.smith.cole.domportal");
                if (null == intent) {
                    Toast.makeText(SuccessLogin.this, "You never install DomPortal.", Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(SuccessLogin.this)
                            .setTitle("Warning")
                            .setMessage("Are you sure to download a DomPortal ?")
                            .setPositiveButton("Do", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Uri uri = Uri.parse("https://github.com/whatwewant/DomPortal/raw/master/app/app-release.apk");
                                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(it);
                                }
                            })
                            .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            }).show();
                } else {
                    startActivity(intent);
                }
            }
        });

        ipView.setText(getLocalIpAddress());

        // take time
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                String timeCaculate = TimeSet.takeTime();
                msg.obj = timeCaculate;
                /*
                if (Integer.parseInt(timeCaculate.split(":")[2]) >= 10) {
                    try {
                        AnalyseLoginReturnResult analyser = new AnalyseLoginReturnResult();
                        if (analyser.HadLoginedByBaidu() == false) {
                            msg.what = -1;
                        } else {
                            msg.what = 1;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/
                timeViewHandler.sendMessage(msg);
            }
        };
        Timer timer = new Timer(true);
        timer.schedule(task, 1000, 1000);

    }

    class TimeViewHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            timeView.setText((String)msg.obj);
            if(msg.what == -1) {
                Toast.makeText(SuccessLogin.this, "您已断网", Toast.LENGTH_SHORT).show();
            } else if (msg.what == 1){
                Toast.makeText(SuccessLogin.this, "您已联网", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class SignOutThread extends Thread {
        @Override
        public void run() {
            Message msg = new Message();
            msg.obj = Logout.logout();
            signOutHandler.sendMessage(msg);
        }
    }

    class SignOutHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // System.out.println("in SignOutHandler: " + (String)msg.obj);
            String returnMessage = (String)msg.obj;

            if (null == returnMessage) {
                Toast.makeText(SuccessLogin.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                return ;
            }


            Toast.makeText(SuccessLogin.this, returnMessage, Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        // 判断Wifi是否开启
        // wifiManager.isWifiEnabled();

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    private String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    @Override
    public void onBackPressed() {
        /*new AlertDialog.Builder(SuccessLogin.this)
                .setTitle("PortalSign 注销")
                .setMessage("确认注销?")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // progressDialog = ProgressDialog.show(SuccessLogin.this, "请稍候", "正在注销...", false);
                        // new SignOutThread().start();
                        SuccessLogin.super.onBackPressed();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();*/
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
