package org.jwifisd.eyefi;

import java.io.IOException;

import org.jwifisd.api.IDetector;
import org.jwifisd.api.INotifier;
import org.jwifisd.net.LocalNetwork;

/*
 * #%L
 * jwifisd-eyefi
 * %%
 * Copyright (C) 2012 - 2015 jwifisd
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

public class EyeFiDetector implements IDetector {

    private static class RealEyeFiDetector implements IDetector {

        boolean isScanning = false;

        private EyeFiServer eyeFiServer;

        private RealEyeFiDetector() {
        }

        @Override
        public void scan(LocalNetwork network, INotifier notifier) throws IOException {
            isScanning = true;
            eyeFiServer = new EyeFiServer(notifier);
            eyeFiServer.start();
        }

        @Override
        public boolean isScanning() {
            return isScanning;
        }

        @Override
        public void stop() {
            eyeFiServer.stop();
            isScanning = false;
        }
    }

    private static RealEyeFiDetector SINGLETON = new RealEyeFiDetector();

    @Override
    public void scan(LocalNetwork network, INotifier notifier) throws IOException {
        SINGLETON.scan(network, notifier);
    }

    @Override
    public boolean isScanning() {
        return SINGLETON.isScanning();
    }

    @Override
    public void stop() {
        SINGLETON.stop();
    }
}
