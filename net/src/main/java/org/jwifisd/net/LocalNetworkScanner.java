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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this is a scanner that will scann the local system for connected subnets. it
 * will report found local subnets with a interface call back.
 * 
 * @author Richard van Nieuwenhoven
 */
public class LocalNetworkScanner {

    /**
     * bytes in a ip v4 address.
     */
    private static final int BYTES_IN_IP_V6 = 6;

    /**
     * bytes in a ip v6 address.
     */
    private static final int BYTES_IN_IP_V4 = 4;

    /**
     * the logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LocalNetworkScanner.class);

    /**
     * scan the local system for networks (defaults to only ip v4 networks).
     * 
     * @param doWithNetwork
     *            the callback to call when a network is detected.
     * @throws IOException
     *             if something goes wrong
     */
    public void scan(IDoWithNetwork doWithNetwork) throws IOException {
        scan(doWithNetwork, true);
    }

    /**
     * scan the local system for networks (defaults to only ip v4 networks).
     * 
     * @param doWithNetwork
     *            the callback to call when a network is detected.
     * @param ipv4
     *            scan only ipv4 if true
     * @throws IOException
     *             if something goes wrong
     */
    public void scan(IDoWithNetwork doWithNetwork, boolean ipv4) throws IOException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            doWithNetwork(netint, ipv4 ? BYTES_IN_IP_V4 : BYTES_IN_IP_V6, doWithNetwork);
        }
    }

    /**
     * a network interface was found, now detect if it a connected to a local
     * subnet.
     * 
     * @param netint
     *            the network interface
     * @param adresslength
     *            the adress length 4 = ipv4, 6 = ipv6
     * @param doWithNetwork
     *            the callback to call if the network has a local subnet.
     * @throws IOException
     *             if something goes wrong
     */
    private void doWithNetwork(NetworkInterface netint, int adresslength, IDoWithNetwork doWithNetwork) throws IOException {
        LOG.info("Interface with name: {}", netint.getName());

        if (netint.isLoopback()) {
            LOG.info("Inteface is loopback, skip that one");
            return;
        }
        int networkPrefixLength = -1;
        for (InterfaceAddress adress : netint.getInterfaceAddresses()) {
            if (adress.getAddress().getAddress().length == adresslength) {
                networkPrefixLength = Math.max(networkPrefixLength, adress.getNetworkPrefixLength());
                LOG.info("found prefix {}", networkPrefixLength);
            } else {
                LOG.info("Address scipped (ipv4/ipv6)", adress.getAddress());
            }
        }
        if (networkPrefixLength > 1) {
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                if (inetAddress.getAddress().length == adresslength) {
                    LocalNetwork localNetwork = new LocalNetwork(inetAddress, networkPrefixLength);
                    doWithNetwork(localNetwork, doWithNetwork);
                }
            }
        } else {
            LOG.warn("not a normal network (net prefix=0) could be vpn or so ({})", netint.getName());
        }
    }

    /**
     * detetcted a local subnet now call the callback.
     * 
     * @param localNetwork
     *            the detetced local subnet.
     * @param doWithNetwork
     *            the callback to call.
     */
    private void doWithNetwork(LocalNetwork localNetwork, IDoWithNetwork doWithNetwork) {
        LOG.info("localNetwork: {}", localNetwork);
        doWithNetwork.run(localNetwork);
    }
}
