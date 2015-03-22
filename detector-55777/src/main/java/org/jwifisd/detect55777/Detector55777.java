package org.jwifisd.detect55777;

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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import org.jwifisd.api.IDetector;
import org.jwifisd.api.INotifier;
import org.jwifisd.net.LocalNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This detector implementation detects cards that use the 55777 udp port as
 * detection method. A hello message is broadcast and as an answer the card
 * information is send back.
 * 
 * @author Richard van Nieuwenhoven
 */
public class Detector55777 implements IDetector {

    /**
     * we do not expect responses that are longer than one kbyte.
     */
    private static final int MAXIMUM_RESPONSE_LENGTH = 1024;

    /**
     * udp port to send the broadcast to.
     */
    private static final int UDP_HELLO_PORT = 55777;

    /**
     * how long to we wait for a response?
     */
    private static final int TIMEOUT_IN_MILLISECONDS = 2000;

    /**
     * the port number where the answer to the broadcast is send.
     */
    private static final int UDP_ANSWER_PORT = 58255;

    /**
     * is scanning in progress?
     */
    private boolean isScanning = false;

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Detector55777.class);

    @Override
    public void scan(LocalNetwork network, INotifier notifier) throws IOException {
        isScanning = true;
        try (DatagramSocket socket = new DatagramSocket(UDP_ANSWER_PORT)) {
            socket.setReuseAddress(true);
            socket.setSoTimeout(TIMEOUT_IN_MILLISECONDS);

            byte[] m = "hallo".getBytes();
            DatagramPacket request = new DatagramPacket(m, m.length, network.getBroadcast(), UDP_HELLO_PORT);
            socket.send(request);
            byte[] buffer = new byte[MAXIMUM_RESPONSE_LENGTH];
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
                rest = TIMEOUT_IN_MILLISECONDS - (int) (System.currentTimeMillis() - start);
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

    /**
     * ok a response was send back try to create a cord from it.
     * 
     * @param notifier
     *            the notifier to report the new card.
     * @param response
     *            the response of the card to create the new potential card
     *            from.
     */
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
