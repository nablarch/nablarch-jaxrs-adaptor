package nablarch.integration.jaxrs.jersey;

import nablarch.fw.Handler;
import nablarch.fw.jaxrs.BodyConvertHandler;
import nablarch.fw.jaxrs.FormUrlEncodedConverter;
import nablarch.fw.jaxrs.JaxRsBeanValidationHandler;
import nablarch.fw.jaxrs.JaxRsHandlerListFactory;
import nablarch.fw.jaxrs.JaxbBodyConverter;
import nablarch.fw.web.HttpRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Jerseyを使用する{@link JaxRsHandlerListFactory}の実装クラス。
 *
 * @author Kiyohito Itoh
 */
public class JerseyJaxRsHandlerListFactory implements JaxRsHandlerListFactory {

    /** {@link Handler}のリスト */
    private final List<Handler<HttpRequest, ?>> handlerList;

    private final JerseyJackson2BodyConverter jerseyJackson2BodyConverter;

    /**
     * コンストラクタ。
     */
    public JerseyJaxRsHandlerListFactory() {

        final List<Handler<HttpRequest, ?>> list = new ArrayList<>();

        final BodyConvertHandler bodyConvertHandler = new BodyConvertHandler();
        jerseyJackson2BodyConverter = new JerseyJackson2BodyConverter();
        bodyConvertHandler.addBodyConverter(jerseyJackson2BodyConverter);
        bodyConvertHandler.addBodyConverter(new JaxbBodyConverter());
        bodyConvertHandler.addBodyConverter(new FormUrlEncodedConverter());
        list.add(bodyConvertHandler);

        list.add(new JaxRsBeanValidationHandler());

        handlerList = Collections.unmodifiableList(list);
    }

    @Override
    public List<Handler<HttpRequest, ?>> createObject() {
        return handlerList;
    }

    public void setJacksonTimeZone(String jacksonTimeZone) {
        jerseyJackson2BodyConverter.setTimeZone(jacksonTimeZone);
    }
}
