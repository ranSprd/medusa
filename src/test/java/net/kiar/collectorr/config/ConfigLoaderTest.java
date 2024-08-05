package net.kiar.collectorr.config;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import net.kiar.collectorr.config.model.FieldValueMap;
import net.kiar.collectorr.config.model.RootFieldMap;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.config.model.TopicConfigMetric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class ConfigLoaderTest {

    @Test
    public void testLoading() throws FileNotFoundException {

        MappingsConfigLoader conf = MappingsConfigLoader.readFromFile("testcase", "src/test/resources/example-config01.yaml");
        
        assertNotNull(conf);
        
        System.out.println("topics " +conf.getNumberOfTopics());
        assertEquals(12, conf.getNumberOfTopics(), 0);
        
        TopicConfig topicToTest = conf.findTopic("topic/to/test").get();
//        InputStream input = new FileInputStream(new File(
//                "src/test/resources/example-config01.yaml"));
//        Yaml yaml = new Yaml();
//        int counter = 0;
//        for (Object data : yaml.loadAll(input)) {
//            System.out.println(data);
//            counter++;
//        }
    }
    
    @Test
    public void testInvalidInput() {
        checkInput(null);
        checkInput("");
        checkInput("   ");
        checkInput("   topics: ");
        checkInput("   topics: blank"); // prints a warning but catched correctly
    }

    private void checkInput(String input) {
        MappingsConfigLoader r2 = MappingsConfigLoader.readContent(input);
        assertNotNull(r2);
        assertEquals(0, r2.getNumberOfTopics());
    }
    
    @Test
    public void testReading() {
        MappingsConfigLoader conf = MappingsConfigLoader.readContent("""
topics:
- topic: topic/to/test
  pattern: 
    path : /home/{ort}/{device}/
    name: buderus
  metrics:
  - valueField: abc
    labels: [ort, device, field1]
    type: gauge 
  - name:
    valueField: x
    type: gauge

        """
        );
        
        assertNotNull(conf);
        TopicConfig topicToTest = conf.findTopic("topic/to/test").get();
        
        List<TopicConfigMetric> metrics = topicToTest.getMetrics();
        assertNotNull(metrics);
        assertEquals(2, metrics.size());
        
        TopicConfigMetric d1 = metrics.stream()
                .filter(m -> "abc".equals( m.getValueFieldName()))
                .findAny()
                .get();
        assertNotNull(d1);
        assertNotNull(d1.getLabels());
        assertEquals(3, d1.getLabels().size());
    }
    
    @Test
    public void testLabelMappingReading() {
        MappingsConfigLoader conf = MappingsConfigLoader.readContent("""
topics:
- topic: topic/to/test
  mappings:
    sensorId:
      281C5A96F0013CAC:
        sensorName: Abgasrohr
        namePrefix: heating
"""
        );
        
        assertNotNull(conf);
        TopicConfig item1 = conf.getTopicsToObserve().get(0);
        assertNotNull(item1);
        RootFieldMap mappings = item1.getValueMappings();
        assertNotNull(mappings);
        assertEquals(1, mappings.size());
        FieldValueMap fieldMapping = mappings.get("sensorId");
        assertEquals(1, fieldMapping.size());
        
        Map<String, String> pairs = fieldMapping.values().stream().findAny().get();
        assertEquals(2, pairs.size());
        assertEquals("Abgasrohr", pairs.get("sensorName"));
        assertEquals("heating", pairs.get("namePrefix"));
        assertNull(pairs.get("sensorId"));
        
    }
    
    
    @Test
    public void testValueMapping() {
        MappingsConfigLoader conf = MappingsConfigLoader.readContent("""
topics:
- topic: topic/to/test
  valueMappings:
    sensorId:
      281C5A96F0013CAC:
        sensorName: Abgasrohr
        namePrefix: heating

"""
        );
        
        assertNotNull(conf);
        TopicConfig topicConfig1 = conf.getTopicsToObserve().get(0);
        assertNotNull(topicConfig1);
        RootFieldMap mappings = topicConfig1.getValueMappings();
        assertNotNull(mappings);
        assertEquals(1, mappings.size());
        
        
        
    }

}
