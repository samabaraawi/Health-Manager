package ds;

public class Medication {

    private String id;
    private String name;
    private String dosage;
    private String frequency;

    public Medication(String id, String name, String dosage, String frequency) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) {                  // update frequency
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "Medication{id='" + id + "', name='" + name + "', dosage='" + dosage + "', freq='" + frequency + "'}";
    }
}

