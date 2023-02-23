package ar.santandertecnologia.transferencias.kitkat.commons.logger;

import org.springframework.ws.transport.http.HttpComponentsConnection;

import java.net.URI;
import java.net.URISyntaxException;

public class Conecction extends HttpComponentsConnection {
    public Conecction(org.apache.http.client.HttpClient httpClient, org.apache.http.client.methods.HttpPost httpPost, org.apache.http.protocol.HttpContext httpContext) {
        super(httpClient, httpPost, httpContext);
    }

    @Override
    public URI getUri() throws URISyntaxException {
        return new URI("uri");
    }
}
