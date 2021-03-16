package org.eclipse.ditto.protocoladapter.things;

import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.common.HttpStatus;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.json.FieldType;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.DittoProtocolAdapter;
import org.eclipse.ditto.protocoladapter.Payload;
import org.eclipse.ditto.protocoladapter.ProtocolAdapterTest;
import org.eclipse.ditto.protocoladapter.TestConstants;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.base.ErrorRegistry;
import org.eclipse.ditto.signals.base.GlobalErrorRegistry;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ThingErrorResponseAdapter}.
 */
public class ThingErrorResponseAdapterTest implements ProtocolAdapterTest {

    private ThingErrorResponseAdapter underTest;
    private DittoRuntimeException dittoRuntimeException;

    @Before
    public void setUp() {
        final ErrorRegistry<DittoRuntimeException> errorRegistry = GlobalErrorRegistry.getInstance();
        underTest = ThingErrorResponseAdapter.of(DittoProtocolAdapter.getHeaderTranslator(), errorRegistry);
        dittoRuntimeException = DittoRuntimeException
                .newBuilder("error.code", HttpStatus.UNAUTHORIZED)
                .message("the message")
                .description("the description")
                .build();
    }

    @Test
    public void testFromAdaptable() {
        final ThingErrorResponse expected =
                ThingErrorResponse.of(TestConstants.THING_ID, dittoRuntimeException);

        final TopicPath topicPath =
                TopicPath.newBuilder(TestConstants.THING_ID).things().none().errors().build();
        final JsonPointer path = JsonPointer.empty();

        final Adaptable adaptable = Adaptable.newBuilder(topicPath)
                .withPayload(Payload.newBuilder(path)
                        .withValue(dittoRuntimeException.toJson(FieldType.regularOrSpecial()))
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();
        final ThingErrorResponse actual = underTest.fromAdaptable(adaptable);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }

    @Test
    public void testToAdaptable() {
        final ThingErrorResponse errorResponse =
                ThingErrorResponse.of(TestConstants.THING_ID, dittoRuntimeException);

        final TopicPath topicPath =
                TopicPath.newBuilder(TestConstants.THING_ID).things().none().errors().build();
        final JsonPointer path = JsonPointer.empty();

        final Adaptable expected = Adaptable.newBuilder(topicPath)
                .withPayload(Payload.newBuilder(path)
                        .withValue(dittoRuntimeException.toJson(FieldType.regularOrSpecial()))
                        .withStatus(HttpStatus.UNAUTHORIZED)
                        .build())
                .withHeaders(TestConstants.HEADERS_V_2)
                .build();

        final Adaptable actual = underTest.toAdaptable(errorResponse, TopicPath.Channel.NONE);

        assertWithExternalHeadersThat(actual).isEqualTo(expected);
    }
}