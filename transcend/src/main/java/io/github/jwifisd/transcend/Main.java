package io.github.jwifisd.transcend;

/*
 * #%L
 * jwifisd-transcend
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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

public class Main {

    public static void main(String args[]) throws SocketException {
        DatagramSocket aSocket = new DatagramSocket(58255);
        aSocket.setSoTimeout(2000);
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            displayInterfaceInformation(aSocket, netint);
        }

        aSocket.close();
    }

    static void displayInterfaceInformation(DatagramSocket aSocket, NetworkInterface netint) throws SocketException {

        System.out.printf("Display name: %s\n", netint.getDisplayName());
        System.out.printf("Name: %s\n", netint.getName());

        if (netint.isLoopback()) {
            System.out.println("loopback skip that one");
            return;
        }
        int networkPrefixLength = -1;
        for (InterfaceAddress adress : netint.getInterfaceAddresses()) {
            if (adress.getAddress().getAddress().length == 4) {
                networkPrefixLength = Math.max(networkPrefixLength, adress.getNetworkPrefixLength());
                System.out.println("prefix " + networkPrefixLength);
            }
        }
        if (networkPrefixLength > 1) {
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {

                System.out.printf("InetAddress: %s\n", inetAddress);
                send(aSocket, inetAddress);
            }
        } else {
            System.out.printf("not a normal network (net prefix=0) could be vpn or so \n");
        }
        System.out.printf("\n");
    }

    private static void send(DatagramSocket aSocket, InetAddress inetAddress) {
        try {
            byte[] adressBytes = inetAddress.getAddress();
            if (adressBytes.length == 4) {
                adressBytes[3] = (byte) 255;
                InetAddress broadCase = InetAddress.getByAddress(adressBytes);
                byte[] m = "hallo".getBytes();
                DatagramPacket request = new DatagramPacket(m, m.length, broadCase, 55777);
                aSocket.send(request);
                byte[] buffer = new byte[1000];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);

                aSocket.receive(response);
                System.out.println(new TranscendWiFiSD(response));
            } else {
                System.out.println("skipping ipv6");
            }
        } catch (SocketTimeoutException timout) {
            System.out.printf("no wifisd here\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
