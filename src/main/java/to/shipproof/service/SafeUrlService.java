package to.shipproof.service;

import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.util.Locale;

@Service
public class SafeUrlService {
    public URI requirePublicHttpUrl(String raw, String label) {
        try {
            URI uri = new URI(raw == null ? "" : raw.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException(label + " must use http or https");
            }
            if (host == null || host.trim().isEmpty()) {
                throw new IllegalArgumentException(label + " must contain a valid hostname");
            }
            String normalized = host.toLowerCase(Locale.ROOT);
            if ("localhost".equals(normalized) || normalized.endsWith(".localhost") || normalized.endsWith(".local")) {
                throw new IllegalArgumentException(label + " cannot target localhost or a private network");
            }
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new IllegalArgumentException(label + " cannot target localhost or a private network");
                }
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(label + " is not a valid public URL");
        }
    }
}
