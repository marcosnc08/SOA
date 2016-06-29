package com.unlam.alarmaseguridad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.Timer;

public class AlarmActivatedActivity extends AppCompatActivity {
    private BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_activated);

        final Button btnActivar = (Button)findViewById(R.id.btnDesactivarAlarma);
        btnActivar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Alarm.getInstance().setActivated(false);
                Alarm.getInstance().setPanic(false);
                Alarm.getInstance().setRinging(false);
                Alarm.getInstance().setSendMessage(true);

                Intent intentHome = new Intent(AlarmActivatedActivity.this, Home.class);
                startActivity(intentHome);
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(!Alarm.getInstance().isRinging()) {
                    Intent intentHome = new Intent(AlarmActivatedActivity.this, Home.class);
                    startActivity(intentHome);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(StatusIntentService.CHECK_ALARM_INFO)
        );
    }

    @Override
    protected void onStop()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        super.onStop();
    }

}
