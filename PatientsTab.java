package ds;


public class PatientManager {

    private CircularDoublyLinkedList<Patient> patients;

    public PatientManager() {
        patients = new CircularDoublyLinkedList<>();
    }


    public CircularDoublyLinkedList<Patient> getPatients() {
        return patients;
    }



    public boolean existsById(String id) {
        int n = patients.getSize();
        for (int i = 0; i < n; i++) {
            Patient cur = patients.get(i);
            if (cur != null && cur.getId() != null && cur.getId().equals(id)) return true;
        }
        return false;
    }


    public boolean addPatientUnique(Patient p) {
        if (p == null || p.getId() == null) return false;
        if (existsById(p.getId())) return false;
        patients.addLast(p);
        return true;
    }


    public Patient findById(String id) {
        int n = patients.getSize();
        for (int i = 0; i < n; i++) {
            Patient cur = patients.get(i);
            if (cur != null) {
                String pid = cur.getId();
                if (pid != null && pid.equals(id)) return cur;
            }
        }
        return null;
    }


    public boolean updatePatient(String id, String newName, Integer newAge, String newConditionName) {
        Patient p = findById(id);
        if (p == null) return false;
        if (newName != null) p.setName(newName);
        if (newAge != null)  p.setAge(newAge);
        if (newConditionName != null) p.setConditionName(newConditionName);
        return true;
    }


    public boolean deletePatientById(String id) {
        int n = patients.getSize();
        for (int i = 0; i < n; i++) {
            Patient cur = patients.get(i);
            if (cur != null) {
                String pid = cur.getId();
                if (pid != null && pid.equals(id)) {

                    cur.clearAllRecords();
                    return patients.removeAt(i);
                }
            }
        }
        return false;
    }


    public int count() {
        return patients.getSize();
    }
}
