package to.shipproof.model;

import java.time.Instant;

public class AuditEntry {
    private final long sequence;
    private final Instant timestamp;
    private final String action;
    private final String subject;
    private final boolean success;
    private final String previousHash;
    private final String hash;

    public AuditEntry(long sequence, Instant timestamp, String action, String subject,
                      boolean success, String previousHash, String hash) {
        this.sequence = sequence;
        this.timestamp = timestamp;
        this.action = action;
        this.subject = subject;
        this.success = success;
        this.previousHash = previousHash;
        this.hash = hash;
    }

    public long getSequence() { return sequence; }
    public Instant getTimestamp() { return timestamp; }
    public String getAction() { return action; }
    public String getSubject() { return subject; }
    public boolean isSuccess() { return success; }
    public String getPreviousHash() { return previousHash; }
    public String getHash() { return hash; }
}
