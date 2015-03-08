package io.github.jwifisd.flashair;

/*
 * #%L
 * jwifisd-flashair
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

import io.github.jwifisd.api.IBrowse;
import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.IFileListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlashAirWiFiSD implements ICard, Runnable {

    public static final int CARD_LEVEL = 1;

    private static final String GET_FILE_LIST = "100";

    private static final String GET_MAC_ADDRESS = "106";

    private static final String GET_SSID = "104";

    private static final String GET_UPDATE_STATUS = "102";

    private static final Logger LOG = LoggerFactory.getLogger(FlashAirWiFiSD.class);

    private Set<IFileListener> fileListeners = Collections.synchronizedSet(new HashSet<IFileListener>());

    private CloseableHttpClient httpclient;

    private Set<FlashAirWiFiSDFile> knownFiles = new HashSet<>();

    private String mac;

    private ICard potentialCard;

    private String ssid;

    private Thread thread;

    public FlashAirWiFiSD(ICard potentialCard) {
        this.potentialCard = potentialCard;
        this.ssid = executeOperation(GET_SSID, false);
        if (this.ssid != null) {
            Set<FlashAirWiFiSDFile> newFiles = collectCurrentFiles("/DCIM", null);
            executeOperation(GET_UPDATE_STATUS, true);
            thread = new Thread(this, getClass().getSimpleName());
            thread.start();
        }
    }

    public static ICard create(ICard potentialCard) {
        if (potentialCard.level() > CARD_LEVEL) {
            FlashAirWiFiSD flashAirWiFiSD = new FlashAirWiFiSD(potentialCard);
            if (flashAirWiFiSD.mac() != null) {
                return flashAirWiFiSD;
            }
        }
        return null;

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

    public byte[] getData(FlashAirWiFiSDFile flashAirWiFiSDFile) {

        try {
            URI uri = new URIBuilder()//
                    .setScheme("http")//
                    .setHost(ipAddress().getHostAddress())//
                    .setPath(flashAirWiFiSDFile.name())//
                    .build();
            HttpGet httpget = new HttpGet(uri);

            if (LOG.isInfoEnabled()) {
                LOG.info("Executing request " + httpget.getRequestLine());
            }
            return getHttpClient().execute(httpget, new ByteResponseHandler());
        } catch (Exception e) {
            LOG.error("could not communicate with the flashair card", e);
            return null;
        }
    }

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
    public int level() {
        return CARD_LEVEL;
    }

    @Override
    public String mac() {
        return potentialCard.mac();
    }

    @Override
    public void reconnect() {

    }

    @Override
    public boolean removeListener(IFileListener fileListener) {
        return fileListeners.remove(fileListener);
    }

    @Override
    public void run() {
        try {
            while (thread == Thread.currentThread()) {
                try {
                    Thread.sleep(333);
                } catch (InterruptedException e) {
                }
                String status = executeOperation(GET_UPDATE_STATUS, true);
                if (!status.equals("0")) {
                    Set<FlashAirWiFiSDFile> newFiles = collectCurrentFiles("/DCIM", null);
                    if (newFiles != null) {
                        for (FlashAirWiFiSDFile newFile : newFiles) {
                            for (IFileListener listener : fileListeners) {
                                listener.notifyNewFile(this, newFile);
                            }
                        }
                    }
                }
            }
        } finally {

        }
    }

    @Override
    public String title() {
        return ssid;
    }

    private Set<FlashAirWiFiSDFile> collectCurrentFiles(String directory, Set<FlashAirWiFiSDFile> newFiles) {
        String fileList = executeOperation(GET_FILE_LIST, true, "DIR", directory);
        BufferedReader reader = new BufferedReader(new StringReader(fileList));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                FlashAirWiFiSDFile file = FlashAirWiFiSDFile.parseLine(line, this);
                if (file != null) {
                    if (file.isDirectory()) {
                        newFiles = collectCurrentFiles(file.name(), newFiles);
                    } else {
                        if (knownFiles.add(file)) {
                            if (newFiles == null) {
                                newFiles = new HashSet<>();
                            }
                            newFiles.add(file);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // can not happen..
        }
        return newFiles;
    }

    private String executeOperation(String operation, boolean reportError, String... parameter) {
        try {
            URIBuilder operationURI = new URIBuilder()//
                    .setScheme("http")//
                    .setHost(ipAddress().getHostAddress())//
                    .setPath("/command.cgi")//
                    .setParameter("op", operation);
            if (parameter != null && parameter.length > 0) {
                for (int index = 0; index < parameter.length; index += 2) {
                    operationURI.setParameter(parameter[index], parameter[index + 1]);
                }
            }
            URI uri = operationURI.build();
            HttpGet httpget = new HttpGet(uri);

            if (LOG.isInfoEnabled()) {
                LOG.info("Executing request " + httpget.getRequestLine());
            }
            return getHttpClient().execute(httpget, new StringResponseHandler());
        } catch (Exception e) {
            if (reportError) {
                LOG.error("could not communicate with the flashair card", e);
            }
            return null;
        }
    }
}
