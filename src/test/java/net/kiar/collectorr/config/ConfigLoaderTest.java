package net.kiar.collectorr.config;

import java.io.FileNotFoundException;
import java.util.List;
import net.kiar.collectorr.config.model.TopicConfig;
import net.kiar.collectorr.config.model.TopicConfigMappings;
import net.kiar.collectorr.config.model.TopicConfigMetric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author ranSprd
 */
public class ConfigLoaderTest {

    @Test
    public void testLoading() throws FileNotFoundException {

        ConfigLoader conf = ConfigLoader.readFromFile("src/test/resources/example-config01.yaml");
        
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
    
    
    public void testReading() {
        ConfigLoader conf = ConfigLoader.readContent("""
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
                .filter(m -> "abc".equals( m.getValueField()))
                .findAny()
                .get();
        assertNotNull(d1);
        assertNotNull(d1.getLabels());
        assertEquals(3, d1.getLabels().size());
    }
    
    @Test
    public void testLabelMappingReading() {
        ConfigLoader conf = ConfigLoader.readContent("""
topics:
- topic: topic/to/test
  mappings:
    - label:
      source: sensorId
      target: sensorName
      map:
        284B5096F0013CBA: Sensor1
"""
        );
        
        assertNotNull(conf);
        TopicConfig item1 = conf.getTopicsToObserve().get(0);
        assertNotNull(item1);
        List<TopicConfigMappings> mappings = item1.getMappings();
        assertNotNull(mappings);
        assertEquals(1, mappings.size());
        TopicConfigMappings mapping = mappings.get(0);
        assertEquals("sensorId", mapping.getSource());
        assertEquals("sensorName", mapping.getTarget());
        assertEquals(1, mapping.getMap().size());
        
    }

}
