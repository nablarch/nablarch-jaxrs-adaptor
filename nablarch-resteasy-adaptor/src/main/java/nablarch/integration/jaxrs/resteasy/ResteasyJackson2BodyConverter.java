package nablarch.integration.jaxrs.resteasy;

import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nablarch.integration.jaxrs.jackson.Jackson2BodyConverter;

/**
 * {@link ResteasyJaxRsHandlerListFactory}向けの拡張実装として、Jackson2.xを使用してリクエスト/レスポンスの
 * 変換を行う{@link Jackson2BodyConverter}実装クラス。
 * <p>
 * Jackson2.xのモジュールを組み込み、{@link ObjectMapper}のサポートするデータ型の範囲を
 * 拡張する
 */
public class ResteasyJackson2BodyConverter extends Jackson2BodyConverter {
    private ObjectMapper objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure(ObjectMapper mapper) {
        this.objectMapper = mapper;

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * タイムゾーンを設定する
     *
     * @param timeZone タイムゾーン
     */
    public void setTimeZone(String timeZone) {
        objectMapper.setTimeZone(TimeZone.getTimeZone(timeZone));
    }
}
