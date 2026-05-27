package com.eksam.weblagereksam.sprint3;

import com.eksam.weblagereksam.GUI.Util.ShortcutList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShortcutListTest {

    @Test
    void sprint3Settings_shortcutListContainsScannerShortcuts() {
        List<ShortcutList.Shortcut> shortcuts = ShortcutList.all();

        assertTrue(shortcuts.stream().anyMatch(shortcut ->
                shortcut.group().equals("Scanner")
                        && shortcut.keys().equals("F")
                        && shortcut.action().equals("Fetch next file")));
    }

    @Test
    void sprint3Settings_shortcutListContainsAdminShortcuts() {
        List<ShortcutList.Shortcut> shortcuts = ShortcutList.all();

        assertTrue(shortcuts.stream().anyMatch(shortcut ->
                shortcut.group().equals("Admin")
                        && shortcut.keys().equals("Ctrl + F")
                        && shortcut.action().equals("Focus search")));
    }

    @Test
    void sprint3Settings_shortcutListContainsPopupEscClose() {
        List<ShortcutList.Shortcut> shortcuts = ShortcutList.all();

        assertTrue(shortcuts.stream().anyMatch(shortcut ->
                shortcut.group().equals("Popups")
                        && shortcut.keys().equals("Esc")
                        && shortcut.action().equals("Close open popup")));
    }

    @Test
    void sprint3Scanner_shortcutListContainsManualSplitShortcuts() {
        List<ShortcutList.Shortcut> shortcuts = ShortcutList.all();

        assertTrue(shortcuts.stream().anyMatch(shortcut ->
                shortcut.group().equals("Scanner")
                        && shortcut.keys().equals("X")
                        && shortcut.action().equals("Split document at current page")));

        assertTrue(shortcuts.stream().anyMatch(shortcut ->
                shortcut.group().equals("Scanner")
                        && shortcut.keys().equals("M")
                        && shortcut.action().equals("Merge document with previous")));
    }
}
