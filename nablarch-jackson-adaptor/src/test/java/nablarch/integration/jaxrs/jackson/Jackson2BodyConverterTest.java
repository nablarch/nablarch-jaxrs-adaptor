package nablarch.integration.jaxrs.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

/**
 * {@link Jackson2BodyConverter}のテストクラス。
 */
public class Jackson2BodyConverterTest extends JacksonBodyConverterTestSupport<Jackson2BodyConverter> {

    @Override
    protected Jackson2BodyConverter createSut() {
        return new Jackson2BodyConverter();
    }

    @Override
    protected Jackson2BodyConverter createSutWithDisableFailOnUnknownProperties() {
        return new Jackson2BodyConverter() {
            @Override
            protected void configure(ObjectMapper objectMapper) {
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            }
        };
    }

    @Override
    protected Jackson2BodyConverter createSutWithEnableWriteEnumsUsingToString() {
        return new Jackson2BodyConverter() {
            @Override
            protected void configure(ObjectMapper objectMapper) {
                objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
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
        new NonStrictExpectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/json;charset=utf-8";
            mapper.writeValueAsString(bean);
            result = new JsonMappingException("error");
        }};

        sut.write(bean, executionContext);
    }
}
