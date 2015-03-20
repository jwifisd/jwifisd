package org.jwifisd.net.arp;

/*
 * #%L
 * jwifisd-net
 * %%
 * Copyright (C) 2015 jwifisd
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

/**
 * this class represents an arp entry. (a resolvement of a ip address to a mac
 * (hardware) address.
 * 
 * @author Richard van Nieuwenhoven
 */
public class ARP {

    /**
     * the ipaddress.
     */
    private InetAddress ipAdress;

    /**
     * the mac address.
     */
    private String macAdress;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ARP) {
            String objMacAdress = ((ARP) obj).macAdress;
            InetAddress objIpAdress = ((ARP) obj).ipAdress;
            if (macAdress != null && macAdress.equalsIgnoreCase(objMacAdress)) {
                if (ipAdress != null && ipAdress.equals(objIpAdress)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the ip address.
     */
    public InetAddress getIpAdress() {
        return ipAdress;
    }

    /**
     * @return the mac address.
     */
    public String getMacAdress() {
        return macAdress;
    }

    @Override
    public int hashCode() {
        return macAdress == null ? 0 : macAdress.hashCode();
    }

    /**
     * set the ip address.
     * 
     * @param ipAdress
     *            the new ip address.
     */
    protected void setIpAdress(InetAddress ipAdress) {
        this.ipAdress = ipAdress;
    }

    /**
     * set the mac address.
     * 
     * @param macAdress
     *            the new mac address.
     */
    protected void setMacAdress(String macAdress) {
        this.macAdress = macAdress;
    }
}
