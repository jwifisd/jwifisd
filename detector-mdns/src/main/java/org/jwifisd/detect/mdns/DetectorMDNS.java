package org.jwifisd.detect.mdns;

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

import org.jwifisd.api.IDetector;
import org.jwifisd.api.INotifier;
import org.jwifisd.mdns.DNSMessage;
import org.jwifisd.net.LocalNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This detector sends dns broadcasts, and scans mdns messages for potential
 * cards.
 * 
 * @author Richard van Nieuwenhoven
 */
public class DetectorMDNS implements IDetector {

    /**
     * bit mask for a byte (convert a unsigned byte to int).
     */
    private static final int BYTE_BIT_MASK = 0xFF;

    /**
     * how long to we wait for a response?
     */
    private static final int TIMEOUT_IN_MILLISECONDS = 2000;

    /**
     * we do not expect responses that are longer than one kbyte.
     */
    private static final int MAXIMUM_MDNS_MESSAGE_LENGTH = 1024;

    /**
     * multi cast group where mdns messages are send and received.
     */
    private static final String MDNS_MULTI_CASE_GROUP = "224.0.0.251";

    /**
     * standard MDNS port where messages are send and received.
     */
    private static final int MDNS_UDP_PORT = 5353;

    /**
     * is the scanning in progress?
     */
    private boolean isScanning = false;

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DetectorMDNS.class);

    @Override
    public void scan(LocalNetwork network, INotifier notifier) throws IOException {
        isScanning = true;
        String searchNames = notifier.getProperty("mdns.names");
        String[] names = searchNames.split(",");
        try {
            MulticastSocket socket = new MulticastSocket(MDNS_UDP_PORT);
            socket.setReuseAddress(true);
            socket.setSoTimeout(TIMEOUT_IN_MILLISECONDS);

            NetworkInterface nic = NetworkInterface.getByInetAddress(network.getInterfaceIp());
            socket.joinGroup(new InetSocketAddress(MDNS_MULTI_CASE_GROUP, MDNS_UDP_PORT), nic);

            byte[] qd = createDNSQuery(names);

            DatagramPacket q = new DatagramPacket(qd, qd.length, new InetSocketAddress(network.getBroadcast(), MDNS_UDP_PORT));

            socket.send(q);
            byte[] buffer = new byte[MAXIMUM_MDNS_MESSAGE_LENGTH];

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
                rest = TIMEOUT_IN_MILLISECONDS - (int) (System.currentTimeMillis() - start);
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

    /**
     * listen for mdns messages in the mdns group and when a message is received
     * parse it to a DNSMessage object.
     * 
     * @param socket
     *            the mdns group multicast socket.
     * @param buffer
     *            the buffer for the message.
     * @return the DNSMessage or null if a timeout was received.
     * @throws IOException
     *             if something goes wrong.
     */
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
            StringBuffer message = new StringBuffer();
            for (int index = 0; index < buffer.length; index++) {
                String hexString = Integer.toHexString(buffer[index] & BYTE_BIT_MASK);
                if (hexString.length() < 2) {
                    message.append('0');
                }
                message.append(hexString);
            }
            LOG.warn("did not understand dns message, skipping it " + message, e);
            return null;
        }
    }

    /**
     * create a mdns query message and ask specially for the names specified.
     * 
     * @param names
     *            the names to resolve
     * @return the dns message converted to bytes.
     * @throws IOException
     *             if something goes wrong.
     */
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
