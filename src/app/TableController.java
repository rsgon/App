package app;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import service.DbService;
import service.DbServiceImpl;

/**
 * Controller class for table names window.
 */
public class TableController {
    @FXML private Button btn_Ok;
    @FXML private Button btn_Cancel;

    @FXML private TableView<String > viewTableNames = new TableView<>();
    @FXML private TableColumn<String, String > tableNamesColumn = new TableColumn<>("Table Names");

    /** Name of table from database as an observable list of String. */
    private ObservableList<String > tableNames = FXCollections.observableArrayList();
    private Stage tableNamesStage;
    private DbService dbService;
    private static String tableName;
    private boolean okClicked = false;

    /**
     * Initialize table with table names from database.
     * Listen for selection changes in current table.
     * */
    @FXML private void initialize() {
        tableNamesColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        viewTableNames.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    tableName = (newValue);
                });
    }

    public void setTableNamesStage(final Stage tableStage) {
        this.tableNamesStage = tableStage;
    }

    /**
     * Returns chosen table name.
     *
     * @return String
     */
    public static String getTableName() {
        return tableName;
    }

    /** Initialize table names. */
    public void initGetTablesNames() {
        if (dbService == null) {
            dbService = new DbServiceImpl().getInstance();
        }
        tableNames.addAll(dbService.getAllTablesNames());
        viewTableNames.setItems(tableNames);
    }

    /** Returns true if the user clicked OK, false otherwise. */
    public boolean isOkClicked() {
        return okClicked;
    }

    /** Called when the user clicks ok. */
    @FXML private void handleOk() {
        okClicked = true;
        tableNamesStage.close();
    }

    /** Called when the user clicks cancel. */
    @FXML private void handleCancel() {
        tableNamesStage.close();
    }
}


