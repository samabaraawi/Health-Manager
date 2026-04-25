package ds;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;


public class MedicationsTab extends Tab {

    private final PatientManager pm;
    private final ConditionManager cm;

    private final TableView<MedicationRow> table = new TableView<>();
    private ComboBox<String> cbSort;

    public MedicationsTab(PatientManager pm, ConditionManager cm) {
        this.pm = pm;
        this.cm = cm;

        setText("Medications");
        setClosable(false);
        setContent(buildContent());
        refreshTable();
    }


    public static class MedicationRow {
        final String patientId;
        final String patientName;
        final String conditionName;
        final String medicationId;
        String name;
        String dosage;
        String frequency;

        public MedicationRow(String patientId, String patientName, String conditionName,
                             String medicationId, String name, String dosage, String frequency) {
            this.patientId = patientId;
            this.patientName = patientName;
            this.conditionName = conditionName;
            this.medicationId = medicationId;
            this.name = name;
            this.dosage = dosage;
            this.frequency = frequency;
        }

        public String getPatientId()     { return patientId; }
        public String getPatientName()   { return patientName; }
        public String getConditionName() { return conditionName; }
        public String getMedicationId()  { return medicationId; }
        public String getName()          { return name; }
        public String getDosage()        { return dosage; }
        public String getFrequency()     { return frequency; }
    }


    private BorderPane buildContent() {
        TableColumn<MedicationRow, String> cPid   = new TableColumn<>("Patient ID");
        TableColumn<MedicationRow, String> cPName = new TableColumn<>("Patient Name");
        TableColumn<MedicationRow, String> cCond  = new TableColumn<>("Condition");
        TableColumn<MedicationRow, String> cMid   = new TableColumn<>("Medication ID");
        TableColumn<MedicationRow, String> cName  = new TableColumn<>("Name");
        TableColumn<MedicationRow, String> cDose  = new TableColumn<>("Dosage");
        TableColumn<MedicationRow, String> cFreq  = new TableColumn<>("Frequency");

        cPid.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getPatientId()));
        cPName.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getPatientName()));
        cCond.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getConditionName()));
        cMid.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getMedicationId()));
        cName.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getName()));
        cDose.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getDosage()));
        cFreq.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getFrequency()));

        table.getColumns().addAll(cPid, cPName, cCond, cMid, cName, cDose, cFreq);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnAdd = new Button("Add");
        Button btnEdit = new Button("Edit");
        Button btnDel = new Button("Delete");
        Button btnRefresh = new Button("Refresh");

        btnAdd.setOnAction(e -> onAdd());
        btnEdit.setOnAction(e -> onEdit());
        btnDel.setOnAction(e -> onDelete());
        btnRefresh.setOnAction(e -> refreshTable());

        cbSort = new ComboBox<>();
        cbSort.getItems().addAll(
                "Patient ID",
                "Patient Name",
                "Condition",
                "Medication Name"
        );
        cbSort.setValue("Patient ID");

        Button btnApplySort = new Button("Apply Sort");
        btnApplySort.setOnAction(e -> refreshTable());

        HBox sortBox = new HBox(8, new Label("Sort by:"), cbSort, btnApplySort);
        sortBox.setPadding(new Insets(10));

        HBox crudBox = new HBox(10, btnAdd, btnEdit, btnDel, btnRefresh);
        crudBox.setPadding(new Insets(10));

        HBox top = new HBox(20, crudBox, sortBox);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        return root;
    }

    private ObservableList<MedicationRow> buildRows() {
        ObservableList<MedicationRow> rows = FXCollections.observableArrayList();
        CircularDoublyLinkedList<Patient> ps = pm.getPatients();
        int pn = ps.getSize();
        for (int i = 0; i < pn; i++) {
            Patient p = ps.get(i);
            if (p == null) continue;
            String condName = p.getConditionName();
            CircularDoublyLinkedList<Medication> ms = p.getMeds();
            int mn = ms.getSize();
            for (int j = 0; j < mn; j++) {
                Medication m = ms.get(j);
                if (m == null) continue;
                rows.add(new MedicationRow(
                        p.getId(), p.getName(), condName,
                        m.getId(), m.getName(), m.getDosage(), m.getFrequency()
                ));
            }
        }
        return rows;
    }

    private void applySorting(ObservableList<MedicationRow> rows) {
        String crit = cbSort.getValue();
        if (crit == null) return;

        FXCollections.sort(rows, (a, b) -> {
            switch (crit) {
                case "Patient ID":
                    return cmpStr(a.patientId, b.patientId);
                case "Patient Name":
                    return cmpStr(a.patientName, b.patientName);
                case "Condition":
                    return cmpStr(a.conditionName, b.conditionName);
                case "Medication Name":
                    return cmpStr(a.name, b.name);
                default:
                    return 0;
            }
        });
    }


    private int cmpStr(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return 1;
        if (s2 == null) return -1;
        return s1.compareToIgnoreCase(s2);
    }


    public void refreshTable() {
        ObservableList<MedicationRow> rows = buildRows();
        applySorting(rows);
        table.setItems(rows);
    }

    private void onAdd() {
        Dialog<MedicationRow> dlg = buildAddDialog();
        dlg.showAndWait().ifPresent(row -> {
            Patient p = pm.findById(row.patientId);
            if (p == null) { showAlert("Add Medication", "Patient not found."); return; }

            Condition cond = (p.getConditionName() == null) ? null : cm.findByName(p.getConditionName());
            Medication m = new Medication(row.medicationId, row.name, row.dosage, row.frequency);

            boolean ok = p.addMedicationIfAllowed(m, cond);
            if (!ok) showAlert("Add Medication", "Not allowed for patient's condition or invalid input.");
            refreshTable();
        });
    }


    private void onEdit() {
        MedicationRow sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Edit Medication", "Select a medication first."); return; }

        Patient p = pm.findById(sel.patientId);
        if (p == null) { showAlert("Edit Medication", "Patient not found."); return; }

        Dialog<MedicationRow> dlg = buildEditDialog(sel, p);
        dlg.showAndWait().ifPresent(newRow -> {
            boolean removed = p.removeMedicationById(sel.medicationId);
            if (!removed) { showAlert("Edit Medication", "Original medication not found."); refreshTable(); return; }

            Condition cond = (p.getConditionName() == null) ? null : cm.findByName(p.getConditionName());
            Medication mNew = new Medication(newRow.medicationId, newRow.name, newRow.dosage, newRow.frequency);

            boolean ok = p.addMedicationIfAllowed(mNew, cond);
            if (!ok) {
                showAlert("Edit Medication", "Not allowed for patient's condition. Reverting original.");
                Medication mOld = new Medication(sel.medicationId, sel.name, sel.dosage, sel.frequency);
                p.addMedication(mOld);
            }
            refreshTable();
        });
    }


    private void onDelete() {
        MedicationRow sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("Delete Medication", "Select a medication first."); return; }
        Patient p = pm.findById(sel.patientId);
        if (p == null) { showAlert("Delete Medication", "Patient not found."); return; }
        boolean ok = p.removeMedicationById(sel.medicationId);
        if (!ok) showAlert("Delete Medication", "Delete failed.");
        refreshTable();
    }


    private Dialog<MedicationRow> buildAddDialog() {
        Dialog<MedicationRow> d = new Dialog<>();
        d.setTitle("Add Medication");

        Label lPid = new Label("Patient ID:");
        Label lMid = new Label("Medication ID:");
        Label lName = new Label("Name:");
        Label lDose = new Label("Dosage:");
        Label lFreq = new Label("Frequency:");

        ComboBox<String> cbPatient = new ComboBox<>();
        TextField tfMid = new TextField();
        ComboBox<String> cbName = new ComboBox<>();
        TextField tfDose = new TextField();
        TextField tfFreq = new TextField();

        int pn = pm.count();
        CircularDoublyLinkedList<Patient> ps = pm.getPatients();
        for (int i = 0; i < pn; i++) {
            Patient p = ps.get(i);
            if (p != null) cbPatient.getItems().add(p.getId());
        }

        cbPatient.valueProperty().addListener((obs, oldV, newV) -> {
            cbName.getItems().clear();
            Patient p = pm.findById(newV);
            if (p != null) {
                String cname = p.getConditionName();
                Condition c = (cname == null) ? null : cm.findByName(cname);
                if (c != null) {
                    CircularDoublyLinkedList<String> meds = c.getRecMeds();
                    int mn = meds.getSize();
                    for (int i = 0; i < mn; i++) cbName.getItems().add(meds.get(i));
                }
            }
        });

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10); gp.setPadding(new Insets(10));
        gp.addRow(0, lPid, cbPatient);
        gp.addRow(1, lMid, tfMid);
        gp.addRow(2, lName, cbName);
        gp.addRow(3, lDose, tfDose);
        gp.addRow(4, lFreq, tfFreq);

        d.getDialogPane().setContent(gp);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String patientId = cbPatient.getValue();
                String mid = tfMid.getText().trim();
                String name = cbName.getValue();
                String dose = tfDose.getText().trim();
                String freq = tfFreq.getText().trim();
                if (patientId == null || patientId.isEmpty() || mid.isEmpty() || name == null) return null;
                Patient p = pm.findById(patientId);
                String pname = (p == null) ? "" : p.getName();
                String cname = (p == null) ? null : p.getConditionName();
                return new MedicationRow(patientId, pname, cname, mid, name, dose, freq);
            }
            return null;
        });

        return d;
    }

    private Dialog<MedicationRow> buildEditDialog(MedicationRow original, Patient p) {
        Dialog<MedicationRow> d = new Dialog<>();
        d.setTitle("Edit Medication");

        Label lPid = new Label("Patient ID:");
        Label lMid = new Label("Medication ID:");
        Label lName = new Label("Name:");
        Label lDose = new Label("Dosage:");
        Label lFreq = new Label("Frequency:");

        TextField tfPid = new TextField(original.patientId);
        tfPid.setDisable(true);

        TextField tfMid = new TextField(original.medicationId);

        ComboBox<String> cbName = new ComboBox<>();
        TextField tfDose = new TextField(original.dosage);
        TextField tfFreq = new TextField(original.frequency);

        Condition c = (p.getConditionName() == null) ? null : cm.findByName(p.getConditionName());
        if (c != null) {
            CircularDoublyLinkedList<String> meds = c.getRecMeds();
            int mn = meds.getSize();
            for (int i = 0; i < mn; i++) cbName.getItems().add(meds.get(i));
        }
        cbName.setValue(original.name);

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10); gp.setPadding(new Insets(10));
        gp.addRow(0, lPid, tfPid);
        gp.addRow(1, lMid, tfMid);
        gp.addRow(2, lName, cbName);
        gp.addRow(3, lDose, tfDose);
        gp.addRow(4, lFreq, tfFreq);

        d.getDialogPane().setContent(gp);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String mid = tfMid.getText().trim();
                String name = cbName.getValue();
                String dose = tfDose.getText().trim();
                String freq = tfFreq.getText().trim();
                if (mid.isEmpty() || name == null) return null;
                return new MedicationRow(original.patientId, original.patientName, original.conditionName,
                        mid, name, dose, freq);
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
