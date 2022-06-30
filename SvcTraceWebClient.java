package cu.datys.bim.webclients.trace;

import cu.datys.bim.webclients.auth.SecuredWebClient;
import cu.datys.bim.webclients.auth.dto.LoginRequestDTO;
import cu.datys.bim.webclients.trace.dto.TraceDTO;
import cu.datys.bim.webclients.trace.dto.TraceTypeDTO;
import cu.datys.domain.bim.repository.auth.ActiveTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
public class SvcTraceWebClient extends SecuredWebClient {

    public SvcTraceWebClient(String svcName,
                             String baseUrl,
                             RestTemplate restTemplate,
                             String authEndpoint,
                             ActiveTokenRepository activeTokenRepository) {
        super(svcName, baseUrl, restTemplate, authEndpoint, activeTokenRepository);
    }

    public Optional<String> insertTrace(LoginRequestDTO login, TraceDTO dto) {
        String uri = String.format("%s%s", getBaseUrl(), "InsertTrace");
        return securedExchangePOST(login, uri, HttpMethod.POST, dto,
                new ParameterizedTypeReference<String>() {
                });
    }

    public Optional<TraceTypeDTO> getTraceType(LoginRequestDTO login) {
        String uri = String.format("%s%s", getBaseUrl(), "getTraceTypes");
//        //////log.info("Web client request for: {} {}","GET", uri);
        return securedExchange(login, uri, HttpMethod.GET,
                new ParameterizedTypeReference<TraceTypeDTO>() {
                });
    }
}
