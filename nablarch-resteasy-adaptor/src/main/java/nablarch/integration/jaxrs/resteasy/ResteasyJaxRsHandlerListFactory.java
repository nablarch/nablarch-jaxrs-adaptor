package nablarch.integration.jaxrs.resteasy;

import nablarch.fw.Handler;
import nablarch.fw.jaxrs.BodyConvertHandler;
import nablarch.fw.jaxrs.FormUrlEncodedConverter;
import nablarch.fw.jaxrs.JaxRsBeanValidationHandler;
import nablarch.fw.jaxrs.JaxRsHandlerListFactory;
import nablarch.fw.jaxrs.JaxbBodyConverter;
import nablarch.fw.web.HttpRequest;
import nablarch.integration.jaxrs.jackson.Jackson2BodyConverter;

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

    /**
     * コンストラクタ。
     */
    public ResteasyJaxRsHandlerListFactory() {

        final List<Handler<HttpRequest, ?>> list = new ArrayList<Handler<HttpRequest, ?>>();

        final BodyConvertHandler bodyConvertHandler = new BodyConvertHandler();
        bodyConvertHandler.addBodyConverter(new Jackson2BodyConverter());
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
}
