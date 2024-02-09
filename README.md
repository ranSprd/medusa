# Medusa

Medusa is a mapper for prometheus metrics. Currently it can handle mqtt
messages and convert they in a prometheus compatible metric format.

## Features

- transform MQTT message into Prometheus metrics
- automatic mode detects metrics in messages without manual configuration

## How it works

After startup the application connects to the configured MQTT broker in your system 
and subscripes to all available topics. In the background it collects all incoming
messages. For the configured topics it tries to extract metrics, all other metrics 
will be unprocessed and collected as 'unknow' topics.

The web-server part of this application provides a prometheus endpoint. You can 
configure your running prometheus to read all metrics from there.  

### endpoints

The default port of the appilcation is 8085. After startup the following resources
are available:

- prometheus endpoint with collected metrics http://localhost:8085/metrics
- a list of received but not processed metrics http://localhost:8085/topics/unprocessed
  for each entry you see the received count (means, how often a message came in) and a
  possible list of values and labels. The values are important, because these are good
  candidates for metric values.
- Quarkus application start page http://localhost:8085/
- Quarkus application devloper page 
- Swagger UI http://localhost:8085/q/swagger-ui/
- Health endpoint http://localhost:8085/q/health
- Service metrics http://localhost:8085/q/metrics/prometheus

### How to start

Medusa is shipped with a docker configuration. The easiest way is to build and 
start with docker. Build the image with:

    docker build -f src/main/docker/Dockerfile.jvm -t quarkus/medusa-jvm .

Then run the container using:

    docker run -i --rm -p 8085:8085 quarkus/medusa-jvm

#### Quick start configuration

Medusa can handle several data sources and transform the received data into metric compatible
formats. To define a new mqtt data sources, add a new connector entry in section **mqtt-brokers**
of 'config/quickstart-connectors.yaml' file

        mqtt-brokers:
        - name: central
          url: tcp://192.168.1.2:1883
          mapping-file: config/quickstart-topic-mappings.yaml

This creates a new connector named *central* connected to a local mqtt broker. 
The *central* connector subsripes to all mqtt topics but, as now, no metrics are read from the
messages. You can find all detected/received topics under the *unprocessed endpoint* (see above).


**Hint**
The path and name of your connector file (_here config/quickstart-connectors.yaml_) is defined in 
application config file called 'application.yaml'. This file is expected in your 'config' folder



## MQTT configuration


Metrics are only collected if the topic is defined in mapping file. This file is
defined as *mapping-file* in *config/connectors.yaml*

Here is a short example of a mapping for a topic. It contains placeholder and therefor it 
can handle several topics.

    # /home/kitchen/ESP8266-1076281
    - topic: /home/{place}/ESP8266-{deviceId}
      metrics:
      - valueField: freeheap
        labels: [place, deviceId]
        name: devices_free_heap

In short all topics of the following format are catched */home/+/ESP-8266-+*
If the payload contains a field *feeheap* with a valid numeric value, a metric is 
generated. It can be found as *devices_free_heap* (name) and can be picked up by prometheus under 
the default metrics endpoint. Furthermore the metric is enriched with the labels *place* and *deviceId*. 
Both fields are part of the topic path.

### more detailed configuration examples 

- [Example Configuration for Victron Cerbo GX](documentation/victron_cerbo.md)
- [Example Configuration for Zehnder ComfoAir Q350/450/600](documentation/zehnder_comfoair.md)


### use placeholders in metric names

For some cases it is useful to use placeholders in your configurations. For instance if you have many topics like

    N/c0678ab49344/system/+/Ac/L1
    N/c0678ab49344/system/+/Ac/L2
    N/c0678ab49344/system/+/Ac/L3

Instead of define 3 mappings you can use a placeholder in your metric name

    - topic: N/c0678ab49344/system/+/Ac/{phase}
      metrics:
      - valueField: value
        name: "{phase}_data"

In this case we use a reference to to label *phase* which comes from our topic. 
Another option is to use the index of the topic segment. For instance '#5' for our case.

    - topic: N/c0678ab49344/system/+/Ac/{phase}
      metrics:
      - valueField: value
        name: "{#5}_data"

This will produce the same metrics.

#### build-in placeholders

The following placeholders are available out of the box (camel case is important)

 - **valueFieldName** : contains the fieldname of the value field (all . are replaced by _)
 - **topicName** : the full topic of the incoming data (all / are replaced by _)

### partial data 

Some payloads can contain mixed data in a single field. For instance, my weather station from ecowitt 
delivers something like:

    {
      "id": "0x19",
      "val": "5.6 m/s"
    },
    {
      "id": "0x15",
      "val": "26.61 W/m2"
    },

Here the *val* field contains the numeric data for my metric and a unit part. To solve this problem, 
a label and a value definition can contain *field*, *name* and *index* (of whitespace separated array)

means a definition in our mapping configuration like

    'val|unit#1' 

or

    'val#1|unit'

is interpreted as: use the field *val*, splitt its content by whitespace and take the
2nd entry as value. The name should be *unit*. 

For the example above a full definition whould be

    topics:
    - topic: topic/wheater
      metrics:
      - valueField: val#0
        labels: [val|unit#1]

The input

    {
      "id": "0x15",
      "val": "26.61 W/m2"
    }

will produce a metric with value 26.61 and a label unit=W/m2.

### configuration helper

The MQTT connectors support 2 modes which can be set for each topic seperatly.

- **verbose mode** enables verbose logging. It can be set by 'mode: verbose'
- **discovery mode** includes the verbose mode and change some standard behaviour during metric auto detection. 
It can be set by 'mode: discover'.

Here a small example from a mapping.yaml

    topics:
    - topic: topic/A
      mode:verbose
    - topic: topic/B
    - topic: topic/C
      mode:discover


## MQTT based health

In some cases the MQTT connection breaks down and no new messages are received from MQTT. 
Only a connection reset helps. 

The configuration value **maxIdleTimeMillis** is used for detection of this case. It defines the
maximum number of millis between two received and processed mqtt messages. If no new messages 
come in, the measured time for the last valid message increases and at some point the threshold 
is reached. This measurement is used for a health check, means the overall health check of medusa
will report _unhealthy_

example config with 5 minutes (5 *60 *1000) threashold: 

        mqtt-brokers:
        - name: central
          maxIdleTimeMillis: 300000
          url: tcp://192.168.1.2:1883
          mapping-file: config/topic-mappings.yaml

The health is reported under quarkus standard endpoint http://localhost:8085/q/health

## service metrics

The service collects and publishes own runtime metrics under
    http://localhost:8085/q/metrics/prometheus

In addition to standard JVM and operational metrics, the following metrics are also recorded.

- **updated_metrics** number of all metrics collected and updated from configured consumers (rest/mqtt) since start. 
                      Each update of a metric is counted.

