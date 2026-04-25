package ds;

public class Patient {

    private String id;
    private String name;
    private int age;
    private String conditionName;

    private CircularDoublyLinkedList<Treatment> treatments; // list of treatments for this patient
    private CircularDoublyLinkedList<Medication> meds;      // list of medications for this patient
    private CircularDoublyLinkedList<Symptom> symptoms;     // NEW: list of symptoms for this patient


    public Patient(String id, String name, int age, String conditionName) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.conditionName = conditionName;
        this.treatments = new CircularDoublyLinkedList<>(); //create empty treatments list
        this.meds = new CircularDoublyLinkedList<>();       //create empty medications list
        this.symptoms = new CircularDoublyLinkedList<>();   //create empty symptoms list
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) {
        this.age = age;
    }

    public String getConditionName() { return conditionName; }
    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public CircularDoublyLinkedList<Treatment> getTreatments() { //returns treatments CDLL
        return treatments;
    }

    public CircularDoublyLinkedList<Medication> getMeds() {      // returns medications CDLL
        return meds;
    }


    public CircularDoublyLinkedList<Symptom> getSymptoms() {     //returns symptoms CDLL
        return symptoms;
    }

    public void addTreatment(Treatment t) {
        treatments.addLast(t);
    }


    public void addMedication(Medication m) {
        meds.addLast(m);
    }


    public void addSymptom(Symptom s) {
        symptoms.addLast(s);
    }


    public boolean removeSymptomAt(int index) {
        if (index < 0 || index >= symptoms.getSize()) return false;
        return symptoms.removeAt(index);
    }

    public boolean addTreatmentIfAllowed(Treatment t, Condition cond) { // tries to add only if allowed
        if (cond == null) {                                   // if no condition info was provided
            return false;                                     // reject because we cannot verify
        }
        // check that patient's textual condition matches the provided condition object name
        if (this.conditionName == null) {                     // if patient has no condition text set
            return false;                                     // cannot validate, so reject
        }
        if (!this.conditionName.equals(cond.getName())) {     // if names mismatch (e.g., patient "Asthma" vs cond "Diabetes")
            return false;                                     // reject to keep data consistent
        }
        // ensure the treatment type is recommended by the condition
        String type = t == null ? null : t.getType();         // safely read treatment type (could be null)
        if (type == null) {                                   // if no type specified
            return false;                                     // reject because we cannot verify
        }
        if (!cond.isTreatmentAllowed(type)) {                 // if not recommended by the condition
            return false;                                     // reject add
        }
        treatments.addLast(t);                                // otherwise append to the list
        return true;                                          // report success
    }

    public boolean addMedicationIfAllowed(Medication m, Condition cond) { // tries to add only if allowed
        if (cond == null) {                                   // no condition info provided
            return false;                                     // reject due to missing validation context
        }
        if (this.conditionName == null) {                     // patient has no condition text set
            return false;                                     // cannot validate, so reject
        }
        if (!this.conditionName.equals(cond.getName())) {     // mismatch between patient's condition text and provided condition object
            return false;                                     // reject to keep data consistent
        }
        String medName = m == null ? null : m.getName();      // safely read medication name
        if (medName == null) {                                // no name provided
            return false;                                     // reject because we cannot verify
        }
        if (!cond.isMedicationAllowed(medName)) {             // not recommended by this condition
            return false;                                     // reject add
        }
        meds.addLast(m);                                      // append to the medications list
        return true;                                          // report success
    }

    public boolean removeTreatmentById(String treatmentId) {   // removes first treatment matching given id
        if (treatments.isEmpty()) {                            // if list is empty
            return false;                                      // nothing to remove
        }
        int n = treatments.getSize();                          // how many nodes to scan
        for (int i = 0; i < n; i++) {                          // iterate from index 0 to n-1
            Treatment t = treatments.get(i);                   // get treatment at position i
            if (t != null && t.getId().equals(treatmentId)) {  // check for id equality
                treatments.removeAt(i);                        // remove at that index
                return true;                                   // success
            }
        }
        return false;                                          // not found
    }

    public boolean removeMedicationById(String medId) {        // removes first medication matching given id
        if (meds.isEmpty()) {                                  // if meds list is empty
            return false;                                      // nothing to remove
        }
        int n = meds.getSize();                                // how many nodes to scan
        for (int i = 0; i < n; i++) {                          // iterate sequentially
            Medication m = meds.get(i);                        // get medication at position i
            if (m != null && m.getId().equals(medId)) {        // id match?
                meds.removeAt(i);                               // remove at index i
                return true;                                    // success
            }
        }
        return false;
    }




    public void clearAllTreatments() {
        treatments.clear();
    }


    public void clearAllMedications() {
        meds.clear();
    }


    public void clearAllSymptoms() {
        symptoms.clear();
    }


    public void clearAllRecords() {
        treatments.clear();
        meds.clear();
        symptoms.clear();
    }


    @Override
    public String toString() {
        return "Patient{id='" + id + "', name='" + name + "', age=" + age +
                ", condition='" + conditionName + "'}";
    }
}
