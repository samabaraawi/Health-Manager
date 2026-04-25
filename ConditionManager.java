package ds;

public class Condition {

    private String name;
    private CircularDoublyLinkedList<String> recTreats;    //recommended treatment types as plain strings
    private CircularDoublyLinkedList<String> recMeds;      //recommended medication names as plain strings

    public Condition(String name) {
        this.name = name;
        this.recTreats = new CircularDoublyLinkedList<>(); // create an empty list for recommended treatments
        this.recMeds = new CircularDoublyLinkedList<>();   //create an empty list for recommended medications
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CircularDoublyLinkedList<String> getRecTreats() {   //exposes the recommended treatments list
        return recTreats;                                      //return reference to the CDLL
    }

    public CircularDoublyLinkedList<String> getRecMeds() {   //exposes the recommended medications list
        return recMeds;                                      //return reference to the CDLL
    }

    public void addRecommendedTreatment(String type) {       // adds a recommended treatment type
        recTreats.addLast(type);                             // append the type to the end of the list
    }

    public void addRecommendedMedication(String medName) {   // adds a recommended medication name
        recMeds.addLast(medName);                            // append the name to the end of the list
    }

    // validation
    public boolean isTreatmentAllowed(String type) {         // checks if a treatment type is recommended
        return recTreats.contains(type);                     // reuse CDLL.contains to perform a  scan
    }

    public boolean isMedicationAllowed(String medName) {     // checks if a medication name is recommended
        return recMeds.contains(medName);                    // reuse CDLL.contains to perform a  scan
    }

    @Override
    public String toString() {
        return "Condition{name='" + name + "'}";
    }
}
