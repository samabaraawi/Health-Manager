package ds;

public class Treatment {

    private String id;
    private String type;
    private String startDate;
    private String endDate;

    public Treatment(String id, String type, String startDate, String endDate) {
        this.id = id;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "Treatment{id='" + id + "', type='" + type + "', start='" + startDate + "', end='" + endDate + "'}";
    }
}
