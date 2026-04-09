module org.example.mastermind {
    requires javafx.controls;

    exports org.example.mastermind;
    opens org.example.mastermind.view to javafx.base;
}
