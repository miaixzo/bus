package org.aoju.bus.cache.provider;

import java.util.Map;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface BaseProvider {

    void reqIncr(String pattern, int count);

    void hitIncr(String pattern, int count);

    Map<String, ShootingDO> getShooting();

    void reset(String pattern);

    void resetAll();

    default String summaryName() {
        return "zh".equalsIgnoreCase(System.getProperty("user.language")) ? "全局" : "summary";
    }

    class ShootingDO {

        private long hit;

        private long required;

        private String rate;

        private ShootingDO(long hit, long required, String rate) {
            this.hit = hit;
            this.required = required;
            this.rate = rate;
        }

        public static ShootingDO newInstance(long hit, long required) {
            double rate = (required == 0 ? 0.0 : hit * 100.0 / required);
            String rateStr = String.format("%.1f%s", rate, "%");

            return new ShootingDO(hit, required, rateStr);
        }

        public static ShootingDO mergeShootingDO(ShootingDO do1, ShootingDO do2) {
            long hit = do1.getHit() + do2.getHit();
            long required = do1.getRequired() + do2.getRequired();

            return newInstance(hit, required);
        }

        public long getHit() {
            return hit;
        }

        public long getRequired() {
            return required;
        }

        public String getRate() {
            return rate;
        }
    }
}
