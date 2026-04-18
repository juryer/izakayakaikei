package com.example.izakayakaikei;

public class OrderItem {
    public String label;
    public int amount;
    public int qty;
    public int _menuIdx = -1;

    public OrderItem(String label, int amount) {
        this.label = label;
        this.amount = amount;
        this.qty = 1;
    }
}
