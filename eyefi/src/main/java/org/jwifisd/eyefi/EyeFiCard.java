package org.jwifisd.eyefi;

/*
 * #%L
 * jwifisd-eyefi
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jwifisd.api.IBrowse;
import org.jwifisd.api.ICard;
import org.jwifisd.api.IFileListener;

/**
 * The eyefi card detected by soap messages.
 * 
 * @author Richard van Nieuwenhoven
 */
public class EyeFiCard implements ICard {

    /**
     * all listeners that want to know when a new file was send to the eyefi
     * server.
     */
    private Set<IFileListener> fileListeners = Collections.synchronizedSet(new HashSet<IFileListener>());

    /**
     * the ip address of the card.
     */
    private InetAddress ipAddress;

    /**
     * the session request that triggered the creation of the card.
     */
    private StartSessionRequest startSession;

    /**
     * constructor of an eyefi card that was triggered by a soap request.
     * 
     * @param startSession
     *            the http session that connects the server to the card.
     * @throws UnknownHostException
     *             should never happen because the card was just connected.
     */
    public EyeFiCard(StartSessionRequest startSession) throws UnknownHostException {
        this.startSession = startSession;
        ipAddress = InetAddress.getByName(startSession.getIpAddress());
    }

    @Override
    public boolean addListener(IFileListener fileListener) {
        fileListeners.add(fileListener);
        return true;
    }

    @Override
    public IBrowse browse() {
        // not supported
        return null;
    }

    @Override
    public InetAddress ipAddress() {
        return ipAddress;
    }

    @Override
    public int level() {
        return 1;
    }

    @Override
    public String mac() {
        return startSession.getMacaddress();
    }

    @Override
    public void reconnect() {

    }

    @Override
    public boolean removeListener(IFileListener fileListener) {
        return fileListeners.remove(fileListener);
    }

    /**
     * report a new received file to the listeners.
     * 
     * @param eyeFiPhoto
     *            the photo received.
     */
    protected void reportNewFile(EyeFiPhoto eyeFiPhoto) {
        for (IFileListener fileListener : fileListeners) {
            fileListener.notifyNewFile(this, eyeFiPhoto);
        }
    }

    @Override
    public String title() {
        return "EyeFi-" + startSession.getMacaddress();
    }
}
