package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Profile;

import java.util.List;
import java.util.UUID;

public interface IProfileDAO {

    List<Profile> getAllProfiles();

    UUID addProfile(Profile profile);

    boolean updateProfile(Profile profile);

    boolean deleteProfile(UUID id);
}
