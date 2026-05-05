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

    public Box getBoxById(UUID id) {
        return boxDAO.getBoxById(id);
    }

    public Box getBoxByBoxNumber(String boxNumber) {
        return boxDAO.getBoxByBoxNumber(boxNumber);
    }

    public List<Box> getBoxesByClientId(UUID clientId) {
        return boxDAO.getAllBoxes().stream()
                .filter(box -> box.getClientId().equals(clientId))
                .toList();
    }

    public List<Box> getBoxesByProfileId(UUID profileId) {
        return boxDAO.getAllBoxes().stream()
                .filter(box -> profileId.equals(box.getProfileId()))
                .toList();
    }

    public UUID createBox(Box box) {
        return boxDAO.addBox(box);
    }

    public boolean updateBox(Box box) {
        return boxDAO.updateBox(box);
    }

    public boolean assignBoxToUser(UUID userId, UUID boxId) {
        return boxDAO.assignBoxToUser(userId, boxId);
    }

    public boolean removeBoxFromUser(UUID userId, UUID boxId) {
        return boxDAO.removeBoxFromUser(userId, boxId);
    }

    public boolean deleteBox(UUID id) {
        return boxDAO.deleteBox(id);
    }
}
