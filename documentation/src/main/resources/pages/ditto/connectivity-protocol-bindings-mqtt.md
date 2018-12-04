---
title: MQTT 3.1.1 protocol binding
keywords: binding, protocol, mqtt
tags: [protocol, connectivity]
permalink: connectivity-protocol-bindings-mqtt.html
---

[awsiot]: https://docs.aws.amazon.com/iot/

When MQTT messages are sent in [Ditto Protocol](protocol-overview.html),
the payload should be `UTF-8` encoded strings.

If messages which are not in Ditto Protocol should be processed, a [payload mapping](connectivity-mapping.html) must
be configured for the connection in order to transform the messages.

## MQTT 3.1.1 properties

MQTT 3.1.1 messages have no application headers. Transmission-relevant properties are set in the
`"headers"` field as a part of [Ditto protocol messages](protocol-specification.html#dittoProtocolEnvelope) in the
payload. 

These properties are supported:

* `correlation-id`: For correlating request messages and events. Twin events have the correlation IDs of
  [Twin commands](protocol-twinlive.html#twin) that produced them.
* `reply-to`: The value should be an MQTT topic.
  If a command sets the header `reply-to`, then its response is published at the topic equal to the header value.

## Specific connection configuration

### Source format

For an MQTT connection:

* Source `"addresses"` are MQTT topics to subscribe to. Wildcards `+` and `#` are allowed.
* `"authorizationContext"` may _not_ contain placeholders `{%raw%}{{ header:<header-name> }}{%endraw%}` as MQTT 3.1.1
  has no application headers.
* The optional field `"qos"` sets the maximum Quality of Service to request when subscribing for messages. Its value
  can be `0` for at-most-once delivery, `1` for at-least-once delivery and `2` for exactly-once delivery.
  The default value is `2` (exactly-once).
  Support of any Quality of Service depends on the external MQTT broker; [AWS IoT][awsiot] for example does not
  acknowledge subscriptions with `qos=2`.

```json
{
  "addresses": [
    "<mqtt_topic>",
    "..."
  ],
  "authorizationContext": ["ditto:inbound-auth-subject", "..."],
  "qos": 2
}
```

#### Enforcement

{% include_relative connectivity-enforcement.md %}

The following placeholders are available for the `input` field:

| Placeholder    | Description  | Example   |
|-----------|-------|---------------|
| `{%raw%}{{ source:address }}{%endraw%}` | The topic on which the message was received. | devices/sensors/temperature1  |

Assuming a device `temperature1` publishes its telemetry data to an MQTT broker on topic `devices/sensors/temperature1`.
The MQTT broker verifies that no other device is allowed to publish on this topic. To enforce that the device can 
only send data to the Thing `sensors:temperature1` the following enforcement configuration can be used: 
```json
{
  "addresses": [ "devices/sensors/#" ],
  "authorizationContext": ["ditto:inbound-auth-subject", "..."],
  "qos": 1,
  "enforcement": {
    "input": "{%raw%}{{ source:address }}{%endraw%}",
    "filters": [ "{%raw%}devices/{{ thing:namespace }}/{{ thing:name }}{%endraw%}" ]
  }
}
```

#### Source header mapping

As MQTT 3.1.1 does not support headers in its protocol, a [header mapping](connectivity-header-mapping.html) is not possible to configure here.


### Target format

For an MQTT connection, the target address is the MQTT topic to publish events and messages to.
The target address may contain placeholders; see
[placeholders](basic-connections.html#placeholder-for-target-addresses) section for more information.

Further, `"topics"` is a list of strings, each list entry representing a subscription of
[Ditto protocol topics](protocol-specification-topic.html).

Outbound messages are published to the configured target address if one of the subjects in `"authorizationContext"`
have READ permission on the Thing that is associated with a message.

The additional field `"qos"` sets the Quality of Service with which messages are published.
Its value can be `0` for at-most-once delivery, `1` for at-least-once delivery and `2` for exactly-once delivery.
Support of any Quality of Service depends on the external MQTT broker.
The default value is `0` (at-most-once).


```json
{
  "address": "mqtt/topic/of/my/device/{%raw%}{{ thing:id }}{%endraw%}",
  "topics": [
    "_/_/things/twin/events",
    "_/_/things/live/messages"
  ],
  "authorizationContext": ["ditto:outbound-auth-subject", "..."],
  "qos": 0
}
```

#### Filtering

In order to only consume specific events like described in [change notifications](basic-changenotifications.html), the
following parameters can additionally be provided when specifying the `topics` of a target:

| Description | Topic | Filter by namespaces | Filter by RQL expression |
|-------------|-----------------|------------------|-----------|
| Subscribe for [events/change notifications](basic-changenotifications.html) | `_/_/things/twin/events` | &#10004; | &#10004; |
| Subscribe for [messages](basic-messages.html) | `_/_/things/live/messages` | &#10004; | &#10060; |
| Subscribe for [live commands](protocol-twinlive.html) | `_/_/things/live/commands` | &#10004; | &#10060; |
| Subscribe for [live events](protocol-twinlive.html) | `_/_/things/live/events` | &#10004; | &#10004; |

The parameters are specified similar to HTTP query parameters, the first one separated with a `?` and all following ones
with `&`. You have to URL encode the filter values before using them in a configuration.

For example this way the connection session would register for all events in the namespace `org.eclipse.ditto` and which
would match an attribute "counter" to be greater than 42. Additionally it would subscribe to messages in the namespace
`org.eclipse.ditto`:
```json
{
  "address": "eclipse-ditto-sandbox/{%raw%}{{ thing:id }}{%endraw%}",
  "topics": [
    "_/_/things/twin/events?namespaces=org.eclipse.ditto&filter=gt(attributes/counter,42)",
    "_/_/things/live/messages?namespaces=org.eclipse.ditto"
  ],
  "authorizationContext": ["ditto:outbound-auth-subject", "..."]
}
```

#### Target header mapping

As MQTT 3.1.1 does not support headers in its protocol, a [header mapping](connectivity-header-mapping.html) is not possible to configure here.


## Establishing a connection to an MQTT 3.1.1 endpoint

Ditto's [Connectivity service](architecture-services-connectivity.html) is responsible for creating new and managing
existing connections.

This can be done dynamically at runtime without the need to restart any microservice using a
[Ditto DevOps command](installation-operating.html#devops-commands).

Example 

Connection configuration to create a new MQTT connection:

```json
{
  "id": "mqtt-example-connection-123",
  "connectionType": "mqtt",
  "connectionStatus": "open",
  "failoverEnabled": true,
  "uri": "tcp://test.mosquitto.org:1883",
  "sources": [
    {
      "addresses": [
        "eclipse-ditto-sandbox/#"
      ],
      "authorizationContext": ["ditto:inbound-auth-subject"],
      "qos": 0,
      "filters": []
    }
  ],
  "targets": [
    {
      "address": "eclipse-ditto-sandbox/{%raw%}{{ thing:id }}{%endraw%}",
      "topics": [
        "_/_/things/twin/events"
      ],
      "authorizationContext": ["ditto:outbound-auth-subject"],
      "qos": 0
    }
  ]
}
```

## Messages

Messages consumed via the MQTT binding are treated similar to the
[WebSocket binding](httpapi-protocol-bindings-websocket.html), 
meaning that the messages are expected to be [Ditto Protocol](protocol-overview.html) messages serialized as
UTF-8-coded JSON (as shown for example in the [protocol examples](protocol-examples.html)).
If your payload does not conform to the [Ditto Protocol](protocol-overview.html) or uses any character set other
than UTF-8, you can configure a custom [payload mapping](connectivity-mapping.html).

## Client-certificate authentication

Ditto supports certificate-based authentication for MQTT connections. Consult 
[Certificates for Transport Layer Security](connectivity-tls-certificates.html)
for how to set it up.

Here is an example MQTT connection that checks the broker certificate and authenticates by a client certificate.

```json
{
  "id": "mqtt-example-connection-124",
  "connectionType": "mqtt",
  "connectionStatus": "open",
  "failoverEnabled": true,
  "uri": "ssl://test.mosquitto.org:8884",
  "validateCertificates": true,
  "ca": "-----BEGIN CERTIFICATE-----\n<test.mosquitto.org certificate>\n-----END CERTIFICATE-----",
  "credentials": {
    "type": "client-cert",
    "cert": "-----BEGIN CERTIFICATE-----\n<signed client certificate>\n-----END CERTIFICATE-----",
    "key": "-----BEGIN PRIVATE KEY-----\n<client private key>\n-----END PRIVATE KEY-----"
  },
  "sources": [
    {
      "addresses": [
        "eclipse-ditto-sandbox/#"
      ],
      "authorizationContext": ["ditto:inbound-auth-subject"],
      "qos": 0,
      "filters": []
    }
  ],
  "targets": [
    {
      "address": "eclipse-ditto-sandbox/{%raw%}{{ thing:id }}{%endraw%}",
      "topics": [
        "_/_/things/twin/events"
      ],
      "authorizationContext": ["ditto:outbound-auth-subject"],
      "qos": 0
    }
  ]
}
```
