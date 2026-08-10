package to.shipproof.service;

import to.shipproof.model.AuditEntry;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AuditService {
    private final List<AuditEntry> entries = new ArrayList<AuditEntry>();
    private String lastHash = repeat("0", 64);

    public synchronized AuditEntry record(String action, String subject, boolean success) {
        long sequence = entries.size() + 1L;
        Instant now = Instant.now();
        String safeSubject = subject == null ? "" : subject;
        String payload = sequence + "|" + now + "|" + action + "|" + safeSubject + "|" + success + "|" + lastHash;
        String hash = sha256(payload);
        AuditEntry entry = new AuditEntry(sequence, now, action, safeSubject, success, lastHash, hash);
        entries.add(entry);
        lastHash = hash;
        return entry;
    }

    public synchronized List<AuditEntry> list() {
        List<AuditEntry> copy = new ArrayList<AuditEntry>(entries);
        Collections.reverse(copy);
        return copy;
    }

    public synchronized boolean verify() {
        String previous = repeat("0", 64);
        for (AuditEntry entry : entries) {
            String payload = entry.getSequence() + "|" + entry.getTimestamp() + "|" + entry.getAction()
                    + "|" + entry.getSubject() + "|" + entry.isSuccess() + "|" + previous;
            if (!previous.equals(entry.getPreviousHash()) || !sha256(payload).equals(entry.getHash())) {
                return false;
            }
            previous = entry.getHash();
        }
        return true;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : digest) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String repeat(String value, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) result.append(value);
        return result.toString();
    }
}
