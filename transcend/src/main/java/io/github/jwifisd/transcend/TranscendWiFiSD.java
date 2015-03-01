package io.github.jwifisd.transcend;

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

import io.github.jwifisd.api.IBrowse;
import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.IEvent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class TranscendWiFiSD implements ICard, Runnable {

    public static final int CARD_LEVEL = 1;

    private ICard potentialCard;

    private Socket westec;

    private Thread thread;

    private CloseableHttpClient httpclient;

    public TranscendWiFiSD(ICard potentialCard, Socket westec) {
        this.potentialCard = potentialCard;
        startListening(westec);
    }

    protected void startListening(Socket westec) {
        this.westec = westec;
        this.thread = new Thread(this, getClass().getSimpleName() + westec.getPort());
        this.thread.start();
    }

    @Override
    public String title() {
        return potentialCard.title();
    }

    @Override
    public InetAddress ipAddress() {
        return potentialCard.ipAddress();
    }

    @Override
    public IBrowse browse() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IEvent event() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int level() {
        return CARD_LEVEL;
    }

    @Override
    public String id() {
        return potentialCard.id();
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024 * 16];
            InputStream inputStream = westec.getInputStream();
            int offset = 0;
            while (thread == Thread.currentThread()) {
                int count = inputStream.read(buffer, offset, buffer.length - offset);
                int size = count + offset;
                int start = -1;
                int end = -1;
                for (int index = 0; index < size; index++) {
                    if (buffer[index] == '>') {
                        start = index;
                    } else if (buffer[index] == '<') {
                        start = index;
                    }
                    if (start >= 0 && buffer[index] == '\0') {
                        end = index;
                        String file = new String(buffer, start + 1, end - start - 1, "UTF-8");
                        TranscendWifiSDFile wifiFile = new TranscendWifiSDFile(this, file);
                        wifiFile.getData();
                        System.arraycopy(buffer, end + 1, buffer, 0, buffer.length - end - 1);
                        offset = offset - end - 1;
                        start = -1;
                    }
                }
                offset = offset + count;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO log
        } finally {
            thread = null;
        }
    }

    @Override
    public void reconnect() {
        if (!westec.isConnected()) {
            try {
                startListening(new Socket(ipAddress(), 5577));
            } catch (IOException e) {
                // todo log
            }
        }
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

            System.out.println("Executing request " + httpget.getRequestLine());

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
            FileOutputStream out = new FileOutputStream("target/" + fileName);
            out.write(responseBody);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
