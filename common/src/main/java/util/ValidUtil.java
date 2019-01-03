package util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Author : moon
 * Date  : 2018/12/28 11:12
 * Description : Class for
 */
public class ValidUtil {

    private static final String REGEX_PHONE = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$";
    private static final String REGEX_MAIL = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";

    /**
     * valid验证返回错误信息
     *
     * @param result
     * @return
     */
    public static String getMsg(BindingResult result) {
        FieldError fieldError = result.getFieldError();
        return fieldError.getDefaultMessage();
    }

    /**
     * mail验证
     *
     * @param mail
     * @return
     */
    public static boolean isMail(String mail) {
        if (stringIsBlank(mail))
            return false;
        Pattern pattern = Pattern.compile(REGEX_MAIL);
        return pattern.matcher(mail).matches();
    }

    public static boolean isPhone(String phone) {
        if (stringIsBlank(phone))
            return false;
        Pattern pattern = Pattern.compile(REGEX_PHONE);
        return pattern.matcher(phone).matches();
    }

    /**
     * Map校验
     *
     * @param map
     * @return
     */
    public static boolean mapIsBlank(Map map) {
        return map == null || map.isEmpty();
    }

    /**
     * list校验
     *
     * @param list
     * @return
     */
    public static boolean listIsBlank(List list) {
        return list == null || list.isEmpty();
    }

    public static boolean stringIsBlank(String... args) {
        return StringUtils.isAnyBlank(args);
    }


}
