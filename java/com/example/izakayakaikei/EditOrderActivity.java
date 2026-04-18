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

// 既存の注文を修正する画面
// teamIndex と editItemIndex（currentTeam.itemsのインデックス）を受け取る
public class EditOrderActivity extends AppCompatActivity {

    private SaveManager saveManager;
    private List<Team> teams;
    private List<MenuItem> menu;
    private int teamIndex;
    private int editItemIndex; // 編集対象のOrderItemインデックス
    private Team currentTeam;
    private AppSettings settings;

    // 修正後の選択内容（セッション）
    private List<OrderItem> sessionItems = new ArrayList<>();

    private LinearLayout drinkGrid;
    private LinearLayout foodGrid;
    private LinearLayout orderItemsLayout;
    private TextView tvOrderTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order); // 同じレイアウトを流用

        saveManager   = new SaveManager(this);
        teams         = saveManager.loadTeams();
        menu          = saveManager.loadMenuItems();
        settings      = saveManager.loadSettings();
        teamIndex     = getIntent().getIntExtra("teamIndex", 0);
        editItemIndex = getIntent().getIntExtra("editItemIndex", -1);
        currentTeam   = teams.get(teamIndex);

        // タイトルを「修正中」に変更
        ((TextView) findViewById(R.id.tvTeamTitle)).setText(currentTeam.name + "　修正中");

        // ボタンのテキストを変更
        ((android.widget.Button) findViewById(R.id.btnCheckout)).setText("修正を保存する");

        drinkGrid        = findViewById(R.id.drinkGrid);
        foodGrid         = findViewById(R.id.foodGrid);
        orderItemsLayout = findViewById(R.id.orderItemsLayout);
        tvOrderTotal     = findViewById(R.id.tvOrderTotal);

        // 編集対象の注文をセッションに読み込む
        if (editItemIndex >= 0 && editItemIndex < currentTeam.items.size()) {
            OrderItem original = currentTeam.items.get(editItemIndex);
            // コピーをセッションに入れる
            OrderItem copy = new OrderItem(original.label, original.amount);
            copy.qty = original.qty;
            copy._menuIdx = original._menuIdx;
            sessionItems.add(copy);
        }

        // 戻るボタン → 保存せずに戻る
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 修正を保存するボタン
        findViewById(R.id.btnCheckout).setOnClickListener(v -> saveEdit());

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
            refreshBadge(badge, menuIdx);

            btn.findViewById(R.id.itemTapBtn).setOnClickListener(v -> {
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
            empty.setText("商品を選択してください");
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
                buildMenuGrids();
            });
            orderItemsLayout.addView(row);
            total += item.amount;
        }
        tvOrderTotal.setText("¥" + String.format("%,d", total));
    }

    private void saveEdit() {
        if (sessionItems.isEmpty()) {
            // 空になった場合は元の注文を削除
            currentTeam.items.remove(editItemIndex);
        } else {
            // 修正後の内容で上書き
            // 複数商品になった場合は最初の1件を上書き、残りを追加
            currentTeam.items.set(editItemIndex, sessionItems.get(0));
            for (int i = 1; i < sessionItems.size(); i++) {
                currentTeam.items.add(sessionItems.get(i));
            }
        }
        teams.set(teamIndex, currentTeam);
        saveManager.saveTeams(teams);
        Toast.makeText(this, "修正しました", Toast.LENGTH_SHORT).show();
        finish();
    }
}
