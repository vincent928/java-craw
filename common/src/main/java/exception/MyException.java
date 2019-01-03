package exception;


import enums.ResultEnum;

/**
 * Author : moon
 * Date  : 2018/12/28 10:50
 * Description : Class for
 */
public class MyException extends RuntimeException {

    private int code;


    public MyException() {
        super();
    }

    public MyException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.code = resultEnum.getCode();
    }

    public MyException(int code, String msg) {
        super(msg);
        this.code = code;
    }


    public int getCode() {
        return code;
    }
}
