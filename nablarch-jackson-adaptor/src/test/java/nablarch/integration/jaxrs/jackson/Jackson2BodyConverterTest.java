package nablarch.integration.jaxrs.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import mockit.Mocked;
import mockit.Expectations;
import nablarch.fw.web.HttpErrorResponse;
import org.junit.Test;

import java.io.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertThat;

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

        final TestBean bean = new TestBean("aaa", 123L, true);
        new Expectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/json;charset=utf-8";
            mapper.writeValueAsString(bean);
            result = new JsonMappingException("error");
        }};

        sut.write(bean, executionContext);
    }

    /**
     * {@link Serializable} を継承したクラスを使用してjackson-databindのデシリアライズを確認する。
     * セキュリティホールになる可能性がある。
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testRead_polymorphic() throws Exception {
        new Expectations() {{
            jaxRsContext.getRequestClass();
            result = Serializable.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            minTimes = 0;
            servletRequest.getCharacterEncoding();
            result = "utf-8";
            servletRequest.getReader();
            result = new BufferedReader(new StringReader("[\"nablarch.integration.jaxrs.jackson.Jackson2BodyConverterTest$ExampleBean\", { \"str\" : \"str\" }]"));
        }};
        Jackson2BodyConverter target = new Jackson2BodyConverter() {
            @Override
            protected void configure(ObjectMapper objectMapper) {
                // この設定によりJSONでデシリアライズするクラスを指定できるようになる。
                objectMapper.enableDefaultTyping();
            }
        };
        Serializable bean = (Serializable) target.read(request, executionContext);
        assertThat(((ExampleBean) bean).getStr(), is("str"));
    }

    /**
     * {@link Serializable} を継承したクラスを使用してjackson-databindのデシリアライズできないことを確認する。
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testRead_polymorphic_fail() throws Exception {
        expectedException.expect(HttpErrorResponse.class);
        expectedException.expectCause(isA(InvalidDefinitionException.class));
        new Expectations() {{
            jaxRsContext.getRequestClass();
            result = Serializable.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            minTimes = 0;
            servletRequest.getCharacterEncoding();
            result = "utf-8";
            servletRequest.getReader();
            result = new BufferedReader(new StringReader("[\"nablarch.integration.jaxrs.jackson.Jackson2BodyConverterTest$ExampleBean\", { \"str\" : \"str\" }]"));
        }};
        Serializable bean = (Serializable) sut.read(request, executionContext);
    }

    /**
     * {@link Serializable} のフィールドにアノテーションを付与することでJSONで指定したクラスをデシリアライズできることを確認する。
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testRead_polymorphic_nested_class() throws Exception {
        new Expectations() {{
            jaxRsContext.getRequestClass();
            result = ExampleBeanInField.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            minTimes = 0;
            servletRequest.getCharacterEncoding();
            result = "utf-8";
            servletRequest.getReader();
            result = new BufferedReader(new StringReader("{ \"field1\" : { \"str\" : \"str\" }, \"field2\" : { \"@class\" : \"nablarch.integration.jaxrs.jackson.Jackson2BodyConverterTest$ExampleBean\", \"str\" : \"str\" } }"));
        }};
        ExampleBeanInField bean = (ExampleBeanInField) sut.read(request, executionContext);
        assertThat(((ExampleBean) bean.getField2()).getStr(), is("str"));
    }

    /**
     * {@link Serializable} のフィールドにアノテーションを付与していてもJSONで指定したクラスをデシリアライズできないことを確認する。
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testRead_polymorphic_nested_class_with_annotation_disabled() throws Exception {
        expectedException.expect(HttpErrorResponse.class);
        expectedException.expectCause(isA(InvalidDefinitionException.class));
        new Expectations() {{
            jaxRsContext.getRequestClass();
            result = ExampleBeanInField.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            minTimes = 0;
            servletRequest.getCharacterEncoding();
            result = "utf-8";
            servletRequest.getReader();
            result = new BufferedReader(new StringReader("{ \"field1\" : { \"str\" : \"str\" }, \"field2\" : { \"@class\" : \"nablarch.integration.jaxrs.jackson.Jackson2BodyConverterTest$ExampleBean\", \"str\" : \"str\" } }"));
        }};
        Jackson2BodyConverter target = new Jackson2BodyConverter() {
            @Override
            protected void configure(ObjectMapper objectMapper) {
                // アノテーションを使用してのデシリアライズを禁止する。
                objectMapper.disable(MapperFeature.USE_ANNOTATIONS);
            }
        };
        ExampleBeanInField bean = (ExampleBeanInField) target.read(request, executionContext);
    }

    public static class ExampleBean implements Serializable {
        private String str;

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }
    }

    public static class ExampleBeanInField {
        private ExampleBean field1;

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
        private Serializable field2;

        public ExampleBean getField1() {
            return field1;
        }

        public void setField1(ExampleBean field1) {
            this.field1 = field1;
        }

        public Serializable getField2() {
            return field2;
        }

        public void setField2(Serializable field2) {
            this.field2 = field2;
        }
    }
}
