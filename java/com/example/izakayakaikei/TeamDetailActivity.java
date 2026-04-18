package com.example.izakayakaikei;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class TeamDetailActivity extends AppCompatActivity {

    private SaveManager saveManager;
    private List<Team> teams;
    private int teamIndex;
    private Team currentTeam;
    private LinearLayout itemsLayout;
    private TextView tvTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        saveManager = new SaveManager(this);
        teams       = saveManager.loadTeams();
        teamIndex   = getIntent().getIntExtra("teamIndex", 0);
        currentTeam = teams.get(teamIndex);

        ((TextView) findViewById(R.id.tvTeamTitle)).setText(currentTeam.name + "　内訳");
        itemsLayout = findViewById(R.id.itemsLayout);
        tvTotal     = findViewById(R.id.tvTotal);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        renderItems();
    }

    private void renderItems() {
        itemsLayout.removeAllViews();

        if (currentTeam.items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("注文がありません");
            empty.setTextColor(0xFFAAAAAA);
            empty.setPadding(0, 30, 0, 30);
            itemsLayout.addView(empty);
            tvTotal.setText("合計：¥0");
            return;
        }

        for (int i = 0; i < currentTeam.items.size(); i++) {
            final int idx = i;
            OrderItem item = currentTeam.items.get(i);

            View row = LayoutInflater.from(this).inflate(R.layout.item_order_edit, itemsLayout, false);

            TextView labelText = row.findViewById(R.id.itemLabel);
            TextView amountText = row.findViewById(R.id.itemAmount);
            TextView qtyText   = row.findViewById(R.id.itemQty);
            Button btnMinus    = row.findViewById(R.id.btnMinus);
            Button btnPlus     = row.findViewById(R.id.btnPlus);
            Button btnDelete   = row.findViewById(R.id.deleteBtn);

            // ラベルから商品名だけ取り出す（"ビール×3" → "ビール"）
            String baseName = item.label.contains("×") ? item.label.split("×")[0] : item.label;
            labelText.setText(baseName);
            qtyText.setText(String.valueOf(item.qty));
            amountText.setText("¥" + String.format("%,d", item.amount));

            // 単価を計算
            final int unitPrice = item.qty > 0 ? item.amount / item.qty : item.amount;

            btnMinus.setOnClickListener(v -> {
                if (item.qty <= 1) return; // 1未満にはしない（削除はdeleteボタンで）
                item.qty--;
                item.amount = unitPrice * item.qty;
                item.label = item.qty > 1 ? baseName + "×" + item.qty : baseName;
                save();
                renderItems();
            });

            btnPlus.setOnClickListener(v -> {
                item.qty++;
                item.amount = unitPrice * item.qty;
                item.label = item.qty > 1 ? baseName + "×" + item.qty : baseName;
                save();
                renderItems();
            });

            btnDelete.setOnClickListener(v -> {
                currentTeam.items.remove(idx);
                save();
                renderItems();
            });

            itemsLayout.addView(row);
        }

        tvTotal.setText("合計：¥" + String.format("%,d", currentTeam.getTotal()));
    }

    private void save() {
        teams.set(teamIndex, currentTeam);
        saveManager.saveTeams(teams);
    }
}
