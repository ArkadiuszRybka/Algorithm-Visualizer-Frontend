module com.example.algorithmvisualizerfrontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;
    requires com.fasterxml.jackson.databind;
    requires static lombok;


    opens com.example.algorithmvisualizerfrontend to javafx.fxml;
    opens com.example.algorithmvisualizerfrontend.controller to javafx.fxml;
    opens com.example.algorithmvisualizerfrontend.model to com.fasterxml.jackson.databind;
    exports com.example.algorithmvisualizerfrontend;
}