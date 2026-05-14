package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Box;
import com.eksam.weblagereksam.BE.Profile;
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

    public List<Box> getBoxesByUserId(UUID userId) {
        return boxDAO.getBoxesByUserId(userId);
    }

    public Box getBoxById(UUID id) {
        return boxDAO.getBoxById(id);
    }

    public Box openBoxForScanning(String boxNumber, Profile profile, UUID userId) throws Exception {
        Box box = boxDAO.getBoxByBoxNumber(boxNumber);

        if (box == null) {
            box = new Box(null, profile.getClientId(), profile.getId(), profile.getClientName(), profile.getName(),
                    boxNumber, boxNumber, "IN_PROGRESS", null);
            UUID boxId = boxDAO.addBox(box);
            if (boxId == null) {
                throw new Exception("Could not create box.");
            }
            box = getBoxById(boxId);
        } else {
            box.setClientId(profile.getClientId());
            box.setProfileId(profile.getId());
            box.setClientName(profile.getClientName());
            box.setProfileName(profile.getName());
            box.setStatus("IN_PROGRESS");
            updateBox(box);
        }

        if (userId != null) {
            boxDAO.assignBoxToUser(userId, box.getId());
        }

        return box;
    }

    public boolean updateBox(Box box) {
        return boxDAO.updateBox(box);
    }

    public boolean removeBoxFromUser(UUID userId, UUID boxId) {
        return boxDAO.removeBoxFromUser(userId, boxId);
    }

    public boolean deleteBox(UUID id) {
        return boxDAO.deleteBox(id);
    }
}
