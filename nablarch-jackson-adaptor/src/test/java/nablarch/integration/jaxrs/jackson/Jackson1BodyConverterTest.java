package nablarch.integration.jaxrs.jackson;

import mockit.Mocked;
import mockit.Expectations;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.junit.Test;

/**
 * {@link Jackson1BodyConverter}のテストクラス。
 */
public class Jackson1BodyConverterTest extends JacksonBodyConverterTestSupport<Jackson1BodyConverter> {

    @Override
    protected Jackson1BodyConverter createSut() {
        return new Jackson1BodyConverter();
    }

    @Override
    protected Jackson1BodyConverter createSutWithDisableFailOnUnknownProperties() {
        return new Jackson1BodyConverter() {
            @Override
            protected void configure(ObjectMapper objectMapper) {
                objectMapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
            }
        };
    }

    @Override
    protected Jackson1BodyConverter createSutWithEnableWriteEnumsUsingToString() {
        return new Jackson1BodyConverter() {
            @Override
            protected void configure(ObjectMapper objectMapper) {
                objectMapper.enable(Feature.WRITE_ENUMS_USING_TO_STRING);
            }
        };
    }

    @Override
    protected Class<?> getUnrecognizedPropertyExceptionClass() {
        return UnrecognizedPropertyException.class;
    }

    @Test
    public void testWrite_json_failed(@Mocked final ObjectMapper mapper) throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("failed to write response.");

        final TestBean bean = new TestBean("aaa",123L, true);
        new Expectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/json;charset=utf-8";
            mapper.writeValueAsString(bean);
            result = new JsonMappingException("error");
        }};

        sut.write(bean, executionContext);
    }
}
