package to.shipproof.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditServiceTest {
    @Test
    void createsVerifiableLinkedProofs() {
        AuditService service = new AuditService();
        service.record("network.snapshot", "", true);
        service.record("wallet.balance", "f01234", true);
        assertEquals(2, service.list().size());
        assertTrue(service.verify());
        assertEquals(service.list().get(1).getHash(), service.list().get(0).getPreviousHash());
    }
}
