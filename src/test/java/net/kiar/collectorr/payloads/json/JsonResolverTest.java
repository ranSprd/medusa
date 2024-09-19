package net.kiar.collectorr.payloads.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.kiar.collectorr.metrics.FieldDescription;
import net.kiar.collectorr.payloads.PayloadDataNode;
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

        JsonResolver instance = JsonResolver.consume("{ \"freeheap\" : 47360, \"cpuSpeed\" : 80}").get();
        assertNotNull(instance);
        assertNotNull(instance.getLabelNamesAsString());
        assertNotNull(instance.getLabelNodes());
        assertTrue(instance.getLabelNodes().isEmpty());

        String names = instance.getValueNamesAsString();
        assertNotNull(names);
        assertFalse(names.isBlank());
        assertNotNull(instance.getValueNodes());
        assertEquals(2, instance.getValueNodes().size());
    }

    @Test
    public void testFileLoading() throws IOException {

        JsonResolver instance = JsonResolver.consume(
                Files.readString(Path.of("src/test/resources/mqtt/payloads/shellyPlus.json"))).get();
        assertNotNull(instance);
    }

    @Test
    public void testReadEmbeddedObjects() throws IOException {

        JsonResolver instance = JsonResolver.consume(
                Files.readString(Path.of("src/test/resources/mqtt/payloads/shellyMotion-status01.json"))).get();
        assertNotNull(instance);
        assertEquals(4, instance.getValueNodes().size());
        assertEquals(1, instance.findNodes(new FieldDescription("tmp.value")).size());
        assertTrue(instance.findNode("tmp.units").isPresent());
    }

    @Test
    public void testPlainNumberInput() throws IOException {

        Optional<JsonResolver> instance42 = JsonResolver.consume("42.0");
        assertNotNull(instance42);
        assertTrue(instance42.isPresent());
        assertEquals("42.0", instance42.get().getValueNodes().getFirst().value());

//        Optional<JsonResolver> instanceBlank = JsonResolver.consume("   ");
//        assertNotNull(instanceBlank);
//        assertTrue(instanceBlank.isEmpty());
    }
    
    @Test
    public void testInvalidInput() throws IOException {

        Optional<JsonResolver> instanceNull = JsonResolver.consume(null);
        assertNotNull(instanceNull);
        assertTrue(instanceNull.isEmpty());

        Optional<JsonResolver> instanceBlank = JsonResolver.consume("   ");
        assertNotNull(instanceBlank);
        assertTrue(instanceBlank.isEmpty());

        Optional<JsonResolver> instanceInvalidJson = JsonResolver.consume(" { 'foo'  ");
        assertNotNull(instanceInvalidJson);
        assertTrue(instanceInvalidJson.isEmpty());
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

    @Test
    public void testFindNodes() {
        JsonResolver resolver = JsonResolver.consume("{ \"foo\": [{ \"item\" : \"1\"}, { \"item\" : \"2\", \"other\" : \"2\"}], \"item\" : \"3\" }").get();
        List<PayloadDataNode> result = resolver.findNodes(new FieldDescription("foo.*.item"));

        assertEquals(2, result.size());
    }
}
