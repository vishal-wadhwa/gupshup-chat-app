package com.example.lenovo.gupshup.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class RecentChatsTable implements Serializable {
    String name;
    int chatId;
    String phone;

    public String getName() {
        return name;
    }

    public RecentChatsTable(String name, int chatId, String phone) {
        this.name = name;
        this.chatId = chatId;
        this.phone = phone;
    }

    public int getChatId() {
        return chatId;
    }

    public String getPhone() {
        return phone;
    }
}
