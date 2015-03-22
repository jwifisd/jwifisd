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
/**
 * This detector will detect eyefi cards pressent in the subnet. No not realy,
 * it starts a http server on the eyefi port and waits for the card to send a
 * soap message to it. So in this case we wait for the card to report itself.
 * 
 * @author Richard van Nieuwenhoven
 */
public class EyeFiDetector implements IDetector {

    /**
     * Because there can only be one server in the jvm this must be a singleton.
     * 
     * @author Richard van Nieuwenhoven
     */
    private static final class RealEyeFiDetector implements IDetector {

        /**
         * is scanning in progress (server started).
         */
        private boolean isScanning = false;

        /**
         * the eyefi http server.
         */
        private EyeFiServer eyeFiServer;

        /**
         * private constructor so nobody else can start the server.
         */
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

    /**
     * the singleton detector that controls the server.
     */
    private static final RealEyeFiDetector SINGLETON = new RealEyeFiDetector();

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
