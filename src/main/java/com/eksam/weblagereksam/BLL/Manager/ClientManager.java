package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.DAL.ClientDAO;
import com.eksam.weblagereksam.DAL.IClientDAO;
import com.eksam.weblagereksam.DAL.IProfileDAO;
import com.eksam.weblagereksam.DAL.ProfileDAO;

import java.util.List;
import java.util.UUID;

public class ClientManager {

    private final IClientDAO clientDAO;
    private final IProfileDAO profileDAO;

    public ClientManager() throws Exception {
        clientDAO = new ClientDAO();
        profileDAO = new ProfileDAO();
    }

    public List<Client> getAllClients() {
        return clientDAO.getAllClients();
    }

    public List<Client> getActiveClients() {
        return clientDAO.getActiveClients();
    }

    public UUID createClient(String name) {
        return clientDAO.addClient(new Client(null, name, null));
    }

    public boolean updateClient(Client client) {
        return clientDAO.updateClient(client);
    }

    public boolean deactivateClient(UUID id) {
        boolean clientDeactivated = clientDAO.deactivateClient(id);
        boolean profilesDeactivated = profileDAO.deactivateProfilesByClientId(id);
        return clientDeactivated && profilesDeactivated;
    }

    public boolean activateClient(UUID id) {
        return clientDAO.activateClient(id);
    }
}
