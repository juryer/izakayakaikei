package com.example.izakayakaikei;

import java.util.ArrayList;
import java.util.List;

public class Team {
    public String name;
    public List<OrderItem> items;
    public long createdAt;

    public Team(String name) {
        this.name = name;
        this.items = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public int getTotal() {
        int total = 0;
        for (OrderItem item : items) {
            total += item.amount;
        }
        return total;
    }
}
