package com.pax.abl.core.ipacker;

/**
 * 打包模块监听器
 * 
 * @author Steven.W
 * 
 */
public interface PackListener {
    /**
     * 计算mac
     * 
     * @param data
     * @return mac值
     */
    public byte[] onCalcMac(byte[] data);

    /**
     * 磁道加密
     * 
     * @param track
     * @return
     */
    public byte[] onEncTrack(byte[] track);
}
