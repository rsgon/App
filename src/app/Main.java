package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main class of application.
 * Application parsing xls file and write data to database line by line.
 */

public class Main extends Application {
    private static Stage primaryStage;
    private static BorderPane rootLayout;

    /** Main method. */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Method which starting application.
     *
     * @param primaryStage - main stage.
     */
    @Override
    public void start(final Stage primaryStage){
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Test task App");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/app/rootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            Scene sceneMain = new Scene(rootLayout);
            primaryStage.setScene(sceneMain);
            primaryStage.show();
        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
        }
        showTables();
    }

    /**
     * Method load main view of application with two tables -
     * first contains headers from parsed xls file,
     * second table contains duplicated table headers from table database
     *
     * @return
     */
    public boolean showTables(){
        try {
            //Loading fxml-file with main view.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/app/mainView.fxml"));
            AnchorPane tables = (AnchorPane) loader.load();
            rootLayout.setCenter(tables);
            // Give the controller access to the main app.
            Controller controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Method load new scene with list of tables in new window.
     *
     * @param tableName - String data.
     * @return true if user choose table, else returns false.
     */
    public static boolean showTablesNamesWindow(final String tableName){
        //Loading fxml-file and create new stage for new window.
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/app/tableView.fxml"));
            AnchorPane tableViewLayout = (AnchorPane) loader.load();
            Stage tableStage = new Stage();
            tableStage.setTitle("Table names");
            tableStage.initModality(Modality.WINDOW_MODAL);
            tableStage.initOwner(primaryStage);
            Scene sceneTable = new Scene(tableViewLayout);
            tableStage.setScene(sceneTable);
            // Give the controller access.
            TableController controller = loader.getController();
            controller.setTableNamesStage(tableStage);
            controller.initGetTablesNames();
            tableStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            return false;
        }
    }
}
