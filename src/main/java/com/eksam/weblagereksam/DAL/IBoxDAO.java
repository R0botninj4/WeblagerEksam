package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Box;

import java.util.List;
import java.util.UUID;

public interface IBoxDAO {

    List<Box> getAllBoxes();

    List<Box> getBoxesByUserId(UUID userId);

    Box getBoxById(UUID id);

    Box getBoxByBoxNumber(String boxNumber);

    UUID addBox(Box box);

    boolean updateBox(Box box);

    boolean deleteBox(UUID id);
}
