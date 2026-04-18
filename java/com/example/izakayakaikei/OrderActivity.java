package com.example.izakayakaikei;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity {

    private SaveManager saveManager;
    private List<Team> teams;
    private List<MenuItem> menu;
    private int teamIndex;
    private Team currentTeam;
    private AppSettings settings;

    // 今回のセッションで選択した商品（まだ保存しない）
    private List<OrderItem> sessionItems = new ArrayList<>();

    private LinearLayout drinkGrid;
    private LinearLayout foodGrid;
    private LinearLayout orderItemsLayout;
    private TextView tvOrderTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        saveManager  = new SaveManager(this);
        teams        = saveManager.loadTeams();
        menu         = saveManager.loadMenuItems();
        settings     = saveManager.loadSettings();
        teamIndex    = getIntent().getIntExtra("teamIndex", 0);
        currentTeam  = teams.get(teamIndex);

        ((TextView) findViewById(R.id.tvTeamTitle)).setText(currentTeam.name);

        drinkGrid        = findViewById(R.id.drinkGrid);
        foodGrid         = findViewById(R.id.foodGrid);
        orderItemsLayout = findViewById(R.id.orderItemsLayout);
        tvOrderTotal     = findViewById(R.id.tvOrderTotal);

        // 戻るボタン → 保存せずに戻る
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 注文を追加するボタン → 保存して戻る
        findViewById(R.id.btnCheckout).setOnClickListener(v -> checkout());

        buildMenuGrids();
        renderSessionItems();
    }

    private void buildMenuGrids() {
        drinkGrid.removeAllViews();
        foodGrid.removeAllViews();

        List<MenuItem> drinks = new ArrayList<>();
        List<MenuItem> foods  = new ArrayList<>();
        for (MenuItem m : menu) {
            if ("food".equals(m.category)) foods.add(m);
            else drinks.add(m);
        }

        buildGrid(drinkGrid, drinks);
        buildGrid(foodGrid, foods);

        findViewById(R.id.drinkSection).setVisibility(drinks.isEmpty() ? View.GONE : View.VISIBLE);
        findViewById(R.id.foodSection).setVisibility(foods.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void buildGrid(LinearLayout grid, List<MenuItem> items) {
        grid.removeAllViews();
        if (items.isEmpty()) return;

        LinearLayout row = null;
        for (int i = 0; i < items.size(); i++) {
            final MenuItem m = items.get(i);
            final int menuIdx = menu.indexOf(m);

            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
                grid.addView(row);
            }

            View btn = LayoutInflater.from(this).inflate(R.layout.item_tap_button, row, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(4, 4, 4, 4);
            btn.setLayoutParams(lp);

            ((TextView) btn.findViewById(R.id.menuName)).setText(m.name);
            int displayPrice = settings.taxEnabled ? (int)(m.price * 1.1) : m.price;
            ((TextView) btn.findViewById(R.id.menuPrice)).setText("¥" + String.format("%,d", displayPrice));

            TextView badge = btn.findViewById(R.id.menuQtyBadge);

            // セッション内の数量をバッジに表示
            refreshBadge(badge, menuIdx);

            btn.findViewById(R.id.itemTapBtn).setOnClickListener(v -> {
                // sessionItemsに追加（まだ保存しない）
                OrderItem existing = null;
                for (OrderItem it : sessionItems) {
                    if (it._menuIdx == menuIdx) { existing = it; break; }
                }
                int price = settings.taxEnabled ? (int)(m.price * 1.1) : m.price;
                if (existing != null) {
                    existing.qty++;
                    existing.amount = price * existing.qty;
                    existing.label = m.name + "×" + existing.qty;
                } else {
                    OrderItem newItem = new OrderItem(m.name, price);
                    newItem._menuIdx = menuIdx;
                    newItem.qty = 1;
                    sessionItems.add(newItem);
                }
                refreshBadge(badge, menuIdx);
                renderSessionItems();
            });

            if (row != null) row.addView(btn);
        }

        // 余白埋め
        if (items.size() % 3 != 0 && row != null) {
            int fill = 3 - (items.size() % 3);
            for (int i = 0; i < fill; i++) {
                View empty = new View(this);
                empty.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
                row.addView(empty);
            }
        }
    }

    private void refreshBadge(TextView badge, int menuIdx) {
        int qty = 0;
        for (OrderItem it : sessionItems) {
            if (it._menuIdx == menuIdx) { qty = it.qty; break; }
        }
        badge.setText(String.valueOf(qty));
        badge.setVisibility(qty > 0 ? View.VISIBLE : View.GONE);
    }

    private void renderSessionItems() {
        orderItemsLayout.removeAllViews();
        if (sessionItems.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("商品をタップして追加してください");
            empty.setTextColor(0xFFAAAAAA);
            empty.setPadding(0, 20, 0, 20);
            orderItemsLayout.addView(empty);
            tvOrderTotal.setText("¥0");
            return;
        }
        int total = 0;
        for (int i = 0; i < sessionItems.size(); i++) {
            final int idx = i;
            OrderItem item = sessionItems.get(i);
            View row = LayoutInflater.from(this).inflate(R.layout.item_order, orderItemsLayout, false);
            ((TextView) row.findViewById(R.id.itemLabel)).setText(item.label);
            ((TextView) row.findViewById(R.id.itemAmount)).setText("¥" + String.format("%,d", item.amount));
            row.findViewById(R.id.deleteBtn).setOnClickListener(v -> {
                sessionItems.remove(idx);
                renderSessionItems();
                // バッジも再描画
                buildMenuGrids();
            });
            orderItemsLayout.addView(row);
            total += item.amount;
        }
        tvOrderTotal.setText("¥" + String.format("%,d", total));
    }

    private void checkout() {
        if (sessionItems.isEmpty()) {
            Toast.makeText(this, "商品が選択されていません", Toast.LENGTH_SHORT).show();
            return;
        }

        // 既存のチームのitemsにセッションのitemsをマージして保存
        for (OrderItem sessionItem : sessionItems) {
            boolean merged = false;
            for (OrderItem existing : currentTeam.items) {
                if (existing._menuIdx == sessionItem._menuIdx && existing._menuIdx >= 0) {
                    existing.qty += sessionItem.qty;
                    existing.amount += sessionItem.amount;
                    existing.label = sessionItem.label.split("×")[0] + "×" + existing.qty;
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                currentTeam.items.add(sessionItem);
            }
        }

        teams.set(teamIndex, currentTeam);
        saveManager.saveTeams(teams);

        Toast.makeText(this, "注文を追加しました！", Toast.LENGTH_SHORT).show();
        finish(); // お会計管理に戻る
    }
}
