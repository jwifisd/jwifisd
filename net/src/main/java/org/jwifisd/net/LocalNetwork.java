package org.jwifisd.net;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * this class describes a locally available subnet .
 * 
 * @author Richard van Nieuwenhoven
 *
 */
public class LocalNetwork {

    /**
     * mask all bits in a byte.
     */
    private static final int BYTE_MASK = 0xFF;

    /**
     * number of bits in a byte.
     */
    private static final int BITS_IN_A_BYTE = 8;

    /**
     * the network prefix length. e.g. how many bits og the address are fix, the
     * rest represents the subnet.
     */
    private final int networkPrefixLength;

    /**
     * the ip address of the interface that is locally connected to the subnet.
     */
    private final InetAddress interfaceIp;

    /**
     * the subnet mask of the subnet.
     */
    private final InetAddress netmask;

    /**
     * the broadcast address of the subnet.
     */
    private final InetAddress broadcast;

    /**
     * constructor for the subnet description.
     * 
     * @param interfaceIp
     *            the ip address of the interface that is locally connected to
     *            the subnet.
     * @param networkPrefixLength
     *            the network prefix length.
     * @throws UnknownHostException
     *             if the address could not be resolved.
     */
    public LocalNetwork(InetAddress interfaceIp, int networkPrefixLength) throws UnknownHostException {
        this.interfaceIp = interfaceIp;
        this.networkPrefixLength = networkPrefixLength;
        byte[] adressBytes = this.interfaceIp.getAddress();
        byte[] maskBytes = new byte[adressBytes.length];
        byte[] broadcastBytes = new byte[adressBytes.length];
        int nrOfFullBytes = networkPrefixLength / BITS_IN_A_BYTE;
        int index;
        for (index = 0; index < nrOfFullBytes; index++) {
            maskBytes[index] = (byte) BYTE_MASK;
            broadcastBytes[index] = adressBytes[index];
        }
        int nrOfBits = networkPrefixLength % BITS_IN_A_BYTE;
        if (nrOfBits != 0) {
            int bits = (1 << (nrOfBits + 1)) - 1;
            int inverted = bits ^ BYTE_MASK;
            maskBytes[index] = (byte) inverted;
            broadcastBytes[index] = (byte) ((adressBytes[index] & inverted) | bits);
            index++;
        }
        for (; index < adressBytes.length; index++) {
            maskBytes[index] = (byte) 0;
            broadcastBytes[index] = (byte) BYTE_MASK;
        }
        netmask = InetAddress.getByAddress(maskBytes);
        broadcast = InetAddress.getByAddress(broadcastBytes);
    }

    /**
     * @return the ip address of the interface that is locally connected to the
     *         subnet.
     */
    public InetAddress getInterfaceIp() {
        return interfaceIp;
    }

    /**
     * 
     * @return the subnet mask of the subnet.
     */
    public InetAddress getNetmask() {
        return netmask;
    }

    /**
     * @return the network prefix length. e.g. how many bits og the address are
     *         fix, the
     *         rest represents the subnet.
     */
    public int getNetworkPrefixLength() {
        return networkPrefixLength;
    }

    /**
     * @return the broadcast address of the subnet.
     */
    public InetAddress getBroadcast() {
        return broadcast;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + interfaceIp.toString() + "\\" + networkPrefixLength + ")";
    }
}
