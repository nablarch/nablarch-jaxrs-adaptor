package nablarch.integration.jaxrs.jersey;

import java.time.ZoneId;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nablarch.integration.jaxrs.jackson.Jackson2BodyConverter;

/**
 * {@link JerseyJaxRsHandlerListFactory}向けの拡張実装として、Jackson2.xを使用してリクエスト/レスポンスの
 * 変換を行う{@link Jackson2BodyConverter}実装クラス。
 *
 * Jackson2.xのモジュールを組み込み、{@link ObjectMapper}のサポートするデータ型の範囲を
 * 拡張する
 */
public class JerseyJackson2BodyConverter extends Jackson2BodyConverter {
    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure(ObjectMapper objectMapper) {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
