package cn.com.sina.alan.utils;

/**
 * Created by whf on 8/27/16.
 */
@FunctionalInterface
public interface AlanTask<T_RETURN> {
    T_RETURN execute();
}
