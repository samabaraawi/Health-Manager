package ds;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class ReportsTab extends Tab {

    private final PatientManager pm;
    private final ConditionManager cm;

    private TableView<ConditionSummaryRow> tblConditionSummary;
    private TableView<PatientTreatmentRow> tblPatientTreatments;
    private TableView<MedicationAdherenceRow> tblMedicationAdherence;

    // ComboBoxes للتحكم بالـ Asc / Desc
    private ComboBox<String> cbCondSortOrder;
    private ComboBox<String> cbPatientSortOrder;
    private ComboBox<String> cbMedSortOrder;

    // Condition Summary
    public static class ConditionSummaryRow {
        private final String conditionName;
        private final int patients;
        private final String treatments;
        private final String medications;

        public ConditionSummaryRow(String conditionName, int patients,
                                   String treatments, String medications) {
            this.conditionName = conditionName;
            this.patients = patients;
            this.treatments = treatments;
            this.medications = medications;
        }

        public String getConditionName() { return conditionName; }
        public int getPatients()         { return patients; }
        public String getTreatments()    { return treatments; }
        public String getMedications()   { return medications; }
    }

    // Patient Treatment Report
    public static class PatientTreatmentRow {
        private final String patientName;
        private final String conditionName;
        private final String treatments;
        private final String medications;

        public PatientTreatmentRow(String patientName, String conditionName,
                                   String treatments, String medications) {
            this.patientName = patientName;
            this.conditionName = conditionName;
            this.treatments = treatments;
            this.medications = medications;
        }

        public String getPatientName()   { return patientName; }
        public String getConditionName() { return conditionName; }
        public String getTreatments()    { return treatments; }
        public String getMedications()   { return medications; }
    }

    //  Medication Adherence Report
    public static class MedicationAdherenceRow {
        private final String patientName;
        private final String medicationName;
        private final String frequency;
        private final String lastRefill;

        public MedicationAdherenceRow(String patientName, String medicationName,
                                      String frequency, String lastRefill) {
            this.patientName = patientName;
            this.medicationName = medicationName;
            this.frequency = frequency;
            this.lastRefill = lastRefill;
        }

        public String getPatientName()    { return patientName; }
        public String getMedicationName() { return medicationName; }
        public String getFrequency()      { return frequency; }
        public String getLastRefill()     { return lastRefill; }
    }


    public ReportsTab(PatientManager pm, ConditionManager cm) {
        this.pm = pm;
        this.cm = cm;

        setText("Reports");
        setClosable(false);
        setContent(buildContent());
        refreshIntro();
    }


    private TabPane buildContent() {
        Tab tabA = new Tab("Condition Summary");
        Tab tabB = new Tab("Patient Treatments");
        Tab tabC = new Tab("Medication Adherence");

        tabA.setClosable(false);
        tabB.setClosable(false);
        tabC.setClosable(false);

        tabA.setContent(buildConditionSummaryPane());
        tabB.setContent(buildPatientTreatmentsPane());
        tabC.setContent(buildMedicationAdherencePane());

        return new TabPane(tabA, tabB, tabC);
    }

    // ========== A) Condition Summary ==========

    private BorderPane buildConditionSummaryPane() {
        tblConditionSummary = new TableView<>();

        TableColumn<ConditionSummaryRow, String> cCond =
                new TableColumn<>("Condition");
        TableColumn<ConditionSummaryRow, Number> cPatients =
                new TableColumn<>("Patients");
        TableColumn<ConditionSummaryRow, String> cTreats =
                new TableColumn<>("Treatments");
        TableColumn<ConditionSummaryRow, String> cMeds =
                new TableColumn<>("Medications");

        cCond.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getConditionName()));
        cPatients.setCellValueFactory(v ->
                new javafx.beans.property.SimpleIntegerProperty(v.getValue().getPatients()));
        cTreats.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getTreatments()));
        cMeds.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getMedications()));

        tblConditionSummary.getColumns().addAll(cCond, cPatients, cTreats, cMeds);
        tblConditionSummary.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ComboBox للـ Asc/Desc
        cbCondSortOrder = new ComboBox<>();
        cbCondSortOrder.getItems().addAll("Ascending", "Descending");
        cbCondSortOrder.setValue("Ascending");

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> tblConditionSummary.setItems(buildConditionSummaryRows()));

        HBox top = new HBox(10,
                new Label("Condition Summary Report (sorted by condition)"),
                new Label("Order:"),
                cbCondSortOrder,
                btnRefresh);
        top.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(tblConditionSummary);

        tblConditionSummary.setItems(buildConditionSummaryRows());
        return root;
    }


    private ObservableList<ConditionSummaryRow> buildConditionSummaryRows() {
        ObservableList<ConditionSummaryRow> rows = FXCollections.observableArrayList();

        CircularDoublyLinkedList<Condition> cs = cm.getConditions();
        int cn = cs.getSize();
        for (int i = 0; i < cn; i++) {
            Condition c = cs.get(i);
            if (c == null) continue;
            String name = c.getName();
            int patientCount = countPatientsForCondition(name);
            String treatsText = joinStringList(c.getRecTreats());
            String medsText   = joinStringList(c.getRecMeds());
            rows.add(new ConditionSummaryRow(name, patientCount, treatsText, medsText));
        }

        // قراءة اتجاه الفرز من ComboBox
        String order = "Ascending";
        if (cbCondSortOrder != null && cbCondSortOrder.getValue() != null) {
            order = cbCondSortOrder.getValue();
        }

        if ("Descending".equals(order)) {
            FXCollections.sort(rows, (a, b) -> cmpStr(b.getConditionName(), a.getConditionName()));
        } else {
            FXCollections.sort(rows, (a, b) -> cmpStr(a.getConditionName(), b.getConditionName()));
        }

        return rows;
    }

    // ========== B) Patient Treatments ==========

    private BorderPane buildPatientTreatmentsPane() {
        tblPatientTreatments = new TableView<>();

        TableColumn<PatientTreatmentRow, String> cName =
                new TableColumn<>("Patient Name");
        TableColumn<PatientTreatmentRow, String> cCond =
                new TableColumn<>("Condition");
        TableColumn<PatientTreatmentRow, String> cTreats =
                new TableColumn<>("Treatments");
        TableColumn<PatientTreatmentRow, String> cMeds =
                new TableColumn<>("Medications");

        cName.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getPatientName()));
        cCond.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getConditionName()));
        cTreats.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getTreatments()));
        cMeds.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getMedications()));

        tblPatientTreatments.getColumns().addAll(cName, cCond, cTreats, cMeds);
        tblPatientTreatments.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        cbPatientSortOrder = new ComboBox<>();
        cbPatientSortOrder.getItems().addAll("Ascending", "Descending");
        cbPatientSortOrder.setValue("Ascending");

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> tblPatientTreatments.setItems(buildPatientTreatmentRows()));

        HBox top = new HBox(10,
                new Label("Patient Treatment Report (sorted by patient name)"),
                new Label("Order:"),
                cbPatientSortOrder,
                btnRefresh);
        top.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(tblPatientTreatments);

        tblPatientTreatments.setItems(buildPatientTreatmentRows());
        return root;
    }


    private ObservableList<PatientTreatmentRow> buildPatientTreatmentRows() {
        ObservableList<PatientTreatmentRow> rows = FXCollections.observableArrayList();

        CircularDoublyLinkedList<Patient> ps = pm.getPatients();
        int pn = ps.getSize();
        for (int i = 0; i < pn; i++) {
            Patient p = ps.get(i);
            if (p == null) continue;
            String name = p.getName();
            String condName = p.getConditionName();
            String treats = joinTreatmentTypes(p);
            String meds   = joinMedicationNames(p);
            rows.add(new PatientTreatmentRow(name, condName, treats, meds));
        }

        String order = "Ascending";
        if (cbPatientSortOrder != null && cbPatientSortOrder.getValue() != null) {
            order = cbPatientSortOrder.getValue();
        }

        if ("Descending".equals(order)) {
            FXCollections.sort(rows, (a, b) -> cmpStr(b.getPatientName(), a.getPatientName()));
        } else {
            FXCollections.sort(rows, (a, b) -> cmpStr(a.getPatientName(), b.getPatientName()));
        }

        return rows;
    }

    // ========== C) Medication Adherence ==========

    private BorderPane buildMedicationAdherencePane() {
        tblMedicationAdherence = new TableView<>();

        TableColumn<MedicationAdherenceRow, String> cPName =
                new TableColumn<>("Patient Name");
        TableColumn<MedicationAdherenceRow, String> cMed =
                new TableColumn<>("Medication");
        TableColumn<MedicationAdherenceRow, String> cFreq =
                new TableColumn<>("Frequency");
        TableColumn<MedicationAdherenceRow, String> cLast =
                new TableColumn<>("Last Refill");

        cPName.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getPatientName()));
        cMed.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getMedicationName()));
        cFreq.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getFrequency()));
        cLast.setCellValueFactory(v ->
                new javafx.beans.property.SimpleStringProperty(v.getValue().getLastRefill()));

        tblMedicationAdherence.getColumns().addAll(cPName, cMed, cFreq, cLast);
        tblMedicationAdherence.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        cbMedSortOrder = new ComboBox<>();
        cbMedSortOrder.getItems().addAll("Ascending", "Descending");
        cbMedSortOrder.setValue("Ascending");

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> tblMedicationAdherence.setItems(buildMedicationAdherenceRows()));

        HBox top = new HBox(10,
                new Label("Medication Adherence Report (sorted by patient name, then medication)"),
                new Label("Order:"),
                cbMedSortOrder,
                btnRefresh);
        top.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(tblMedicationAdherence);

        tblMedicationAdherence.setItems(buildMedicationAdherenceRows());
        return root;
    }


    private ObservableList<MedicationAdherenceRow> buildMedicationAdherenceRows() {
        ObservableList<MedicationAdherenceRow> rows = FXCollections.observableArrayList();

        CircularDoublyLinkedList<Patient> ps = pm.getPatients();
        int pn = ps.getSize();
        for (int i = 0; i < pn; i++) {
            Patient p = ps.get(i);
            if (p == null) continue;

            CircularDoublyLinkedList<Medication> ms = p.getMeds();
            if (ms == null) continue;
            int mn = ms.getSize();
            for (int j = 0; j < mn; j++) {
                Medication m = ms.get(j);
                if (m == null) continue;
                String lastRefill = "";
                rows.add(new MedicationAdherenceRow(
                        p.getName(),
                        m.getName(),
                        m.getFrequency(),
                        lastRefill
                ));
            }
        }

        String order = "Ascending";
        if (cbMedSortOrder != null && cbMedSortOrder.getValue() != null) {
            order = cbMedSortOrder.getValue();
        }

        if ("Descending".equals(order)) {
            FXCollections.sort(rows, (a, b) -> {
                int c = cmpStr(b.getPatientName(), a.getPatientName());
                if (c != 0) return c;
                return cmpStr(b.getMedicationName(), a.getMedicationName());
            });
        } else {
            FXCollections.sort(rows, (a, b) -> {
                int c = cmpStr(a.getPatientName(), b.getPatientName());
                if (c != 0) return c;
                return cmpStr(a.getMedicationName(), b.getMedicationName());
            });
        }

        return rows;
    }

    // ====== Refresh من برا (من المين) ======
    public void refreshIntro() {
        if (tblConditionSummary != null)
            tblConditionSummary.setItems(buildConditionSummaryRows());
        if (tblPatientTreatments != null)
            tblPatientTreatments.setItems(buildPatientTreatmentRows());
        if (tblMedicationAdherence != null)
            tblMedicationAdherence.setItems(buildMedicationAdherenceRows());
    }

    // ====== Helpers ======

    private int countPatientsForCondition(String condName) {
        if (condName == null) return 0;
        int count = 0;
        CircularDoublyLinkedList<Patient> ps = pm.getPatients();
        int pn = ps.getSize();
        for (int i = 0; i < pn; i++) {
            Patient p = ps.get(i);
            if (p == null) continue;
            String c = p.getConditionName();
            if (c != null && c.equals(condName)) count++;
        }
        return count;
    }

    private String joinStringList(CircularDoublyLinkedList<String> list) {
        if (list == null || list.getSize() == 0) return "-";
        StringBuilder sb = new StringBuilder();
        int n = list.getSize();
        for (int i = 0; i < n; i++) {
            sb.append(list.get(i));
            if (i < n - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private String joinTreatmentTypes(Patient p) {
        CircularDoublyLinkedList<Treatment> ts = p.getTreatments();
        if (ts == null || ts.getSize() == 0) return "-";
        StringBuilder sb = new StringBuilder();
        int n = ts.getSize();
        for (int i = 0; i < n; i++) {
            Treatment t = ts.get(i);
            if (t == null) continue;
            String type = t.getType();
            if (type == null || type.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(type);
        }
        if (sb.length() == 0) return "-";
        return sb.toString();
    }

    private String joinMedicationNames(Patient p) {
        CircularDoublyLinkedList<Medication> ms = p.getMeds();
        if (ms == null || ms.getSize() == 0) return "-";
        StringBuilder sb = new StringBuilder();
        int n = ms.getSize();
        for (int i = 0; i < n; i++) {
            Medication m = ms.get(i);
            if (m == null) continue;
            String name = m.getName();
            if (name == null || name.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(name);
        }
        if (sb.length() == 0) return "-";
        return sb.toString();
    }

    private int cmpStr(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return 1;
        if (s2 == null) return -1;
        return s1.compareToIgnoreCase(s2);
    }
}
