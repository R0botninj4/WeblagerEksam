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


    opens com.eksam.weblagereksam to javafx.fxml;
    opens com.eksam.weblagereksam.GUI.Login to javafx.fxml;
    exports com.eksam.weblagereksam.GUI.Login;
    exports com.eksam.weblagereksam;
    exports com.eksam.weblagereksam.GUI;
    opens com.eksam.weblagereksam.GUI to javafx.fxml;

}

