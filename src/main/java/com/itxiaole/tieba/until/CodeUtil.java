package com.itxiaole.tieba.until;

import java.util.Random;

public class CodeUtil {
    public static String generateCode() {
        // nextInt(1000000) 会生成 0 ~ 999999 之间的数字
        // %06d 表示格式化为 6 位数字，不足 6 位在前面补 0
        return String.format("%06d", new Random().nextInt(1000000));
    }
}