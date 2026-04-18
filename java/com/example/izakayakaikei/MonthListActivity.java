package com.example.izakayakaikei;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MonthListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_list);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        SaveManager saveManager = new SaveManager(this);
        List<HistoryRecord> allHistory = saveManager.loadHistory();

        // 月ごとにグループ化（順序保持）
        Map<String, List<HistoryRecord>> byMonth = new LinkedHashMap<>();
        for (HistoryRecord rec : allHistory) {
            String ym = rec.dateKey != null
                ? DateUtil.getYearMonthFromKey(rec.dateKey)
                : "不明";
            if (!byMonth.containsKey(ym)) byMonth.put(ym, new ArrayList<>());
            byMonth.get(ym).add(rec);
        }

        LinearLayout layout = findViewById(R.id.monthListLayout);

        if (byMonth.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("会計データがありません");
            empty.setTextColor(0xFFAAAAAA);
            empty.setPadding(0, 60, 0, 0);
            layout.addView(empty);
            return;
        }

        for (Map.Entry<String, List<HistoryRecord>> entry : byMonth.entrySet()) {
            String yearMonth = entry.getKey();
            int monthTotal = 0;
            for (HistoryRecord r : entry.getValue()) monthTotal += r.total;

            View btn = LayoutInflater.from(this).inflate(R.layout.item_day_button, layout, false);
            ((TextView) btn.findViewById(R.id.tvDayLabel)).setText(yearMonth + "の会計");
            ((TextView) btn.findViewById(R.id.tvDayTotal)).setText("¥" + String.format("%,d", monthTotal));

            final String ym = yearMonth;
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(this, MonthlyActivity.class);
                intent.putExtra("yearMonth", ym);
                startActivity(intent);
            });

            layout.addView(btn);
        }
    }
}
