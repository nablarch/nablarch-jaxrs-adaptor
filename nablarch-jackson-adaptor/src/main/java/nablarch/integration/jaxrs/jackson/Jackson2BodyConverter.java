package nablarch.integration.jaxrs.jackson;

import java.io.IOException;
import java.io.Reader;

import com.fasterxml.jackson.databind.ObjectMapper;

import nablarch.fw.jaxrs.BodyConverter;

/**
 * Jackson2.xを使用してリクエスト/レスポンスの変換を行う{@link BodyConverter}実装クラス。
 *
 * @author Kiyohito Itoh
 */
public class Jackson2BodyConverter extends JacksonBodyConverterSupport {

    /** {@link ObjectMapper} */
    private final ObjectMapper objectMapper;

    /**
     * {@code JacksonBodyConverter}を生成する。
     */
    public Jackson2BodyConverter() {
        objectMapper = new ObjectMapper();
        configure(objectMapper);
    }

    /**
     * {@link ObjectMapper}に対するオプション設定などを行う。
     * <p/>
     * このクラスでは特に何も行わないので、オプション設定はサブクラス側で行う必要がある。
     *
     * @param objectMapper {@link ObjectMapper}
     */
    protected void configure(ObjectMapper objectMapper) {
        // nop
    }

    @Override
    protected Object readValue(Reader src, Class<?> valueType) throws IOException {
        return objectMapper.readValue(src, valueType);
    }

    @Override
    protected String writeValueAsString(Object value) throws IOException {
        return objectMapper.writeValueAsString(value);
    }
}
