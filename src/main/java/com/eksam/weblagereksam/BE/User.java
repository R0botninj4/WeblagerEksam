package com.eksam.weblagereksam.BE;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty passwordHash = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phoneNumber = new SimpleStringProperty();
    private final IntegerProperty roleInt = new SimpleIntegerProperty();
    private final StringProperty roleText = new SimpleStringProperty();

    // FULL constructor (DB / login)
    public User(int id, String username, String passwordHash, String name, String email, String phoneNumber, int roleInt) {
        this.id.set(id);
        this.username.set(username);
        this.passwordHash.set(passwordHash);
        this.name.set(name);
        this.email.set(email);
        this.phoneNumber.set(phoneNumber);
        this.roleInt.set(roleInt);

        this.roleText.set(mapRole(roleInt));
    }

    // constructor uden password (til UI hvis du vil)
    public User(int id, String username, String name, String email, String phoneNumber, int roleInt) {
        this(id, username, null, name, email, phoneNumber, roleInt);
    }

    private String mapRole(int roleInt) {
        return switch (roleInt) {
            case 1 -> "Admin";
            case 2 -> "Coordinator";
            default -> "Customer";
        };
    }

    // GETTERS
    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return passwordHash.get(); }
    public String getName() { return name.get(); }
    public String getEmail() { return email.get(); }
    public String getPhoneNumber() { return phoneNumber.get(); }
    public int getRoleInt() { return roleInt.get(); }
    public String getRoleText() { return roleText.get(); }

    // PROPERTIES
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty nameProperty() { return name; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneNumberProperty() { return phoneNumber; }
    public IntegerProperty roleIntProperty() { return roleInt; }
    public StringProperty roleTextProperty() { return roleText; }

    // SETTERS
    public void setUsername(String username) { this.username.set(username); }
    public void setName(String name) { this.name.set(name); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber.set(phoneNumber); }

    public void setRoleInt(int roleInt) {
        this.roleInt.set(roleInt);
        this.roleText.set(mapRole(roleInt));
    }
}