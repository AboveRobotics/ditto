---
title: Errors
keywords: error, failure, exception, model
tags: [model]
permalink: basic-errors.html
---

Errors are datatypes containing information about occurred failures which were either
cause by the user or appeared in the server.  

## Error model specification

{% include docson.html schema="jsonschema/error.json" %}

### Error codes

A Ditto error defines an "error code" which is a string identifier that uniquely identifies the error.

Ditto itself uses the following prefixes for its error codes:

* `things:` - for errors related to [things](basic-thing.html)
* `policies:` - for errors related to [policies](basic-policy.html)
* `things-search:` - for errors related to the [things search](basic-search.html)
* `acknowledgement:` - for errors related to [acknowledgements](basic-acknowledgements.html)
* `messages:` - for errors related to [messages](basic-messages.html)
* `placeholder:` - for errors related to [placeholders](basic-placeholders.html)
* `jwt:` - for errors related to <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.jwt}}">JWT</a> based [authentication](basic-auth.html)
* `gateway:` - for errors produced by the (HTTP/WS) [gateway](architecture-services-gateway.html) service
* `connectivity:` - for errors produced by the [connectivity](architecture-services-connectivity.html) service

## Examples

```json
{
  "status": 404,
  "error": "things:attribute.notfound",
  "message": "The attribute with key 'unknown-key' on the thing with ID 'org.eclipse.ditto:my-thing' could not be found or the requester had insufficient permissions to access it.",
  "description": "Check if the ID of the thing and the key of your requested attribute was correct and you have sufficient permissions."
}
```

```json
{
  "status": 400,
  "error": "messages:id.invalid",
  "message": "Thing ID 'foobar2000' is not valid!",
  "description": "It must conform to the namespaced entity ID notation (see Ditto documentation)",
  "href": "https://www.eclipse.org/ditto/basic-namespaces-and-names.html#namespaced-id"
}
```

