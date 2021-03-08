package center.exception;

public enum ErrorCode implements BaseErrorInterface {

    OK(0, "OK"),
    SERVICE_ERROR(1, "出错了"),

    SERVICE_TASK_CREATE_NAME_DUP(101, "新建任务名重复"),
    SERVICE_TASK_CREATE_URL_DUP(102, "新建任务start_url重复"),
    SERVICE_TASK_CREATE_MISS_PARAM(103, "缺少必要的参数"),
    SERVICE_TASK_CRON_INVALID(103, "cron不合法"),
    SERVICE_TASK_NOT_EXIST(104, "任务不存在"),
    SERVICE_TASK_URL_INVALID(105, "url不合法"),
    SERVICE_PARSER_MISS_FIELD_NAME(106, "额外属性缺少name"),
    SERVICE_PARSER_MISS_MUST_FIELD(106, "解析器缺少必须属性"),
    SERVICE_PARSER_MISS_FIELD_VALUE(107, "额外属性缺少具体值"),
    SERVICE_PARSER_FIELD_DUP(108, "额外属性中有重复name"),
    SERVICE_PARSER_NOT_EXIST(109, "解析器数据不存在"),

    SERVICE_DOC_MISS_TASK_ID(201, "缺少taskId");


    private Integer code;
    private String msg;

    ErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
