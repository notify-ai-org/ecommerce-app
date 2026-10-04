package com.notify.ecommerce.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Serves the React storefront from the Spring Boot classpath, mirroring the
 * access service's {@code PortalController}.
 *
 * <h3>How routing works</h3>
 *
 * <pre>
 * Browser: GET /portals/shop/products/P-1001
 *           │
 *           ├─ /portals/shop/assets/* → hashed Vite assets, cached for a year
 *           │
 *           └─ anything else under /portals/shop/ → index.html with
 *               Cache-Control: no-store; React Router handles the path.
 * </pre>
 *
 * <h3>Adding a new portal</h3>
 * Add the name to {@link #KNOWN_PORTALS} and build its assets into
 * {@code src/main/resources/static/portals/<name>/}. The storefront itself is
 * built from {@code ui/} with {@code npm run build} (or {@code mvn -Pui}).
 */
@RestController
public class PortalController {

    static final String DEFAULT_PORTAL = "shop";

    /** Requests for portal names not in this set return HTTP 404. */
    static final Set<String> KNOWN_PORTALS = Set.of(DEFAULT_PORTAL);

    @GetMapping("/portals/{portal}/assets/{filename}")
    public ResponseEntity<Resource> servePortalAsset(@PathVariable String portal, @PathVariable String filename) {
        if (!KNOWN_PORTALS.contains(portal) || filename.contains("..")) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new ClassPathResource("static/portals/" + portal + "/assets/" + filename);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).immutable())
                .body(resource);
    }

    /** Normalize portal roots to a trailing slash, e.g. {@code /portals/shop/}. */
    @GetMapping(value = "/portals/{portal}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Void> portalRootRedirect(@PathVariable String portal, HttpServletRequest request) {
        if (!KNOWN_PORTALS.contains(portal)) {
            return ResponseEntity.notFound().build();
        }
        return redirect(request, "/portals/" + portal + "/");
    }

    /**
     * Catch-all handler: serves {@code index.html} for any HTML route inside a
     * known portal, allowing React Router to handle client-side navigation.
     */
    @GetMapping(value = "/portals/{portal}/**", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> servePortal(@PathVariable String portal) {
        if (!KNOWN_PORTALS.contains(portal)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new ClassPathResource("static/portals/" + portal + "/index.html");
        if (!resource.exists()) {
            // Portal is registered but the UI has not been built yet
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                // Never cache index.html so code-split chunk names stay current.
                .cacheControl(CacheControl.noStore())
                .body(resource);
    }

    /** Default entry points: {@code /} and {@code /portals} open the storefront. */
    @GetMapping(value = { "/", "/portals", "/portals/" })
    public ResponseEntity<Void> defaultPortal(HttpServletRequest request) {
        return redirect(request, "/portals/" + DEFAULT_PORTAL + "/");
    }

    /** Redirects within the app, keeping the context path (e.g. {@code /ecommerce}) it is deployed under. */
    private static ResponseEntity<Void> redirect(HttpServletRequest request, String location) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(request.getContextPath() + location))
                .build();
    }
}
