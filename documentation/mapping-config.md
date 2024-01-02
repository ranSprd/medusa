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


### labels

Normally all string fields from a payload are interpreted as *label*.

#### fixed content 

    topics:
    - topic: foo/bar/blub
      metrics:
      - valueField: value
        labels: [label-1=fixed, label-2, label-3=overwritten]


    topics:
    - topic: foo/bar/blub
      labels: [-label1, label2]
      metrics:
      - valueField: value
        labels: [sourceType, type]
        name: "{phase}_voltage"


