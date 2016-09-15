package nablarch.integration.jaxrs.resteasy;

import mockit.Deencapsulation;
import nablarch.fw.Handler;
import nablarch.fw.jaxrs.BodyConvertHandler;
import nablarch.fw.jaxrs.BodyConverter;
import nablarch.fw.jaxrs.FormUrlEncodedConverter;
import nablarch.fw.jaxrs.JaxRsBeanValidationHandler;
import nablarch.fw.jaxrs.JaxbBodyConverter;
import nablarch.fw.web.HttpRequest;
import nablarch.integration.jaxrs.jackson.Jackson2BodyConverter;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
        List<BodyConverter> bodyConverters = Deencapsulation.getField(list.get(0), "bodyConverters");
        assertThat(bodyConverters.size(), is(3));
        assertThat(bodyConverters.get(0), instanceOf(Jackson2BodyConverter.class));
        assertThat(bodyConverters.get(1), instanceOf(JaxbBodyConverter.class));
        assertThat(bodyConverters.get(2), instanceOf(FormUrlEncodedConverter.class));

        assertThat(list.get(1), instanceOf(JaxRsBeanValidationHandler.class));
    }
}
