package musinsa.points.common.log.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ErrorLog {

    private LogType logType;
    private int status;
    private String message;
    private String type;
    private String customCode;
    private String stackTrace;
}
