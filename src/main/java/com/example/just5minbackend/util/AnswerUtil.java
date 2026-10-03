package com.example.just5minbackend.util;

import java.util.Arrays;

/**
 * 答案规范化工具。
 * <p>统一规则：去掉非字母字符 → 转大写 → 去重 → 字母升序。
 * 用于「用户提交答案」判分与「题库导入答案」入库，避免 "BA" 与 "AB" 被判为不同。</p>
 */
public final class AnswerUtil {

    private AnswerUtil() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String letters = raw.replaceAll("[^A-Za-z]", "").toUpperCase();
        if (letters.isEmpty()) {
            return "";
        }
        char[] chars = letters.toCharArray();
        Arrays.sort(chars);
        StringBuilder sb = new StringBuilder(chars.length);
        for (char c : chars) {
            if (sb.indexOf(String.valueOf(c)) < 0) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
