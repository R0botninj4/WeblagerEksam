module com.eksam.weblagereksam {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.naming;
    requires com.microsoft.sqlserver.jdbc;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.base;
    requires de.mkammerer.argon2.nolibs;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.net.http;
    requires javafx.swing;


    opens com.eksam.weblagereksam to javafx.fxml;
    opens com.eksam.weblagereksam.GUI.Login to javafx.fxml;
    opens com.eksam.weblagereksam.GUI.Admin to javafx.fxml;
    opens com.eksam.weblagereksam.GUI.Scanning to javafx.fxml;
    exports com.eksam.weblagereksam.GUI.Login;
    exports com.eksam.weblagereksam.GUI.Admin;
    exports com.eksam.weblagereksam.GUI.Scanning;
    exports com.eksam.weblagereksam;

}
