package ds;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PatientsTab extends Tab {

    private final PatientManager pm;
    private final ConditionManager cm;

    private final TableView<Patient> table = new TableView<>();

    private int currentIndex = 0;

    public PatientsTab(PatientManager pm, ConditionManager cm) {
        this.pm = pm;
        this.cm = cm;

        setText("Patients");
        setClosable(false);
        setContent(buildContent());
        refreshTable();
    }


    private BorderPane buildContent() {
        TableColumn<Patient, String> colId   = new TableColumn<>("ID");
        TableColumn<Patient, String> colName = new TableColumn<>("Name");
        TableColumn<Patient, Number> colAge  = new TableColumn<>("Age");
        TableColumn<Patient, String> colCond = new TableColumn<>("Condition");
        // ربط بسيط بالـgetters
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colAge.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAge()));
        colCond.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getConditionName()));

        table.getColumns().addAll(colId, colName, colAge, colCond);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
            if (newP == null) return;
            CircularDoublyLinkedList<Patient> ps = pm.getPatients();
            int n = ps.getSize();
            for (int i = 0; i < n; i++) {
                Patient p = ps.get(i);
                if (p == newP) { // same refrence
                    currentIndex = i;
                    break;
                }
            }
        });

        Button btnAdd = new Button("Add");
        Button btnEdit = new Button("Edit");
        Button btnDel = new Button("Delete");
        Button btnRefresh = new Button("Refresh");

        Button btnPrev = new Button("Previous");
        Button btnNext = new Button("Next");

        btnAdd.setOnAction(e -> onAdd());
        btnEdit.setOnAction(e -> onEdit());
        btnDel.setOnAction(e -> onDelete());
        btnRefresh.setOnAction(e -> refreshTable());

        btnPrev.setOnAction(e -> navigate(-1));
        btnNext.setOnAction(e -> navigate(+1));

        HBox top = new HBox(10, btnAdd, btnEdit, btnDel, btnRefresh, btnPrev, btnNext);
        top.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        return root;
    }

    private ObservableList<Patient> toObservable(CircularDoublyLinkedList<Patient> cdll) {
        ObservableList<Patient> list = FXCollections.observableArrayList();
        if (cdll == null) return list;
        int n = cdll.getSize();
        for (int i = 0; i < n; i++) {
            list.add(cdll.get(i));
        }
        return list;
    }


    public void refreshTable() {
        CircularDoublyLinkedList<Patient> ps = pm.getPatients();
        int n = ps.getSize();

        table.setItems(toObservable(ps));

        if (n == 0) {
            currentIndex = 0;
            return;
        }


        if (currentIndex >= n) currentIndex = 0;
        if (currentIndex < 0) currentIndex = 0;

        Patient target = ps.get(currentIndex);
        table.getSelectionModel().select(target);
        table.scrollTo(target);
    }


    private void navigate(int delta) {
        CircularDoublyLinkedList<Patient> ps = pm.getPatients();
        int n = ps.getSize();
        if (n == 0) return;

        // (index + delta + n) % n
        currentIndex = (currentIndex + delta + n) % n;

        Patient target = ps.get(currentIndex);
        table.getSelectionModel().select(target);
        table.scrollTo(target);
    }


    private void onAdd() {
        Dialog<Patient> dlg = buildPatientDialog(null);
        dlg.showAndWait().ifPresent(p -> {
            boolean ok = pm.addPatientUnique(p);
            if (!ok) showAlert("Add Patient", "Duplicate ID or invalid input.");
            refreshTable();
        });
    }


    private void onEdit() {
        Patient sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Edit Patient", "Select a patient first."); return; }
        Dialog<Patient> dlg = buildPatientDialog(sel);
        dlg.showAndWait().ifPresent(pNew -> {
            boolean ok = pm.updatePatient(sel.getId(), pNew.getName(), pNew.getAge(), pNew.getConditionName());
            if (!ok) showAlert("Edit Patient", "Update failed.");
            refreshTable();
        });
    }


    private void onDelete() {
        Patient sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Delete Patient", "Select a patient first."); return; }
        boolean ok = pm.deletePatientById(sel.getId());
        if (!ok) showAlert("Delete Patient", "Delete failed.");
        refreshTable();
    }


    private Dialog<Patient> buildPatientDialog(Patient original) {
        Dialog<Patient> d = new Dialog<>();
        d.setTitle(original == null ? "Add Patient" : "Edit Patient");

        Label lId = new Label("ID:");
        Label lName = new Label("Name:");
        Label lAge = new Label("Age:");
        Label lCond = new Label("Condition:");

        TextField tfId = new TextField();
        TextField tfName = new TextField();
        TextField tfAge = new TextField();
        ComboBox<String> cbCond = new ComboBox<>();

        int cn = cm.count();
        for (int i = 0; i < cn; i++) {
            Condition c = cm.getConditions().get(i);
            if (c != null) cbCond.getItems().add(c.getName());
        }

        if (original != null) {
            tfId.setText(original.getId());
            tfId.setDisable(true);
            tfName.setText(original.getName());
            tfAge.setText(String.valueOf(original.getAge()));
            cbCond.setValue(original.getConditionName());
        }

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10); gp.setPadding(new Insets(10));
        gp.addRow(0, lId, tfId);
        gp.addRow(1, lName, tfName);
        gp.addRow(2, lAge, tfAge);
        gp.addRow(3, lCond, cbCond);

        d.getDialogPane().setContent(gp);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String id = (original == null ? tfId.getText().trim() : original.getId());
                String name = tfName.getText().trim();
                int age = 0;
                try { age = Integer.parseInt(tfAge.getText().trim()); } catch (NumberFormatException ignored) {}
                String cond = cbCond.getValue();
                if (id == null || id.isEmpty() || name.isEmpty()) return null;
                return new Patient(id, name, age, cond);
            }
            return null;
        });

        return d;
    }


    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
}
