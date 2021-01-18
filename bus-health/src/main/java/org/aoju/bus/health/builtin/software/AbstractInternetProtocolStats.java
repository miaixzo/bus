package org.aoju.bus.health.builtin.software;

import org.aoju.bus.health.unix.NetStat;

import java.util.Collections;
import java.util.List;

public abstract class AbstractInternetProtocolStats implements InternetProtocolStats {

    public AbstractInternetProtocolStats() {
        super();
    }

    @Override
    public List<IPConnection> getConnections() {
        return Collections.unmodifiableList(NetStat.queryNetstat());
    }

}