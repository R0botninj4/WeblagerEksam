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

    public UUID createProfile(UUID clientId, String name, String barcodeRule, String metadataSchema) {
        return profileDAO.addProfile(new Profile(null, clientId, null, name, barcodeRule, metadataSchema, null));
    }

    public boolean updateProfile(Profile profile) {
        return profileDAO.updateProfile(profile);
    }

    public boolean deleteProfile(UUID id) {
        return profileDAO.deleteProfile(id);
    }
}
