
package org.x2ools.xappsearchlib.tools;

import android.annotation.SuppressLint;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ToPinYinUtils {

    public static String getPinYin(String zhongwen, boolean full) {

        StringBuilder zhongWenPinYin = new StringBuilder();
        char[] chars = zhongwen.toCharArray();

        for (char aChar : chars) {
            String[] pinYin;
            try {
                pinYin = PinyinHelper.toHanyuPinyinStringArray(aChar,
                        getDefaultOutputFormat());
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                badHanyuPinyinOutputFormatCombination.printStackTrace();
                pinYin = null;
            }
            if (pinYin != null) {
                String word = pinYin[0];
                if (full) {
                    zhongWenPinYin.append(word);
                } else {
                    zhongWenPinYin.append(word.charAt(0));
                }
            } else {
                zhongWenPinYin.append(aChar);
            }
        }
        return zhongWenPinYin.toString().toLowerCase();
    }

    private static HanyuPinyinOutputFormat getDefaultOutputFormat() {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_U_AND_COLON);
        return format;
    }

    @SuppressLint("DefaultLocale")
    public static String getPinyinNum(String pinyin) {
        if (pinyin != null && pinyin.length() != 0) {
            int len = pinyin.length();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb.append(getOneNumFromAlpha(pinyin.charAt(i)));
            }
            return sb.toString();
        }
        return null;
    }

    private static String getOneNumFromAlpha(char firstAlpha) {
        if (firstAlpha <= '9' && firstAlpha >= '0') return String.valueOf(firstAlpha);
        switch (firstAlpha) {
            case 'a':
            case 'b':
            case 'c':
                return "2";
            case 'd':
            case 'e':
            case 'f':
                return "3";
            case 'g':
            case 'h':
            case 'i':
                return "4";
            case 'j':
            case 'k':
            case 'l':
                return "5";
            case 'm':
            case 'n':
            case 'o':
                return "6";
            case 'p':
            case 'q':
            case 'r':
            case 's':
                return "7";
            case 't':
            case 'u':
            case 'v':
                return "8";
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                return "9";
            default:
                return "0";
        }
    }

}
