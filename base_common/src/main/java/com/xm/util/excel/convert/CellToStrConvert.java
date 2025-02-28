package com.xm.util.excel.convert;

/**
 * excel值转字符串时使用
 * @param <V>
 */
public interface CellToStrConvert<V> {
    String convert(V value);
}
