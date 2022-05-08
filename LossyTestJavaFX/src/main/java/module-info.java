module com.example.lossytestjavafx {
    requires javafx.controls;
    requires java.desktop;
    requires javafx.swing;


    opens com.example.lossytestjavafx to javafx.fxml;
    exports com.example.lossytestjavafx;
}