package controller;

import model.Employee;
import service.DatabaseService;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.*;
import java.util.Optional;

public class EmployeeController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> colId;
    @FXML private TableColumn<Employee, String> colNom;
    @FXML private TableColumn<Employee, String> colPrenom;
    @FXML private TableColumn<Employee, String> colPoste;
    @FXML private TableColumn<Employee, String> colDepartement;
    @FXML private TableColumn<Employee, Double> colSalaire;
    @FXML private TableColumn<Employee, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> deptFilterCombo;
    @FXML private ComboBox<String> posteFilterCombo;
    @FXML private Label lblTotalEmployees;
    @FXML private Label lblPageInfo;

    private ObservableList<Employee> employeeList;
    private ObservableList<Employee> filteredList;

    private int currentPage = 1;
    private final int itemsPerPage = 20;

    @FXML
    public void initialize() {
        employeeList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();

        setupTableColumns();
        loadEmployees();
        setupFilters();
        setupSearch();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
        colDepartement.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colSalaire.setCellValueFactory(new PropertyValueFactory<>("salaire"));

        // Format du salaire avec 2 décimales
        colSalaire.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.0f DT", item));
            }
        });

        setupActionsColumn();
        employeeTable.setItems(filteredList);
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Employee, Void>, TableCell<Employee, Void>> cellFactory = param ->
            new TableCell<>() {
                private final Button btnEdit   = new Button("✏ Modifier");
                private final Button btnDelete = new Button("🗑 Supprimer");
                private final HBox   pane      = new HBox(5, btnEdit, btnDelete);

                {
                    btnEdit.getStyleClass().add("btn-secondary");
                    btnEdit.setOnAction(e -> handleEditEmployee(getTableView().getItems().get(getIndex())));

                    btnDelete.getStyleClass().add("btn-secondary");
                    btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    btnDelete.setOnAction(e -> handleDeleteEmployee(getTableView().getItems().get(getIndex())));
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            };
        colActions.setCellFactory(cellFactory);
    }

    private void loadEmployees() {
        employeeList.clear();
        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employee ORDER BY id")) {

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setId(rs.getInt("id"));
                emp.setNom(rs.getString("nom"));
                emp.setPrenom(rs.getString("prenom"));
                emp.setPoste(rs.getString("poste"));
                emp.setSalaire(rs.getDouble("salaire"));
                emp.setDepartement(rs.getString("departement"));
                emp.setDateEmbauche(rs.getString("date_embauche"));
                employeeList.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les employés : " + e.getMessage(), Alert.AlertType.ERROR);
        }
        filteredList.setAll(employeeList);
        updateStats();
    }

    private void setupFilters() {
        ObservableList<String> departments = FXCollections.observableArrayList("Tous");
        ObservableList<String> postes      = FXCollections.observableArrayList("Tous");

        for (Employee emp : employeeList) {
            if (emp.getDepartement() != null && !departments.contains(emp.getDepartement()))
                departments.add(emp.getDepartement());
            if (emp.getPoste() != null && !postes.contains(emp.getPoste()))
                postes.add(emp.getPoste());
        }

        deptFilterCombo.setItems(departments);
        deptFilterCombo.setValue("Tous");
        deptFilterCombo.setOnAction(e -> applyFilters());

        posteFilterCombo.setItems(postes);
        posteFilterCombo.setValue("Tous");
        posteFilterCombo.setOnAction(e -> applyFilters());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String search = searchField.getText().toLowerCase();
        String dept   = deptFilterCombo.getValue();
        String poste  = posteFilterCombo.getValue();

        filteredList.setAll(employeeList.stream()
            .filter(emp -> {
                boolean matchSearch = search.isEmpty()
                    || emp.getNom().toLowerCase().contains(search)
                    || emp.getPrenom().toLowerCase().contains(search);
                boolean matchDept  = "Tous".equals(dept) || dept.equals(emp.getDepartement());
                boolean matchPoste = "Tous".equals(poste) || poste.equals(emp.getPoste());
                return matchSearch && matchDept && matchPoste;
            })
            .toList());
        updateStats();
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        deptFilterCombo.setValue("Tous");
        posteFilterCombo.setValue("Tous");
        applyFilters();
    }

    @FXML
    private void handleAddEmployee() {
        Dialog<Employee> dialog = createEmployeeDialog(null);
        dialog.showAndWait().ifPresent(emp -> {
            if (saveEmployee(emp)) {
                loadEmployees();
                setupFilters();
                showAlert("Succès", "Employé ajouté avec succès.", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void handleEditEmployee(Employee employee) {
        Dialog<Employee> dialog = createEmployeeDialog(employee);
        dialog.showAndWait().ifPresent(emp -> {
            if (updateEmployee(emp)) {
                loadEmployees();
                showAlert("Succès", "Employé modifié avec succès.", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void handleDeleteEmployee(Employee employee) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cet employé ?");
        confirm.setContentText(employee.getPrenom() + " " + employee.getNom());

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && deleteEmployee(employee.getId())) {
                loadEmployees();
                setupFilters();
                showAlert("Succès", "Employé supprimé.", Alert.AlertType.INFORMATION);
            }
        });
    }

    private Dialog<Employee> createEmployeeDialog(Employee employee) {
        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle(employee == null ? "Nouvel Employé" : "Modifier Employé");
        dialog.setHeaderText(employee == null ? "Ajouter un nouvel employé" : "Modifier les informations");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField     = new TextField(employee != null ? employee.getNom() : "");
        TextField prenomField  = new TextField(employee != null ? employee.getPrenom() : "");
        TextField posteField   = new TextField(employee != null ? employee.getPoste() : "");
        TextField deptField    = new TextField(employee != null ? employee.getDepartement() : "");
        TextField salaireField = new TextField(employee != null ? String.valueOf((int)employee.getSalaire()) : "");

        grid.add(new Label("Nom :"),            0, 0); grid.add(nomField,     1, 0);
        grid.add(new Label("Prénom :"),         0, 1); grid.add(prenomField,  1, 1);
        grid.add(new Label("Poste :"),          0, 2); grid.add(posteField,   1, 2);
        grid.add(new Label("Département :"),    0, 3); grid.add(deptField,    1, 3);
        grid.add(new Label("Salaire (DT) :"),   0, 4); grid.add(salaireField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    Employee emp = employee != null ? employee : new Employee();
                    emp.setNom(nomField.getText().trim());
                    emp.setPrenom(prenomField.getText().trim());
                    emp.setPoste(posteField.getText().trim());
                    emp.setDepartement(deptField.getText().trim());
                    emp.setSalaire(Double.parseDouble(salaireField.getText().trim()));
                    return emp;
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Le salaire doit être un nombre (ex: 45000).", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });
        return dialog;
    }

    private boolean saveEmployee(Employee emp) {
        String sql = "INSERT INTO employee (nom, prenom, poste, salaire, departement) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getNom());
            ps.setString(2, emp.getPrenom());
            ps.setString(3, emp.getPoste());
            ps.setDouble(4, emp.getSalaire());
            ps.setString(5, emp.getDepartement());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean updateEmployee(Employee emp) {
        String sql = "UPDATE employee SET nom=?, prenom=?, poste=?, salaire=?, departement=? WHERE id=?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getNom());
            ps.setString(2, emp.getPrenom());
            ps.setString(3, emp.getPoste());
            ps.setDouble(4, emp.getSalaire());
            ps.setString(5, emp.getDepartement());
            ps.setInt(6, emp.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la modification.", Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean deleteEmployee(int id) {
        String sql = "DELETE FROM employee WHERE id = ?";
        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la suppression.", Alert.AlertType.ERROR);
            return false;
        }
    }

    @FXML
    private void handleExport() {
        if (filteredList.isEmpty()) {
            showAlert("Export", "Aucune donnée à exporter.", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        fileChooser.setInitialFileName("employes_export.csv");

        File file = fileChooser.showSaveDialog(employeeTable.getScene().getWindow());

        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println("ID;Nom;Prénom;Poste;Département;Salaire");
                for (Employee emp : filteredList) {
                    writer.printf("%d;%s;%s;%s;%s;%.2f%n",
                        emp.getId(), emp.getNom(), emp.getPrenom(), 
                        emp.getPoste(), emp.getDepartement(), emp.getSalaire());
                }
                showAlert("Succès", "Exportation réussie vers : " + file.getName(), Alert.AlertType.INFORMATION);
            } catch (java.io.IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de l'exportation : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handlePreviousPage() {
        if (currentPage > 1) { currentPage--; updateStats(); }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) filteredList.size() / itemsPerPage);
        if (currentPage < totalPages) { currentPage++; updateStats(); }
    }

    private void updateStats() {
        if (lblTotalEmployees != null)
            lblTotalEmployees.setText("Total : " + filteredList.size() + " employé(s)");
        if (lblPageInfo != null) {
            int totalPages = Math.max(1, (int) Math.ceil((double) filteredList.size() / itemsPerPage));
            lblPageInfo.setText("Page " + currentPage + " / " + totalPages);
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
