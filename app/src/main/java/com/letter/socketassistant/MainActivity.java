package com.letter.socketassistant;

import com.letter.socketassistant.connection.ConnectionConfigActivity;
import com.letter.socketassistant.connection.ConnectionInfo;
import com.letter.socketassistant.connection.ConnectionReceivedListener;
import com.letter.socketassistant.connection.TcpClientConnection;
import com.letter.socketassistant.connection.UdpConnection;
import com.letter.socketassistant.esptouch.EsptouchActivity;
import com.letter.socketassistant.message.Message;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.letter.socketassistant.message.MessageAdapter;
import com.letter.socketassistant.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int MSG_RECV = 1;

    private List<Message> msgList = new ArrayList<>();

    private int connectionType = ConnectionInfo.CONN_NULL;
    private TcpClientConnection tcpClientConnection;
    private UdpConnection udpConnection;

    private TextView localIpText;
    private EditText inputText;
    private ImageButton sendButton;
    private RecyclerView msgRecyclerView;
    private MessageAdapter adapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton addButton;
    private LinearLayout extraPanelLayout;
    private LinearLayout extraConnect;

    private final String TAG = "MainActivity";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_RECV:
                    receivedMessage(msg.getData().getString("msg"));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.Toolbar);
        setSupportActionBar(toolbar);
        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }


        inputText = findViewById(R.id.input_text);
        sendButton = findViewById(R.id.send_button);
        msgRecyclerView = findViewById(R.id.recycler_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        addButton = findViewById(R.id.add_button);
        extraPanelLayout = findViewById(R.id.extra_panel);
        extraConnect = findViewById(R.id.extra_connect);

        extraPanelLayout.setVisibility(View.GONE);

        localIpText = navigationView.getHeaderView(0).findViewById(R.id.head_local_ip);
        localIpText.setText(CommonUtil.getIpAddress(MainActivity.this));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    switch (connectionType) {
                        case ConnectionInfo.CONN_TCP_CLIENT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    tcpClientConnection.write(inputText.getText().toString());
                                }
                            }).start();
                            break;

                        case ConnectionInfo.CONN_TCP_SERVER:
                            break;

                        case ConnectionInfo.CONN_UDP:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    udpConnection.write(inputText.getText().toString());
                                }
                            }).start();
                            break;

                        case ConnectionInfo.CONN_NULL:
                            Toast.makeText(MainActivity.this, "none connection", Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            break;
                    }
                    if (connectionType != ConnectionInfo.CONN_NULL) {
                        Message message = new Message(content, Message.TYPE_SEND);
                        addItem(message);
                    }
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.nav_tcp_server:
                        intent = new Intent(MainActivity.this, ConnectionConfigActivity.class);
                        intent.putExtra("connection_type", ConnectionInfo.CONN_TCP_SERVER);
                        startActivityForResult(intent, 1);
                        break;

                    case R.id.nav_tcp_client:
                        intent = new Intent(MainActivity.this, ConnectionConfigActivity.class);
                        intent.putExtra("connection_type", ConnectionInfo.CONN_TCP_CLIENT);
                        startActivityForResult(intent, 1);
                        break;

                    case R.id.nav_udp:
                        intent = new Intent(MainActivity.this, ConnectionConfigActivity.class);
                        intent.putExtra("connection_type", ConnectionInfo.CONN_UDP);
                        startActivityForResult(intent, 1);
                        break;
                    case R.id.nav_esp:
                        intent = new Intent(MainActivity.this, EsptouchActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_about:
                        intent = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(intent);
                        break;

                    default:
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm =  (InputMethodManager)getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
                if(imm != null) {
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }
                if (extraPanelLayout.getVisibility() != View.GONE) {
                    extraPanelLayout.setVisibility(View.GONE);
                } else {
                    extraPanelLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        inputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extraPanelLayout.getVisibility() != View.GONE) {
                    extraPanelLayout.setVisibility(View.GONE);
                }
            }
        });
        inputText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && extraPanelLayout.getVisibility() != View.GONE) {
                    extraPanelLayout.setVisibility(View.GONE);
                }
            }
        });

        extraConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (connectionType) {
                    case ConnectionInfo.CONN_TCP_SERVER:
                        break;
                    case ConnectionInfo.CONN_TCP_CLIENT:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                tcpClientConnection.disconnect();
                            }
                        }).start();
                        break;
                    case ConnectionInfo.CONN_UDP:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                udpConnection.disconnect();
                            }
                        }).start();
                        break;
                    default:
                        break;
                }
                connectionType = ConnectionInfo.CONN_NULL;
                sendButton.setImageResource(R.drawable.ic_send_gray);
                Toast.makeText(MainActivity.this, "disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;

            default:
                break;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    ConnectionInfo connectionInfo = (ConnectionInfo) data.getSerializableExtra("connection_info");
                    Log.d(TAG, "onActivityResult: type" + String.valueOf(connectionInfo.getType()));
                    switch (connectionInfo.getType()) {
                        case ConnectionInfo.CONN_TCP_CLIENT:
                            connectionType = ConnectionInfo.CONN_TCP_CLIENT;
                            tcpClientConnection = new TcpClientConnection(connectionInfo.getRemoteIp(), connectionInfo.getRemotePort());
                            tcpClientConnection.start();
                            tcpClientConnection.setConnectionReceivedListener(new ConnectionReceivedListener() {
                                @Override
                                public void onReceivedListener(String string) {
                                    android.os.Message message = new android.os.Message();
                                    message.what = MSG_RECV;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("msg", string);
                                    message.setData(bundle);
                                    handler.sendMessage(message);
                                }
                            });
                            break;

                        case ConnectionInfo.CONN_TCP_SERVER:
                            break;

                        case ConnectionInfo.CONN_UDP:
                            connectionType = ConnectionInfo.CONN_UDP;
                            udpConnection = new UdpConnection(connectionInfo.getRemoteIp(), connectionInfo.getRemotePort(), connectionInfo.getLocalPort());
                            udpConnection.start();
                            udpConnection.setConnectionReceivedListener(new ConnectionReceivedListener() {
                                @Override
                                public void onReceivedListener(String string) {
                                    android.os.Message message = new android.os.Message();
                                    message.what = MSG_RECV;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("msg", string);
                                    message.setData(bundle);
                                    handler.sendMessage(message);
                                }
                            });
                            break;

                        default:
                            break;
                    }
                    if (connectionType != ConnectionInfo.CONN_NULL) {
                        sendButton.setImageResource(R.drawable.ic_send);
                    }
                }
                break;

            default:
                break;
        }
    }


    private void addItem(Message message) {
        msgList.add(message);
        adapter.notifyItemInserted(msgList.size() - 1);
        msgRecyclerView.scrollToPosition(msgList.size() - 1);
        inputText.setText("");
    }


    private void receivedMessage (String str) {
        Message message = new Message(str, Message.TYPE_RECEIVED);
        addItem(message);
    }
}
