package ds;


public class ConditionManager {

    private CircularDoublyLinkedList<Condition> conditions;

    public ConditionManager() {
        conditions = new CircularDoublyLinkedList<>();
    }


    public CircularDoublyLinkedList<Condition> getConditions() {
        return conditions;
    }


    public void addCondition(Condition c) {
        conditions.addLast(c);
    }

    public boolean existsByName(String name) {
        return findByName(name) != null;
    }

    public Condition findByName(String name) {
        int n = conditions.getSize();
        for (int i = 0; i < n; i++) {
            Condition c = conditions.get(i);
            if (c != null) {
                String nm = c.getName();
                if (nm != null && nm.equals(name)) return c;
            }
        }
        return null;
    }


    public boolean deleteByName(String name) {
        int n = conditions.getSize();
        for (int i = 0; i < n; i++) {
            Condition c = conditions.get(i);
            if (c != null) {
                String nm = c.getName();
                if (nm != null && nm.equals(name)) {
                    return conditions.removeAt(i);
                }
            }
        }
        return false;
    }

    public boolean addRecommendedTreatment(String conditionName, String treatmentType) {
        Condition c = findByName(conditionName);
        if (c == null) return false;
        c.addRecommendedTreatment(treatmentType);
        return true;
    }


    public boolean addRecommendedMedication(String conditionName, String medName) {
        Condition c = findByName(conditionName);
        if (c == null) return false;
        c.addRecommendedMedication(medName);
        return true;
    }


    public boolean removeRecommendedTreatment(String conditionName, String treatmentType) {
        Condition c = findByName(conditionName);
        if (c == null) return false;
        CircularDoublyLinkedList<String> treats = c.getRecTreats();
        int sz = treats.getSize();
        for (int i = 0; i < sz; i++) {
            String t = treats.get(i);
            boolean match = (t == null && treatmentType == null) ||
                    (t != null && t.equals(treatmentType));
            if (match) return treats.removeAt(i);
        }
        return false;
    }

    public boolean removeRecommendedMedication(String conditionName, String medName) {
        Condition c = findByName(conditionName);
        if (c == null) return false;
        CircularDoublyLinkedList<String> meds = c.getRecMeds();
        int sz = meds.getSize();
        for (int i = 0; i < sz; i++) {
            String m = meds.get(i);
            boolean match = (m == null && medName == null) ||
                    (m != null && m.equals(medName));
            if (match) return meds.removeAt(i);
        }
        return false;
    }
    public int count() {
        return conditions.getSize();
    }
}
