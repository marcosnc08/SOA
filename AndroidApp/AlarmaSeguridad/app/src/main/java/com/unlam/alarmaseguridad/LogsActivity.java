package com.unlam.alarmaseguridad;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.ArrayList;

public class LogsActivity extends ListActivity {

    ArrayList<String> listItems=new ArrayList<>();

    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_logs);

        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);

        loadLogs();
    }

    public void addItem(String log) {
        listItems.add(0,log);

        adapter.notifyDataSetChanged();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void loadLogs() {
        Alarm alarm = Alarm.getInstance();
        String allLogs = alarm.getLogs();

        if(!allLogs.isEmpty()) {
            String[] logs = allLogs.split("\\|");

            for (String i: logs) {
                addItem(i);
            }
        }
    }
}
