package com.eksam.weblagereksam.DAL;

import com.eksam.weblagereksam.BE.Client;

import java.util.List;
import java.util.UUID;

public interface IClientDAO {

    List<Client> getAllClients();

    UUID addClient(Client client);

    boolean updateClient(Client client);

    boolean deleteClient(UUID id);
}
