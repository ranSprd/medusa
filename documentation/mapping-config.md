# Mapping configuration

## Simple structure

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

Normally all _**string**_ fields from a payload are interpreted as *label* which is 
added to prometheus metric entry.

#### fixed content 

    topics:
    - topic: foo/bar/blub
      metrics:
      - valueField: value
        labels: [label-1=fixed, label-2, label-3=overwritten]

The above definition produces at least 2 labels. The label _**label-1**_ always has the content _fixed_
while the _**label-3**_ always has the content _overwritten_. If _**label-2**_ has a value in the payload
a 3rd label with this value is added.

An output could look like this:

    # HELP foo_bar_blub_value 
    # TYPE foo_bar_blub_value gauge
    foo_bar_blub_value[label-1="fixed",label-3="overwritten"] 122.0 1722921836039

Note: In that configuration no metric is set. By default the name of the metric is generated
from the value field name an the topic. 

The following configuration shows how to exclude a label. 

    topics:
    - topic: foo/bar/blub
      labels: [-label1, label2]
      metrics:
      - valueField: value
        labels: [sourceType, type]
        name: "{phase}_voltage"


## Payload processing

### Arrays

Some payload can contain arrays, for instance

    {
            "common_list":  [
                {
                    "id":   "0x02",
                    "val":  "21.5",
                    "unit": "C"
                }, {
                    "id":   "0x07",
                    "val":  "70%"
                }, {
                    "id":   "3",
                    "val":  "21.5",
                }, {
                    "id":   "0x0B",
                    "val":  "0.0 m/s"
                }]
    }

Additionally all fields in this sample payload are json strings. For this reason, 
no value fields are automatically detected. But the fields are assigned to the 
following keys and available as *labels* when read.

    common_list.#0.id
    common_list.#0.val
    common_list.#0.unit

    common_list.#1.id
    common_list.#1.val
    
    common_list.#2.id
    ...

If a metric is to be created from these fields, the names must be specified as
**valueField**. There are two ways to do this.

#### Access via field index

    metrics:
    - valueField: "common_list.#1.val"

This access is unique and references exactly one value, i.e. a single metric is generated.

#### Access with Wildcard

    metrics:
    - valueField: "common_list.*.val"

This access references all items of the array. It generates multiples metrics. 


## metric types

Medusa supports 2 prometheus metric types *gauge* and *counter*. Whereby *gauge* is the 
default type and is used if no setting is present.
The setting is available under *topics.topic.metrics.metric.type*

    topics:
    - topic: foo/bar/blub
      metrics:
      - name: non_gauge_metric
        type: counter

### Counter

A counter is a cumulative metric that represents a single monotonically increasing counter whose value can only increase or be reset to zero on restart.

### Gauge

A gauge is a metric that represents a single numerical value that can arbitrarily go up and down.
