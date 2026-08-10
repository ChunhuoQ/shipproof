package to.shipproof.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SafeUrlServiceTest {
    private final SafeUrlService urls = new SafeUrlService();

    @Test
    void rejectsLocalAndNonHttpTargets() {
        assertThrows(IllegalArgumentException.class,
                () -> urls.requirePublicHttpUrl("http://localhost:8080/admin", "Demo URL"));
        assertThrows(IllegalArgumentException.class,
                () -> urls.requirePublicHttpUrl("file:///etc/passwd", "Demo URL"));
        assertThrows(IllegalArgumentException.class,
                () -> urls.requirePublicHttpUrl("http://127.0.0.1/", "Demo URL"));
    }

    @Test
    void acceptsPublicHttpsUrl() {
        assertEquals("https", urls.requirePublicHttpUrl("https://example.com/demo", "Demo URL").getScheme());
    }
}
