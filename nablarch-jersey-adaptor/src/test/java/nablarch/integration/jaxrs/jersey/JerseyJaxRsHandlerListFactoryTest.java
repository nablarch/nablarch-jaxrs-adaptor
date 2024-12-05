package nablarch.integration.jaxrs.jersey;

import com.fasterxml.jackson.databind.ObjectMapper;
import nablarch.fw.Handler;
import nablarch.fw.jaxrs.BodyConvertHandler;
import nablarch.fw.jaxrs.BodyConverter;
import nablarch.fw.jaxrs.FormUrlEncodedConverter;
import nablarch.fw.jaxrs.JaxRsBeanValidationHandler;
import nablarch.fw.jaxrs.JaxbBodyConverter;
import nablarch.fw.jaxrs.MultipartFormDataBodyConverter;
import nablarch.fw.web.HttpRequest;
import nablarch.test.support.reflection.ReflectionUtil;
import org.junit.Test;

import java.util.List;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

/**
 * {@link JerseyJaxRsHandlerListFactory}のテストクラス。
 */
public class JerseyJaxRsHandlerListFactoryTest {

    private JerseyJaxRsHandlerListFactory sut;

    @Test
    public void testCreate() throws Exception {
        sut = new JerseyJaxRsHandlerListFactory();
        List<Handler<HttpRequest, ?>> list = sut.createObject();

        assertThat(list.size(), is(2));

        assertThat(list.get(0), instanceOf(BodyConvertHandler.class));
        List<BodyConverter> bodyConverters = ReflectionUtil.getFieldValue(list.get(0), "bodyConverters");
        assertThat(bodyConverters.size(), is(4));
        assertThat(bodyConverters.get(0), instanceOf(JerseyJackson2BodyConverter.class));
        assertThat(bodyConverters.get(1), instanceOf(JaxbBodyConverter.class));
        assertThat(bodyConverters.get(2), instanceOf(FormUrlEncodedConverter.class));
        assertThat(bodyConverters.get(3), instanceOf(MultipartFormDataBodyConverter.class));

        ObjectMapper objectMapper = ReflectionUtil.getFieldValue(bodyConverters.get(0), "objectMapper");
        assertThat(objectMapper.getDeserializationConfig().getTimeZone(), is(TimeZone.getTimeZone("UTC")));

        assertThat(list.get(1), instanceOf(JaxRsBeanValidationHandler.class));
    }
}
