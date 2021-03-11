package center.exception;

public enum ErrorCode implements BaseErrorInterface {

    OK(0, "OK"),
    SERVICE_ERROR(1, "出错了"),

    SERVICE_TASK_CREATE_NAME_DUP(101, "新建任务名重复"),
    SERVICE_TASK_CREATE_URL_DUP(103, "新建任务start_url重复"),
    SERVICE_TASK_CREATE_MISS_PARAM(105, "缺少必要的参数"),
    SERVICE_TASK_CRON_INVALID(107, "cron不合法"),
    SERVICE_TASK_NOT_EXIST(109, "任务不存在"),
    SERVICE_TASK_URL_INVALID(111, "url不合法"),
    SERVICE_TASK_PAPER_URL_INVALID(113, "电子报初始url 不符合模板格式 {YYYY} {MM} {dd}"),
    SERVICE_PARSER_MISS_FIELD_NAME(115, "额外属性名字不能为空"),
    SERVICE_PARSER_FIELD_LOCATOR_MISS(117, "解析器缺少必须定位属性"),
    SERVICE_PARSER_CUSTOM_ERROR(118, "自定义解析器设定错误"),
    SERVICE_PARSER_MISS_FIELD_VALUE(119, "额外属性缺少具体值"),
    SERVICE_PARSER_FIELD_DUP(121, "额外属性中含有重复名字"),
    SERVICE_PARSER_NOT_EXIST(123, "解析器数据不存在"),
    SERVICE_PARSER_MISS_FIELD_TYPE(125, "额外字段的属性类型不能为空"),

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
