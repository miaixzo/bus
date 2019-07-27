package org.aoju.bus.spring.limiter;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.spring.annotation.EnableLimiter;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;

import java.util.ArrayList;
import java.util.List;

public class LimiterConfigurationSelector extends AdviceModeImportSelector<EnableLimiter> {

    @Override
    protected String[] selectImports(AdviceMode adviceMode) {
        Logger.info("limiter start success...");
        switch (adviceMode) {
            case PROXY:
                return getProxyImports();
            case ASPECTJ:
                throw new RuntimeException("NotImplemented");
            default:
                return null;
        }
    }

    private String[] getProxyImports() {
        List<String> list = new ArrayList<>();
        list.add(AutoProxyRegistrar.class.getName());
        list.add(LimiterConfiguration.class.getName());
        return StringUtils.toStringArray(list);
    }

}
