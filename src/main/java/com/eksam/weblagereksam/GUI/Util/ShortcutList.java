package com.eksam.weblagereksam.GUI.Util;

import java.util.List;

public class ShortcutList {

    private ShortcutList() {
    }

    public record Shortcut(String group, String keys, String action) {
    }

    public static List<Shortcut> all() {
        return List.of(
                new Shortcut("Scanner", "Left / Right", "Previous or next page"),
                new Shortcut("Scanner", "PageUp / PageDown", "Previous or next page"),
                new Shortcut("Scanner", "Home / End", "First or last page"),
                new Shortcut("Scanner", "Ctrl + Up / Down", "Previous or next document"),
                new Shortcut("Scanner", "R or +", "Rotate right"),
                new Shortcut("Scanner", "Shift + R or -", "Rotate left"),
                new Shortcut("Scanner", "F", "Fetch next file"),
                new Shortcut("Scanner", "T", "Fetch 10 files"),
                new Shortcut("Scanner", "S", "Start scan"),
                new Shortcut("Scanner", "B", "My boxes"),
                new Shortcut("Scanner", "Delete", "Delete current page"),
                new Shortcut("Scanner", "Esc", "Log out with warning"),
                new Shortcut("Popups", "Esc", "Close open popup"),
                new Shortcut("Admin", "Ctrl + 1", "Attendance"),
                new Shortcut("Admin", "Ctrl + 2", "Users"),
                new Shortcut("Admin", "Ctrl + 3", "Profiles"),
                new Shortcut("Admin", "Ctrl + 4", "Clients"),
                new Shortcut("Admin", "Ctrl + 5", "Logged activity"),
                new Shortcut("Admin", "Ctrl + F", "Focus search"),
                new Shortcut("Admin", "Esc", "Log out with warning"),
                new Shortcut("Admin", "F5 or R", "Refresh"),
                new Shortcut("Admin", "A or Insert", "Add"),
                new Shortcut("Admin", "E or Enter", "Edit selected row"),
                new Shortcut("Admin", "Delete or D", "Deactivate selected row"),
                new Shortcut("Admin", "V", "Activate selected row")
        );
    }
}
