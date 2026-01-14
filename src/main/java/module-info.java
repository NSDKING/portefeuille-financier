module fr.univ.projet {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires com.fasterxml.jackson.datatype.jsr310;
    // Autorise JavaFX à lire tes fichiers FXML
    opens fr.univ.projet.view to javafx.fxml;
    // Autorise JavaFX à accéder à ton contrôleur
    opens fr.univ.projet.controller to javafx.fxml;
    opens fr.univ.projet.model to com.fasterxml.jackson.databind;
    
    exports fr.univ.projet;
    exports fr.univ.projet.model;
}