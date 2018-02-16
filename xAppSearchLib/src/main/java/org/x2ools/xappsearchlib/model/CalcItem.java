package org.x2ools.xappsearchlib.model;

import java.text.NumberFormat;

/**
 * @User zhoubinjia
 * @Date 2018/02/15
 */
public class CalcItem extends SearchItem {

    public CalcItem(double result) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(3);
        setName(format.format(result));
    }
}
