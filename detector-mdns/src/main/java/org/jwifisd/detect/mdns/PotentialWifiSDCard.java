package org.jwifisd.detect.mdns;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.net.InetAddress;

import org.jwifisd.api.IBrowse;
import org.jwifisd.api.ICard;
import org.jwifisd.api.IFileListener;
import org.jwifisd.net.arp.ARPControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A potential WiFiSD acrd that was detected using mdns messages.
 * 
 * @author Richard van Nieuwenhoven
 */
public class PotentialWifiSDCard implements ICard {

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PotentialWifiSDCard.class);

    /**
     * the full qualified name of the device. (normally "xxx.local")
     */
    private final String fullQualifiedName;

    /**
     * the ip address of the device. (this could change with every detection of
     * the same card)
     */
    private final InetAddress ip;

    /**
     * the mac (hardware) address of the device. (this will always stay the same
     * for a spesific device)
     */
    private String mac;

    /**
     * create a new mdns detected potential card.
     * 
     * @param fullQualifiedName
     *            the full qualified name.
     * @param ip
     *            the ip address of the card.
     */
    protected PotentialWifiSDCard(String fullQualifiedName, InetAddress ip) {
        this.fullQualifiedName = fullQualifiedName;
        this.ip = ip;
    }

    @Override
    public String title() {
        return fullQualifiedName;
    }

    @Override
    public InetAddress ipAddress() {
        return ip;
    }

    @Override
    public IBrowse browse() {
        return null;
    }

    @Override
    public int level() {
        return ICard.BARE_PRIMITIVE_CARD_LEVEL;
    }

    @Override
    public String mac() {
        if (mac == null) {
            try {
                mac = ARPControl.macAdressOf(this.ip);
            } catch (Exception e) {
                LOG.error("could not detect mac address of card with ip " + ip, e);
                mac = "00:00:00:00:00:00";
            }
        }
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
