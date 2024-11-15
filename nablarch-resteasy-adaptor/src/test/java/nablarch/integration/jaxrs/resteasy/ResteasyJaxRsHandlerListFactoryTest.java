package nablarch.integration.jaxrs.resteasy;

import com.fasterxml.jackson.databind.ObjectMapper;
import nablarch.fw.Handler;
import nablarch.fw.jaxrs.BodyConvertHandler;
import nablarch.fw.jaxrs.BodyConverter;
import nablarch.fw.jaxrs.FormUrlEncodedConverter;
import nablarch.fw.jaxrs.JaxRsBeanValidationHandler;
import nablarch.fw.jaxrs.JaxbBodyConverter;
import nablarch.fw.web.HttpRequest;
import nablarch.test.support.reflection.ReflectionUtil;
import org.junit.Test;

import java.util.List;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * {@link ResteasyJaxRsHandlerListFactory}のテストクラス。
 */
public class ResteasyJaxRsHandlerListFactoryTest {

    private ResteasyJaxRsHandlerListFactory sut;

    @Test
    public void testCreate() throws Exception {
        sut = new ResteasyJaxRsHandlerListFactory();
        List<Handler<HttpRequest, ?>> list = sut.createObject();

        assertThat(list.size(), is(2));

        assertThat(list.get(0), instanceOf(BodyConvertHandler.class));
        List<BodyConverter> bodyConverters = ReflectionUtil.getFieldValue(list.get(0), "bodyConverters");
        assertThat(bodyConverters.size(), is(3));
        assertThat(bodyConverters.get(0), instanceOf(ResteasyJackson2BodyConverter.class));
        assertThat(bodyConverters.get(1), instanceOf(JaxbBodyConverter.class));
        assertThat(bodyConverters.get(2), instanceOf(FormUrlEncodedConverter.class));

        ObjectMapper objectMapper = ReflectionUtil.getFieldValue(bodyConverters.get(0), "objectMapper");
        assertThat(objectMapper.getDeserializationConfig().getTimeZone(), is(TimeZone.getTimeZone("UTC")));

        assertThat(list.get(1), instanceOf(JaxRsBeanValidationHandler.class));
    }

    @Test
    public void testCreateInitialize() throws Exception {
        sut = new ResteasyJaxRsHandlerListFactory();
        sut.setJacksonTimeZone("Asia/Tokyo");

        List<Handler<HttpRequest, ?>> list = sut.createObject();

        assertThat(list.size(), is(2));

        assertThat(list.get(0), instanceOf(BodyConvertHandler.class));
        List<BodyConverter> bodyConverters = ReflectionUtil.getFieldValue(list.get(0), "bodyConverters");
        assertThat(bodyConverters.size(), is(3));
        assertThat(bodyConverters.get(0), instanceOf(ResteasyJackson2BodyConverter.class));
        assertThat(bodyConverters.get(1), instanceOf(JaxbBodyConverter.class));
        assertThat(bodyConverters.get(2), instanceOf(FormUrlEncodedConverter.class));

        ObjectMapper objectMapper = ReflectionUtil.getFieldValue(bodyConverters.get(0), "objectMapper");
        assertThat(objectMapper.getDeserializationConfig().getTimeZone(), is(TimeZone.getTimeZone("Asia/Tokyo")));

        assertThat(list.get(1), instanceOf(JaxRsBeanValidationHandler.class));
    }
}
