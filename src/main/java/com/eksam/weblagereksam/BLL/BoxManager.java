package com.eksam.weblagereksam.BLL;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.DAL.BoxDAO;
import com.eksam.weblagereksam.DAL.IBoxDAO;

import java.util.List;
import java.util.UUID;

/**
 * BLL manager for boxes.
 *
 * The GUI should call this class instead of calling BoxDAO directly.
 * That keeps the project in a 3-layer structure: GUI -> BLL -> DAL.
 */
public class BoxManager {

    // ===== DAL dependency =====

    private final IBoxDAO boxDAO;

    public BoxManager() throws Exception {
        boxDAO = new BoxDAO();
    }

    // ===== Read methods =====

    public List<Box> getAllBoxes() {
        return boxDAO.getAllBoxes();
    }

    /**
     * Used by the scanning screen so each scanner only sees assigned boxes.
     */
    public List<Box> getBoxesByUserId(UUID userId) {
        return boxDAO.getBoxesByUserId(userId);
    }

    public Box getBoxById(UUID id) {
        return boxDAO.getBoxById(id);
    }

    public Box getBoxByBoxNumber(String boxNumber) {
        return boxDAO.getBoxByBoxNumber(boxNumber);
    }

    // ===== Write methods =====

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
