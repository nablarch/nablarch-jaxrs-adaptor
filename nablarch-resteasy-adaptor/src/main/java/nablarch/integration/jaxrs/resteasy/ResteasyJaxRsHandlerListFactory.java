package nablarch.integration.jaxrs.resteasy;

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
 * Resteasyを使用する{@link JaxRsHandlerListFactory}の実装クラス。
 *
 * @author Naoki Yamamoto
 */
public class ResteasyJaxRsHandlerListFactory implements JaxRsHandlerListFactory {

    /** {@link Handler}のリスト */
    private final List<Handler<HttpRequest, ?>> handlerList;

    private final ResteasyJackson2BodyConverter resteasyJackson2BodyConverter;

    private String jacksonTimeZone;

    /**
     * コンストラクタ。
     */
    public ResteasyJaxRsHandlerListFactory() {

        final List<Handler<HttpRequest, ?>> list = new ArrayList<>();

        final BodyConvertHandler bodyConvertHandler = new BodyConvertHandler();
        resteasyJackson2BodyConverter = new ResteasyJackson2BodyConverter();
        bodyConvertHandler.addBodyConverter(resteasyJackson2BodyConverter);
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
        resteasyJackson2BodyConverter.setTimeZone(jacksonTimeZone);
    }
}
