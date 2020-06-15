---
title: Payload mapping in connectivity service
keywords: mapping, transformation, payload, javascript
tags: [connectivity]
permalink: connectivity-mapping.html
---

{% include callout.html content="**TL;DR**<br/>The payload mapping feature in Ditto's connectivity APIs can be used to 
    transform arbitrary payload consumed via the different supported protocols 
    to [Ditto Protocol](protocol-overview.html) messages and vice versa." type="primary" %}


## Motivation

Eclipse Ditto is about providing access to IoT devices via the [digital twin](intro-digitaltwins.html) pattern. In order to
provide structured APIs for different heterogeneous devices Ditto defines a lightweight JSON based [model](basic-overview.html).

A [Thing](basic-thing.html) might look like in the following example:

```json
{
  "thingId": "the.namespace:the-thing-id",
  "policyId": "the.namespace:the-policy-id",
  "attributes": {
    "location": "kitchen"
  },
  "features": {
    "transmission": {
       "properties": {
         "cur_speed": 90
       }
     }
  }
}
```

Devices in the IoT, may they be brownfield devices or newly produced devices, will probably not send their data to the
cloud in the structure and [protocol](protocol-overview.html) Ditto requires.

They should not need to be aware of something like Ditto running in the cloud mirroring them as digital twins.

So for example device payload could look like this:

```json
{
  "val": "23.42 °C",
  "ts": 1523946112727
}
```

In case of constrained devices or IoT protocols, even binary payload might be common.

```
0x08BD (hex representation)
```

## Builtin mappers

The following message mappers are included in the Ditto codebase:

| Mapper Alias | Description                    | Inbound           | Outbound           |
|------------|--------------------------------|---------------------------|---------------------------|
| [Ditto](#ditto-mapper) | Assumes that inbound/outbound messages are already in [Ditto Protocol](protocol-overview.html) (JSON) format. | ✓ | ✓ |
| [JavaScript](#javascript-mapper) | Converts arbitrary messages from and to the [Ditto Protocol](protocol-overview.html) format using **custom** JavaScript code executed by Ditto. | ✓ | ✓ |
| [Normalized](#normalized-mapper) | Transforms the payload of events to a normalized view. |  | ✓ |
| [ConnectionStatus](#connectionstatus-mapper) | This mapper handles messages containing `creation-time` and `ttd` headers by updating a feature of the targeted thing with [definition](basic-feature.html#feature-definition) [ConnectionStatus](https://vorto.eclipse.org/#/details/org.eclipse.ditto:ConnectionStatus:1.0.0). | ✓ |  |

### Ditto mapper

This is the default [Ditto Protocol](protocol-overview.html) mapper. If you do not specify any payload mapping this
 mapper is used to map inbound and outbound messages. The mapper requires has no mandatory options, so its alias can
 be directly used as a mapper reference.

It assumes that received messages are in [Ditto Protocol JSON](protocol-specification.html) and emits outgoing messages
 also in that format.

### JavaScript mapper

This mapper may be used whenever any inbound messages are not yet in [Ditto Protocol](protocol-overview.html). By using 
 the built in [JavaScript mapping engine](#javascript-mapping-engine) (based on Rhino) custom defined JavaScript scripts
 can be executed which are responsible for creating [Ditto Protocol JSON](protocol-specification.html) message from 
 arbitrary consumed payload.

The same is possible for outbound messages in order to transform [Ditto Protocol JSON](protocol-specification.html)
 messages (e.g. events or responses) to arbitrary other formats.

#### Configuration options

* `incomingScript` (required): the mapping script for incoming messages
* `outgoingScript` (required):  the mapping script for outgoing messages
* `loadBytebufferJS` (optional, default: `"false"`): whether to load ByteBufferJS library
* `loadLongJS` (optional, default: `"false"`): whether to load LongJS library

### Normalized Mapper

This mapper transforms `created` and `modified` events (other type of messages are dropped) to a normalized view. 
Events are mapped to a nested sparse JSON.

```json
{
  "topic": "thing/id/things/twin/events/modified",
  "headers": { "content-type": "application/vnd.eclipse.ditto+json" },
  "path": "/features/sensors/properties/temperature/indoor/value",
  "value": 42
}
```

would result in the following normalized JSON representation:

```json
{
  "thingId": "thing:id",
  "features": {
    "sensors": {
      "properties": {
        "temperature": {
          "indoor": {
            "value": 42
          }
        }
      }
    }
  },
  "_context": {
    "topic": "thing/id/things/twin/events/modified",
    "path": "/features/sensors/properties/temperature/indoor/value",
    "headers": {
      "content-type": "application/vnd.eclipse.ditto+json"
    }
  }
}
```
The `_context` field contains the original message content excluding the `value`.

#### Configuration options

* `fields` (optional, default: all fields): comma separated list of fields that are contained in the result (see also
 chapter about [field selectors](httpapi-concepts.html#with-field-selector))
 
### ConnectionStatus Mapper
This mapper transforms the information from the `ttd` and `creation-time` message headers (see Eclipse Hono [device
 notifications](https://www.eclipse.org/hono/docs/concepts/device-notifications/)) into a ModifyFeature
 command that complies with the [Vorto functionblock](https://vorto.eclipse.org/#/details/org.eclipse.ditto:ConnectionStatus:1.0.0) `{%raw%}org.eclipse.ditto:ConnectionStatus{%endraw%}`. 
 
The connectivity state of the device is then represented in a Feature.<br/>
 It is mostly used in conjunction with another mapper that transforms the payload e.g.:<br/>
 `"payloadMapping": [ "Ditto" , "connectionStatus" ]`
 
Example of a resulting `ConnectionStatus` feature:
```json
{
  "thingId": "eclipse:ditto",
  "features": {
    "ConnectionStatus": {
      "definition": [ "org.eclipse.ditto:ConnectionStatus:1.0.0" ],
      "properties": {
        "status": {
          "readySince": "2019-10-29T14:16:18Z",
          "readyUntil": "2019-10-29T14:21:18Z"
        }
      }
    }
  }
}
```
 
#### Configuration options

* `thingId` (required): The ID of the Thing that is updated with the connectivity state. It can either be a fixed value
 or a header placeholder (e.g. `{%raw%}{{ header:device_id }}{%endraw%}`).
* `featureId` (optional, default: `ConnectionStatus`): The ID of the Feature that is updated. It can either be a
 fixed value or resolved from a message header (e.g. `{%raw%}{{ header:feature_id }}{%endraw%}`).



## Example connection with multiple mappers

The following example connection defines a `ConnectionStatus` mapping with the ID `status` and references it in a
 source.<br/>
 Messages received via this source will be mapped by the `Ditto` mapping and the `ConnectionStatus` mapping.<br/> 
 The `Ditto` mapping requires no options to be configured, so you can directly use its alias `Ditto`.  

```json
{ 
  ...  
  "name": "exampleConnection",
  "sources": [{
      "addresses": ["<source>"],
      "authorizationContext": ["ditto:inbound"],
      "payloadMapping": ["Ditto", "status"]
    }
  ],
  "mappingDefinitions": {
    "status": {
      "mappingEngine": "ConnectionStatus",
      "options": {
        "thingId": "{%raw%}{{ header:device_id }}{%endraw%}"
      }
    }
  }
}
```

{% include note.html content="Starting aliases with an uppercase character and IDs with a lowercase character is
 encouraged to avoid confusion but this is not enforced. "%}



## JavaScript mapping engine

Ditto utilizes the [Rhino](https://github.com/mozilla/rhino) JavaScript engine for Java for evaluating the JavaScript
to apply for mapping payloads.

Using Rhino instead of Nashorn, the newer JavaScript engine shipped with Java, has the benefit that sandboxing can be 
applied in a better way. 

Sandboxing of different payload scripts is required as Ditto is intended to be run as cloud service where multiple
connections to different endpoints are managed for different tenants at the same time. This requires the isolation of
each single script to avoid interference with other scripts and to protect the JVM executing the script against harmful
code execution.


### Constraints

Rhino does not fully support EcmaScript 6. Check which language constructs are supported before using
them in a mapping function. See [http://mozilla.github.io/rhino/compat/engines.html](http://mozilla.github.io/rhino/compat/engines.html).

Ditto currently includes Rhino version `1.7.11` and has the `VERSION_ES6` flag enabled.

#### Sandboxing

For sandboxing/security reasons following restrictions apply:


* access to Java packages and classes is not possible
* using `exit`, `quit`, `print`, etc. is not possible
* file access is not possible
* doing remote calls (e.g. to foreign web-servers) is not possible
* programming an endless-loop will terminate the script
* programming a recursion will terminate the script
* the file size of the script is limited
* no foreign JS libraries can be loaded (unless they fit in the file size limit and are included into the mapping script)

### Helper libraries

In order to work more conveniently with binary payloads, the following libraries may be loaded for payload transformations:


* [bytebuffer.js](https://github.com/dcodeIO/bytebuffer.js) a ByteBuffer implementation using ArrayBuffers
* [long.js](https://github.com/dcodeIO/long.js) for representing a 64-bit two's-complement integer value


### Helper functions

Ditto comes with a few helper functions, which makes writing the mapping scripts easier. They are available under the
`Ditto` scope:

```javascript
/**
 * Builds a Ditto Protocol message from the passed parameters.
 * @param {string} namespace - The namespace of the entity in java package notation, e.g.: "org.eclipse.ditto"
 * @param {string} id - The ID of the entity
 * @param {string} group - The affected group/entity, one of: "things"
 * @param {string} channel - The channel for the signal, one of: "twin"|"live"
 * @param {string} criterion - The criterion to apply, one of: "commands"|"events"|"search"|"messages"|"errors"
 * @param {string} action - The action to perform, one of: "create"|"retrieve"|"modify"|"delete"
 * @param {string} path - The path which is affected by the message, e.g.: "/attributes"
 * @param {Object.<string, string>} dittoHeaders - The headers Object containing all Ditto Protocol header values
 * @param {*} [value] - The value to apply / which was applied (e.g. in a "modify" action)
 * @param {number} status - The status code that indicates the result of the command.
 * @param {Object} extra - The enriched extra fields when selected via "extraFields" option.
 * @returns {DittoProtocolMessage} dittoProtocolMessage - 
 *  the mapped Ditto Protocol message or 
 *  <code>null</code> if the message could/should not be mapped
 */
function buildDittoProtocolMsg(namespace, id, group, channel, criterion, action, path, dittoHeaders, value, status, extra) {

    let dittoProtocolMsg = {};
    dittoProtocolMsg.topic = namespace + "/" + id + "/" + group + "/" + channel + "/" + criterion + "/" + action;
    dittoProtocolMsg.path = path;
    dittoProtocolMsg.headers = dittoHeaders;
    dittoProtocolMsg.value = value;
    dittoProtocolMsg.status = status;
    dittoProtocolMsg.extra = extra;
    return dittoProtocolMsg;
}

/**
 * Builds an external message from the passed parameters.
 * @param {Object.<string, string>} headers - The external headers Object containing header values
 * @param {string} [textPayload] - The external mapped String
 * @param {ArrayBuffer} [bytePayload] - The external mapped bytes as ArrayBuffer
 * @param {string} [contentType] - The returned Content-Type
 * @returns {ExternalMessage} externalMessage - 
 *  the mapped external message
 *  or <code>null</code> if the message could/should not be mapped
 */
function buildExternalMsg(headers, textPayload, bytePayload, contentType) {

    let externalMsg = {};
    externalMsg.headers = headers;
    externalMsg.textPayload = textPayload;
    externalMsg.bytePayload = bytePayload;
    externalMsg.contentType = contentType;
    return externalMsg;
}

/**
 * Transforms the passed ArrayBuffer to a String interpreting the content of the passed arrayBuffer as unsigned 8
 * bit integers.
 *
 * @param {ArrayBuffer} arrayBuffer the ArrayBuffer to transform to a String
 * @returns {String} the transformed String
 */
function arrayBufferToString(arrayBuffer) {

    return String.fromCharCode.apply(null, new Uint8Array(arrayBuffer));
}

/**
 * Transforms the passed String to an ArrayBuffer using unsigned 8 bit integers.
 *
 * @param {String} string the String to transform to an ArrayBuffer
 * @returns {ArrayBuffer} the transformed ArrayBuffer
 */
function stringToArrayBuffer(string) {

    let buf = new ArrayBuffer(string.length);
    let bufView = new Uint8Array(buf);
    for (let i=0, strLen=string.length; i<strLen; i++) {
        bufView[i] = string.charCodeAt(i);
    }
    return buf;
}

/**
 * Transforms the passed ArrayBuffer to a {ByteBuffer} (from bytebuffer.js library which needs to be loaded).
 *
 * @param {ArrayBuffer} arrayBuffer the ArrayBuffer to transform
 * @returns {ByteBuffer} the transformed ByteBuffer
 */
function asByteBuffer(arrayBuffer) {
    
    let byteBuffer = new ArrayBuffer(arrayBuffer.byteLength);
    new Uint8Array(byteBuffer).set(new Uint8Array(arrayBuffer));
    return dcodeIO.ByteBuffer.wrap(byteBuffer);
}
```

### Mapping incoming messages

Incoming external messages can be mapped to Ditto Protocol conform messages by implementing the following JavaScript function:

```javascript
/**
 * Maps the passed parameters to a Ditto Protocol message.
 * @param {Object.<string, string>} headers - The headers Object containing all received header values
 * @param {string} [textPayload] - The String to be mapped
 * @param {ArrayBuffer} [bytePayload] - The bytes to be mapped as ArrayBuffer
 * @param {string} [contentType] - The received Content-Type, e.g. "application/json"
 * @returns {(DittoProtocolMessage|Array<DittoProtocolMessage>)} dittoProtocolMessage(s) -
 *  the mapped Ditto Protocol message,
 *  an array of Ditto Protocol messages or
 *  <code>null</code> if the message could/should not be mapped
 */
function mapToDittoProtocolMsg(
    headers,
    textPayload,
    bytePayload,
    contentType
) {

    // ###
    // Insert your mapping logic here:
    // ###

    return Ditto.buildDittoProtocolMsg(
        namespace,
        id,
        group,
        channel,
        criterion,
        action,
        path,
        dittoHeaders,
        value,
        status
    );
}
```

The result of the function has to be a JavaScript object in [Ditto Protocol](protocol-overview.html) or an array of 
such JavaScript objects. That's where the helper method `Ditto.buildDittoProtocolMsg` is useful: it explicitly 
defines which parameters are required for the Ditto Protocol message.

### Mapping outgoing messages

Outgoing Ditto Protocol messages (e.g. [responses](basic-signals-commandresponse.html) or [events](basic-signals-event.html)) 
can be mapped to external messages by implementing the following JavaScript function:

```javascript
/**
 * Maps the passed parameters which originated from a Ditto Protocol message to an external message.
 * @param {string} namespace - The namespace of the entity in java package notation, e.g.: "org.eclipse.ditto"
 * @param {string} id - The ID of the entity
 * @param {string} channel - The channel for the signal, one of: "twin"|"live"
 * @param {string} group - The affected group/entity, one of: "things"
 * @param {string} criterion - The criterion to apply, one of: "commands"|"events"|"search"|"messages"|"errors"
 * @param {string} action - The action to perform, one of: "create"|"retrieve"|"modify"|"delete"
 * @param {string} path - The path which is affected by the message, e.g.: "/attributes"
 * @param {Object.<string, string>} dittoHeaders - The headers Object containing all Ditto Protocol header values
 * @param {*} [value] - The value to apply / which was applied (e.g. in a "modify" action)
 * @param {number} status - The status code that indicates the result of the command.
 * @param {Object} extra - The enriched extra fields when selected via "extraFields" option.
 * @returns {(ExternalMessage|Array<ExternalMessage>)} externalMessage -
 *  The mapped external message,
 *  an array of external messages or
 *  <code>null</code> if the message could/should not be mapped
 */
function mapFromDittoProtocolMsg(
    namespace,
    id,
    group,
    channel,
    criterion,
    action,
    path,
    dittoHeaders,
    value,
    status,
    extra
) {

    // ###
    // Insert your mapping logic here:
    // ###

    return  Ditto.buildExternalMsg(
        headers,
        textPayload,
        bytePayload,
        contentType
    );
}
```

The result of the function has to be a JavaScript object or an array of JavaScript objects with the fields `headers`, 
`textPayload`, `bytePayload` and `contentType`. That's where the helper method `Ditto.buildExternalMsg` is useful: it
 explicitly defines which parameters are required for the external message.


## JavaScript payload types

Both, text payloads and byte payloads may be mapped.

### Text payloads

Working with text payloads is as easy as it gets in JavaScript. For example, for the content-type `application/json`
structured data may be processed like this:

```javascript
let value;
if (contentType === 'application/json') {
    let parsedJson = JSON.parse(textPayload);
    value = parsedJson.number1 + parsedJson['sub-field']; // remember to access JSON keys with dashes in a JS special way
} else {
    // a script may decide to not map other content-types than application/json
    return null;
}
// proceed ...
```

### Byte payloads

Working with byte payloads is also possible but does require a little bit of knowledge about JavaScipt's 
[ArrayBuffer](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer) 
[TypedArrays](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/TypedArray) and
[DataView](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/DataView).

What you get in the mapping scripts is a `bytePayload` of type `ArrayBuffer` which lets you work on the bytes in different ways: 

#### Typed Arrays

> A TypedArray \[is\] a view into an ArrayBuffer where every item has the same size and type.<br/> [source](https://hacks.mozilla.org/2017/01/typedarray-or-dataview-understanding-byte-order/)

With TypedArrays you can simply wrap the `bytePayload` `ArrayBuffer` and work on all the items e.g. as unsigned 8-bit integers:

```javascript
let bytes = new Uint8Array(bytePayload);
bytes[0]; // access the first byte
bytes[1]; // access the second byte
``` 

#### DataViews

> The DataView \[is\] another view into an ArrayBuffer, but one which allows items of different size and type in the ArrayBuffer.<br/> [source](https://hacks.mozilla.org/2017/01/typedarray-or-dataview-understanding-byte-order/)

```javascript
let view = new DataView(bytePayload);
view.getInt8(0); // access a 8-bit signed integer (byte) on offset=0
view.getUint16(1); // access a 16-bit unsigned integer (usigned short) on offset=1
``` 

DataViews also allow to `set` bytes to an underlying ArrayBuffer conveniently.

#### ByteBuffer.js

Alternatively, Ditto's JavaScript transformation may be loaded with the [above mentioned](#helper-libraries) libraries, e.g. "bytebuffer.js".<br />
With `ByteBuffer`, the content of an `ArrayBuffer` can be accessed in a buffered way:

```javascript
let byteBuf = Ditto.asByteBuffer(bytePayload);
let numberFromBytes = parseInt(byteBuf.toHex(), 16);

let base64encoded = byteBuf.toBase64();
let buf = dcodeIO.ByteBuffer.fromBase64(base64encoded);

buf.readInt(); // read a 32bit signed integer + advances the offset in the buffer
buf.readUTF8String(4); // read 4 characters of UTF-8 encoded string + advances the offset in the buffer
buf.remaining(); // gets the number of remaining readable bytes in the buffer
```

Check the [ByteBuffer API documentation](https://github.com/dcodeIO/bytebuffer.js/wiki/API) to find out what is possible with that helper.


## JavaScript Examples

### Text payload example

Let's assume your device sends telemetry data via [Eclipse Hono's](https://www.eclipse.org/hono/) MQTT adapter into the cloud.
And that an example payload of your device is:

```json
{
  "temp": "23.42 °C",
  "hum": 78,
  "pres": {
    "value": 760,
    "unit": "mmHg"
  }
}
```

We want to map a single message of this device containing updates for all 3 values to a Thing in the following structure:

```json
{
  "thingId": "the.namespace:the-thing-id",
  "policyId": "the.namespace:the-policy-id",
  "features": {
    "temperature": {
       "properties": {
         "value": 23.42
       }
     },
    "pressure": {
       "properties": {
         "value": 760
       }
     },
    "humidity": {
       "properties": {
         "value": 78
       }
     }
  }
}
```

Therefore, we define following `incoming` mapping function:

```javascript
function mapToDittoProtocolMsg(
    headers,
    textPayload,
    bytePayload,
    contentType
) {
    
    if (contentType !== 'application/json') {
        return null; // only handle messages with content-type application/json
    }
    
    let jsonData = JSON.parse(textPayload);
    
    let value = {
        temperature: {
            properties: {
                value: jsonData.temp.split(" ")[0] // omit the unit
            }
        },
        pressure: {
            properties: {
                value: jsonData.pres.value
            }
        },
        humidity: {
            properties: {
                value: jsonData.hum
            }
        }
    };

    return Ditto.buildDittoProtocolMsg(
        'org.eclipse.ditto', // in this example always the same
        headers['device_id'], // Eclipse Hono sets the authenticated device_id as AMQP 1.0 header
        'things', // we deal with a Thing
        'twin', // we want to update the twin
        'commands', // we want to create a command to update a twin
        'modify', // modify the twin
        '/features', // modify all features at once
        headers, // pass through the headers from AMQP 1.0
        value
    );
}
```

When your device now sends its payload via the MQTT adapter of Eclipse Hono:

```bash
mosquitto_pub -u 'sensor1@DEFAULT_TENANT' -P hono-secret -t telemetry -m '{"temp": "23.42 °C","hum": 78,"pres": {"value": 760,"unit": "mmHg"}}'
```

Your digital twin is updated by applying the specified script and extracting the relevant values from the passed `textPayload`.


### Bytes payload example

For this example, let's assume your device sends telemetry data via [Eclipse Hono's](https://www.eclipse.org/hono/) HTTP adapter into the cloud.
An example payload of your device - displayed as hexadecimal - is:

```
0x09EF03F72A
```

Let us now also assume that

* the first 2 bytes `09 EF` represent 
  * the temperature as 16bit signed integer (thus, may also be negative)
  * this is not a float in oder to save space (as float needs at least 32 bit)
* the second 2 bytes `03 F7` represent the pressure as 16bit signed integer
* the last byte `2A` represents the humidity as 8bit unsigned integer of our device.

We want to map a single message of this device containing updates for all 3 values to a Thing in the following structure:

```json
{
  "thingId": "the.namespace:the-thing-id",
  "policyId": "the.namespace:the-policy-id",
  "features": {
    "temperature": {
       "properties": {
         "value": 25.43
       }
     },
    "pressure": {
       "properties": {
         "value": 1015
       }
     },
    "humidity": {
       "properties": {
         "value": 42
       }
     }
  }
}
```

Therefore, we define following `incoming` mapping function:

```javascript
function mapToDittoProtocolMsg(
    headers,
    textPayload,
    bytePayload,
    contentType
) {
    
    if (contentType !== 'application/octet-stream') {
        return null; // only handle messages with content-type application/octet-stream
    }
    
    let view = new DataView(bytePayload);
    
    let value = {
        temperature: {
            properties: {
                // interpret the first 2 bytes (16 bit) as signed int and divide through 100.0:
                value: view.getInt16(0) / 100.0
            }
        },
        pressure: {
            properties: {
                // interpret the next 2 bytes (16 bit) as signed int:
                value: view.getInt16(2)
            }
        },
        humidity: {
            properties: {
                // interpret the next 1 bytes (8 bit) as unsigned int:
                value: view.getUint8(4)
            }
        }
    };

    return Ditto.buildDittoProtocolMsg(
        'org.eclipse.ditto', // in this example always the same
        headers['device_id'], // Eclipse Hono sets the authenticated device_id as AMQP 1.0 header
        'things', // we deal with a Thing
        'twin', // we want to update the twin
        'commands', // we want to create a command to update a twin
        'modify', // modify the twin
        '/features', // modify all features at once
        headers, // pass through the headers from AMQP 1.0
        value
    );
}
```

When your device now sends its payload via the HTTP adapter of Eclipse Hono:

```bash
echo -e $((0x09EF03F72A)) | curl -i -X POST -u sensor1@DEFAULT_TENANT:hono-secret -H 'Content-Type: application/octet-stream' --data-binary @- http://127.0.0.1:8080/telemetry
```

Your digital twin is updated by applying the specified script and extracting the relevant values from the passed `bytePayload`.


## Custom Java based implementation

Beside the JavaScript based mapping - which can be configured/changed at runtime without the need of restarting the
connectivity service - there is also the possibility to implement a custom Java based mapper.

The interface to be implemented is `org.eclipse.ditto.services.connectivity.mapping.MessageMapper` ([find the source here](https://github.com/eclipse/ditto/blob/master/services/connectivity/mapping/src/main/java/org/eclipse/ditto/services/connectivity/mapping/MessageMapper.java))
with the following signature to implement (this is only for experts, the sources contain JavaDoc):

```java
public interface MessageMapper {
    
    void configure(MessageMapperConfiguration configuration);
    
    List<Adaptable> map(ExternalMessage message);
    
    List<ExternalMessage> map(Adaptable adaptable);
}
```

After instantiation of the custom `MessageMapper`, the `configure` method is called with all the *options* which were 
provided to the mapper in the [configured connection](connectivity-manage-connections.html#create-connection). Use them
in order to pass in configurations, thresholds, etc.

Then, simply implement both of the `map` methods:

* `List<Adaptable> map(ExternalMessage message)` maps from an incoming external message to
  * an empty list of `Adaptable`s if the incoming message should be dropped
  * a list of one or many [Ditto Protocol](protocol-overview.html) `Adaptable`s
* `List<ExternalMessage> map(Adaptable adaptable)` maps from an outgoing [Ditto Protocol](protocol-overview.html) `Adaptable` to
  * an empty list of `ExternalMessage`s if the outgoing message should be dropped
  * a list of one or many external messages

In order to use this custom Java based mapper implementation, the following steps are required:

* the implementing Class must be annotated with `@PayloadMapper(alias="customMapper")` and define at least one alias
* if the custom mapper requires mandatory options then specify `@PayloadMapper(alias="customMapper", requiresMandatoryConfiguration=true)`
* the Class needs obviously to be on the classpath of the [connectivity](architecture-services-connectivity.html) microservice 
  in order to be loaded
* when creating a new connection you have to specify the alias of your mapper as the `mappingEngine` in the
  connection's `mappingDefinitions` and reference the ID of your mapper in a source or a target

{% include tip.html content="If your mapper does not require any options (`requiresMandatoryConfiguration=false`), you can
 directly reference the alias in a source or a target without first defining it inside `mappingDefinitions`." %} 
