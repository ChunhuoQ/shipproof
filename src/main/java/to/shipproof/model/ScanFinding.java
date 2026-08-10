package to.shipproof.model;

public class ScanFinding {
    private final String id;
    private final String severity;
    private final String title;
    private final String evidence;
    private final String remediation;
    private final int points;
    private final int maxPoints;
    private final boolean passed;

    public ScanFinding(String id, String severity, String title, String evidence,
                       String remediation, int points, int maxPoints, boolean passed) {
        this.id = id;
        this.severity = severity;
        this.title = title;
        this.evidence = evidence;
        this.remediation = remediation;
        this.points = points;
        this.maxPoints = maxPoints;
        this.passed = passed;
    }

    public String getId() { return id; }
    public String getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getEvidence() { return evidence; }
    public String getRemediation() { return remediation; }
    public int getPoints() { return points; }
    public int getMaxPoints() { return maxPoints; }
    public boolean isPassed() { return passed; }
}
