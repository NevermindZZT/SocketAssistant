package com.letter.socketassistant.connection;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.letter.socketassistant.R;

public class ConnectionConfigActivity extends AppCompatActivity {

    private int type;

    private Button confirmButton;
    private LinearLayout localPortLayout;
    private LinearLayout remoteIpLayout;
    private LinearLayout remotePortLayout;
    private EditText localPortText;
    private EditText remoteIpText;
    private EditText remotePortText;

    private final String TAG = "Config";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conn_config);

        confirmButton = findViewById(R.id.confirm_button);
        localPortLayout = findViewById(R.id.local_port_layout);
        remoteIpLayout = findViewById(R.id.remote_ip_layout);
        remotePortLayout = findViewById(R.id.remote_port_layout);
        localPortText = findViewById(R.id.local_port);
        remoteIpText = findViewById(R.id.remote_ip);
        remotePortText = findViewById(R.id.remote_port);

        final Intent intent = getIntent();
        type = intent.getIntExtra("connection_type", ConnectionInfo.CONN_TCP_SERVER);
        switch (type) {
            case ConnectionInfo.CONN_TCP_SERVER:
                this.setTitle("TCP Server");
                remoteIpLayout.setVisibility(View.GONE);
                remotePortLayout.setVisibility(View.GONE);
                break;
            case ConnectionInfo.CONN_TCP_CLIENT:
                this.setTitle("TCP Client");
                localPortLayout.setVisibility(View.GONE);
                break;
            case ConnectionInfo.CONN_UDP:
                this.setTitle("UDP");
                break;
            default :
                break;
        }


        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectionInfo connectionInfo = new ConnectionInfo();
                connectionInfo.setType(type);
                try {
                    if (localPortLayout.getVisibility() != View.GONE) {
                        connectionInfo.setLocalPort(Integer.parseInt(localPortText.getText().toString()));
                    }
                    if (remoteIpLayout.getVisibility() != View.GONE) {
                        connectionInfo.setRemoteIp(remoteIpText.getText().toString());
                    }
                    if (remotePortLayout.getVisibility() != View.GONE) {
                        connectionInfo.setRemotePort(Integer.parseInt(remotePortText.getText().toString()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent1 = new Intent();
                intent1.putExtra("connection_info", connectionInfo);
                setResult(RESULT_OK, intent1);
                Log.d(TAG, "onClick: finish");
                finish();
            }
        });
    }
}
