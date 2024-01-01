# Mapping configuration

## Simple structure

    generic:
      name: "collectorr"
    
    topics:   
    - topic: tele/tasmota/meter/SENSOR
    - topic: foo/bar/blub

Each file starts with a generic header. Currently it contains only the global name of the connector.
This name is used for identifiy this mapping in log files.

The next section is the **topic** part. It contains a list of all observed mqtt topics. 

### TOPIC Entries

#### minimal topic to metric configuration

    topics:
    - topic: foo/bar/blub
      labels: [-label1, label2]
      metrics:
      - valueField: value
        labels: [sourceType, type]
        name: "{phase}_voltage"


