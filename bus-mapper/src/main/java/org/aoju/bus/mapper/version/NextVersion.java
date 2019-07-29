package org.aoju.bus.mapper.version;


public interface NextVersion<T> {

    /**
     * 返回下一个版本
     *
     * @param current
     * @return
     */
    T nextVersion(T current) throws VersionException;

}
