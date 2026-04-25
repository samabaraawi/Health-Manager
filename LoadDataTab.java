package ds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

import javafx.stage.FileChooser;
import javafx.stage.Window;


public class FileLoader {

    private final String delimiter;
    private final char listSep;
    private File lastDir;


    public FileLoader() {
        this("|", ',');
    }

    public FileLoader(String delimiter, char listSep) {
        this.delimiter = delimiter;
        this.listSep = listSep;
        this.lastDir = null;
    }



    private File chooseTxtFile(Window owner, String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
        if (lastDir != null && lastDir.exists() && lastDir.isDirectory()) {
            fc.setInitialDirectory(lastDir);
        }
        File f = fc.showOpenDialog(owner);
        if (f != null && f.getParentFile() != null) lastDir = f.getParentFile();
        return f;
    }



    private CircularDoublyLinkedList<String> tokenize(String line) {
        CircularDoublyLinkedList<String> parts = new CircularDoublyLinkedList<>();
        if (line == null) return parts;

        int n = line.length();
        int dlen = delimiter.length();
        int i = 0, start = 0;


        while (i <= n - dlen) {
            boolean hit = true;
            for (int j = 0; j < dlen; j++) {
                if (line.charAt(i + j) != delimiter.charAt(j)) { hit = false; break; }
            }
            if (hit) {
                String token = line.substring(start, i).trim();
                parts.addLast(token);
                i += dlen;
                start = i;
            } else {
                i++;
            }
        }
        String last = line.substring(start).trim();
        parts.addLast(last);
        return parts;
    }

    private CircularDoublyLinkedList<String> splitInnerList(String field) {
        CircularDoublyLinkedList<String> list = new CircularDoublyLinkedList<>();
        if (field == null || field.isEmpty()) return list;

        int n = field.length();
        int i = 0, start = 0;
        while (i < n) {
            if (field.charAt(i) == listSep) {
                String token = field.substring(start, i).trim();
                if (!token.isEmpty()) list.addLast(token);
                i++; start = i;
            } else {
                i++;
            }
        }
        String last = field.substring(start).trim();
        if (!last.isEmpty()) list.addLast(last);

        return list;
    }


    private boolean skipLine(String s) {
        if (s == null) return true;
        String t = s.trim();
        return t.isEmpty() || t.startsWith("#") || t.startsWith("//");
    }


    private CircularDoublyLinkedList<String> tokenizeWith(String line, String delimiter) {
        CircularDoublyLinkedList<String> parts = new CircularDoublyLinkedList<>();
        if (line == null || delimiter == null || line.isEmpty()) return parts;

        int dlen = delimiter.length();
        int start = 0;
        while (true) {
            int idx = line.indexOf(delimiter, start);
            if (idx == -1) {
                String token = line.substring(start).trim();
                if (!token.isEmpty()) parts.addLast(token);
                break;
            } else {
                String token = line.substring(start, idx).trim();
                if (!token.isEmpty()) parts.addLast(token);
                start = idx + dlen;
            }
        }
        return parts;
    }



    public int loadConditionsWithChooser(Window owner, ConditionManager cm) throws IOException {
        File f = chooseTxtFile(owner, "Choose conditions.txt");
        if (f == null) return 0;

        cm.getConditions().clear();
        int added = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (skipLine(line)) continue;

                CircularDoublyLinkedList<String> parts = tokenize(line);
                String name       = parts.getSize() > 0 ? parts.get(0) : null;
                String treatsStr  = parts.getSize() > 1 ? parts.get(1) : "";
                String medsStr    = parts.getSize() > 2 ? parts.get(2) : "";

                if (name == null || name.isEmpty()) continue;

                Condition c = new Condition(name);

                CircularDoublyLinkedList<String> treats = splitInnerList(treatsStr);
                for (int i = 0; i < treats.getSize(); i++) {
                    c.addRecommendedTreatment(treats.get(i));
                }

                CircularDoublyLinkedList<String> meds = splitInnerList(medsStr);
                for (int i = 0; i < meds.getSize(); i++) {
                    c.addRecommendedMedication(meds.get(i));
                }

                cm.addCondition(c);
                added++;
            }
        }
        return added;
    }


    public int loadPatientsWithChooser(Window owner, PatientManager pm) throws IOException {
        File f = chooseTxtFile(owner, "Choose patients.txt");
        if (f == null) return 0;

        pm.getPatients().clear();
        int added = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (skipLine(line)) continue;

                CircularDoublyLinkedList<String> parts = tokenizeWith(line, ",");
                String id   = parts.getSize() > 0 ? parts.get(0) : null;
                String name = parts.getSize() > 1 ? parts.get(1) : null;
                String ageS = parts.getSize() > 2 ? parts.get(2) : null;
                String cond = parts.getSize() > 3 ? parts.get(3) : null;

                if (id == null || name == null || ageS == null) continue;

                int age = 0;
                try {
                    age = Integer.parseInt(ageS.trim());
                } catch (NumberFormatException ignored) {}

                Patient p = new Patient(id, name, age, cond);

                if (pm.addPatientUnique(p)) {
                    added++;
                }
            }
        }
        return added;
    }



    public int loadTreatmentsWithChooser(Window owner, PatientManager pm, ConditionManager cm) throws IOException {
        File f = chooseTxtFile(owner, "Choose treatments.txt");
        if (f == null) return 0;

        int added = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (skipLine(line)) continue;
                CircularDoublyLinkedList<String> parts = tokenizeWith(line, ",");
                String tid       = parts.getSize() > 0 ? parts.get(0).trim() : null;
                String patientId = parts.getSize() > 1 ? parts.get(1).trim() : null;
                String type      = parts.getSize() > 2 ? parts.get(2).trim() : null;
                String start     = parts.getSize() > 3 ? parts.get(3).trim() : null;
                String end       = parts.getSize() > 4 ? parts.get(4).trim() : null;
                if (tid == null || patientId == null || type == null) continue;

                Patient p = pm.findById(patientId);
                if (p == null) continue;
                Condition cond = (p.getConditionName() == null) ? null : cm.findByName(p.getConditionName());
                if (p.addTreatmentIfAllowed(new Treatment(tid, type, start, end), cond)) added++;
            }
        }
        return added;
    }


    public int loadMedicationsWithChooser(Window owner, PatientManager pm, ConditionManager cm) throws IOException {
        File f = chooseTxtFile(owner, "Choose medications.txt");
        if (f == null) return 0;

        int added = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (skipLine(line)) continue;
                CircularDoublyLinkedList<String> parts = tokenizeWith(line, ",");
                String mid       = parts.getSize() > 0 ? parts.get(0).trim() : null;
                String patientId = parts.getSize() > 1 ? parts.get(1).trim() : null;
                String name      = parts.getSize() > 2 ? parts.get(2).trim() : null;
                String dosage    = parts.getSize() > 3 ? parts.get(3).trim() : null;
                String freq      = parts.getSize() > 4 ? parts.get(4).trim() : null;
                if (mid == null || patientId == null || name == null) continue;

                Patient p = pm.findById(patientId);
                if (p == null) continue;
                Condition cond = (p.getConditionName() == null) ? null : cm.findByName(p.getConditionName());
                if (p.addMedicationIfAllowed(new Medication(mid, name, dosage, freq), cond)) added++;
            }
        }
        return added;
    }
}
