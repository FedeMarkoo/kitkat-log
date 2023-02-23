package ar.santandertecnologia.transferencias.kitkat.commons.logger;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;


@Configuration
public class WebServiceTemplateImpl {


    @Bean("WebServiceTemplate")
    public WebServiceTemplate prismaWebServiceTemplate() {
        return Mockito.mock(WebServiceTemplate.class);
    }

}