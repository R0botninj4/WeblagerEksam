package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.DAL.BoxDAO;
import com.eksam.weblagereksam.DAL.IBoxDAO;

import java.util.List;
import java.util.UUID;

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

    public List<Box> getBoxesByUserId(UUID userId) {
        return boxDAO.getBoxesByUserId(userId);
    }
}
