package cu.datys.bim.webclients.trace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraceDTO {

    private TraceDataDTO data;
    private TraceDetailDTO detail;

}
