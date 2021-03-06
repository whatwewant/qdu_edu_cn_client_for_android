package com.example.qdu_edu_cnclient.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Service.DatabaseHelper;
import Service.Login;
import Service.MyNetworkInfo;
import Service.TimeSet;
import Service.CheckForUpdate;


public class MainActivity extends Activity {

    private final String VERSION = CheckForUpdate.QDU_EDU_CN_VERSION;

    private TextView versionTextView;
    private Handler checkForUpdateHandler;

    private AutoCompleteTextView usernameView;
    private EditText passwordView;

    private CheckBox isSavePwdView;

    private Button loginButton;

    private Handler networkHandler;
    private Handler hadLoginedHandler;
    private Handler monitorButtonsEnable;

    private ProgressDialog progressDialog;

    private static String ROOT_URL = "http://10.0.109.2";
    private static String LOGIN_URL = ROOT_URL;

    // 保存用户名和密码的文件名
    private static final String FILE_NAME = "saveUserNamePwd";
    private SharedPreferences sharedPreferences;
    private String usernameContent;
    private String passwordContent;
    private boolean issavepwd;

    private String DATABASE_NAME = "PortalSignDB";
    private String TABLE_USER = "user";
    private DatabaseHelper dbHelper;
    private SQLiteDatabase sqliteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 创建数据库存储用户名密码
        dbHelper = new DatabaseHelper(this, DATABASE_NAME);
        sqliteDatabase = dbHelper.getReadableDatabase();

        versionTextView = (TextView)findViewById(R.id.version);
        versionTextView.setText("Version " + VERSION);
        checkForUpdateHandler = new CheckForUpdateHandler();

        usernameView = (AutoCompleteTextView)findViewById(R.id.username);
        passwordView = (EditText)findViewById(R.id.password);
        isSavePwdView = (CheckBox)findViewById(R.id.isSavePwd);

        loginButton = (Button)findViewById(R.id.login);

        networkHandler = new NetworkHandler();
        hadLoginedHandler = new HadLoginHandler();
        monitorButtonsEnable = new MonitorButtonsEnable();

        loginButton.setOnClickListener(new LoginListener());

        // AutoCompleteView
        List<String> autoCompleteList = new ArrayList<String>();
        // autoCompleteList.add("18765203305");
        //autoCompleteList.add("15764231503");
        //autoCompleteList.add("201140701039");
        // 数据库取出数据
        Cursor cursor = sqliteDatabase.query(TABLE_USER, new String[] {"username", "password", "save"}, null, null, null, null, null);
        final ArrayList<NameValuePair> userpass = new ArrayList<NameValuePair>();
        while (cursor.moveToNext()) {
            //取数据
            String username = cursor.getString(cursor.getColumnIndex("username"));
            String password = cursor.getString(cursor.getColumnIndex("password"));
            String save = cursor.getString(cursor.getColumnIndex("save"));

            userpass.add(new BasicNameValuePair(username, password+"#"+save));

            autoCompleteList.add(username);
        }
        cursor.close();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.list_item, autoCompleteList);
        usernameView.setAdapter(arrayAdapter);
        // setOnItemClickListener
        usernameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // System.out.println("onItemSelected: " + ((TextView) view).getText());
                for(NameValuePair each : userpass)
                    if (each.getName() == ((TextView) view).getText()) {
                        String [] tmp = each.getValue().split("#");
                        String tmppass = new String(Base64.decode(tmp[0], Base64.NO_WRAP));
                        boolean tmpsave = tmp[1].equals("1");
                        System.out.println(tmppass + "___" +tmp[1]);
                        if(tmpsave) {
                            passwordView.setText(tmppass);
                            isSavePwdView.setChecked(tmpsave);
                        }
                    }
            }
        });

        // 检查网络状态
        MyNetworkInfo myNetworkInfo = new MyNetworkInfo();
        boolean networkState= myNetworkInfo.networkOn(getApplicationContext());
        if (! networkState) {
            Toast.makeText(MainActivity.this, "没有网络链接", Toast.LENGTH_SHORT).show();
            loginButton.setText("未连接网络");
            loginButton.setEnabled(false);
            usernameView.setEnabled(false);
            passwordView.setEnabled(false);
            isSavePwdView.setEnabled(false);
            versionTextView.setEnabled(false);
        }

        // take time
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MyNetworkInfo myNetworkInfo = new MyNetworkInfo();
                Message msg = new Message();
                msg.obj = myNetworkInfo.networkOn(getApplicationContext());
                monitorButtonsEnable.sendMessage(msg);
            }
        };
        Timer timer = new Timer(true);
        timer.schedule(task, 1000, 3000);

        // 启动时载入数据
        Cursor loadData = sqliteDatabase.query(TABLE_USER, new String[]{"username", "password", "save"}, "save=? and active=?", new String[]{"1","1"}, null, null, null);
        System.out.println("In onCreate load data: count = ? " + loadData.getCount());
        //sharedPreferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        //issavepwd = sharedPreferences.getBoolean("issavepwd", false);
        //usernameContent = sharedPreferences.getString("username", "");
        //passwordContent = sharedPreferences.getString("password", "");


        if (loadData.getCount() == 1) {
            // 不能马上去数据
            while(loadData.moveToNext()) {
                usernameContent = loadData.getString(loadData.getColumnIndex("username"));
                passwordContent = new String(Base64.decode(loadData.getString(loadData.getColumnIndex("password")), Base64.NO_WRAP));
                // System.out.println("onCreate password : " + passwordContent);
                issavepwd = loadData.getString(loadData.getColumnIndex("save")).equals("1");
                isSavePwdView.setChecked(issavepwd);
                usernameView.setText(usernameContent);
                passwordView.setText(passwordContent);
            }
        } else {
            usernameView.setText(null);
            passwordView.setText(null);
        }
        loadData.close();

        versionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("QDU_EDU_CN Client")
                        .setMessage("检查更新?")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this, "检查更新中...", Toast.LENGTH_SHORT).show();
                                new CheckForUpdateThread(1).start();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this, "取消检查更新", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });
    }

    public class CheckForUpdateThread extends Thread {
        private int choice = 1;

        public CheckForUpdateThread() {}

        public CheckForUpdateThread(int value) {
            choice = value;
        }

        @Override
        public void run() {
            Message msg = new Message();
            msg.what = choice;
            if (choice == 1) {// 检测更新
                msg.obj = CheckForUpdate.check();
            }
            else if (choice == 2) {
                msg.obj = CheckForUpdate.update();
            }
            checkForUpdateHandler.sendMessage(msg);
        }
    }

    public class CheckForUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 1) {
                String message = (String)msg.obj;
                if (message == null) {
                    Toast.makeText(MainActivity.this, "检查失败,软件出错", Toast.LENGTH_SHORT).show();
                }


                if (message.contains("检测到新版本")) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("QDU_EDU_CN Client")
                            .setMessage(message + ", 是否更新?")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(MainActivity.this, "更新中...", Toast.LENGTH_SHORT).show();
                                    new CheckForUpdateThread(2).start();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(MainActivity.this, "取消更新", Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                } else {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == 2) {
                installAPK((File)msg.obj);
            }
        }
    }

    public class MonitorButtonsEnable extends Handler {
        @Override
        public void handleMessage(Message msg) {

            if (msg == null) {
                Toast.makeText(MainActivity.this, "Cannot Get Messages.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean networkState = (Boolean) msg.obj;
            if (!networkState) {
                // Toast.makeText(MainActivity.this, "网络未连接", Toast.LENGTH_SHORT).show();
                loginButton.setText("未连接网络");
                loginButton.setEnabled(false);
                usernameView.setEnabled(false);
                passwordView.setEnabled(false);
                isSavePwdView.setEnabled(false);
                versionTextView.setEnabled(false);
            } else {
                // Toast.makeText(MainActivity.this, "网络已连接", Toast.LENGTH_SHORT).show();
                loginButton.setText("登入");
                loginButton.setEnabled(true);
                usernameView.setEnabled(true);
                passwordView.setEnabled(true);
                isSavePwdView.setEnabled(true);
                versionTextView.setEnabled(true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("QDU_EDU_CN Client")
                .setMessage("确认退出?")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.super.onBackPressed();
                        // kill this process
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
    }

    class LoginListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            new NetworkThreaed().start();
            // progressDialog = ProgressDialog.show(MainActivity.this, "请稍候", "正在登入...", false);
        }
    }

    class NetworkThreaed extends Thread {
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void run() {
            //
            issavepwd = isSavePwdView.isChecked();
            usernameContent = usernameView.getText().toString().trim();
            passwordContent = passwordView.getText().toString().trim();
            // SharedPreferences.Editor editor = sharedPreferences.edit();

            //if(issavepwd) {
            // 添加要保存的数据
            // 在保存数据之前，检查数据库是否存在该用户，如存在，直接修改;否则创建
            //editor.putString("username", usernameContent);
            //editor.putString("password", passwordContent);

            // 数据库插入
            dbHelper = new DatabaseHelper(MainActivity.this, DATABASE_NAME);
            sqliteDatabase = dbHelper.getWritableDatabase();
            // ContentValue
            ContentValues values = new ContentValues();
            values.put("username", usernameContent);
            values.put("password", Base64.encodeToString(passwordContent.getBytes(), Base64.NO_WRAP));
            values.put("save", issavepwd);
            if(issavepwd) {
                // active用于初始化程序时载入的数据只有一条
                values.put("active", true);
                // 将其他active == true 的数据 false; sqlite3数据库实际上存储1
                //Cursor deactive = sqliteDatabase.query(TABLE_USER, null, "active=?", new String[]{"1"}, null, null, null);
                //System.out.println("in NetworkThread active Count = ?  " + deactive.getCount());
                ContentValues tmpDeactive = new ContentValues();
                tmpDeactive.put("active", false);
                sqliteDatabase.update(TABLE_USER, tmpDeactive, "active=?", new String[]{"1"});
            }
            // 检查冲突
            Cursor cursor = sqliteDatabase.query(TABLE_USER, new String[]{"password", "save", "active"}, "username=?", new String[]{usernameContent}, null, null, null);

            // insert or update
            // System.out.println("cursor exists ? " + cursor.getCount());
            if (cursor.getCount() == 0 ) {
                // insert
                sqliteDatabase.insert(TABLE_USER, null, values);
            } else {
                // update
                sqliteDatabase.update(TABLE_USER, values, "username=?", new String[]{usernameContent});
            }
            //}
            //editor.putBoolean("issavepwd", issavepwd);
            // 确认保存
            //editor.commit();


            /*System.out.println("username: " + usernameContent.isEmpty());
            System.out.println("username: " + usernameContent);
            System.out.println("password: " + passwordContent.isEmpty());
            System.out.println("password: " + passwordContent);
            System.out.println("issavepwd: " + issavepwd);*/

            //System
            Message msg = new Message();
            if (usernameContent.isEmpty() || passwordContent.isEmpty()) {
                msg.what = -1;
            } else {
                try {
                    String result = Login.loginPortal(LOGIN_URL, usernameContent, passwordContent);
                    System.out.println("Login: " + result);
                    //
                    msg.obj = result;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            networkHandler.sendMessage(msg);
        }
    }

    class HadLoginHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if((Boolean)msg.obj) {
                Toast.makeText(MainActivity.this, "已经登入，勿重复登入,可以直接上网了", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SuccessLogin.class);
                startActivity(intent);
            }
        }
    }

    class NetworkHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // progressDialog.dismiss();
            // Just test
            /*if (true) {
                Intent tintent = new Intent();
                tintent.setClass(MainActivity.this, SuccessLogin.class);
                startActivity(tintent);
                return ;
            }*/

            if (msg.what == -1) {
                Toast.makeText(MainActivity.this, "用户名、密码不能为空", Toast.LENGTH_SHORT).show();
            } else {
                String [] serverMessage = {"Logged in successfully.", "已经登录", "E63022: 在线用户数量限制。", "E63032: 用户密码错误，您还可以重试9次（如果您连续输入错误的密码10次后，将被加入黑名单，第二天才能解除，或者请联系管理员）。"};
                String returnMessage = (String)msg.obj;

                if (null == returnMessage) {
                    Toast.makeText(MainActivity.this, "请检查是否连接到Wlan: QDU_EDU_CN", Toast.LENGTH_SHORT).show();
                    return ;
                }

                if ( returnMessage.equals(serverMessage[0])) {
                    Toast.makeText(MainActivity.this, "登入成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, SuccessLogin.class);
                    startActivity(intent);
                } else if (returnMessage.equals(serverMessage[1])) {
                    Toast.makeText(MainActivity.this, "已经登入，勿重复登入,可以直接上网了", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, SuccessLogin.class);
                    startActivity(intent);
                } else if (returnMessage.equals("在线用户数量限制")) {
                    Toast.makeText(MainActivity.this, "在线用户数量限制。请使用其他帐号登入。", Toast.LENGTH_SHORT).show();
                } else if (returnMessage.equals("用户不存在或者用户没有申请该服务")) {
                    Toast.makeText(MainActivity.this, "用户不存在或者用户没有申请该服务", Toast.LENGTH_SHORT).show();
                } else if (returnMessage.indexOf("E63032") != -1){
                    Toast.makeText(MainActivity.this, returnMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Unknown Error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void installAPK(File apk) {
        // TODO Auto-generated method stub
        Log.e("OpenFile", apk.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apk),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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