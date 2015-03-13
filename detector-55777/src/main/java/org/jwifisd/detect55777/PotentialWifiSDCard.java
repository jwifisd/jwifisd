package org.jwifisd.detect55777;

/*
 * #%L
 * jwifisd-detector-55777
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

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.jwifisd.api.IBrowse;
import org.jwifisd.api.ICard;
import org.jwifisd.api.IFileListener;
import org.jwifisd.net.arp.ARPControl;

/**
 * a potential detected wifi card that was detected, still with a low api level
 * because we only have the address / name of the card.
 * 
 * @author Richard van Nieuwenhoven
 */
public class PotentialWifiSDCard implements ICard {

    /**
     * title of the card received over udp.
     */
    private String title;

    /**
     * ipadress of the wifisd card.
     */
    private InetAddress ip;

    /**
     * netmask the wifisd card uses.
     */
    private InetAddress netmask;

    /**
     * the router ip the card uses.
     */
    private InetAddress router;

    /**
     * the card mode if it could be deteted.
     */
    private String mode;

    /**
     * the WiFi ssid of the card.
     */
    private String essid;

    /**
     * the mac adress of the card.
     */
    private String mac;

    /**
     * construct a wifisd card from a received datagram packet. This will throw
     * an illegal state exception if the datagram did not contain enough
     * information to create the card.
     * 
     * @param response
     *            the datagram packet received from the card.
     */
    public PotentialWifiSDCard(DatagramPacket response) {
        byte[] packet = new byte[response.getLength()];
        packet = Arrays.copyOfRange(response.getData(), response.getOffset(), response.getLength() + response.getOffset());
        String packetString = new String(packet, Charset.forName("UTF-8"));
        this.ip = response.getAddress();
        BufferedReader reader = new BufferedReader(new StringReader(packetString));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                int equalPos = line.indexOf('=');
                if (equalPos >= 0) {
                    String key = line.substring(0, equalPos);
                    String value = line.substring(equalPos + 1).trim();
                    if (key.equalsIgnoreCase("netmask")) {
                        this.netmask = InetAddress.getByName(value);
                    } else if (key.equalsIgnoreCase("router")) {
                        this.router = InetAddress.getByName(value);
                    } else if (key.equalsIgnoreCase("mode")) {
                        this.mode = value;
                    } else if (key.equalsIgnoreCase("essid")) {
                        this.essid = value;
                    } else if (line.indexOf(' ') < equalPos && title == null && !line.trim().isEmpty()) {
                        this.title = line.trim();
                    }
                }
            }
            mac = ARPControl.macAdressOf(this.ip);
        } catch (Exception e) {
            // its a string so this can not happen..
            throw new IllegalStateException("io exception in strean reading?", e);
        }
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("55777 type WiFiSD {");
        if (ip != null) {
            result.append("\n\tip=");
            result.append(ip.getHostAddress());
        }
        if (netmask != null) {
            result.append("\n\tnetmask=");
            result.append(netmask.getHostAddress());
        }
        if (router != null) {
            result.append("\n\trouter=");
            result.append(router.getHostAddress());
        }
        if (mode != null) {
            result.append("\n\tmode=");
            result.append(mode);
        }
        if (essid != null) {
            result.append("\n\tessid=");
            result.append(essid);
        }
        result.append("\n}");
        return result.toString();
    }

    @Override
    public IBrowse browse() {
        return null;
    }

    @Override
    public InetAddress ipAddress() {
        return ip;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public int level() {
        return ICard.BARE_PRIMITIV_CARD_LAVEL;
    }

    @Override
    public String mac() {
        return mac;
    }

    @Override
    public void reconnect() {
    }

    @Override
    public boolean addListener(IFileListener fileListener) {
        return false;
    }

    @Override
    public boolean removeListener(IFileListener fileListener) {
        return false;
    }
}
