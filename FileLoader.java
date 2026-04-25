package ds;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;


public class ConditionsTab extends Tab {

    private final ConditionManager cm;
    private final TableView<ConditionRow> table = new TableView<>();

    public ConditionsTab(ConditionManager cm) {
        this.cm = cm;

        setText("Conditions");
        setClosable(false);
        setContent(buildContent());
        refreshTable();
    }


    public static class ConditionRow {
        final String name;
        final int recTreatsCount;
        final int recMedsCount;
        final String recTreatsText;
        final String recMedsText;

        public ConditionRow(String name, int recTreatsCount, int recMedsCount,
                            String recTreatsText, String recMedsText) {
            this.name = name;
            this.recTreatsCount = recTreatsCount;
            this.recMedsCount = recMedsCount;
            this.recTreatsText = recTreatsText;
            this.recMedsText = recMedsText;
        }

        public String getName() {
            return name; }
        public int getRecTreatsCount() {
            return recTreatsCount; }
        public int getRecMedsCount() {
            return recMedsCount; }
        public String getRecTreatsText() {
            return recTreatsText; }
        public String getRecMedsText() {
            return recMedsText; }
    }


    private BorderPane buildContent() {
        TableColumn<ConditionRow, String> cName  = new TableColumn<>("Condition");
        TableColumn<ConditionRow, String> cTText = new TableColumn<>("Recommended Treatments");
        TableColumn<ConditionRow, String> cMText = new TableColumn<>("Recommended Medications");
        TableColumn<ConditionRow, Number> cTCnt  = new TableColumn<>("#Treatments");
        TableColumn<ConditionRow, Number> cMCnt  = new TableColumn<>("#Medications");

        cName.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getName()));
        cTText.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getRecTreatsText()));
        cMText.setCellValueFactory(v -> new javafx.beans.property.SimpleStringProperty(v.getValue().getRecMedsText()));
        cTCnt.setCellValueFactory(v -> new javafx.beans.property.SimpleIntegerProperty(v.getValue().getRecTreatsCount()));
        cMCnt.setCellValueFactory(v -> new javafx.beans.property.SimpleIntegerProperty(v.getValue().getRecMedsCount()));

        table.getColumns().addAll(cName, cTCnt, cMCnt, cTText, cMText);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnAdd = new Button("Add");
        Button btnEdit = new Button("Edit");
        Button btnDel = new Button("Delete");
        Button btnRefresh = new Button("Refresh");

        btnAdd.setOnAction(e -> onAdd());
        btnEdit.setOnAction(e -> onEdit());
        btnDel.setOnAction(e -> onDelete());
        btnRefresh.setOnAction(e -> refreshTable());

        HBox top = new HBox(10, btnAdd, btnEdit, btnDel, btnRefresh);
        top.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(table);
        return root;
    }


    private ObservableList<ConditionRow> buildRows() {
        ObservableList<ConditionRow> rows = FXCollections.observableArrayList();
        CircularDoublyLinkedList<Condition> cs = cm.getConditions();
        int cn = cs.getSize();
        for (int i = 0; i < cn; i++) {
            Condition c = cs.get(i);
            if (c == null) continue;
            int tCount = c.getRecTreats() == null ? 0 : c.getRecTreats().getSize();
            int mCount = c.getRecMeds()   == null ? 0 : c.getRecMeds().getSize();
            String tText = previewList(c.getRecTreats(), 3);
            String mText = previewList(c.getRecMeds(), 3);
            rows.add(new ConditionRow(c.getName(), tCount, mCount, tText, mText));
        }
        return rows;
    }


    public void refreshTable() {
        table.setItems(buildRows());
    }


    private void onAdd() {
        Dialog<Condition> dlg = buildAddDialog();
        dlg.showAndWait().ifPresent(cond -> {
            if (cond.getName() == null || cond.getName().trim().isEmpty()) {
                show("Add Condition", "Name is required.");
                return;
            }
            if (cm.findByName(cond.getName()) != null) {
                show("Add Condition", "Condition already exists.");
                return;
            }
            cm.addCondition(cond);
            refreshTable();
        });
    }


    private void onEdit() {
        ConditionRow sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            show("Edit Condition", "Select a condition first.");
            return;
        }

        Condition original = cm.findByName(sel.name);
        if (original == null) {
            show("Edit Condition", "Original not found.");
            return;
        }

        Dialog<Condition> dlg = buildEditDialog(original);
        dlg.showAndWait().ifPresent(updated -> {
            if (!updated.getName().equals(original.getName()) && cm.findByName(updated.getName()) != null) {
                show("Edit Condition", "Another condition with the same name exists.");
                return;
            }
            original.setName(updated.getName());
            copyList(updated.getRecTreats(), original.getRecTreats());
            copyList(updated.getRecMeds(), original.getRecMeds());
            refreshTable();
        });
    }


    private void onDelete() {
        ConditionRow sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            show("Delete Condition", "Select a condition first."); return; }

        boolean ok = cm.deleteByName(sel.name);
        if (!ok)
            show("Delete Condition", "Condition not found.");
        refreshTable();

    }



    private Dialog<Condition> buildAddDialog() {
        Dialog<Condition> d = new Dialog<>();
        d.setTitle("Add Condition");

        Label lName = new Label("Condition Name:");
        Label lTreats = new Label("Recommended Treatments (comma-separated):");
        Label lMeds = new Label("Recommended Medications (comma-separated):");

        TextField tfName = new TextField();
        TextArea taTreats = new TextArea();
        TextArea taMeds = new TextArea();

        taTreats.setPromptText("e.g., Insulin Therapy,Diet Counseling");
        taMeds.setPromptText("e.g., Insulin,Metformin");

        taTreats.setPrefRowCount(3);
        taMeds.setPrefRowCount(3);

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10); gp.setPadding(new Insets(10));
        gp.addRow(0, lName, tfName);
        gp.add(lTreats, 0, 1); gp.add(taTreats, 1, 1);
        gp.add(lMeds, 0, 2); gp.add(taMeds, 1, 2);

        d.getDialogPane().setContent(gp);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String name = tfName.getText().trim();
                if (name.isEmpty()) return null;

                Condition c = new Condition(name);
                CircularDoublyLinkedList<String> t = tokenizeCSV(taTreats.getText());
                CircularDoublyLinkedList<String> m = tokenizeCSV(taMeds.getText());
                copyList(t, c.getRecTreats());
                copyList(m, c.getRecMeds());
                return c;
            }
            return null;
        });

        return d;
    }


    private Dialog<Condition> buildEditDialog(Condition original) {
        Dialog<Condition> d = new Dialog<>();
        d.setTitle("Edit Condition");

        Label lName = new Label("Condition Name:");
        Label lTreats = new Label("Recommended Treatments (comma-separated):");
        Label lMeds = new Label("Recommended Medications (comma-separated):");

        TextField tfName = new TextField(original.getName());
        TextArea taTreats = new TextArea(joinList(original.getRecTreats()));
        TextArea taMeds = new TextArea(joinList(original.getRecMeds()));

        taTreats.setPrefRowCount(3);
        taMeds.setPrefRowCount(3);

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10); gp.setPadding(new Insets(10));
        gp.addRow(0, lName, tfName);
        gp.add(lTreats, 0, 1); gp.add(taTreats, 1, 1);
        gp.add(lMeds, 0, 2); gp.add(taMeds, 1, 2);

        d.getDialogPane().setContent(gp);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String name = tfName.getText().trim();
                if (name.isEmpty()) return null;

                Condition c = new Condition(name);
                CircularDoublyLinkedList<String> t = tokenizeCSV(taTreats.getText());
                CircularDoublyLinkedList<String> m = tokenizeCSV(taMeds.getText());
                copyList(t, c.getRecTreats());
                copyList(m, c.getRecMeds());
                return c;
            }
            return null;
        });

        return d;
    }


// Converts a comma-separated string into a CDLL<String> (without using String.split)
    private CircularDoublyLinkedList<String> tokenizeCSV(String text) {
        CircularDoublyLinkedList<String> out = new CircularDoublyLinkedList<>();
        if (text == null || text.isEmpty()) return out;
        int start = 0;
        while (true) {
            int idx = text.indexOf(',', start);
            if (idx == -1) {
                String token = text.substring(start).trim();
                if (!token.isEmpty()) out.addLast(token);
                break;
            } else {
                String token = text.substring(start, idx).trim();
                if (!token.isEmpty()) out.addLast(token);
                start = idx + 1;
            }
        }
        return out;
    }


    private String previewList(CircularDoublyLinkedList<String> list, int limit) {
        if (list == null || list.getSize() == 0) return "-";
        int n = list.getSize();
        StringBuilder sb = new StringBuilder();
        int shown = Math.min(n, limit);
        for (int i = 0; i < shown; i++) {
            sb.append(list.get(i));
            if (i < shown - 1) sb.append(", ");
        }
        if (n > limit) sb.append(" +").append(n - limit);
        return sb.toString();
    }

  // Copies the contents of src into dst (after clearing dst).
    private void copyList(CircularDoublyLinkedList<String> src, CircularDoublyLinkedList<String> dst) {
        if (dst == null) return;
        dst.clear();
        if (src == null) return;
        int n = src.getSize();
        for (int i = 0; i < n; i++) dst.addLast(src.get(i));
    }

    private void show(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

// Converts a CDLL<String> into a CSV string
   private String joinList(CircularDoublyLinkedList<String> list) {
        if (list == null || list.getSize() == 0) return "";
        StringBuilder sb = new StringBuilder();
        int n = list.getSize();
        for (int i = 0; i < n; i++) {
            sb.append(list.get(i));
            if (i < n - 1) sb.append(",");
        }
        return sb.toString();
    }
}
