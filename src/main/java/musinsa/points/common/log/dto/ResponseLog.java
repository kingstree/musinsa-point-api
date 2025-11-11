package musinsa.points.common.log.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResponseLog {

    private LogType logType;
    private String uri;
    private int status;
    private String body;
}
