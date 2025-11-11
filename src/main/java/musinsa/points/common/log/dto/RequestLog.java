package musinsa.points.common.log.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RequestLog {

    private LogType logType;
    private String method;
    private String uri;
    private String body;
}
