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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.net.InetAddress;

import org.junit.Assert;
import org.junit.Test;
import org.jwifisd.net.LocalNetwork;

public class TestLocalNetwork {

    static private byte[] testAdress = createByteArray(192, 168, 127, 127);

    @Test
    public void testLocalNetworkToString() throws Exception {
        InetAddress test = InetAddress.getByAddress(testAdress);
        LocalNetwork localNetwork = new LocalNetwork(test, 0);
        Assert.assertTrue(localNetwork.toString().indexOf("192.168.127.127") >= 0);
    }

    @Test
    public void testLocalNetworkAll() throws Exception {
        InetAddress test = InetAddress.getByAddress(testAdress);

        LocalNetwork localNetwork = new LocalNetwork(test, 0);
        Assert.assertEquals(0, localNetwork.getNetworkPrefixLength());
        Assert.assertArrayEquals(createByteArray(0, 0, 0, 0), localNetwork.getNetmask().getAddress());
        Assert.assertArrayEquals(createByteArray(255, 255, 255, 255), localNetwork.getBroadcast().getAddress());
    }

    @Test
    public void testLocalNetworkFirstPart() throws Exception {
        InetAddress test = InetAddress.getByAddress(testAdress);

        LocalNetwork localNetwork = new LocalNetwork(test, 4);
        Assert.assertEquals(4, localNetwork.getNetworkPrefixLength());
        Assert.assertArrayEquals(createByteArray(224, 0, 0, 0), localNetwork.getNetmask().getAddress());
        Assert.assertArrayEquals(createByteArray(223, 255, 255, 255), localNetwork.getBroadcast().getAddress());

    }

    @Test
    public void testLocalNetworkMiddlePart() throws Exception {
        InetAddress test = InetAddress.getByAddress(testAdress);

        LocalNetwork localNetwork = new LocalNetwork(test, 20);
        Assert.assertEquals(20, localNetwork.getNetworkPrefixLength());
        Assert.assertArrayEquals(createByteArray(255, 255, 224, 0), localNetwork.getNetmask().getAddress());
        Assert.assertArrayEquals(createByteArray(192, 168, 127, 255), localNetwork.getBroadcast().getAddress());
    }

    @Test
    public void testLocalNetworkMiddle() throws Exception {
        InetAddress test = InetAddress.getByAddress(testAdress);

        LocalNetwork localNetwork = new LocalNetwork(test, 24);
        Assert.assertEquals(24, localNetwork.getNetworkPrefixLength());
        Assert.assertArrayEquals(createByteArray(255, 255, 255, 0), localNetwork.getNetmask().getAddress());
        Assert.assertArrayEquals(createByteArray(192, 168, 127, 255), localNetwork.getBroadcast().getAddress());
    }

    @Test
    public void testLocalNetworkLastPart() throws Exception {
        InetAddress test = InetAddress.getByAddress(testAdress);

        LocalNetwork localNetwork = new LocalNetwork(test, 30);
        Assert.assertEquals(30, localNetwork.getNetworkPrefixLength());
        Assert.assertArrayEquals(createByteArray(255, 255, 255, 128), localNetwork.getNetmask().getAddress());
        Assert.assertArrayEquals(createByteArray(192, 168, 127, 127), localNetwork.getBroadcast().getAddress());

        new LocalNetwork(test, 32);
    }

    @Test
    public void testLocalNetworkEmpty() throws Exception {
        InetAddress test = InetAddress.getByAddress(testAdress);

        LocalNetwork localNetwork = new LocalNetwork(test, 32);
        Assert.assertEquals(32, localNetwork.getNetworkPrefixLength());
        Assert.assertArrayEquals(createByteArray(255, 255, 255, 255), localNetwork.getNetmask().getAddress());
        Assert.assertArrayEquals(createByteArray(192, 168, 127, 127), localNetwork.getBroadcast().getAddress());
    }

    private static byte[] createByteArray(int... bytes) {
        byte[] result = new byte[bytes.length];
        for (int index = 0; index < result.length; index++) {
            result[index] = (byte) bytes[index];
        }
        return result;
    }
}
