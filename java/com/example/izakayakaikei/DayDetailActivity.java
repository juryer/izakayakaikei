package com.example.izakayakaikei;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class DayDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);

        String dateKey = getIntent().getStringExtra("dateKey");
        String displayDate = DateUtil.formatDateKeyToDisplay(dateKey);
        ((TextView) findViewById(R.id.tvDayTitle)).setText(displayDate + "の会計");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        SaveManager saveManager = new SaveManager(this);
        List<HistoryRecord> allHistory = saveManager.loadHistory();

        List<HistoryRecord> dayHistory = new ArrayList<>();
        for (HistoryRecord rec : allHistory) {
            if (dateKey.equals(rec.dateKey)) dayHistory.add(rec);
        }

        LinearLayout layout = findViewById(R.id.dayDetailLayout);
        int dayTotal = 0;

        for (HistoryRecord rec : dayHistory) {
            View card = LayoutInflater.from(this).inflate(R.layout.item_history_card, layout, false);
            ((TextView) card.findViewById(R.id.tvHistTeamName)).setText(rec.teamName);
            ((TextView) card.findViewById(R.id.tvHistDate)).setText(rec.date);
            ((TextView) card.findViewById(R.id.tvHistTotal)).setText("¥" + String.format("%,d", rec.total));

            // タップで内訳表示
            String itemsJson = new Gson().toJson(rec.items);
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, HistoryDetailActivity.class);
                intent.putExtra("teamName", rec.teamName);
                intent.putExtra("date", rec.date);
                intent.putExtra("total", rec.total);
                intent.putExtra("itemsJson", itemsJson);
                startActivity(intent);
            });

            layout.addView(card);
            dayTotal += rec.total;
        }

        ((TextView) findViewById(R.id.tvDayTotal)).setText("合計：¥" + String.format("%,d", dayTotal));
    }
}
