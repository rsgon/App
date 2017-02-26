package app;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.json.simple.JSONObject;
import service.DbService;
import service.DbServiceImpl;
import service.FileService;
import service.FileServiceImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static app.Main.showTablesNamesWindow;

/**
 * Controller class for main view
 */
public class Controller {
    @FXML private Button btn_chooseFile;
    @FXML private Button btn_getTablesFromDB;
    @FXML private Button btn_saveToDB;
    @FXML private Button btn_exit;
    @FXML private Button btn_changeColumnName;
    @FXML private TextField columnNameField;
    @FXML private TableView <String > viewFileHeaders = new TableView<>();
    @FXML private TableColumn <String, String > fileHeadersColumn = new TableColumn<>("Column names from file");
    @FXML private TableView<String > viewTableHeaders = new TableView<>();
    @FXML private TableColumn<String , String > tableHeadersColumn = new TableColumn<>("Duplicate column name");

    /** The data file headers as an observable list of String. */
    private ObservableList<String > fileHeaders = FXCollections.observableArrayList();

    /** Name of table headers as an observable list of String. */
    private ObservableList<String > tableHeaders = FXCollections.observableArrayList();

    // Reference to the main application
    private Main main;
    private String columnName;
    private File selectedFile;
    private static File csvFile;
    private static String tableName;
    private FileService fileService = new FileServiceImpl().getInstance() ;
    private DbService dbService = new DbServiceImpl().getInstance();
    private JSONObject jsonObject = new JSONObject();



    /** Static holder of headers from parsed file. */
    private static List<String > listFileHeaders = new ArrayList<>();

    /** Static holder of headers from chosen table. */
    private static List<String > listTableHeaders = new ArrayList<>();

    /** Default constructor. */
    public Controller() {
    }

    /**
     * Initialize table file headers and duplicate headers of table from database.
     * And listen for selection changes in table with table headers.
     */
    @FXML
    private void initialize() {
        fileHeadersColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        tableHeadersColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        viewTableHeaders.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    columnName = (newValue);
                });
    }

    /**
     * Method called by the main application to give a reference back to itself.
     *
     * @param mainApp type Main.
     */
    public void setMainApp(final Main mainApp) {
        this.main = mainApp;
        viewFileHeaders.setItems(fileHeaders);
        if (tableHeaders.size() >= 1) {
            viewTableHeaders.setItems(tableHeaders);
        }
    }

    /**
     * Method is called when user press button which open dialog window with files for choose.
     * Parsing xls file to csv, and add headers to table
     */
    @FXML
    public void onActionChooseFile() {
        fileHeaders.setAll();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xls files", "*.xls"));
        fileChooser.setTitle("Open Resource File");
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            csvFile = fileService.parse(selectedFile);
            listFileHeaders = fileService.getFileHeaders();
            fileHeaders.addAll(listFileHeaders);
            return;
        }
    }

    /**
     * Method is called when user press button which open dialog window with list of table names.
     */
    @FXML
    public void onActionChooseTableName() {
        boolean okClicked = showTablesNamesWindow(tableName);
        if (okClicked) {
            tableName = TableController.getTableName();
            getDuplicatedHeaders();
            viewTableHeaders.setItems(tableHeaders);
            refreshTableHeaders();
        }
    }

    /**
     * Method get duplicated headers from chosen file and chosen table,
     * and add to observable list of table headers.
     */
    public void getDuplicatedHeaders() {
        tableHeaders.setAll();
        List<String  > duplicates = new ArrayList<>();
        listTableHeaders = dbService.getTableHeaders(tableName);
        for (String i: listTableHeaders) {
            for (String j:  listFileHeaders) {
                if (i.equalsIgnoreCase(j)) {
                    duplicates.add(i);
                }
            }
        }
        tableHeaders.addAll(duplicates);
    }

    /** Refresh data in table with headers from chosen table. */
    private void refreshTableHeaders() {
        int selectedIndex = viewTableHeaders.getSelectionModel().getSelectedIndex();
        viewTableHeaders.setItems(null);
        viewTableHeaders.layout();
        viewTableHeaders.setItems(tableHeaders);
        viewTableHeaders.getSelectionModel().select(selectedIndex);
    }

    /** Rename chosen column name in table. */
    @FXML
    private void onActionSetColumnName() {
        String  newColumnName = columnNameField.getText();
        columnNameField.clear();
        if (!validateNewColumnName(newColumnName)) {
            return;
        }
        int selectedIndex = viewTableHeaders.getSelectionModel().getSelectedIndex();
        String selectedItem = tableHeadersColumn.getCellData(selectedIndex);
        if (newColumnName != null && newColumnName.length() > 0) {
            if (selectedIndex >= 0) {
                for (int i = 0; i < tableHeaders.size(); i++) {
                    if (selectedItem.equals(tableHeaders.get(i))) {
                        tableHeaders.set(i, newColumnName);
                        dbService.renameColumnNameInDbTable(selectedItem, tableHeaders.get(i).trim());
                        jsonObject.put(selectedItem, newColumnName);
                    }
                }
                refreshTableHeaders();
                return;
            } else {
                return;
            }
        } else return;
    }

    /** Called when the user clicks on the exit button. */
    @FXML
    private void onActionExit() {
        System.exit(0);
    }

    /**
     * When user click on button to save, all data writing to database line by line.
     * Mapping data saving to json file.
     */
    @FXML
    private void onActionSaveToDb() {
        fileService.saveGsonObjectToFile(jsonObject);
        dbService.createTable(tableName);
        for (String i : listFileHeaders) {
            dbService.addColumnToTable(tableName, i);
        }
        fileService.readCsvFile(csvFile);
        for (String line : fileService.getCsvFileData()) {
            dbService.writeLineToDb(tableName, line);
        }
        fileHeaders.clear();
        listFileHeaders.clear();
    }

    /**
     * Checks the String if it is a valid column name.
     *
     * @param newColumnName - String data.
     * @return true if String is valid name.
     */
    private boolean validateNewColumnName(final String newColumnName) {
        for (String columnName : dbService.getTableHeaders(tableName)) {
            if (newColumnName.equalsIgnoreCase(columnName)) {
                return false;
            }
        }
        if (!newColumnName.isEmpty() && newColumnName.length() < 3 && newColumnName.contains(" ")) {
            return false;
        } else
            return true;
    }

    /** Returns chosen table name. */
    public static String getTableName() {
        return tableName;
    }
}
