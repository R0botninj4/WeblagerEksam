package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.DAL.BoxDAO;
import com.eksam.weblagereksam.DAL.IBoxDAO;

import java.util.List;
import java.util.UUID;

public class BoxManager {

    private final IBoxDAO boxDAO;

    public BoxManager() throws Exception {
        boxDAO = new BoxDAO();
    }

    public List<Box> getAllBoxes() {
        return boxDAO.getAllBoxes();
    }

    public Box getBoxById(UUID id) {
        return boxDAO.getBoxById(id);
    }

    public Box getBoxByBoxNumber(String boxNumber) {
        return boxDAO.getBoxByBoxNumber(boxNumber);
    }

    public UUID addBox(Box box) {
        return boxDAO.addBox(box);
    }

    public boolean updateBox(Box box) {
        return boxDAO.updateBox(box);
    }

    public boolean deleteBox(UUID id) {
        return boxDAO.deleteBox(id);
    }
}