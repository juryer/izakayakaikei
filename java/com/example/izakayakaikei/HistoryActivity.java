package com.example.izakayakaikei;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        SaveManager saveManager = new SaveManager(this);
        List<HistoryRecord> history = saveManager.loadHistory();
        LinearLayout layout = findViewById(R.id.historyLayout);

        if (history.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("履歴はありません");
            empty.setTextColor(0xFFAAAAAA);
            empty.setPadding(0, 60, 0, 0);
            layout.addView(empty);
            return;
        }

        for (HistoryRecord rec : history) {
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
        }
    }
}
