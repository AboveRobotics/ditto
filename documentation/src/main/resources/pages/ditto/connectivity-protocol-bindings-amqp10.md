---
title: AMQP 1.0 protocol binding
keywords: binding, protocol, amqp, amqp10
tags: [protocol, connectivity, rql]
permalink: connectivity-protocol-bindings-amqp10.html
---

When messages are sent in [Ditto Protocol](protocol-overview.html) (as `UTF-8` encoded String payload), 
the `content-type` of AMQP 1.0 messages must be set to:

```
application/vnd.eclipse.ditto+json
```

If messages which are not in Ditto Protocol should be processed, a [payload mapping](connectivity-mapping.html) must
be configured for the AMQP 1.0 connection in order to transform the messages. 

## AMQP 1.0 properties

Supported AMQP 1.0 properties which are interpreted in a specific way are:

* `content-type`: for defining the Ditto Protocol content-type
* `correlation-id`: for correlating request messages to responses

## Specific connection configuration

### Source format

Any `source` item defines an `addresses` array of source identifiers (e.g. Eclipse Hono's 
[Telemetry API](https://www.eclipse.org/hono/api/telemetry-api)) to consume messages from
and `authorizationContext` array that contains the authorization subjects in whose context
inbound messages are processed. These subjects may contain placeholders, see 
[placeholders](basic-connections.html#placeholder-for-source-authorization-subjects) section for more information.

```json
{
  "addresses": [
    "<source>",
    "..."
  ],
  "authorizationContext": ["ditto:inbound-auth-subject", "..."]
}
```

#### Enforcement

{% include_relative connectivity-enforcement.md %}

The following placeholders are available for the `input` field:

| Placeholder    | Description  | Example   |
|-----------|-------|---------------|
| `{%raw%}{{ header:<name> }}{%endraw%}` | Any header from the message received via the source. | `{%raw%}{{header:device_id }}{%endraw%}`  |

Assuming a device `sensor:temperature1` pushes its telemetry data to Ditto which is stored in a Thing 
`sensor:temperature1`. The device identity is provided in a header field `device_id`. To enforce that the device can 
only send data to the Thing `sensor:temperature1` the following enforcement configuration can be used: 
```json
{
  "addresses": [ "telemetry/hono_tenant" ],
  "authorizationContext": ["ditto:inbound-auth-subject"],
  "enforcement": {
    "input": "{%raw%}{{ header:device_id }}{%endraw%}",
    "filters": [ "{%raw%}{{ thing:id }}{%endraw%}" ]
  }
}
```

#### Source header mapping

For incoming AMQP 1.0 messages, an optional [header mapping](connectivity-header-mapping.html) may be applied.

The JSON for an AMQP 1.0 source with header mapping could like this:
```json
{
  "addresses": [
    "<source>"
  ],
  "authorizationContext": ["ditto:inbound-auth-subject"],
  "headerMapping": {
    "correlation-id": "{%raw%}{{ header:message-id }}{%endraw%}",
    "content-type": "{%raw%}{{ header:content-type }}{%endraw%}"
  }
}
```

### Target format

An AMQP 1.0 connection requires the protocol configuration target object to have an `address` property with a source
identifier. The target address may contain placeholders; see
[placeholders](basic-connections.html#placeholder-for-target-addresses) section for more 
information.

Further, `"topics"` is a list of strings, each list entry representing a subscription of
[Ditto protocol topics](protocol-specification-topic.html).

Outbound messages are published to the configured target address if one of the subjects in `"authorizationContext"`
have READ permission on the Thing that is associated with a message.

```json
{
  "address": "<target>",
  "topics": [
    "_/_/things/twin/events",
    "_/_/things/live/messages"
  ],
  "authorizationContext": ["ditto:outbound-auth-subject", "..."]
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
  "address": "<target>",
  "topics": [
    "_/_/things/twin/events?namespaces=org.eclipse.ditto&filter=gt(attributes/counter,42)",
    "_/_/things/live/messages?namespaces=org.eclipse.ditto"
  ],
  "authorizationContext": ["ditto:outbound-auth-subject", "..."]
}
```

#### Target header mapping

For outgoing AMQP 1.0 messages, an optional [header mapping](connectivity-header-mapping.html) may be applied.

The JSON for an AMQP 1.0 target with header mapping could like this:
```json
{
  "address": "<target>",
  "topics": [
    "_/_/things/twin/events",
    "_/_/things/live/messages?namespaces=org.eclipse.ditto"
  ],
  "authorizationContext": ["ditto:inbound-auth-subject"],
  "headerMapping": {
    "message-id": "{%raw%}{{ header:correlation-id }}{%endraw%}",
    "content-type": "{%raw%}{{ header:content-type }}{%endraw%}",
    "subject": "{%raw%}{{ topic:subject }}{%endraw%}",
    "reply-to": "all-replies"
  }
}
```


### Specific configuration properties

The specific configuration properties are interpreted as 
[JMS Configuration options](https://qpid.apache.org/releases/qpid-jms-0.34.0/docs/index.html#jms-configuration-options). 
Use these to customize and tweak your connection as needed.



## Establishing connecting to an AMQP 1.0 endpoint

Ditto's [Connectivity service](architecture-services-connectivity.html) is responsible for creating new and managing 
existing connections.

This can be done dynamically at runtime without the need to restart any microservice using a
[Ditto DevOps command](installation-operating.html#devops-commands).

Example connection configuration to create a new AMQP 1.0 connection:

```json
{
  "id": "hono-example-connection-123",
  "connectionType": "amqp-10",
  "connectionStatus": "open",
  "failoverEnabled": true,
  "uri": "amqps://user:password@hono.eclipse.org:5671",
  "sources": [
    {
      "addresses": [
        "telemetry/FOO"
      ],
      "authorizationContext": ["ditto:inbound-auth-subject"]
    }
  ],
  "targets": [
    {
      "address": "events/twin",
      "topics": [
        "_/_/things/twin/events"
      ],
      "authorizationContext": ["ditto:outbound-auth-subject"]
    }
  ]
}
```

## Messages

Messages consumed via the AMQP 1.0 binding are treated similar to the [WebSocket binding](httpapi-protocol-bindings-websocket.html)
meaning that the messages are expected to be [Ditto Protocol](protocol-overview.html) messages serialized as JSON (as 
shown for example in the [protocol examples](protocol-examples.html)). If your payload is not conform to the [Ditto
Protocol](protocol-overview.html), you can configure a custom [payload mapping](connectivity-mapping.html).
