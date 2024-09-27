package nablarch.integration.jaxrs.jackson;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.util.annotation.Published;
import nablarch.fw.ExecutionContext;
import nablarch.fw.jaxrs.BodyConverterSupport;
import nablarch.fw.jaxrs.JaxRsContext;
import nablarch.fw.web.HttpErrorResponse;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.io.Reader;

/**
 * Jackson用の{@link nablarch.fw.jaxrs.BodyConverter}の実装をサポートするクラス。
 * <p>
 * このConverterは、メディアタイプが{@code application/json}で始まっている場合に
 * リクエスト/レスポンスを変換する。(大文字、小文字は問わない)
 *
 * @author Kiyohito Itoh
 */
@Published(tag = "architect")
public abstract class JacksonBodyConverterSupport extends BodyConverterSupport {

    /** ロガー */
    private static final Logger LOGGER = LoggerManager.get(JacksonBodyConverterSupport.class);

    @Override
    protected Object convertRequest(final HttpRequest request, final ExecutionContext context) {

        final JaxRsContext jaxRsContext = JaxRsContext.get(context);
        final Class<?> beanClass = jaxRsContext.getRequestClass();
        final ServletRequest servletRequest = ((ServletExecutionContext) context).getServletRequest();

        try {
            return readValue(servletRequest.getReader(), beanClass);
        } catch (IOException e) {
            LOGGER.logInfo("failed to read request. cause = [" + e.getMessage() + ']');
            throw new HttpErrorResponse(HttpResponse.Status.BAD_REQUEST.getStatusCode(), e);
        }
    }

    /**
     * JSON文字列から指定された型のオブジェクトに値を読み込む。
     *
     * @param src JSON文字列のソース
     * @param valueType 値の型
     * @return JSON文字列を読み込んだオブジェクト
     * @throws IOException 読み込みに失敗した場合
     */
    protected abstract Object readValue(Reader src, Class<?> valueType) throws IOException;

    @Override
    protected HttpResponse convertResponse(Object response, ExecutionContext context) {

        final JaxRsContext jaxRsContext = JaxRsContext.get(context);
        final HttpResponse httpResponse = new HttpResponse();

        httpResponse.setContentType(getContentType(jaxRsContext.getProducesMediaType()).getValue());

        try {
            httpResponse.write(writeValueAsString(response));
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to write response.", e);
        }
        return httpResponse;
    }

    /**
     * 指定されたオブジェクトからJSON文字列に書き込む。
     *
     * @param value オブジェクト
     * @return JSON文字列
     * @throws IOException 書き込みに失敗した場合
     */
    protected abstract String writeValueAsString(Object value) throws IOException;

    @Override
    public boolean isConvertible(String mediaType) {
        return mediaType.toLowerCase().startsWith("application/json");
    }
}
