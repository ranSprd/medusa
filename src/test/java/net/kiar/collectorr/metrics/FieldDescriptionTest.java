package net.kiar.collectorr.metrics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ranSprd
 */
public class FieldDescriptionTest {
    
    @Test
    public void testDescriptorParsingOfInvalidInput() {
        assertTrue( FieldDescription.parseFieldDescriptor(null).isEmpty());
        assertTrue( FieldDescription.parseFieldDescriptor("").isEmpty());
        assertTrue( FieldDescription.parseFieldDescriptor("|").isEmpty());
        assertTrue( FieldDescription.parseFieldDescriptor("|something").isEmpty());
    }
    
    @Test
    public void testDescriptorParsingOf() {
        FieldDescription fd1 = FieldDescription.parseFieldDescriptor("field1").get();
        assertEquals("field1", fd1.getFieldName());
        assertEquals("field1", fd1.getName());
        assertEquals(-1, fd1.getFieldIndex());
        
        FieldDescription fd2 = FieldDescription.parseFieldDescriptor("field1|name").get();
        assertEquals("field1", fd2.getFieldName());
        assertEquals("name", fd2.getName());
        assertEquals(-1, fd2.getFieldIndex());
    }
    
    @Test
    public void testWithFieldIndex() {
        FieldDescription fd2 = FieldDescription.parseFieldDescriptor("field1#2|name#9").get();
        assertEquals("field1", fd2.getFieldName());
        assertEquals("name", fd2.getName());
        assertEquals(2, fd2.getFieldIndex());
    }
    
    @Test
    public void testIgnoreField() {
        assertTrue(FieldDescription.parseFieldDescriptor("-field").isEmpty());
        assertTrue(FieldDescription.parseFieldDescriptor(" -field").isEmpty());
    }
    
}
