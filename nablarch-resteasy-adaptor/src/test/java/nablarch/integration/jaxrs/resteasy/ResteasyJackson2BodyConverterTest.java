package nablarch.integration.jaxrs.resteasy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
 * {@link ResteasyJackson2BodyConverter}のテストクラス
 */
public class ResteasyJackson2BodyConverterTest {
    @Test
    public void dateAndTimeSerialize() throws NoSuchMethodException {
        ResteasyJackson2BodyConverter sut = new ResteasyJackson2BodyConverter();

        ZoneOffset offset = ZonedDateTime.now(ZoneId.systemDefault()).getOffset();

        DataClass data = new DataClass(
                LocalDate.of(2024, 12, 17),
                LocalDateTime.of(2024, 12, 17, 11, 56, 29),
                OffsetDateTime.of(2024, 12, 17, 11, 56, 29, 0, offset)
        );

        Method method = Resource.class.getMethod("get", DataClass.class);

        HttpResponse response = sut.write(data, new ExecutionContext() {
            @Override
            public JaxRsContext getRequestScopedVar(String varName) throws ClassCastException {
                return new JaxRsContext(method);
            }
        });

        String json = response.getBodyString();

        assertThat(json, is("{\"date\":\"2024-12-17\",\"localDateTime\":\"2024-12-17T11:56:29\",\"offsetDateTime\":\"2024-12-17T11:56:29" + offset + "\"}"));
    }

    @Test
    public void dateAndTimeDeserialize() throws NoSuchMethodException {
        ResteasyJackson2BodyConverter sut = new ResteasyJackson2BodyConverter();

        ZoneOffset offset = ZonedDateTime.now(ZoneId.systemDefault()).getOffset();

        String json = "{\"date\":\"2024-12-17\",\"localDateTime\":\"2024-12-17T11:56:29\",\"offsetDateTime\":\"2024-12-17T11:56:29" + offset + "\"}";

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

        DataClass expected = new DataClass(
                LocalDate.of(2024, 12, 17),
                LocalDateTime.of(2024, 12, 17, 11, 56, 29),
                OffsetDateTime.of(2024, 12, 17, 11, 56, 29, 0, offset)
        );


        assertThat(data, is(expected));
    }

    public record DataClass(
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