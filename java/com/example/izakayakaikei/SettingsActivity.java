package com.example.izakayakaikei;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private SaveManager saveManager;
    private AppSettings settings;
    private static final int REQUEST_PERMISSION = 1001;
    private static final String BACKUP_FILENAME = "izakaya_backup.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        saveManager = new SaveManager(this);
        settings    = saveManager.loadSettings();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Switch switchTax    = findViewById(R.id.switchTax);
        Switch switchProfit = findViewById(R.id.switchProfit);

        switchTax.setChecked(settings.taxEnabled);
        switchProfit.setChecked(settings.profitEnabled);

        switchTax.setOnCheckedChangeListener((btn, isChecked) -> {
            settings.taxEnabled = isChecked;
            saveManager.saveSettings(settings);
        });

        switchProfit.setOnCheckedChangeListener((btn, isChecked) -> {
            settings.profitEnabled = isChecked;
            saveManager.saveSettings(settings);
        });

        // バックアップ
        findViewById(R.id.btnBackupExport).setOnClickListener(v -> exportBackup());
        findViewById(R.id.btnBackupImport).setOnClickListener(v -> importBackup());
    }

    // バックアップを書き出す
    private void exportBackup() {
        try {
            Gson gson = new Gson();
            BackupData backup = new BackupData();
            backup.teams    = saveManager.loadTeams();
            backup.history  = saveManager.loadHistory();
            backup.menu     = saveManager.loadMenuItems();
            backup.settings = saveManager.loadSettings();

            String json = gson.toJson(backup);

            // ダウンロードフォルダに保存
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.JAPAN).format(new Date());
            File file = new File(dir, "izakaya_backup_" + timestamp + ".json");

            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();

            Toast.makeText(this,
                "バックアップを保存しました\n場所：ダウンロード/" + file.getName(),
                Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "バックアップに失敗しました：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // バックアップから復元する
    private void importBackup() {
        // ダウンロードフォルダのバックアップファイルを探す
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] files = dir.listFiles((d, name) -> name.startsWith("izakaya_backup") && name.endsWith(".json"));

        if (files == null || files.length == 0) {
            Toast.makeText(this, "バックアップファイルが見つかりません\nダウンロードフォルダを確認してください", Toast.LENGTH_LONG).show();
            return;
        }

        // 最新のファイルを使用
        File latestFile = files[0];
        for (File f : files) {
            if (f.lastModified() > latestFile.lastModified()) latestFile = f;
        }

        final File targetFile = latestFile;
        new AlertDialog.Builder(this)
            .setTitle("バックアップから復元")
            .setMessage("「" + targetFile.getName() + "」から復元しますか？\n現在のデータは上書きされます。")
            .setPositiveButton("復元する", (d, w) -> {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(targetFile));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    Gson gson = new Gson();
                    BackupData backup = gson.fromJson(sb.toString(), BackupData.class);

                    if (backup.teams    != null) saveManager.saveTeams(backup.teams);
                    if (backup.history  != null) saveManager.saveHistory(backup.history);
                    if (backup.menu     != null) saveManager.saveMenuItems(backup.menu);
                    if (backup.settings != null) saveManager.saveSettings(backup.settings);

                    Toast.makeText(this, "復元が完了しました！", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(this, "復元に失敗しました：" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("キャンセル", null)
            .show();
    }

    // バックアップデータのモデル
    static class BackupData {
        List<Team> teams;
        List<HistoryRecord> history;
        List<MenuItem> menu;
        AppSettings settings;
    }
}
