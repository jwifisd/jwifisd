package org.jwifisd.transcend;

/*
 * #%L
 * jwifisd-transcend
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.jwifisd.api.ICard;
import org.jwifisd.httpclient.ByteResponseHandler;
import org.jwifisd.httpclient.HttpBasedCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wifi card implementation for Transcend WiFi SD cards, uses a lower level
 * detected card as a base.
 * 
 * @author Richard van Nieuwenhoven
 */
public class TranscendWiFiSD extends HttpBasedCard implements Runnable {

    /**
     * standard WESTEC socket to listen to.
     */
    protected static final int TRANSCEND_EVENTING_PORT = 5566;

    /**
     * ok we know the full api so use level 1.
     */
    public static final int CARD_LEVEL = 1;

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TranscendWiFiSD.class);

    /**
     * the running thread that periodically checks for new files.
     */
    private Thread thread;

    /**
     * socket to receive notifications of newly created files.
     */
    private Socket westec;

    /**
     * constructor for the Transcend WiFi SD card based on qa potential card.
     * 
     * @param potentialCard
     *            the potential card to use.
     * @param westec
     *            the eventing socket.
     */
    protected TranscendWiFiSD(ICard potentialCard, Socket westec) {
        super(potentialCard);
        startListening(westec);
    }

    /**
     * get the file contents of the specified file. No cache here so every call
     * will get the data from the card.
     * 
     * @param wifiFile
     *            the file to get the contents of
     */
    protected void downloadFile(final TranscendWifiSDFile wifiFile) {
        try {
            String fileName = wifiFile.name().substring(wifiFile.name().lastIndexOf('/') + 1);
            String fileDir = wifiFile.name().substring(0, wifiFile.name().lastIndexOf('/')).replace("/mnt/sd", "/www/sd");
            URI uri = new URIBuilder()//
                    .setScheme("http")//
                    .setHost(ipAddress().getHostAddress())//
                    .setPath("/cgi-bin/wifi_download")//
                    .setParameter("fn", fileName) // file name
                    .setParameter("fd", fileDir) // file directory
                    .build();
            HttpGet httpget = new HttpGet(uri);

            if (LOG.isInfoEnabled()) {
                LOG.info("Executing request " + httpget.getRequestLine());
            }

            byte[] responseBody = getHttpClient().execute(httpget, new ByteResponseHandler());
            wifiFile.setData(responseBody);
            // TODO: set timestamp?
        } catch (Exception e) {
            LOG.error("TranscendWiFiSD could not download file data!", e);
        }
    }

    @Override
    public int level() {
        return CARD_LEVEL;
    }

    @Override
    public void reconnect() {
        if (!westec.isConnected()) {
            try {
                startListening(new Socket(ipAddress(), TRANSCEND_EVENTING_PORT));
            } catch (IOException e) {
                LOG.error("TranscendWiFiSD event channel could not reconnect!", e);
            }
        }
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = westec.getInputStream();
            int oneByte;

            ByteArrayOutputStream lineBytes = new ByteArrayOutputStream();
            while (thread == Thread.currentThread()) {
                oneByte = inputStream.read();
                if (oneByte < 0) {
                    break;
                }
                if (oneByte != 0) {
                    lineBytes.write(oneByte);
                } else {
                    // line complete
                    byte[] lineByteArray = lineBytes.toByteArray();
                    int offset = 0;
                    while (lineByteArray[offset] == '>' || lineByteArray[offset] == '<') {
                        offset++;
                    }
                    String line = new String(lineByteArray, offset, lineByteArray.length - offset, "UTF-8");
                    TranscendWifiSDFile wifiFile = new TranscendWifiSDFile(this, line);
                    notifyNewFile(wifiFile);
                    lineBytes.reset();
                }
            }
        } catch (Exception e) {
            LOG.error("TranscendWiFiSD event channel broke!", e);
        } finally {
            thread = null;
        }
    }

    /**
     * start listening to the event socket in a new tread.
     * 
     * @param newWestecSocket
     *            the socket to listen to.
     */
    private void startListening(Socket newWestecSocket) {
        this.westec = newWestecSocket;
        this.thread = new Thread(this, getClass().getSimpleName() + westec.getPort());
        this.thread.start();
    }
}
