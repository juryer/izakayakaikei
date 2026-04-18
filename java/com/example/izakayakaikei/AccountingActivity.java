package com.example.izakayakaikei;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AccountingActivity extends AppCompatActivity {

    private SaveManager saveManager;
    private List<Team> teams;
    private LinearLayout teamListLayout;
    private EditText etTeamName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting);

        saveManager    = new SaveManager(this);
        teamListLayout = findViewById(R.id.teamListLayout);
        etTeamName     = findViewById(R.id.etTeamName);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddTeam).setOnClickListener(v -> addTeam());
        findViewById(R.id.btnEndDay).setOnClickListener(v -> endDay());
    }

    @Override
    protected void onResume() {
        super.onResume();
        teams = saveManager.loadTeams();
        renderTeams();
    }

    private void addTeam() {
        String name = etTeamName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "チーム名を入力してください", Toast.LENGTH_SHORT).show();
            return;
        }
        teams.add(new Team(name));
        saveManager.saveTeams(teams);
        etTeamName.setText("");
        renderTeams();
    }

    private void endDay() {
        boolean hasItems = false;
        for (Team t : teams) {
            if (!t.items.isEmpty()) { hasItems = true; break; }
        }
        if (!hasItems) {
            Toast.makeText(this, "保存する会計データがありません", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("本日の営業を終了する")
            .setMessage("本日の営業を終了し、全チームの会計を保存しますか？")
            .setPositiveButton("次へ", (d, w) ->
                new AlertDialog.Builder(this)
                    .setTitle("本当に終了しますか？")
                    .setMessage("この操作は元に戻せません。\n全チームのデータが保存されます。")
                    .setPositiveButton("終了して保存", (d2, w2) -> saveAllTeams())
                    .setNegativeButton("キャンセル", null)
                    .show()
            )
            .setNegativeButton("キャンセル", null)
            .show();
    }

    private void saveAllTeams() {
        List<HistoryRecord> history = saveManager.loadHistory();
        String dateKey = DateUtil.getBusinessDateKey();
        String displayDate = DateUtil.getDisplayDateTime();
        for (Team t : teams) {
            if (!t.items.isEmpty()) {
                history.add(0, new HistoryRecord(
                    t.name, new ArrayList<>(t.items), t.getTotal(), displayDate, dateKey));
            }
        }
        saveManager.saveHistory(history);
        teams.clear();
        saveManager.saveTeams(teams);
        renderTeams();
        Toast.makeText(this, "営業終了しました。お疲れ様でした！", Toast.LENGTH_LONG).show();
    }

    private void renderTeams() {
        teamListLayout.removeAllViews();
        if (teams.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("チームを追加してください");
            empty.setTextColor(0xFFAAAAAA);
            empty.setPadding(0, 40, 0, 40);
            teamListLayout.addView(empty);
            return;
        }

        for (int i = 0; i < teams.size(); i++) {
            final int teamIdx = i;
            Team team = teams.get(i);

            View card = LayoutInflater.from(this).inflate(R.layout.item_team, teamListLayout, false);

            TextView nameText  = card.findViewById(R.id.teamName);
            TextView totalText = card.findViewById(R.id.teamTotal);
            Button btnOpen     = card.findViewById(R.id.btnOpenTeam);
            Button btnDelete   = card.findViewById(R.id.btnDeleteTeam);
            Button btnDetail   = card.findViewById(R.id.btnTeamDetail);

            // ラベル・自動削除テキストは非表示
            card.findViewById(R.id.teamStatus).setVisibility(View.GONE);
            card.findViewById(R.id.teamItemCount).setVisibility(View.GONE);

            int total = team.getTotal();
            boolean hasItems = !team.items.isEmpty();

            nameText.setText(team.name);
            if (hasItems) {
                totalText.setText("¥" + String.format("%,d", total) + "　(" + team.items.size() + "品)");
                totalText.setTextColor(0xFF1D9E75);
            } else {
                totalText.setText("¥−");
                totalText.setTextColor(0xFFCCCCCC);
            }

            // 内訳・修正ボタン
            btnDetail.setVisibility(hasItems ? View.VISIBLE : View.GONE);
            btnDetail.setOnClickListener(v -> {
                Intent intent = new Intent(this, TeamDetailActivity.class);
                intent.putExtra("teamIndex", teamIdx);
                startActivity(intent);
            });

            // 注文を追加するボタン
            btnOpen.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrderActivity.class);
                intent.putExtra("teamIndex", teamIdx);
                startActivity(intent);
            });

            // チーム削除
            btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                    .setTitle("チームを削除")
                    .setMessage("「" + team.name + "」を削除しますか？")
                    .setPositiveButton("削除", (d, w) -> {
                        teams.remove(teamIdx);
                        saveManager.saveTeams(teams);
                        renderTeams();
                    })
                    .setNegativeButton("キャンセル", null)
                    .show()
            );

            teamListLayout.addView(card);
        }
    }
}
