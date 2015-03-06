package io.github.jwifisd.net.arp;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.net.InetAddress;

public class ARP {

    public InetAddress getIpAdress() {
        return ipAdress;
    }

    public String getMacAdress() {
        return macAdress;
    }

    private InetAddress ipAdress;

    private String macAdress;

    protected void setIpAdress(InetAddress ipAdress) {
        this.ipAdress = ipAdress;
    }

    protected void setMacAdress(String macAdress) {
        this.macAdress = macAdress;
    }

    @Override
    public int hashCode() {
        return macAdress == null ? 0 : macAdress.hashCode();
    }

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
}
