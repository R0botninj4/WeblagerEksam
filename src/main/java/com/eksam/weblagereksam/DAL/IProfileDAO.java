package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Profile;

import java.util.List;
import java.util.UUID;

public interface IProfileDAO {

    List<Profile> getAllProfiles();

    List<Profile> getActiveProfiles();

    UUID addProfile(Profile profile);

    boolean updateProfile(Profile profile);

    boolean deactivateProfile(UUID id);

    boolean deactivateProfilesByClientId(UUID clientId);

    boolean activateProfile(UUID id);
}
