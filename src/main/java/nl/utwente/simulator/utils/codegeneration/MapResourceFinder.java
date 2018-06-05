package nl.utwente.simulator.utils.codegeneration;

import org.codehaus.janino.util.resource.Resource;
import org.codehaus.janino.util.resource.ResourceFinder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Extends Janino's resource finder to find in-memory java classes
 * enabling us to generate classes without files
 */
public class MapResourceFinder extends ResourceFinder {
    private final Map map;
    private long lastModified = 0L;

    public MapResourceFinder(Map map) {
        this.map = map;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public final Resource findResource(final String resourceName) {
        int p = resourceName.indexOf(".java");
        final String s = resourceName.substring(0, p);
        final byte[] ba = (byte[]) this.map.get(s);
        if (ba == null) return null;

        return new Resource() {
            public InputStream open() throws IOException {
                return new ByteArrayInputStream(ba);
            }

            public String getFileName() {
                return s;
            }

            public long lastModified() {
                return MapResourceFinder.this.lastModified;
            }
        };
    }
}