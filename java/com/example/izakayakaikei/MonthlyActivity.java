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

public class MonthlyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly);

        // IntentからyearMonthを受け取る、なければ今月
        String currentYearMonth = getIntent().getStringExtra("yearMonth");
        if (currentYearMonth == null) currentYearMonth = DateUtil.getBusinessYearMonth();
        ((TextView) findViewById(R.id.tvMonthTitle)).setText(currentYearMonth + "の会計");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        SaveManager saveManager = new SaveManager(this);
        AppSettings settings    = saveManager.loadSettings();
        List<MenuItem> menuList = saveManager.loadMenuItems();
        List<HistoryRecord> allHistory = saveManager.loadHistory();

        // 今月分のみ抽出
        List<HistoryRecord> monthHistory = new ArrayList<>();
        for (HistoryRecord rec : allHistory) {
            String ym = rec.dateKey != null ? DateUtil.getYearMonthFromKey(rec.dateKey) : "";
            if (currentYearMonth.equals(ym)) monthHistory.add(rec);
        }

        // 日付ごとにグループ化
        Map<String, List<HistoryRecord>> byDate = new LinkedHashMap<>();
        for (HistoryRecord rec : monthHistory) {
            String key = rec.dateKey != null ? rec.dateKey : "不明";
            if (!byDate.containsKey(key)) byDate.put(key, new ArrayList<>());
            byDate.get(key).add(rec);
        }

        LinearLayout layout = findViewById(R.id.monthlyLayout);
        TextView tvMonthTotal  = findViewById(R.id.tvMonthTotal);
        TextView tvMonthProfit = findViewById(R.id.tvMonthProfit);

        if (byDate.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("今月の会計データはありません");
            empty.setTextColor(0xFFAAAAAA);
            empty.setPadding(0, 60, 0, 0);
            layout.addView(empty);
            tvMonthTotal.setText("今月合計：¥0");
            tvMonthProfit.setVisibility(View.GONE);
            return;
        }

        // 月合計
        int monthTotal = 0;
        for (HistoryRecord rec : monthHistory) monthTotal += rec.total;
        tvMonthTotal.setText("今月合計：¥" + String.format("%,d", monthTotal));

        // 純利益（設定オンの場合のみ）
        if (settings.profitEnabled) {
            int totalCost = 0;
            for (HistoryRecord rec : monthHistory) {
                for (OrderItem item : rec.items) {
                    // メニューリストから原価を検索
                    for (MenuItem m : menuList) {
                        if (m.name.equals(item.label) || item.label.startsWith(m.name)) {
                            totalCost += m.cost * item.qty;
                            break;
                        }
                    }
                }
            }
            int profit = monthTotal - totalCost;
            tvMonthProfit.setVisibility(View.VISIBLE);
            tvMonthProfit.setText("純利益：¥" + String.format("%,d", profit));
        } else {
            tvMonthProfit.setVisibility(View.GONE);
        }

        // 日付ボタンを表示
        for (Map.Entry<String, List<HistoryRecord>> entry : byDate.entrySet()) {
            String dateKey = entry.getKey();
            List<HistoryRecord> records = entry.getValue();
            int dayTotal = 0;
            for (HistoryRecord r : records) dayTotal += r.total;

            View btn = LayoutInflater.from(this).inflate(R.layout.item_day_button, layout, false);
            ((TextView) btn.findViewById(R.id.tvDayLabel)).setText(DateUtil.formatDateKeyToDisplay(dateKey) + "の会計");
            ((TextView) btn.findViewById(R.id.tvDayTotal)).setText("¥" + String.format("%,d", dayTotal));

            final String dk = dateKey;
            btn.setOnClickListener(v -> {
                Intent intent = new Intent(this, DayDetailActivity.class);
                intent.putExtra("dateKey", dk);
                startActivity(intent);
            });

            layout.addView(btn);
        }
    }
}
