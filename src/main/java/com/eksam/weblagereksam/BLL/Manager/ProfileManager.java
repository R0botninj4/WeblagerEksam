package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.DAL.IProfileDAO;
import com.eksam.weblagereksam.DAL.ProfileDAO;

import java.util.List;
import java.util.UUID;

public class ProfileManager {

    private final IProfileDAO profileDAO;

    public ProfileManager() throws Exception {
        profileDAO = new ProfileDAO();
    }

    public List<Profile> getAllProfiles() {
        return profileDAO.getAllProfiles();
    }

    public List<Profile> getActiveProfiles() {
        return profileDAO.getActiveProfiles();
    }

    public UUID createProfile(UUID clientId, String name) {
        return profileDAO.addProfile(new Profile(null, clientId, null, name, null));
    }

    public boolean updateProfile(Profile profile) {
        return profileDAO.updateProfile(profile);
    }

    public boolean deactivateProfile(UUID id) {
        return profileDAO.deactivateProfile(id);
    }

    public boolean activateProfile(UUID id) {
        return profileDAO.activateProfile(id);
    }
}
