package vo;

import enums.ResultEnum;
import exception.MyException;
import lombok.Data;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.io.Serializable;

/**
 * Author : moon
 * Date  : 2018/12/28 10:55
 * Description : Class for
 */
@Data
public class ResultData implements Serializable {

    private Integer code;
    private String msg;
    private Object data;

    public ResultData(){}

    private ResultData(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static ResultData result(Object o) {
        return new ResultData(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMsg(), o);
    }

    public static ResultData result(ResultEnum resultEnum) {
        return new ResultData(resultEnum.getCode(), resultEnum.getMsg(), null);
    }

    public static ResultData result(ResultEnum resultEnum, Object o) {
        return new ResultData(resultEnum.getCode(), resultEnum.getMsg(), o);
    }

    public static ResultData result(Integer code, String msg) {
        return new ResultData(code, msg, null);
    }

    public static ResultData result(Integer code, String msg, Object o) {
        return new ResultData(code, msg, o);
    }

    /*************************************************************************
     *                            错误返回                                   *
     ************************************************************************/

    public static ResultData error(MyException e) {
        return new ResultData(e.getCode(), e.getMessage(), null);
    }

    public static ResultData error(BindingResult result) {
        FieldError fieldError = result.getFieldError();
        return new ResultData(ResultEnum.FAIL.getCode(), fieldError.getDefaultMessage(), null);
    }
}
