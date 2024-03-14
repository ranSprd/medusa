The topics section in 


### Matching

### Wildcards

A topic can be specified in its entirety `/some/topic/path/used/as/metric`
or it can contain wildcards.


#### Multi Level Wirldcard: \#

The multi-level wildcard covers multiple topic levels. It is represented by the 
hash symbol (#) and must be placed as the last character in the topic, 
preceded by a forward slash. Or with other words, everything after the hash symbol (#)
is ignored.

    myhome/groundfloor/#

would match:

    myhome/groundfloor/kitchen/temperature
    myhome/groundfloor/kitchen/humidity


#### Single Level Wildcard: +

The single-level wildcard is represented by the plus symbol (+) and allows the 
replacement of a single topic level. By subscribing to a topic with a single-level 
wildcard, any topic that contains an arbitrary string in place of the wildcard 
will be matched. 

This 
    myhome/groundfloor/+/temperature

would match several topics. For instance:

    myhome/groundfloor/kitchen/temperature
    myhome/groundfloor/bathroom/temperature
    myhome/groundfloor/bedroom/temperature
    myhome/groundfloor/livingroom/temperature
    myhome/groundfloor/child-1/temperature

#### Placeholders

If you need the different parts 


#### Matching

For some cases only a subset of all topics should be used. In such cases the following 
syntax is used

    myhome/groundfloor/{roomName}[kitchen, bathroom]/temperature

This selects only the topics for `kitchen` and `bathroom`. This feature works currently 
only in combination with placeholders.
