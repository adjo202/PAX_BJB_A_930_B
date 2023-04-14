package com.pax.abl.core.ipacker;

/**
 * 打包模块抽象接口
 * 
 * @author Steven.W
 * 
 * @param <T>
 * @param <O>
 */
public interface IPacker<T, O> {
    /**
     * 打包接口
     * 
     * @param t
     * @return
     */
    public O pack(T t);

    /**
     * 解包接口
     * 
     * @param t
     * @return
     */
    public int unpack(T t, O o);
    int dumpIso(O o);
}
