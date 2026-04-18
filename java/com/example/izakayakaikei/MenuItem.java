package com.example.izakayakaikei;

public class MenuItem {
    public String name;
    public int price;
    public String category; // "drink" or "food"
    public int cost; // 原価（売上計算用）

    public MenuItem(String name, int price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.cost = 0;
    }
}
