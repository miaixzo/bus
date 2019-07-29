package org.aoju.bus.mapper.version;


public class VersionUtil {

    /**
     * 获取下一个版本
     *
     * @param nextVersionClass
     * @param current
     * @return
     * @throws VersionException
     */
    public static Object nextVersion(String nextVersionClass, Object current) throws VersionException {
        try {
            NextVersion nextVersion = (NextVersion) Class.forName(nextVersionClass).newInstance();
            return nextVersion.nextVersion(current);
        } catch (Exception e) {
            throw new VersionException("获取下一个版本号失败!", e);
        }
    }

}
