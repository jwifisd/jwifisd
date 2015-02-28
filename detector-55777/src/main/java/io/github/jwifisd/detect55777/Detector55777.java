package io.github.jwifisd.detect55777;

/*
 * #%L
 * jwifisd-detector-55777
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

import io.github.jwifisd.api.IDetector;
import io.github.jwifisd.api.INotifier;
import io.github.jwifisd.net.LocalNetwork;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Detector55777 implements IDetector {

    boolean isScanning = false;

    private static final Logger LOG = LoggerFactory.getLogger(Detector55777.class);

    @Override
    public void scan(LocalNetwork network, INotifier notifier) throws IOException {
        isScanning = true;
        try (DatagramSocket socket = new DatagramSocket(58255)) {
            socket.setReuseAddress(true);
            socket.setSoTimeout(2000);

            byte[] m = "hallo".getBytes();
            DatagramPacket request = new DatagramPacket(m, m.length, network.getBroadcast(), 55777);
            socket.send(request);
            byte[] buffer = new byte[1000];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);

            int rest;
            long start = System.currentTimeMillis();
            do {
                try {
                    socket.receive(response);
                    createCard(notifier, response);
                } catch (SocketTimeoutException timeout) {
                    return;
                }
                rest = 2000 - (int) (System.currentTimeMillis() - start);
                if (rest > 0) {
                    socket.setSoTimeout(rest);
                }
            } while (rest > 0);
            return;
        } finally {
            isScanning = false;
        }
    }

    @Override
    public boolean isScanning() {
        return isScanning;
    }

    private void createCard(INotifier notifier, DatagramPacket response) {
        try {
            notifier.newCard(new PotentialWifiSDCard(response));
        } catch (Exception e) {
            LOG.error("could not analyse card, probably something else ...");
        }
    }

    @Override
    public void stop() {
    }
}
