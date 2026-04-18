package com.example.izakayakaikei;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnAccounting).setOnClickListener(v ->
            startActivity(new Intent(this, AccountingActivity.class))
        );
        findViewById(R.id.btnMenuSettings).setOnClickListener(v ->
            startActivity(new Intent(this, MenuSettingsActivity.class))
        );
        findViewById(R.id.btnMonthly).setOnClickListener(v ->
            startActivity(new Intent(this, MonthListActivity.class))
        );
        findViewById(R.id.btnHistory).setOnClickListener(v ->
            startActivity(new Intent(this, HistoryActivity.class))
        );
        findViewById(R.id.btnSettings).setOnClickListener(v ->
            startActivity(new Intent(this, SettingsActivity.class))
        );
        findViewById(R.id.btnHelp).setOnClickListener(v ->
            startActivity(new Intent(this, HelpActivity.class))
        );
    }
}
