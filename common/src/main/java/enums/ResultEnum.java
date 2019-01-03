package enums;

/**
 * Author : moon
 * Date  : 2018/12/28 10:52
 * Description : Enum for
 */
public enum ResultEnum {

    SUCCESS(200, "成功"),
    FAIL(500, "失败"),
    MISS(400, "参数缺失"),
    ;

    private int code;
    private String msg;

    ResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
