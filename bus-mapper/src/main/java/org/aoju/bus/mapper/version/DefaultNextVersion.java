package org.aoju.bus.mapper.version;


public class DefaultNextVersion implements NextVersion {

    @Override
    public Object nextVersion(Object current) throws VersionException {
        if (current == null) {
            throw new VersionException("当前版本号为空!");
        }
        if (current instanceof Integer) {
            return (Integer) current + 1;
        } else if (current instanceof Long) {
            return (Long) current + 1L;
        } else {
            throw new VersionException("默认的 NextVersion 只支持 Integer 和 Long 类型的版本号，如果有需要请自行扩展!");
        }
    }

}
