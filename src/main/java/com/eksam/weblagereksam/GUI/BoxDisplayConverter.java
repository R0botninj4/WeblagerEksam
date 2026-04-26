package com.eksam.weblagereksam.GUI;

import com.eksam.weblagereksam.BE.Box;
import javafx.util.StringConverter;

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
        return null;
    }
}
