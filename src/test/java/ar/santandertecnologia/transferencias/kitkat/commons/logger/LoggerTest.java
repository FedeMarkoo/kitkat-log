package ar.santandertecnologia.transferencias.kitkat.commons.logger;


import ar.santandertecnologia.transferencias.kitkat.commons.logger.constants.Header;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.filter.LogFilter;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.interceptor.IncomingSOAPInterceptor;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.interceptor.LogClientInterceptor;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.interceptor.OutgoingCallsInterceptor;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LoggerUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.AbstractWebServiceConnection;
import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.ws.transport.context.DefaultTransportContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestApplication.class, LogFilter.class})
@RequiredArgsConstructor
class LoggerTest {

    private MockMvc mockMvc;
    ClientInterceptor interceptor = new LogClientInterceptor();

    @Autowired
    LogConfiguration logConfiguration;
    private IncomingSOAPInterceptor incomingSOAPInterceptor = new IncomingSOAPInterceptor();

    @Autowired
    public void setMockMvc(WebApplicationContext webApplicationContext, LogFilter logFilter) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(logFilter).build();
    }

    @Test
    void test() throws Exception {
        MvcResult result = mockMvc.perform(post("/test").contentType(MediaType.APPLICATION_JSON).content("{\"userName\":\"testUserDetails\",\"token\":\"xxx\",\"lastName\":\"xxx\",\"password\":\"xxx\"}")).andExpect(status().isOk()).andReturn();

        result.getResponse().getContentAsString();
    }

    @Test
    void testEmpty() throws Exception {
        MvcResult result = mockMvc.perform(get("/empty")).andExpect(status().isOk()).andReturn();

        result.getResponse().getContentAsString();
    }

    @Test
    void testHealth() throws Exception {
        MvcResult result = mockMvc.perform(get("/health").servletPath("/health")).andExpect(status().isOk()).andReturn();

        result.getResponse().getContentAsString();
    }

    @Test
    void coverageHandleFault() {
        assertThat(interceptor.handleFault(null)).isTrue();
    }

    @Test
    void coverageHandleResponse() {
        assertThat(interceptor.handleResponse(null)).isTrue();
    }

    @Test
    @SneakyThrows
    void coverageHandleRequest() {

        TransportContext context = mock(TransportContext.class);
        TransportContextHolder.setTransportContext(context);
        Conecction conecction = mock(Conecction.class);
        when(conecction.getUri()).thenCallRealMethod();
        List<String> strings = new ArrayList();
        strings.add("test");
        strings.add("fail");
        when(conecction.getResponseHeaderNames()).thenReturn(strings.iterator()).thenThrow(new NullPointerException());
        when(conecction.getResponseHeaders("test")).thenReturn(Collections.singletonList("testValue").iterator());
        when(conecction.getResponseHeaders("fail")).thenThrow(new NullPointerException());
        when(context.getConnection()).thenReturn(conecction);

        MessageContext messageContext = mock(MessageContext.class);
        WebServiceMessage mock1 = mock(WebServiceMessage.class);
        when(messageContext.getRequest()).thenReturn(mock1);

        assertThat(interceptor.handleRequest(messageContext)).isTrue();
        MDC.clear();
        assertThat(interceptor.handleRequest(messageContext)).isTrue();
        assertThat(interceptor.handleRequest(messageContext)).isTrue();

        assertThat(incomingSOAPInterceptor.handleRequest(messageContext, null)).isTrue();
        assertThat(incomingSOAPInterceptor.handleFault(messageContext, null)).isTrue();
        assertThat(incomingSOAPInterceptor.handleResponse(messageContext, null)).isTrue();
        assertThat(incomingSOAPInterceptor.shouldIntercept(messageContext, null)).isTrue();
        assertThat(incomingSOAPInterceptor.resolveException(messageContext, null, null)).isFalse();
    }

    @Test
    void coverageAfterCompletion() {
        Assertions.assertDoesNotThrow(() ->
                interceptor.afterCompletion(null, null));
    }

    @Test
    void coverageConfiguration() {
        Assertions.assertDoesNotThrow(()
                -> logConfiguration.addInterceptor(new RestTemplate(), new OutgoingCallsInterceptor())
        );
    }

    @Test
    void coverageOutgoingCallsInterceptor() {
        MockClientHttpRequest request1 = new MockClientHttpRequest();

        MDC.put(Header.CORRELATIONID, "123");
        new OutgoingCallsInterceptor().intercept(request1, "{\"asd\":123}".getBytes(StandardCharsets.UTF_8), (request, body) ->
                new MockClientHttpResponse("{\"asd\":123}".getBytes(StandardCharsets.UTF_8), HttpStatus.OK));

        MDC.clear();
        new OutgoingCallsInterceptor().intercept(request1, "{\"asd\":123}".getBytes(StandardCharsets.UTF_8), (request, body) ->
                new MockClientHttpResponse("{\"asd\":123}".getBytes(StandardCharsets.UTF_8), HttpStatus.OK));

        Assertions.assertThrowsExactly(RuntimeException.class,
                () -> new OutgoingCallsInterceptor().intercept(request1, null, (request, body) -> {
                    throw new RuntimeException();
                }));
    }

    @Test
    void coverageOutgoingCallsInterceptor2() {
        MockClientHttpRequest request1 = new MockClientHttpRequest();
        request1.setMethod(null);

        MDC.put(Header.CORRELATIONID, "123");
        OutgoingCallsInterceptor outgoingCallsInterceptor = new OutgoingCallsInterceptor();

        outgoingCallsInterceptor.intercept(request1, "asd".getBytes(StandardCharsets.UTF_8), (request, body) ->
                null);

        Assertions.assertThrowsExactly(RuntimeException.class,
                () -> new OutgoingCallsInterceptor().intercept(request1, null, (request, body) -> {
                    throw new RuntimeException();
                }));
    }

    @Test
    void testAfterCompletion() {
        MessageContext messageContextMock = mock(MessageContext.class);
        SaajSoapMessage saajSoapMessageMock = mock(SaajSoapMessage.class);
        when(messageContextMock.getRequest()).thenReturn(saajSoapMessageMock);
        when(messageContextMock.getResponse()).thenReturn(saajSoapMessageMock);
        setURI();

        assertThat(interceptor.handleRequest(messageContextMock)).isTrue();
        interceptor.afterCompletion(messageContextMock, null);

        LoggerUtils.log();
    }

    @Test
    void testAfterCompletionSOAP() {
        MessageContext messageContextMock = mock(MessageContext.class);
        SaajSoapMessage saajSoapMessageMock = mock(SaajSoapMessage.class);
        when(messageContextMock.getRequest()).thenReturn(saajSoapMessageMock);
        when(messageContextMock.getResponse()).thenReturn(saajSoapMessageMock);
        setURI();

        assertThat(incomingSOAPInterceptor.handleRequest(messageContextMock, null)).isTrue();
        incomingSOAPInterceptor.afterCompletion(messageContextMock, null, null);

        LoggerUtils.log();
    }

    @Test
    void testAfterCompletionException() {
        MessageContext messageContextMock = mock(MessageContext.class);
        SaajSoapMessage saajSoapMessageMock = mock(SaajSoapMessage.class);
        when(messageContextMock.getRequest()).thenReturn(saajSoapMessageMock);
        when(messageContextMock.getResponse()).thenReturn(saajSoapMessageMock);

        setURI();

        assertThat(interceptor.handleRequest(messageContextMock)).isTrue();
        interceptor.afterCompletion(messageContextMock, new Exception("message exception"));
        LoggerUtils.log();
    }

    @Test
    void testAfterCompletionSOAPException() {
        MessageContext messageContextMock = mock(MessageContext.class);
        SaajSoapMessage saajSoapMessageMock = mock(SaajSoapMessage.class);
        when(messageContextMock.getRequest()).thenReturn(saajSoapMessageMock);
        when(messageContextMock.getResponse()).thenReturn(saajSoapMessageMock);

        setURI();

        assertThat(incomingSOAPInterceptor.handleRequest(messageContextMock, null)).isTrue();
        incomingSOAPInterceptor.afterCompletion(messageContextMock, null, new Exception("message exception"));
        LoggerUtils.log();
    }

    private void setURI() {
        TransportContextHolder.setTransportContext(new DefaultTransportContext(new AbstractWebServiceConnection() {
            @Override
            protected TransportOutputStream createTransportOutputStream() {
                return null;
            }

            @Override
            protected TransportInputStream createTransportInputStream() {
                return null;
            }

            @Override
            public URI getUri() throws URISyntaxException {
                return new URI("localhost");
            }

            @Override
            public boolean hasError() {
                return false;
            }

            @Override
            public String getErrorMessage() {
                return null;
            }
        }));
    }

    @Test
    void testIpInHeader() throws Exception {
        MvcResult result = mockMvc.perform(get("/empty").header("HTTP_CLIENT_IP", "127.0.0.1")).andExpect(status().isOk()).andReturn();

        result.getResponse().getContentAsString();
    }

    @Test
    void testException() throws Exception {
        MvcResult result = mockMvc.perform(post("/exception").contentType(MediaType.APPLICATION_JSON).content("{\"userName\":\"testUserDetails\",\"firstName\":\"xxx\",\"lastName\":\"xxx\",\"password\":\"xxx\"}")).andExpect(status().is5xxServerError()).andReturn();

        result.getResponse().getContentAsString();
    }

    @Test
    void testJmsMessage() throws Exception {
        LoggerUtils.setJMSData("asdasd", "asd");
        Assertions.assertDoesNotThrow(LoggerUtils::log);
    }

    @Test
    void testInitLog() throws Exception {
        LoggerUtils.initLog();
        Assertions.assertDoesNotThrow(LoggerUtils::log);
    }

    @Test
    @SneakyThrows
    void coverage() {
        OutgoingCallsInterceptor.LoggedClientHttpResponse loggedClientHttpResponse = new OutgoingCallsInterceptor.LoggedClientHttpResponse(mock(ClientHttpResponse.class), mock(InputStream.class));

        loggedClientHttpResponse.getBody();
        loggedClientHttpResponse.getHeaders();
        loggedClientHttpResponse.getStatusCode();
        loggedClientHttpResponse.getRawStatusCode();
        loggedClientHttpResponse.close();
        assertThat(loggedClientHttpResponse.getStatusText()).isNull();

    }
}
