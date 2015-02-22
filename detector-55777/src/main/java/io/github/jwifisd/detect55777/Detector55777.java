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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.IDetector;
import io.github.jwifisd.net.LocalNetwork;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Detector55777 implements IDetector {

    private static final Logger LOG = LoggerFactory.getLogger(Detector55777.class);

    @Override
    public List<ICard> scan(LocalNetwork network) throws IOException {
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
            List<ICard> result = new ArrayList<>();
            do {
                socket.receive(response);
                createCard(result, response);
                rest = 2000 - (int) (System.currentTimeMillis() - start);
                socket.setSoTimeout(rest);
            } while (rest > 0);
            return result;
        }
    }

    private void createCard(List<ICard> result, DatagramPacket response) {
        try {
            result.add(new PotentialWifiSDCard(response));
        } catch (Exception e) {
            LOG.error("could not analyse card, probably something else ...");
        }
    }
}
