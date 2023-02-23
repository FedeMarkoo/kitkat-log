package ar.santandertecnologia.transferencias.kitkat.commons.logger;

import ar.santandertecnologia.transferencias.kitkat.commons.logger.interceptor.LogClientInterceptor;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.interceptor.OutgoingCallsInterceptor;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.model.LoggerData;
import ar.santandertecnologia.transferencias.kitkat.commons.logger.util.LogProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EnableAspectJAutoProxy
@Configuration
@ComponentScan(basePackages = "ar.santandertecnologia.transferencias.kitkat.commons.logger.*")
public class LogConfiguration {
    @Autowired(required = false)
    public void addInterceptor(WebServiceTemplate webServiceTemplate) {
        ClientInterceptor[] interceptors = webServiceTemplate.getInterceptors();
        if (interceptors == null) interceptors = new ClientInterceptor[]{};
        List<ClientInterceptor> list = new ArrayList<>(Arrays.asList(interceptors));
        list.add(new LogClientInterceptor());
        webServiceTemplate.setInterceptors(list.toArray(new ClientInterceptor[0]));
    }

    @Autowired(required = false)
    public void addInterceptor(RestTemplate restTemplate, OutgoingCallsInterceptor outgoingCallsInterceptor) {
        restTemplate.getInterceptors().add(outgoingCallsInterceptor);
    }
    @Autowired
    public void addProperties(LogProperties properties){
        LoggerData.setProperties(properties);
    }
}