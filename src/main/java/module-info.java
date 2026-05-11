module com.example.ebook_reader {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.example.ebook_reader to javafx.fxml;
    opens models to javafx.base;
    opens FileHandler to javafx.base;
    
    exports com.example.ebook_reader;
    exports models;
    exports FileHandler;
}