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
        assertEquals("field1", fd1.getFieldName().getFullName());
        assertFalse(fd1.hasName());
        assertNull(fd1.getName());
        assertEquals(-1, fd1.getFieldIndex());
        
        FieldDescription fd2 = FieldDescription.parseFieldDescriptor("field1|name").get();
        assertEquals("field1", fd2.getFieldName().getFullName());
        assertEquals("name", fd2.getName());
        assertEquals(-1, fd2.getFieldIndex());
    }
    
    @Test
    public void testWithFieldIndex() {
        FieldDescription fd2 = FieldDescription.parseFieldDescriptor("field1#2|name#9").get();
        assertEquals("field1", fd2.getFieldName().getFullName());
        assertEquals("name", fd2.getName());
        assertEquals(2, fd2.getFieldIndex());
    }
    
    @Test
    public void testLabelWithFixedContent() {
        FieldDescription fd1 = FieldDescription.parseFieldDescriptor("field=foo").get();
        assertEquals("field", fd1.getFieldName().getFullName());
        assertFalse(fd1.hasName());
        assertNull(fd1.getName());
        assertEquals(-1, fd1.getFieldIndex());
        assertEquals("foo", fd1.getFixedContent());
        assertTrue( fd1.hasFixedContent());
    }
    
    @Test
    public void testInvalidFixedLabelContent() {
        FieldDescription fd1 = FieldDescription.parseFieldDescriptor("field=").get();
        assertEquals("field", fd1.getFieldName().getFullName());
        assertFalse(fd1.hasName());
        assertNull(fd1.getName());
        assertEquals(-1, fd1.getFieldIndex());
        assertFalse( fd1.hasFixedContent());
        
        assertTrue(FieldDescription.parseFieldDescriptor("=content|foo#2").isEmpty());
    }
    
    @Test
    public void testIgnoreField() {
        assertTrue(FieldDescription.parseFieldDescriptor("-field").isEmpty());
        assertTrue(FieldDescription.parseFieldDescriptor(" -field").isEmpty());
    }
    
}
