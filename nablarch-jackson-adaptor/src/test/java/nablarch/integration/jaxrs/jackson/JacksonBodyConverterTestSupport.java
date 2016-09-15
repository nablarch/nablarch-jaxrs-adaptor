package nablarch.integration.jaxrs.jackson;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import nablarch.fw.ExecutionContext;
import nablarch.fw.jaxrs.BodyConverterSupport;
import nablarch.fw.jaxrs.JaxRsContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.NablarchHttpServletRequestWrapper;
import nablarch.fw.web.servlet.ServletExecutionContext;
import nablarch.test.support.log.app.OnMemoryLogWriter;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.UnsupportedCharsetException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link JacksonBodyConverterSupport}を実装したクラスのテストをサポートするクラス。
 */
public abstract class JacksonBodyConverterTestSupport<T extends BodyConverterSupport> {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected T sut;

    @Mocked
    protected HttpRequest request;

    @Mocked
    protected NablarchHttpServletRequestWrapper servletRequest;

    @Mocked
    protected ServletExecutionContext executionContext;

    @Mocked
    protected JaxRsContext jaxRsContext;

    @Before
    public void setUp() {
        sut = createSut();
    }

    protected abstract T createSut();
    protected abstract T createSutWithDisableFailOnUnknownProperties();
    protected abstract T createSutWithEnableWriteEnumsUsingToString();
    protected abstract Class<?> getUnrecognizedPropertyExceptionClass();

    @Test
    public void testRead_json() throws Exception {

        new NonStrictExpectations() {{
            jaxRsContext.getRequestClass();
            result = TestBean.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            servletRequest.getCharacterEncoding();
            result = "utf-8";
            servletRequest.getReader();
            result = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("{\"str\":\"あいう\", \"num\":123, \"flg\":1}".getBytes("utf-8")), "utf-8"));
        }};
        TestBean bean = (TestBean)sut.read(request, executionContext);
        assertThat(bean.getStr(), is("あいう"));
        assertThat(bean.getNum(), is(123L));
        assertThat(bean.isFlg(), is(true));

        new NonStrictExpectations() {{
            jaxRsContext.getRequestClass();
            result = TestBean.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            executionContext.getServletRequest();
            result = servletRequest;
            servletRequest.getCharacterEncoding();
            result = "windows-31j";
            servletRequest.getReader();
            result = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("{\"str\":\"あいう\", \"num\":123, \"flg\":1}".getBytes("windows-31j")), "windows-31j"));
        }};
        bean = (TestBean)sut.read(request, executionContext);
        assertThat(bean.getStr(), is("あいう"));
        assertThat(bean.getNum(), is(123L));
        assertThat(bean.isFlg(), is(true));
    }

    @Test
    public void testRead_with_context() throws Exception {
        new NonStrictExpectations() {{
            jaxRsContext.getRequestClass();
            result = TestBean.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            servletRequest.getCharacterEncoding();
            result = "utf-8";
            servletRequest.getReader();
            result = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("{\"str\":\"bbb\", \"num\":456789, \"flg\":0}".getBytes("utf-8")), "utf-8"));
        }};

        TestBean bean = (TestBean)sut.read(request, executionContext);
        assertThat(bean.getStr(), is("bbb"));
        assertThat(bean.getNum(), is(456789L));
        assertThat(bean.isFlg(), is(false));
    }

    @Test
    public void testRead_bean_null() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("consumes media type and resource method signature is mismatch.");

        new NonStrictExpectations() {{
            jaxRsContext.getRequestClass();
            result = null;
        }};
        sut.read(request, executionContext);
    }

    @Test
    public void testRead_charset_invalid() throws Exception {

        new NonStrictExpectations() {{
            jaxRsContext.getRequestClass();
            result = TestBean.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            servletRequest.getCharacterEncoding();
            result = "test";
        }};

        try {
            sut.read(request, executionContext);
            fail("サポートされていないcharsetによる例外が発生");
        } catch (HttpErrorResponse e) {
            assertThat(e.getCause(), instanceOf(UnsupportedCharsetException.class));
            assertThat(e.getResponse().getStatusCode(), is(400));
        }
    }

    @Test
    public void testRead_json_failed() throws Exception {
        new NonStrictExpectations() {{
            jaxRsContext.getRequestClass();
            result = TestBean.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            servletRequest.getCharacterEncoding();
            result = "utf-8";
            servletRequest.getReader();
            result = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("test=aaa".getBytes("utf-8")), "utf-8"));
        }};

        try {
            sut.read(request, executionContext);
            fail("業務エラーが発生");
        } catch (HttpErrorResponse e) {
            assertThat(e.getResponse().getStatusCode(), is(400));
            assertThat(e.getCause(), instanceOf(IOException.class));
            assertThat(e.getCause().getMessage(), containsString("Unrecognized token"));

            OnMemoryLogWriter.assertLogContains("writer.memory", "failed to read request. cause = [Unrecognized token 'test'");
        }
    }

    /**
     * プロパティに存在しない属性がJSONに含まれている場合はエラーとなること。
     */
    @Test
    public void unknownPropertyInJson_shouldThrowError_defaultImplementation() throws Exception {
        new NonStrictExpectations() {{
            jaxRsContext.getRequestClass();
            result = TestBean.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            servletRequest.getCharacterEncoding();
            result = "utf-8";
            servletRequest.getReader();
            result = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("{\"str\":\"あいう\", \"num\":123, \"flg\":1, \"unknown_property\":true}".getBytes("utf-8")), "utf-8"));
        }};
        try {
            sut.read(request, executionContext);
            fail("未知のプロパティによるエラーが発生");
        } catch (HttpErrorResponse e) {
            assertThat("bad request", e.getResponse().getStatusCode(), is(400));
            Throwable cause = e.getCause();
            assertThat("未知のプロパティであることを示す例外が発生すること", cause, is(instanceOf(getUnrecognizedPropertyExceptionClass())));

            OnMemoryLogWriter.assertLogContains("writer.memory"
                    , "failed to read request. cause = [Unrecognized field \"unknown_property\""
                    , TestBean.class.getName());
        }
    }

    /**
     * カスタム実装で、{@link DeserializationConfig.Feature#FAIL_ON_UNKNOWN_PROPERTIES}を無効にした場合、
     * 未知のプロパティがあってもエラーとならずに正常にBeanが作られること。
     */
    @Test
    public void unknownPropertyInJson_shouldThrowError_customImplementation() throws Exception {
        new NonStrictExpectations() {{
            jaxRsContext.getRequestClass();
            result = TestBean.class;
            jaxRsContext.getConsumesMediaType();
            result = "application/json";
            servletRequest.getCharacterEncoding();
            result = "utf-8";
            servletRequest.getReader();
            result = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("{\"str\":\"あいう\", \"num\":123, \"flg\":true, \"unknown_property\":true}".getBytes("utf-8")), "utf-8"));
        }};

        sut = createSutWithDisableFailOnUnknownProperties();

        TestBean result = (TestBean) sut.read(request, executionContext);

        assertThat("オブジェクトが生成されること", result, is(notNullValue()));
        assertThat(result.str, is("あいう"));
        assertThat(result.num, is(123L));
        assertThat(result.flg, is(true));
    }

    @Test
    public void testWrite_json() throws Exception {

        TestBean bean = new TestBean("aaa",123L, true);

        // ProducesMediaTypeにcharset=utf-8が設定されているパターン
        new NonStrictExpectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/json;charset=utf-8";
        }};

        HttpResponse response = sut.write(bean, executionContext);
        assertThat(response.getBodyString(), is("{\"str\":\"aaa\",\"num\":123,\"flg\":true}"));
        assertThat(response.getContentType(), is("application/json;charset=utf-8"));
        assertThat(response.getContentLength(), is("34"));
        assertThat(response.getStatusCode(), is(200));

        // ProducesMediaTypeにcharset=windows-31jが設定されているパターン
        new NonStrictExpectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/json;charset=windows-31j";
        }};

        response = sut.write(bean, executionContext);
        assertThat(response.getBodyString(), is("{\"str\":\"aaa\",\"num\":123,\"flg\":true}"));
        assertThat(response.getContentType(), is("application/json;charset=windows-31j"));
        assertThat(response.getContentLength(), is("34"));
        assertThat(response.getStatusCode(), is(200));

        // ProducesMediaTypeにcharsetが設定されていないパターン
        new NonStrictExpectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/json";
        }};

        response = sut.write(bean, executionContext);
        assertThat(response.getBodyString(), is("{\"str\":\"aaa\",\"num\":123,\"flg\":true}"));
        assertThat(response.getContentType(), is("application/json;charset=UTF-8"));
        assertThat(response.getContentLength(), is("34"));
        assertThat(response.getStatusCode(), is(200));

        // ProducesMediaTypeにcharsetが設定されておらず、デフォルトエンコーディングにISO-8859-1を指定しているパターン
        new NonStrictExpectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/json";
        }};

        sut.setDefaultEncoding("ISO-8859-1");
        response = sut.write(bean, executionContext);
        assertThat(response.getBodyString(), is("{\"str\":\"aaa\",\"num\":123,\"flg\":true}"));
        assertThat(response.getContentType(), is("application/json;charset=ISO-8859-1"));
        assertThat(response.getContentLength(), is("34"));
        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    public void testWrite_charset_invalid() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("produces charset is invalid. charset = [test]");

        new NonStrictExpectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/json;charset=test";
        }};

        sut.write(new TestBean(), executionContext);
    }

    @Test
    public void testWrite_httpResponse() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("produces media type and resource method signature is mismatch.");

        sut.write(new HttpResponse(200), executionContext);
    }

    /**
     * デフォルトの実装では、enumは{@link Enum#name()}で変換されること
     */
    @Test
    public void enum_shouldReturnEnumName_defaultImplementation() throws Exception {

        // ProducesMediaTypeにcharset=utf-8が設定されているパターン
        new NonStrictExpectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/json;charset=utf-8";
        }};

        HasEnumBean bean = new HasEnumBean();
        bean.type = HasEnumBean.TYPE.TYPE1;


        HttpResponse response = sut.write(bean, executionContext);
        String body = response.getBodyString();
        assertThat("EnumはEnum#nameで変換されること", body, is("{\"type\":\"TYPE1\"}"));
    }

    /**
     * カスタム実装で、{@link Feature#WRITE_ENUMS_USING_TO_STRING}を有効にした場合、
     * {@link Enum#toString()}で変換されること
     */
    @Test
    public void enum_shouldReturnEnumName_customImplementation() throws Exception {

        // ProducesMediaTypeにcharset=utf-8が設定されているパターン
        new NonStrictExpectations() {{
            jaxRsContext.getProducesMediaType();
            result = "application/json;charset=utf-8";
        }};

        sut = createSutWithEnableWriteEnumsUsingToString();

        HasEnumBean bean = new HasEnumBean();
        bean.type = HasEnumBean.TYPE.TYPE2;

        HttpResponse response = sut.write(bean, executionContext);
        String body = response.getBodyString();
        assertThat("EnumはEnum#toStringで変換されること", body, is("{\"type\":\"2\"}"));
    }

    /**
     * application/jsonは変換対象であるべき
     */
    @Test
    public void applicationJson_shouldBeConvertible() throws Exception {
        assertThat(sut.isConvertible(APPLICATION_JSON), is(true));
        assertThat("大文字、小文字は問わない", sut.isConvertible("APPLICATION/JSON"), is(true));
        assertThat("application/jsonから始まっていれば良い", sut.isConvertible("application/json-patch+json"), is(true));
    }

    /**
     * application/json以外は変換対象ではない
     */
    @Test
    public void otherThanApplicationJson_shouldNotBeConvertible() throws Exception {
        assertThat("application/jsonで始まっていない", sut.isConvertible("[" + MediaType.APPLICATION_JSON), is(false));
        assertThat("他のメディアタイプ", sut.isConvertible(MediaType.APPLICATION_XML), is(false));
    }

    public static class TestBean {
        private String str;
        private Long num;
        private boolean flg;

        public TestBean() {
        }

        public TestBean(String str, Long num, boolean flg) {
            this.str = str;
            this.num = num;
            this.flg = flg;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public Long getNum() {
            return num;
        }

        public void setNum(Long num) {
            this.num = num;
        }

        public boolean isFlg() {
            return flg;
        }

        public void setFlg(boolean flg) {
            this.flg = flg;
        }
    }

    private static class TestAction {
        public HttpResponse test(HttpRequest request, TestBean bean) {
            return null;
        }

        public HttpResponse test(HttpRequest request, ExecutionContext ctx, TestBean bean) {
            return null;
        }

        public HttpResponse test(MultivaluedMap map) {
            return null;
        }

        public HttpResponse test() {
            return null;
        }
    }

    public static class HasEnumBean {

        private TYPE type;

        public TYPE getType() {
            return type;
        }

        public void setType(TYPE type) {
            this.type = type;
        }

        enum TYPE {
            TYPE1,
            TYPE2;

            @Override
            public String toString() {
                switch (this) {
                    case TYPE1:
                        return "1";
                    case  TYPE2:
                        return "2";
                    default:
                        return "unknown";
                }
            }
        }
    }
}
