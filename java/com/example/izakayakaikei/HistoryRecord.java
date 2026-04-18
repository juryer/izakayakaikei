package com.example.izakayakaikei;

import java.util.List;

public class HistoryRecord {
    public String teamName;
    public List<OrderItem> items;
    public int total;
    public String date;       // 表示用 "4/19 23:30"
    public String dateKey;    // 集計用 "2026-04-19"（午前6時ルール適用済み）

    public HistoryRecord(String teamName, List<OrderItem> items, int total, String date, String dateKey) {
        this.teamName = teamName;
        this.items = items;
        this.total = total;
        this.date = date;
        this.dateKey = dateKey;
    }
}
