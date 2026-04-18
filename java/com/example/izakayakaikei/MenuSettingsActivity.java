package com.example.izakayakaikei;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MenuSettingsActivity extends AppCompatActivity {

    private SaveManager saveManager;
    private List<MenuItem> menuItems;
    private LinearLayout drinkListLayout;
    private LinearLayout foodListLayout;
    private AppSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_settings);

        saveManager     = new SaveManager(this);
        menuItems       = saveManager.loadMenuItems();
        settings        = saveManager.loadSettings();
        drinkListLayout = findViewById(R.id.drinkListLayout);
        foodListLayout  = findViewById(R.id.foodListLayout);

        // 原価入力欄は設定オンのときのみ表示
        View costRow = findViewById(R.id.costInputRow);
        costRow.setVisibility(settings.profitEnabled ? View.VISIBLE : View.GONE);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddMenuItem).setOnClickListener(v -> addMenuItem());

        renderMenuList();
    }

    private void addMenuItem() {
        EditText etName   = findViewById(R.id.etMenuName);
        EditText etPrice  = findViewById(R.id.etMenuPrice);
        EditText etCost   = findViewById(R.id.etMenuCost);
        RadioGroup rgCat  = findViewById(R.id.rgCategory);

        String name     = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (name.isEmpty())     { Toast.makeText(this, "商品名を入力してください", Toast.LENGTH_SHORT).show(); return; }
        if (priceStr.isEmpty()) { Toast.makeText(this, "値段を入力してください",   Toast.LENGTH_SHORT).show(); return; }

        String category = rgCat.getCheckedRadioButtonId() == R.id.rbFood ? "food" : "drink";
        MenuItem item = new MenuItem(name, Integer.parseInt(priceStr), category);

        // 原価（設定オンのときのみ）
        if (settings.profitEnabled) {
            String costStr = etCost.getText().toString().trim();
            item.cost = costStr.isEmpty() ? 0 : Integer.parseInt(costStr);
        }

        menuItems.add(item);
        saveManager.saveMenuItems(menuItems);
        etName.setText("");
        etPrice.setText("");
        etCost.setText("");
        renderMenuList();
    }

    private void renderMenuList() {
        drinkListLayout.removeAllViews();
        foodListLayout.removeAllViews();

        boolean hasDrink = false, hasFood = false;

        for (int i = 0; i < menuItems.size(); i++) {
            final int idx = i;
            MenuItem item = menuItems.get(i);
            boolean isFood = "food".equals(item.category);

            View row = LayoutInflater.from(this).inflate(R.layout.item_menu_setting, null, false);
            ((TextView) row.findViewById(R.id.settingMenuName)).setText(item.name);

            EditText priceEdit = row.findViewById(R.id.settingMenuPrice);
            priceEdit.setText(String.valueOf(item.price));
            priceEdit.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String val = priceEdit.getText().toString().trim();
                    if (!val.isEmpty()) {
                        menuItems.get(idx).price = Integer.parseInt(val);
                        saveManager.saveMenuItems(menuItems);
                    }
                }
            });

            EditText costEdit = row.findViewById(R.id.settingMenuCost);
            if (settings.profitEnabled) {
                costEdit.setVisibility(View.VISIBLE);
                costEdit.setText(item.cost > 0 ? String.valueOf(item.cost) : "");
                costEdit.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        String val = costEdit.getText().toString().trim();
                        menuItems.get(idx).cost = val.isEmpty() ? 0 : Integer.parseInt(val);
                        saveManager.saveMenuItems(menuItems);
                    }
                });
            } else {
                costEdit.setVisibility(View.GONE);
            }

            row.findViewById(R.id.deleteMenuBtn).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                    .setTitle("削除確認")
                    .setMessage("「" + item.name + "」を削除しますか？")
                    .setPositiveButton("削除", (d, w) -> {
                        menuItems.remove(idx);
                        saveManager.saveMenuItems(menuItems);
                        renderMenuList();
                    })
                    .setNegativeButton("キャンセル", null).show()
            );

            if (isFood) { foodListLayout.addView(row); hasFood = true; }
            else { drinkListLayout.addView(row); hasDrink = true; }
        }

        TextView drinkEmpty = findViewById(R.id.drinkEmpty);
        TextView foodEmpty  = findViewById(R.id.foodEmpty);
        drinkEmpty.setVisibility(hasDrink ? View.GONE : View.VISIBLE);
        foodEmpty.setVisibility(hasFood ? View.GONE : View.VISIBLE);
    }
}
