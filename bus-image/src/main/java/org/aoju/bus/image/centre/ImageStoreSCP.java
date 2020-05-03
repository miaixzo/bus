package org.aoju.bus.image.centre;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.image.Centre;
import org.aoju.bus.image.Rollers;
import org.aoju.bus.image.plugin.StoreSCP;

public class ImageStoreSCP implements Centre {

    private StoreSCP storeSCP;
    private DeviceService deviceService;
    private Rollers rollers;

    @Override
    public boolean isRunning() {
        return storeSCP.getConnection().isListening();
    }

    @Override
    public void start() throws InstrumentException {

    }

    @Override
    public void stop() throws InstrumentException {

    }

}
