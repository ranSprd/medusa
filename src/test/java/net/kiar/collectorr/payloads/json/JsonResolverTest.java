package net.kiar.collectorr.payloads.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.kiar.collectorr.metrics.FieldDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class JsonResolverTest {
    
    @Test
    public void testConsume() {
        
        JsonResolver instance = JsonResolver.consume("{ \"freeheap\" : 47360, \"cpuSpeed\" : 80}");
        assertNotNull(instance);
        assertNotNull( instance.getLabelNamesAsString());
        assertNotNull(instance.getLabelNodes());
        assertTrue(instance.getLabelNodes().isEmpty());
        
        String names = instance.getValueNamesAsString();
        assertNotNull( names);
        assertFalse(names.isBlank());
        assertNotNull(instance.getValueNodes());
        assertEquals(2, instance.getValueNodes().size());
    }
    
    @Test
    public void testFileLoading() throws IOException {
        
        JsonResolver instance = JsonResolver.consume(
                Files.readString( Path.of("src/test/resources/mqtt/payloads/shellyPlus.json")));
        assertNotNull(instance);
    }
    
    @Test
    public void testReadEmbeddedObjects() throws IOException {
        
        JsonResolver instance = JsonResolver.consume(
                Files.readString( Path.of("src/test/resources/mqtt/payloads/shellyMotion-status01.json")));
        assertNotNull(instance);
        assertEquals(4, instance.getValueNodes().size());
        assertTrue(instance.findValueNode( new FieldDescription("tmp.value")).isPresent());
        assertTrue(instance.findNode("tmp.units").isPresent());
    }
    
    
    @Test
    public void testInvalidInput() throws IOException {
        
        JsonResolver instanceNull = JsonResolver.consume(null);
        assertNotNull(instanceNull);
        
        JsonResolver instanceBlank = JsonResolver.consume("   ");
        assertNotNull(instanceBlank);
        
        JsonResolver instanceInvalidJson = JsonResolver.consume(" { 'foo'  ");
        assertNotNull(instanceInvalidJson);
    }
    
    @Test
    public void testValueArray() {
//        JsonResolver instance = JsonResolver.consume("""
//  { "id": "0x19", "val": "5.6 m/s" }
//                                                         """);
        assertEquals("", JsonResolver.extractPart("", 0));
        assertEquals("", JsonResolver.extractPart("       ", 0));
        assertEquals("", JsonResolver.extractPart(null, 0));
        
        assertEquals("5.6", JsonResolver.extractPart("5.6 m/s", 0));
        assertEquals("m/s", JsonResolver.extractPart("5.6 m/s", 1));
        assertEquals("", JsonResolver.extractPart("5.6 m/s", 2));
        assertEquals("", JsonResolver.extractPart("5.6 m/s", -2));
    }
    
}
