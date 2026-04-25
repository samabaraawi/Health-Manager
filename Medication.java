package ds;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;


public class LoadDataTab extends Tab {

    private final FileLoader loader;
    private final PatientManager pm;
    private final ConditionManager cm;

  // Callback invoked after a successful load to update the remaining tabs
   private Runnable onAfterLoad;

    public LoadDataTab(FileLoader loader, PatientManager pm, ConditionManager cm) {
        this.loader = loader;
        this.pm = pm;
        this.cm = cm;

        setText("Load Data");
        setClosable(false);
        setContent(buildContent());
    }

    public void setOnAfterLoad(Runnable r) {
        this.onAfterLoad = r;
    }

    private GridPane buildContent() {
        Button btnLoadCond = new Button("Load Conditions...");
        Button btnLoadPat  = new Button("Load Patients...");
        Button btnLoadTrt  = new Button("Load Treatments...");
        Button btnLoadMed  = new Button("Load Medications...");
        Label status = new Label();


        btnLoadCond.setOnAction(e -> {
            try {
                int loadedCount = loader.loadConditionsWithChooser(
                        getTabPane().getScene().getWindow(),
                        cm
                );
                status.setText("Loaded conditions: " + loadedCount);
                if (onAfterLoad != null) onAfterLoad.run();
            } catch (Exception ex) {
                status.setText("Error loading conditions: " + ex.getMessage());
            }
        });



        btnLoadPat.setOnAction(e -> {
            try {
                int loadedCount = loader.loadPatientsWithChooser(
                        getTabPane().getScene().getWindow(),
                        pm
                );
                status.setText("Loaded patients: " + loadedCount);
                if (onAfterLoad != null) onAfterLoad.run();
            } catch (Exception ex) {
                status.setText("Error loading patients: " + ex.getMessage());
            }
        });



        btnLoadTrt.setOnAction(e -> {
            try {
                int added = loader.loadTreatmentsWithChooser(getTabPane().getScene().getWindow(), pm, cm);
                status.setText("Treatments added (recommended only): " + added);
                if (onAfterLoad != null) onAfterLoad.run();
            } catch (Exception ex) {
                status.setText("Error loading treatments: " + ex.getMessage());
            }
        });


        btnLoadMed.setOnAction(e -> {
            try {
                int added = loader.loadMedicationsWithChooser(getTabPane().getScene().getWindow(), pm, cm);
                status.setText("Medications added (recommended only): " + added);
                if (onAfterLoad != null) onAfterLoad.run();
            } catch (Exception ex) {
                status.setText("Error loading medications: " + ex.getMessage());
            }
        });

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(10); gp.setPadding(new Insets(16));

        gp.add(new Label("conditions.txt"), 0, 0); gp.add(btnLoadCond, 1, 0);
        gp.add(new Label("patients.txt"),   0, 1); gp.add(btnLoadPat,  1, 1);
        gp.add(new Label("treatments.txt"), 0, 2); gp.add(btnLoadTrt,  1, 2);
        gp.add(new Label("medications.txt"),0, 3); gp.add(btnLoadMed,  1, 3);

        HBox footer = new HBox(10, new Label("Status:"), status);
        gp.add(footer, 0, 4, 2, 1);

        return gp;
    }
}
