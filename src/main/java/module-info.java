module me.yattaw.project.plproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens me.yattaw.project.plproject to javafx.fxml;
    exports me.yattaw.project.plproject;
}