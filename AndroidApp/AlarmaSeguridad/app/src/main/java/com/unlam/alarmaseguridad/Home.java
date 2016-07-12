package com.unlam.alarmaseguridad;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class Home extends Activity implements SensorEventListener {
    public static Home activity;
    private SensorManager sensor;
    private Sensor mProximity;
    private final static float ACC = 18;
    alarmStatuses alarmaActivada = alarmStatuses.ALARMA_DESACTIVADA;
    private BroadcastReceiver receiver;

    public enum alarmStatuses {
        ALARMA_ACTIVADA,
        ALARMA_DESACTIVADA,
        ACTIVANDO_ALARMA,
        DESACTIVANDO_ALARMA
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=this;

        StatusIntentService.startActionFoo(this);

        setContentView(R.layout.activity_home);
        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);
        mProximity = sensor.getDefaultSensor(Sensor.TYPE_PROXIMITY);


        final ImageButton btnActivar = (ImageButton)findViewById(R.id.btnActivar);
        final ImageButton btnPanic = (ImageButton)findViewById(R.id.btnPanic);
        final Button btnLogs = (Button)findViewById(R.id.btnLogs);
        setTxtAlarmStatus();
        registerSensor();
        
        btnActivar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                switchState();
            }
        });
        btnPanic.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                btnPanicHandler();
            }
        });
        btnLogs.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                btnLogsHandler();
            }
        });

        final Home home = this;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                home.SyncBtnAlarmaActivadaState();
            }
        };
    }

    private void btnLogsHandler() {
        Intent intent = new Intent(Home.this, LogsActivity.class);
        startActivity(intent);
    }

    public void SyncBtnAlarmaActivadaState() {
        final ImageButton btnActivar = (ImageButton) findViewById(R.id.btnActivar);
        final TextView txtGas = (TextView) findViewById(R.id.txtGas);
        Alarm alarm = Alarm.getInstance();

        if (alarm.isRinging()) {
            this.AlarmActivated();

        } else {
            if (alarm.isActivated()) {
                btnActivar.setBackgroundResource(R.drawable.activaralarmaverde1);
                alarmaActivada = alarmStatuses.ALARMA_ACTIVADA;
            } else {
                btnActivar.setBackgroundResource(R.drawable.activaralarma1);
                alarmaActivada = alarmStatuses.ALARMA_DESACTIVADA;

            }
            setTxtAlarmStatus();
        }

        txtGas.setText(alarm.getGas());
    }

    private void AlarmActivated(){
        Intent intent = new Intent(Home.this, AlarmActivatedActivity.class);
        startActivity(intent);

    }
    private void btnPanicHandler() {
        Alarm.getInstance().setPanic(true);

        Alarm.getInstance().setSendMessage(true);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        registerSensor();
        sensor.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
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
        unregisterSenser();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        super.onStop();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterSenser();
        sensor.unregisterListener(this);
    }
    private void unregisterSenser()
    {
        sensor.unregisterListener(this);
        Log.i("sensor", "unregister");
    }

    protected void setTxtAlarmStatus() {
        TextView txtAlarmStatus = (TextView)findViewById(R.id.alarmStatus);
        txtAlarmStatus.setText(alarmaActivada.toString().replaceAll("_", " "));
    }

    private void registerSensor()
    {
        sensor.registerListener(this, sensor.getDefaultSensor(1), 3);
        Log.i("sensor", "register");
    }

    public void onSensorChanged(SensorEvent event)
    {

        int sensorType = event.sensor.getType();

        float[] values = event.values;

        if (sensorType == Sensor.TYPE_ACCELEROMETER)
        {
            if ((Math.abs(values[0]) > ACC || Math.abs(values[1]) > ACC || Math.abs(values[2]) > ACC))
            {
                switchState();
                Log.i("sensor", "running");
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] == 0) {
                switchState();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    private void switchState() {
        Alarm alarm = Alarm.getInstance();
        final ImageButton btnActivar = (ImageButton)findViewById(R.id.btnActivar);
        if(!alarm.isActivated()) {
            //btnActivar.setBackgroundResource(R.drawable.activaralarmaverde1);
            alarmaActivada = alarmStatuses.ACTIVANDO_ALARMA;

        }
        else {
            //btnActivar.setBackgroundResource(R.drawable.activaralarma1);
            alarmaActivada = alarmStatuses.DESACTIVANDO_ALARMA;

        }

        alarm.setChange(true);
        setTxtAlarmStatus();
    }
}
