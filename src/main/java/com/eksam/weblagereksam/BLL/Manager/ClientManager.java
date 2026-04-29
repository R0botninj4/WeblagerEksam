package com.eksam.weblagereksam.BLL.Manager;

import com.eksam.weblagereksam.BE.Client;
import com.eksam.weblagereksam.DAL.ClientDAO;
import com.eksam.weblagereksam.DAL.IClientDAO;

import java.util.List;

public class ClientManager {

    private final IClientDAO clientDAO;

    public ClientManager() throws Exception {
        clientDAO = new ClientDAO();
    }

    public List<Client> getAllClients() {
        return clientDAO.getAllClients();
    }
}
