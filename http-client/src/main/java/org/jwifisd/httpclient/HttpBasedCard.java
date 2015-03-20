package org.jwifisd.httpclient;

/*
 * #%L
 * jwifisd-http-client
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jwifisd.api.IBrowse;
import org.jwifisd.api.ICard;
import org.jwifisd.api.IFileListener;
import org.jwifisd.api.IWifiFile;

/**
 * abstract super class for cards that are based on a http server running on the
 * card. this class provides a http-client connection and a lower level
 * potential card.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class HttpBasedCard implements ICard {

    /**
     * the http client connection to the http server of the card.
     */
    private CloseableHttpClient httpclient;

    /**
     * the lower level card this card wrappes.
     */
    private final ICard potentialCard;

    /**
     * construtor for http based cards.
     * 
     * @param potentialCard
     *            the lower level potential card with the ip and mac address.
     */
    protected HttpBasedCard(ICard potentialCard) {
        this.potentialCard = potentialCard;
    }

    /**
     * @return lazy created http client to connect to the card.
     */
    public CloseableHttpClient getHttpClient() {
        if (httpclient == null) {
            httpclient = HttpClients.createDefault();
        }
        return httpclient;
    }

    @Override
    public InetAddress ipAddress() {
        return potentialCard.ipAddress();
    }

    @Override
    public String mac() {
        return potentialCard.mac();
    }

    @Override
    public String title() {
        return potentialCard.title();
    }

    @Override
    public boolean addListener(IFileListener fileListener) {
        fileListeners.add(fileListener);
        return true;
    }

    @Override
    public IBrowse browse() {
        return null;
    }

    /**
     * list with listeners that want to know new detected files.
     */
    private Set<IFileListener> fileListeners = Collections.synchronizedSet(new HashSet<IFileListener>());

    @Override
    public boolean removeListener(IFileListener fileListener) {
        return fileListeners.remove(fileListener);
    }

    /**
     * report a new file to all listening.
     * 
     * @param newFile
     *            the new file to report.
     */
    protected void notifyNewFile(IWifiFile newFile) {
        for (IFileListener listener : fileListeners) {
            listener.notifyNewFile(this, newFile);
        }
    }
}
