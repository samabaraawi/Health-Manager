package ds;

public class Symptom {

    private String description;
    private String dateRecorded;


    public Symptom(String description, String dateRecorded) {
        this.description = description;
        this.dateRecorded = dateRecorded;
    }

    public String getDescription() {
        return description; }

    public void setDescription(String description) {
        this.description = description; }


    public String getDateRecorded() {
        return dateRecorded; }

    public void setDateRecorded(String dateRecorded) {
        this.dateRecorded = dateRecorded; }



    @Override
    public String toString() {
        return "Symptom{desc='" + description + "', date='" + dateRecorded + "'}";
    }
}
