package ds;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class SymptomsTab extends Tab {

    private final PatientManager pm;
    private final ComboBox<Patient> cbPatients = new ComboBox<>();
    private final TableView<SymptomRow> table = new TableView<>();

    public static class SymptomRow {
        private final String patientId;
        private final String patientName;
        private final String description;
        private final String dateRecorded;
        private final int symptomIndex;

        public SymptomRow(String patientId, String patientName,
                          String description, String dateRecorded,
                          int symptomIndex) {
            this.patientId = patientId;
            this.patientName = patientName;
            this.description = description;
            this.dateRecorded = dateRecorded;
            this.symptomIndex = symptomIndex;
        }

        public String getPatientId() { return patientId; }
        public String getPatientName() { return patientName; }
        public String getDescription() { return description; }
        public String getDateRecorded() { return dateRecorded; }
        public int getSymptomIndex() { return symptomIndex; }
    }

    public void refreshAll() {
        loadPatientsIntoCombo();
        refreshTable();
    }



    public SymptomsTab(PatientManager pm) {
        this.pm = pm;

        setText("Symptoms");
        setClosable(false);
        setContent(buildContent());

        loadPatientsIntoCombo();
        refreshTable();
    }

    private BorderPane buildContent() {

        TableColumn<SymptomRow, String> cPid  = new TableColumn<>("Patient ID");
        TableColumn<SymptomRow, String> cPnm  = new TableColumn<>("Patient Name");
        TableColumn<SymptomRow, String> cDesc = new TableColumn<>("Symptom");
        TableColumn<SymptomRow, String> cDate = new TableColumn<>("Date Recorded");

        cPid.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getPatientId()));
        cPnm.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getPatientName()));
        cDesc.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getDescription()));
        cDate.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getDateRecorded()));

        table.getColumns().addAll(cPid, cPnm, cDesc, cDate);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label lblPatient = new Label("Patient:");
        cbPatients.setPromptText("Select patient");

        cbPatients.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Patient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getId() + " - " + item.getName());
            }
        });

        cbPatients.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Patient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("Select patient");
                else setText(item.getId() + " - " + item.getName());
            }
        });

        cbPatients.setOnAction(e -> refreshTable());

        Button btnAdd = new Button("Add Symptom");
        Button btnDelete = new Button("Delete Symptom");
        Button btnClear = new Button("Clear All");
        Button btnRefresh = new Button("Refresh");

        btnAdd.setOnAction(e -> onAddSymptom());
        btnDelete.setOnAction(e -> onDeleteSymptom());
        btnClear.setOnAction(e -> onClearAll());
        btnRefresh.setOnAction(e -> refreshAll());

        HBox top = new HBox(10, lblPatient, cbPatients, btnAdd, btnDelete, btnClear, btnRefresh);
        top.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        return root;
    }

    private void loadPatientsIntoCombo() {
        cbPatients.getItems().clear();

        CircularDoublyLinkedList<Patient> ps = pm.getPatients();
        int n = ps.getSize();
        for (int i = 0; i < n; i++) {
            Patient p = ps.get(i);
            if (p != null) cbPatients.getItems().add(p);
        }
    }

    private ObservableList<SymptomRow> buildRows() {
        ObservableList<SymptomRow> rows = FXCollections.observableArrayList();

        Patient p = cbPatients.getValue();
        if (p == null) return rows;

        CircularDoublyLinkedList<Symptom> list = p.getSymptoms();
        if (list == null) return rows;

        int n = list.getSize();
        for (int i = 0; i < n; i++) {
            Symptom s = list.get(i);
            if (s == null) continue;

            rows.add(new SymptomRow(
                    p.getId(),
                    p.getName(),
                    s.getDescription(),
                    s.getDateRecorded(),
                    i
            ));
        }

        return rows;
    }

    public void refreshTable() {
        table.setItems(buildRows());
    }

    private void onAddSymptom() {
        Patient p = cbPatients.getValue();
        if (p == null) {
            show("Add Symptom", "Select a patient first.");
            return;
        }

        Dialog<Symptom> dlg = buildAddDialog(p);
        dlg.showAndWait().ifPresent(sym -> {
            p.addSymptom(sym);
            refreshTable();
        });
    }

    private void onDeleteSymptom() {
        Patient p = cbPatients.getValue();
        if (p == null) {
            show("Delete Symptom", "Select a patient first.");
            return;
        }

        SymptomRow sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            show("Delete Symptom", "Select a row first.");
            return;
        }

        if (!sel.getPatientId().equals(p.getId())) {
            show("Delete Symptom", "Symptom does not match selected patient.");
            return;
        }

        int idx = sel.getSymptomIndex();
        if (idx < 0 || idx >= p.getSymptoms().getSize()) {
            show("Delete Symptom", "Index out of range.");
            return;
        }

        boolean ok = p.removeSymptomAt(idx);
        if (!ok) {
            show("Delete Symptom", "Index out of range.");
        }
        refreshTable();

    }

    private void onClearAll() {
        Patient p = cbPatients.getValue();
        if (p == null) {
            show("Clear All", "Select a patient first.");
            return;
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Clear All Symptoms");
        a.setHeaderText("Are you sure?");
        a.setContentText("This will delete ALL symptoms for patient: " + p.getName());
        a.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                p.clearAllSymptoms();
                refreshTable();
            }
        });
    }

    private Dialog<Symptom> buildAddDialog(Patient p) {
        Dialog<Symptom> d = new Dialog<>();
        d.setTitle("Add Symptom to " + p.getName());

        Label l1 = new Label("Description:");
        Label l2 = new Label("Date:");

        TextField t1 = new TextField();
        TextField t2 = new TextField();

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(10));

        gp.add(l1, 0, 0); gp.add(t1, 1, 0);
        gp.add(l2, 0, 1); gp.add(t2, 1, 1);

        d.getDialogPane().setContent(gp);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String desc = t1.getText().trim();
                String date = t2.getText().trim();

                if (desc.isEmpty()) return null;

                return new Symptom(desc, date);
            }
            return null;
        });

        return d;
    }

    private void show(String title, String txt) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(txt);
        a.showAndWait();
    }
}
