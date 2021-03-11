package center.exception;

import com.entity.ResponseVO;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
public class MyExceptionHandler {


    /**
     * 这里统一处理返回给前端的异常
     *
     * @return
     */
    @ExceptionHandler(value = WebException.class)
    @ResponseBody
    public ResponseVO webExceptionHandler(WebException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return new ResponseVO(errorCode.getCode(), errorCode.getMsg());
    }
}
