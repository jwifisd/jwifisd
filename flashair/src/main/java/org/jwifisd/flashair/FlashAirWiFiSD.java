package org.jwifisd.flashair;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
import org.jwifisd.api.IBrowse;
import org.jwifisd.api.ICard;
import org.jwifisd.api.IFileListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This card implementation handles the flashair protocol.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class FlashAirWiFiSD implements ICard, Runnable {

    /**
     * default poll interfall for new files (in milliseconds).
     */
    private static final int DEFAULT_POLL_INTERERFALL = 333;

    /**
     * this card can handle the api very good so level 1 is used.
     */
    public static final int CARD_LEVEL = 1;

    /**
     * api operation to get the file listing.
     */
    private static final String GET_FILE_LIST = "100";

    /**
     * this operation is used to get the ssid of the card, it is also used to
     * check if the card is realy a flashair card.
     */
    private static final String GET_SSID = "104";

    /**
     * operation used to check is any write operation has occured on the card.
     * We use this operation to check periotically for changes.
     */
    private static final String GET_UPDATE_STATUS = "102";

    /**
     * the logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FlashAirWiFiSD.class);

    /**
     * list with listeners that want to know new detected files.
     */
    private Set<IFileListener> fileListeners = Collections.synchronizedSet(new HashSet<IFileListener>());

    /**
     * the http client connection to the http server of the card.
     */
    private CloseableHttpClient httpclient;

    /**
     * a list of files that we already know of, we need this to detect the newly
     * created files.
     */
    private Set<FlashAirWiFiSDFile> knownFiles = new HashSet<>();

    /**
     * the lower level card this card wrappes.
     */
    private ICard potentialCard;

    /**
     * the network ssid.
     */
    private String ssid;

    /**
     * the running thread that periotically checks for new files.
     */
    private Thread thread;

    /**
     * constructor for this card (needs a low level card).
     * 
     * @param potentialCard
     *            the wrapped low level card.
     */
    private FlashAirWiFiSD(ICard potentialCard) {
        this.potentialCard = potentialCard;
        this.ssid = executeOperation(GET_SSID, false);
        if (this.ssid != null) {
            collectCurrentFiles("/DCIM", null);
            executeOperation(GET_UPDATE_STATUS, true);
            thread = new Thread(this, getClass().getSimpleName());
            thread.start();
        }
    }

    /**
     * package internal api to create a flashair card.
     * 
     * @param potentialCard
     *            the lower level card to test.
     * @return the flashair implementation or null if the potentialCard was not
     *         recognized as flashair.
     */
    protected static ICard create(ICard potentialCard) {
        if (potentialCard.level() > CARD_LEVEL) {
            FlashAirWiFiSD flashAirWiFiSD = new FlashAirWiFiSD(potentialCard);
            if (flashAirWiFiSD.ssid != null) {
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

    /**
     * get the file contents of the specified file. No cache here so every call
     * will get the date from the card.
     * 
     * @param flashAirWiFiSDFile
     *            the file to get the contents of.
     * @return the byte contents of the file
     */
    protected byte[] getData(FlashAirWiFiSDFile flashAirWiFiSDFile) {

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
                    Thread.sleep(pollInterfall());
                } catch (InterruptedException e) {
                    LOG.info("poll interruppted", e);
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
            thread = null;
        }
    }

    /**
     * @return the poll interfall in milliseconds. this should be configurable
     *         over the card properties.
     */
    public int pollInterfall() {
        return DEFAULT_POLL_INTERERFALL;
    }

    @Override
    public String title() {
        return ssid;
    }

    /**
     * collect a list of currently available files on the card to be able to
     * check the difference if a change happens.
     * 
     * @param directory
     *            the directory to scan
     * @param newFiles
     *            the list of already collected files
     * @return the list of collected files.
     */
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
            LOG.info("very strange, this should not happen", e);
        }
        return newFiles;
    }

    /**
     * execute an api operation on the card and return the string result.
     * 
     * @param operation
     *            the operation to execute.
     * @param reportError
     *            true if an error should be reported
     * @param parameter
     *            parameter for the operation
     * @return the result of the operation or null if it failed.
     */
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
