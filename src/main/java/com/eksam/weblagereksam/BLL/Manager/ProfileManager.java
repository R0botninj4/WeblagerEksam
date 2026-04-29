package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Profile;
import com.eksam.weblagereksam.DAL.IProfileDAO;
import com.eksam.weblagereksam.DAL.ProfileDAO;

import java.util.List;

public class ProfileManager {

    private final IProfileDAO profileDAO;

    public ProfileManager() throws Exception {
        profileDAO = new ProfileDAO();
    }

    public List<Profile> getAllProfiles() {
        return profileDAO.getAllProfiles();
    }
}
