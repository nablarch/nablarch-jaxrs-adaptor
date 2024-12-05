package nablarch.integration.jaxrs.resteasy;

import nablarch.fw.Handler;
import nablarch.fw.jaxrs.BodyConvertHandler;
import nablarch.fw.jaxrs.FormUrlEncodedConverter;
import nablarch.fw.jaxrs.JaxRsBeanValidationHandler;
import nablarch.fw.jaxrs.JaxRsHandlerListFactory;
import nablarch.fw.jaxrs.JaxbBodyConverter;
import nablarch.fw.jaxrs.MultipartFormDataBodyConverter;
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

    /**
     * コンストラクタ。
     */
    public ResteasyJaxRsHandlerListFactory() {

        final List<Handler<HttpRequest, ?>> list = new ArrayList<>();

        final BodyConvertHandler bodyConvertHandler = new BodyConvertHandler();
        bodyConvertHandler.addBodyConverter(new ResteasyJackson2BodyConverter());
        bodyConvertHandler.addBodyConverter(new JaxbBodyConverter());
        bodyConvertHandler.addBodyConverter(new FormUrlEncodedConverter());
        bodyConvertHandler.addBodyConverter(new MultipartFormDataBodyConverter());
        list.add(bodyConvertHandler);

        list.add(new JaxRsBeanValidationHandler());

        handlerList = Collections.unmodifiableList(list);
    }

    @Override
    public List<Handler<HttpRequest, ?>> createObject() {
        return handlerList;
    }
}
