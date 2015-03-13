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
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jwifisd.api.IBrowse;
import org.jwifisd.api.ICard;
import org.jwifisd.api.IFileListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranscendWiFiSD implements ICard, Runnable {

    public static final int CARD_LEVEL = 1;

    private static final Logger LOG = LoggerFactory.getLogger(TranscendWiFiSD.class);

    private Set<IFileListener> fileListeners = Collections.synchronizedSet(new HashSet<IFileListener>());

    private CloseableHttpClient httpclient;

    private ICard potentialCard;

    private Thread thread;

    private Socket westec;

    public TranscendWiFiSD(ICard potentialCard, Socket westec) {
        this.potentialCard = potentialCard;
        startListening(westec);
    }

    @Override
    public boolean addListener(IFileListener fileListener) {
        fileListeners.add(fileListener);
        return true;
    }

    @Override
    public IBrowse browse() {
        // TODO Auto-generated method stub
        return null;
    }

    public void downloadFile(final TranscendWifiSDFile wifiFile) {
        if (httpclient == null) {
            httpclient = HttpClients.createDefault();
        }
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

            // Create a custom response handler
            ResponseHandler<byte[]> responseHandler = new ResponseHandler<byte[]>() {

                @Override
                public byte[] handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toByteArray(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            byte[] responseBody = httpclient.execute(httpget, responseHandler);
            wifiFile.setData(responseBody);
        } catch (Exception e) {
            LOG.error("TranscendWiFiSD could not download file data!", e);
        }
    }

    @Override
    public InetAddress ipAddress() {
        return potentialCard.ipAddress();
    }

    @Override
    public int level() {
        return CARD_LEVEL;
    }

    @Override
    public String mac() {
        return potentialCard.mac();
    }

    @Override
    public void reconnect() {
        if (!westec.isConnected()) {
            try {
                startListening(new Socket(ipAddress(), 5577));
            } catch (IOException e) {
                LOG.error("TranscendWiFiSD event channel could not reconnect!", e);
            }
        }
    }

    @Override
    public boolean removeListener(IFileListener fileListener) {
        return fileListeners.remove(fileListener);
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = westec.getInputStream();
            int oneByte;

            ByteArrayOutputStream lineBytes = new ByteArrayOutputStream();
            while (thread == Thread.currentThread() && (oneByte = inputStream.read()) >= 0) {
                if (oneByte != 0) {
                    lineBytes.write(oneByte);
                } else {
                    // line komplete
                    byte[] lineByteArray = lineBytes.toByteArray();
                    int offset = 0;
                    while (lineByteArray[offset] == '>' || lineByteArray[offset] == '<') {
                        offset++;
                    }
                    String line = new String(lineByteArray, offset, lineByteArray.length - offset, "UTF-8");
                    TranscendWifiSDFile wifiFile = new TranscendWifiSDFile(this, line);
                    for (IFileListener fileListener : fileListeners) {
                        fileListener.notifyNewFile(this, wifiFile);
                    }
                    lineBytes.reset();
                }
            }
        } catch (Exception e) {
            LOG.error("TranscendWiFiSD event channel broke!", e);
        } finally {
            thread = null;
        }
    }

    @Override
    public String title() {
        return potentialCard.title();
    }

    protected void startListening(Socket westec) {
        this.westec = westec;
        this.thread = new Thread(this, getClass().getSimpleName() + westec.getPort());
        this.thread.start();
    }
}
