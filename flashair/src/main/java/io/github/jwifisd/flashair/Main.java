package io.github.jwifisd.flashair;

/*
 * #%L
 * jwifisd-flashair
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

import io.github.jwifisd.mdns.DNSMessage;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;

public class Main {

    static String query = "000000000001000000000000" + Hex.encodeHexString("flashair".getBytes(Charset.forName("UTF-8"))) + "056c6f63616c0000010001";

    public static void main(String[] args) throws Exception {
        MulticastSocket socket = new MulticastSocket(5353);
        socket.setReuseAddress(true);
        socket.setSoTimeout(5000);

        NetworkInterface nic = NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.0.11"));
        socket.joinGroup(new InetSocketAddress("224.0.0.251", 5353), nic);

        byte[] qd = Hex.decodeHex(query.toCharArray());
        DatagramPacket q = new DatagramPacket(qd, qd.length, new InetSocketAddress("192.168.0.255", 5353));

        socket.send(q);
        byte[] buffer = new byte[1000];

        while (true) {
            try {
                DatagramPacket data = new DatagramPacket(buffer, buffer.length);
                socket.receive(data);

                byte[] packet = new byte[data.getLength()];
                packet = Arrays.copyOfRange(data.getData(), data.getOffset(), data.getLength() + data.getOffset());
                DNSMessage message = new DNSMessage();

                message.read(new ByteArrayInputStream(packet));

                if (message.getDnsHeader().isResponse() && message.getPayload().getQuestion(0).getFullQualifiedDomainName().indexOf("flashair") >= 0) {
                    System.out.println(message.getPayload().getAnswer(0).getPayload());
                }
                System.out.println(Hex.encodeHexString(packet));
                System.out.println(message);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
