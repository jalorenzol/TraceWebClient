package cu.datys.bim.webclients.trace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraceDataDTO {
    private String type;
    private String process;
    private String action;
    private String host;
    private String user;
}
