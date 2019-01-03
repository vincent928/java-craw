package util;

import org.apache.commons.lang3.RandomUtils;

import java.util.UUID;

/**
 * Author : moon
 * Date  : 2018/11/21 13:40
 * Description : Class for 字符码工具类
 */
public class CodeUtil {

    private static final String[] charArray = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
            "u", "v", "w", "x", "y", "z",
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"
    };

    /**
     * 获取 6位 MailCode
     *
     * @return
     */
    public static String getMailCode() {
        StringBuffer bf = new StringBuffer();
        for (int i = 0; i < 6; i++) {
            bf.append(charArray[RandomUtils.nextInt(0, charArray.length + 1)]);
        }
        return bf.toString();
    }

    /**
     * 生成4位数验证码
     *
     * @return
     */
    public static String getPhoneCode() {
        return String.valueOf(RandomUtils.nextInt(1000, 10000));
    }

    /**
     * 生成验证码(指定位数)
     *
     * @return
     */
    public static String getPhoneCode(int size) {
        String code = String.valueOf(RandomUtils.nextInt((int) Math.pow(10, size - 1) - 1, (int) Math.pow(10, size)));
        return code;
    }

    /**
     * 唯一UUID生成
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
