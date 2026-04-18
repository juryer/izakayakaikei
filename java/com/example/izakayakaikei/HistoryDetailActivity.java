package com.example.izakayakaikei;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

// 履歴の内訳を表示する画面
// HistoryRecord を JSON 文字列で受け取って表示する
public class HistoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        String teamName   = getIntent().getStringExtra("teamName");
        String date       = getIntent().getStringExtra("date");
        int total         = getIntent().getIntExtra("total", 0);
        String itemsJson  = getIntent().getStringExtra("itemsJson");

        ((TextView) findViewById(R.id.tvHistDetailTitle)).setText(teamName);
        ((TextView) findViewById(R.id.tvHistDetailDate)).setText(date);
        ((TextView) findViewById(R.id.tvHistDetailTotal)).setText("合計：¥" + String.format("%,d", total));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        LinearLayout layout = findViewById(R.id.histDetailLayout);

        if (itemsJson != null) {
            Type type = new TypeToken<List<OrderItem>>() {}.getType();
            List<OrderItem> items = new Gson().fromJson(itemsJson, type);
            if (items != null) {
                for (OrderItem item : items) {
                    View row = LayoutInflater.from(this).inflate(R.layout.item_history_row, layout, false);
                    ((TextView) row.findViewById(R.id.histItemLabel)).setText(item.label);
                    ((TextView) row.findViewById(R.id.histItemAmount)).setText("¥" + String.format("%,d", item.amount));
                    layout.addView(row);
                }
            }
        }
    }
}
