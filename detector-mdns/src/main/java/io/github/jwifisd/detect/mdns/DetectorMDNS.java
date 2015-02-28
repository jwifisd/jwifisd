package io.github.jwifisd.detect.mdns;

/*
 * #%L
 * jwifisd-detector-mdns
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
import io.github.jwifisd.mdns.DNSMessage;
import io.github.jwifisd.net.LocalNetwork;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectorMDNS implements IDetector {

    boolean isScanning = false;

    private static final Logger LOG = LoggerFactory.getLogger(DetectorMDNS.class);

    @Override
    public void scan(LocalNetwork network, INotifier notifier) throws IOException {
        isScanning = true;
        String searchNames = notifier.getProperty("mdns.names");
        String[] names = searchNames.split(",");
        try {
            MulticastSocket socket = new MulticastSocket(5353);
            socket.setReuseAddress(true);
            socket.setSoTimeout(2000);

            NetworkInterface nic = NetworkInterface.getByInetAddress(network.getInterfaceIp());
            socket.joinGroup(new InetSocketAddress("224.0.0.251", 5353), nic);

            byte[] qd = createDNSQuery(names);

            DatagramPacket q = new DatagramPacket(qd, qd.length, new InetSocketAddress(network.getBroadcast(), 5353));

            socket.send(q);
            byte[] buffer = new byte[1000];

            int rest;
            long start = System.currentTimeMillis();
            do {
                DNSMessage message = receiveDNSMessage(socket, buffer);
                if (message != null) {
                    for (int index = 0; index < message.getDnsHeader().getNumberOfEntriesInQuestionSection(); index++) {
                        try {
                            String fullQualifiedName = message.getPayload().getQuestion(0).getFullQualifiedDomainName();
                            InetAddress ip = (InetAddress) message.getPayload().getAnswer(index).getPayload();
                            notifier.newCard(new PotentialWifiSDCard(fullQualifiedName, ip));
                        } catch (Exception e) {
                            LOG.warn("did not undestand dns message {}", message);
                        }
                    }
                }
                rest = 2000 - (int) (System.currentTimeMillis() - start);
                if (rest > 0) {
                    socket.setSoTimeout(rest);
                }
            } while (rest > 0);
        } finally {
            isScanning = false;
        }
    }

    @Override
    public boolean isScanning() {
        return isScanning;
    }

    protected DNSMessage receiveDNSMessage(MulticastSocket socket, byte[] buffer) throws IOException {
        try {
            DatagramPacket data = new DatagramPacket(buffer, buffer.length);
            socket.receive(data);

            byte[] packet = new byte[data.getLength()];
            packet = Arrays.copyOfRange(data.getData(), data.getOffset(), data.getLength() + data.getOffset());
            DNSMessage message = new DNSMessage();
            message.read(new ByteArrayInputStream(packet));
            return message;
        } catch (SocketTimeoutException timeout) {
            return null;
        } catch (Exception e) {
            LOG.warn("did not understand dns message, skipping it", e);
            return null;
        }
    }

    protected byte[] createDNSQuery(String[] names) throws IOException {

        DNSMessage message = new DNSMessage();
        message.getDnsHeader().setResponse(false);
        for (String name : names) {
            message.getPayload().addQuestion(name + ".local");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.write(out);
        byte[] qd = out.toByteArray();
        return qd;
    }

    @Override
    public void stop() {
    }

}
