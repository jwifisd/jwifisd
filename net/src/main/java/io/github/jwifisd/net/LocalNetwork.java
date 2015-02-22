package io.github.jwifisd.net;

/*
 * #%L
 * jwifisd-net
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

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LocalNetwork {

    private final int networkPrefixLength;

    private final InetAddress interfaceIp;

    private final InetAddress netmask;

    private final InetAddress broadcast;

    public LocalNetwork(InetAddress interfaceIp, int networkPrefixLength) throws UnknownHostException {
        this.interfaceIp = interfaceIp;
        this.networkPrefixLength = networkPrefixLength;
        byte[] adressBytes = this.interfaceIp.getAddress();
        byte[] maskBytes = new byte[adressBytes.length];
        byte[] broadcastBytes = new byte[adressBytes.length];
        int nrOfFullBytes = networkPrefixLength / 8;
        int index;
        for (index = 0; index < nrOfFullBytes; index++) {
            maskBytes[index] = (byte) 0xFF;
            broadcastBytes[index] = adressBytes[index];
        }
        int nrOfBits = networkPrefixLength % 8;
        if (nrOfBits != 0) {
            int bits = (1 << (nrOfBits + 1)) - 1;
            int inverted = bits ^ 0xFF;
            maskBytes[index] = (byte) inverted;
            broadcastBytes[index] = (byte) ((adressBytes[index] & inverted) | bits);
            index++;
        }
        for (; index < adressBytes.length; index++) {
            maskBytes[index] = (byte) 0;
            broadcastBytes[index] = (byte) 0xFF;
        }
        netmask = InetAddress.getByAddress(maskBytes);
        broadcast = InetAddress.getByAddress(broadcastBytes);
    }

    public InetAddress getInterfaceIp() {
        return interfaceIp;
    }

    public InetAddress getNetmask() {
        return netmask;
    }

    public int getNetworkPrefixLength() {
        return networkPrefixLength;
    }

    public InetAddress getBroadcast() {
        return broadcast;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + interfaceIp.toString() + "\\" + networkPrefixLength + ")";
    }
}
