package cu.datys.bim.webclients;

import cu.datys.bim.common.constants.BussinesConstants;
import cu.datys.bim.common.constants.ConfigPramConstants;
import cu.datys.bim.common.constants.SvcNamesConstants;
import cu.datys.bim.common.constants.TraceConstants;
import cu.datys.bim.common.credentials.AdminCredentials;
import cu.datys.bim.common.utils.UTCTimeUtil;
import cu.datys.bim.webclients.auth.WCLogin;
import cu.datys.bim.webclients.trace.SvcTraceWebClient;
import cu.datys.bim.webclients.trace.dto.ObjectDTO;
import cu.datys.bim.webclients.trace.dto.TraceDTO;
import cu.datys.bim.webclients.trace.dto.TraceDataDTO;
import cu.datys.bim.webclients.trace.dto.TraceDetailDTO;
import cu.datys.domain.bim.entity.config.SvcHost;
import cu.datys.domain.bim.repository.config.SvcHostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TraceService {

    private final SvcTraceWebClient svcTraceWebClient;
    private SvcHostRepository svcHostRepository;

    public TraceService(WebClientFactory webClientFactory, SvcHostRepository svcHostRepository) {
        this.svcTraceWebClient = webClientFactory
                .build(SvcNamesConstants.SVC_TRACE, SvcTraceWebClient.class)
                .orElse(null);
        this.svcHostRepository = svcHostRepository;
    }

    /**
     * El cuerpo de la traza esta estructurado por la data(TraceDataDTO) y el detail(TraceDetailDTO).
     * La traza utiliza la dir del Weblogic en la tabla de SVC_HOST
     */
    public void createTrace(Map<String, Object> body, String type, String process, String action) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            List<SvcHost> ipAddress = svcHostRepository.findByHostName(ConfigPramConstants.DEPLOY_HOST_DEFVALUE);
            String host = ipAddress.get(0).getHostUrl().split("//")[1].split(":")[0];
            TraceDetailDTO traceDetailDTO = new TraceDetailDTO(createTraceObject(body), body.get("objecttype").toString());
            TraceDataDTO traceDataDTO = new TraceDataDTO(type, process, action, host, auth.getName());
            TraceDTO traceDTO = new TraceDTO(traceDataDTO, traceDetailDTO);
            Optional<String> resp = svcTraceWebClient.insertTrace
                    (WCLogin.of(AdminCredentials.buildExternalAuthService()), traceDTO);
        } catch (Exception ex) {
           log.error(ex.getMessage());
        }
    }

    /**
     * Se obtienen la fecha y hora actual del servidor para incorporarlo al detail de la traza
     */
    public ObjectDTO createTraceObject(Map<String, Object> body) {
        ObjectDTO result = new ObjectDTO();
        result.setFecha(UTCTimeUtil.getYearMonthDay());
        result.setHora(UTCTimeUtil.getActualHour().toString());
//        for (Map.Entry<String, Object> entry : body.entrySet()
//        ) {
//            if (entry.getKey().equals("process")) {
//                result.setProcess(entry.getValue().toString());
//            }
//            if (entry.getKey().equals("action")) {
//                result.setAction(entry.getValue().toString());
//            }
//        }
        return result;
    }
    /**
     * Creación del body como parte del cuerpo de detail para la traza de servicio
     * */

    public Map<String, Object> createMapToTraceService(String objecttype, String level, String message) {
        Map<String, Object> trace = new HashMap<>();
        trace.put("level", level);
        trace.put("message", message);
        trace.put("objecttype", objecttype);
        return trace;
    }


    /**
     * Creación de traza tipo servicio usando constantes predeterminadas y los datos del servicio para su creación
     */
    public void createServiceTrace(String service, String value, String state) {
        Map<String, Object> object = createMapToTraceService(TraceConstants.OBJECT_TYPE_SERVICE, TraceConstants.LEVEL_CONEXION, String.format(state, service));
        if (service.equals(BussinesConstants.CONFIGURATION_SERVICE_NAME)) {
            createTrace(object, TraceConstants.TYPE_SERVICIO, String.format(TraceConstants.PROCESS_SERVICE_VARIABLE_CONSUM, value), TraceConstants.ACTION_TYPE_CONSUM_VARIBLE);
        } else if (service.equals(BussinesConstants.NOMENCLATOR_SERVICE_NAME)) {
            createTrace(object, TraceConstants.TYPE_SERVICIO, String.format(TraceConstants.PROCESS_SERVICE_NOMENCLATOR_CONSUM, value), TraceConstants.ACTION_TYPE_CONSUM_NOMENCLATOR);
        }
    }

    /**
     * Creación de traza tipo Operaciones usando constantes predeterminadas y los datos de la operación para su creación
     */
    public void createOperationVesselsTrace(String objecttype, String mmsi, String resultado) {
        Map<String, Object> object = new HashMap<>();
        object.put("objecttype", objecttype);
        object.put("msi", mmsi);
        object.put("resultado", resultado);
        if (resultado.equals(TraceConstants.MESSAGE_CIRCULATE_RESULT_POSITIVE)) {
            createTrace(object, TraceConstants.TYPE_OPERACION, String.format(TraceConstants.PROCESS_OPERATION_CIRCULATION, mmsi), TraceConstants.ACTION_TYPE_CONSUM_CIRCULATION);
        } else if (resultado.equals(TraceConstants.MESSAGE_NEW_VESSEL)) {
            createTrace(object, TraceConstants.TYPE_OPERACION, String.format(TraceConstants.PROCESS_OPERATION_NEW_VESSEL, mmsi), TraceConstants.ACTION_TYPE_REGISTER_VESSEL);
        }
    }


}
