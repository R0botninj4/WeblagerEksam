package com.eksam.weblagereksam.GUI;

import com.eksam.weblagereksam.BE.Box;
import javafx.util.StringConverter;

/**
 * Controls how boxes are shown in the ComboBox.
 *
 * Keeping this separate makes the scanning controller easier to read.
 */
public class BoxDisplayConverter extends StringConverter<Box> {

    @Override
    public String toString(Box box) {
        if (box == null) {
            return "";
        }

        if (box.getLabel() == null || box.getLabel().isBlank()) {
            return box.getBoxNumber();
        }

        return box.getBoxNumber() + " - " + box.getLabel();
    }

    @Override
    public Box fromString(String string) {
        // The ComboBox only displays existing Box objects. Users do not type boxes manually.
        return null;
    }
}
