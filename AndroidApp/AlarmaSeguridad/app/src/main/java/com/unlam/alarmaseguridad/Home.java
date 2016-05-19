package com.unlam.alarmaseguridad;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class Home extends Activity implements SensorEventListener {
    private SensorManager      sensor;
    private final static float ACC = 18;
    alarmStatuses alarmaActivada = alarmStatuses.ALARMA_DESACTIVADA;
    public enum alarmStatuses {
        ALARMA_ACTIVADA,
        ALARMA_DESACTIVADA
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sensor = (SensorManager) getSystemService("sensor");
        final ImageButton btnActivar = (ImageButton)findViewById(R.id.btnActivar);
        setTxtAlarmStatus();
        registerSensor();
        btnActivar.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                switchState();
            }
        });

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        registerSensor();

    }

    @Override
    protected void onStop()
    {
        unregisterSenser();

        super.onStop();
    }

    @Override
    protected void onPause()
    {
        unregisterSenser();

        super.onPause();
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

//        if (!done)
//        {
//            Toast.makeText(this, getResources().getString(R.string.sensor_unsupported), Toast.LENGTH_SHORT).show();
//            switchButton.setChecked(false);
//        }

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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    private void switchState() {
        final ImageButton btnActivar = (ImageButton)findViewById(R.id.btnActivar);
        if(alarmaActivada == alarmStatuses.ALARMA_DESACTIVADA) {
            btnActivar.setBackgroundResource(R.drawable.activaralarmaverde1);
            alarmaActivada = alarmStatuses.ALARMA_ACTIVADA;
        }
        else {
            btnActivar.setBackgroundResource(R.drawable.activaralarma1);
            alarmaActivada = alarmStatuses.ALARMA_DESACTIVADA;
        }
        setTxtAlarmStatus();
    }
}
