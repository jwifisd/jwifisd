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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DetectorMDNS implements IDetector {

    @Override
    public List<ICard> scan(LocalNetwork network) throws IOException {

        MulticastSocket socket = new MulticastSocket(5353);
        socket.setReuseAddress(true);
        socket.setSoTimeout(2000);

        NetworkInterface nic = NetworkInterface.getByInetAddress(network.getInterfaceIp());
        socket.joinGroup(new InetSocketAddress("224.0.0.251", 5353), nic);

        byte[] qd = createDNSQuery("flashair.local"); 
        
        DatagramPacket q = new DatagramPacket(qd, qd.length, new InetSocketAddress(network.getBroadcast(), 5353));

        socket.send(q);
        byte[] buffer = new byte[1000];

        
        int rest;
        long start = System.currentTimeMillis();
        List<ICard> result = new ArrayList<>();
        do {
            DatagramPacket data = new DatagramPacket(buffer, buffer.length);
            socket.receive(data);

            byte[] packet = new byte[data.getLength()];
            packet = Arrays.copyOfRange(data.getData(), data.getOffset(), data.getLength() + data.getOffset());
            DNSMessage message = new DNSMessage();

            message.read(new ByteArrayInputStream(packet));

            if (message.getDnsHeader().isResponse() && message.getPayload().getQuestion(0).getFullQualifiedDomainName().indexOf("flashair") >= 0) {
                System.out.println(message.getPayload().getAnswer(0).getPayload());
            }

            rest = 2000 - (int) (System.currentTimeMillis() - start);
            socket.setSoTimeout(rest);
        } while (rest > 0);
        return result;
        
        
    
    }

    protected byte[] createDNSQuery(String fullQualifiedDomainName) throws IOException {
        DNSMessage message = new DNSMessage();
        message.getDnsHeader().setResponse(false);
        message.getPayload().addQuestion(fullQualifiedDomainName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.write(out);
        byte[] qd = out.toByteArray();
        return qd;
    }

}
