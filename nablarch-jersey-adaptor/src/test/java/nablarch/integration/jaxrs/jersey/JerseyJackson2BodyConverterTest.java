package nablarch.integration.jaxrs.jersey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import nablarch.fw.ExecutionContext;
import nablarch.fw.jaxrs.JaxRsContext;
import nablarch.fw.jaxrs.JaxRsHttpRequest;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;
import nablarch.test.support.web.servlet.MockServletContext;
import nablarch.test.support.web.servlet.MockServletRequest;
import nablarch.test.support.web.servlet.MockServletResponse;
import org.junit.Test;

/**
 * {@link JerseyJackson2BodyConverter}のテストクラス
 */
public class JerseyJackson2BodyConverterTest {
    @Test
    public void dateAndTimeSerialize() throws NoSuchMethodException {
        JerseyJackson2BodyConverter sut = new JerseyJackson2BodyConverter();

        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, 11, 17, 11, 56, 29);
        calendar.clear(Calendar.MILLISECOND);

        DataClass data = new DataClass(
                calendar.getTime(),
                LocalDate.of(2024, 12, 17),
                LocalDateTime.of(2024, 12, 17, 11, 56, 29),
                OffsetDateTime.of(2024, 12, 17, 11, 56, 29, 0, ZoneOffset.ofHours(9))
        );

        Method method = Resource.class.getMethod("get", DataClass.class);

        HttpResponse response = sut.write(data, new ExecutionContext() {
            @Override
            public JaxRsContext getRequestScopedVar(String varName) throws ClassCastException {
                return new JaxRsContext(method);
            }
        });

        String json = response.getBodyString();

        assertThat(json, is("{\"utilDate\":\"2024-12-17T02:56:29.000+00:00\",\"date\":\"2024-12-17\",\"localDateTime\":\"2024-12-17T11:56:29\",\"offsetDateTime\":\"2024-12-17T02:56:29Z\"}"));
    }

    @Test
    public void dateAndTimeDeserialize() throws NoSuchMethodException {
        JerseyJackson2BodyConverter sut = new JerseyJackson2BodyConverter();

        String json = "{\"utilDate\":\"2024-12-17T02:56:29.000+00:00\",\"date\":\"2024-12-17\",\"localDateTime\":\"2024-12-17T11:56:29\",\"offsetDateTime\":\"2024-12-17T11:56:29+09:00\"}";

        HttpRequest request = new JaxRsHttpRequest(null);

        Method method = Resource.class.getMethod("get", DataClass.class);

        MockServletRequest servletRequest = new MockServletRequest() {
            @Override
            public BufferedReader getReader() throws IOException {
                return new BufferedReader(new StringReader(json));
            }
        };
        MockServletResponse servletResponse = new MockServletResponse();
        MockServletContext servletContext = new MockServletContext();

        DataClass data = (DataClass) sut.read(request, new ServletExecutionContext(servletRequest, servletResponse, servletContext) {
            @Override
            public JaxRsContext getRequestScopedVar(String varName) throws ClassCastException {
                return new JaxRsContext(method);
            }
        });

        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, 11, 17, 11, 56, 29);
        calendar.clear(Calendar.MILLISECOND);

        DataClass expected = new DataClass(
                calendar.getTime(),
                LocalDate.of(2024, 12, 17),
                LocalDateTime.of(2024, 12, 17, 11, 56, 29),
                OffsetDateTime.of(2024, 12, 17, 2, 56, 29, 0, ZoneOffset.UTC)
        );

        assertThat(data, is(expected));
    }

    public record DataClass(
            Date utilDate,
            LocalDate date,
            LocalDateTime localDateTime,
            OffsetDateTime offsetDateTime
    ) {
    }

    public static class Resource {
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public DataClass get(DataClass data) {
            return null;
        }
    }
}