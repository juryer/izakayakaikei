package com.example.izakayakaikei;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {

    private static final String PREF_NAME     = "IzakayaData";
    private static final String KEY_TEAMS     = "teams";
    private static final String KEY_HISTORY   = "history";
    private static final String KEY_MENU      = "menu";
    private static final String KEY_SETTINGS  = "settings";

    private final SharedPreferences prefs;
    private final Gson gson;

    public SaveManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // ─── チーム ───────────────────────────────────────
    public void saveTeams(List<Team> teams) {
        prefs.edit().putString(KEY_TEAMS, gson.toJson(teams)).apply();
    }

    public List<Team> loadTeams() {
        String json = prefs.getString(KEY_TEAMS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Team>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // ─── 履歴 ───────────────────────────────────────
    public void saveHistory(List<HistoryRecord> history) {
        prefs.edit().putString(KEY_HISTORY, gson.toJson(history)).apply();
    }

    public List<HistoryRecord> loadHistory() {
        String json = prefs.getString(KEY_HISTORY, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<HistoryRecord>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // ─── 登録メニュー ─────────────────────────────────
    public void saveMenuItems(List<MenuItem> menuItems) {
        prefs.edit().putString(KEY_MENU, gson.toJson(menuItems)).apply();
    }

    public List<MenuItem> loadMenuItems() {
        String json = prefs.getString(KEY_MENU, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<MenuItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // ─── 設定 ──────────────────────────────────────
    public void saveSettings(AppSettings settings) {
        prefs.edit().putString(KEY_SETTINGS, gson.toJson(settings)).apply();
    }

    public AppSettings loadSettings() {
        String json = prefs.getString(KEY_SETTINGS, null);
        if (json == null) return new AppSettings();
        return gson.fromJson(json, AppSettings.class);
    }
}
