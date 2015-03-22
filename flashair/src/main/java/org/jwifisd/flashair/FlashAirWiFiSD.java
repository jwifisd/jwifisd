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
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.jwifisd.api.ICard;
import org.jwifisd.httpclient.ByteResponseHandler;
import org.jwifisd.httpclient.HttpBasedCard;
import org.jwifisd.httpclient.StringResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This card implementation handles the flashair protocol.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class FlashAirWiFiSD extends HttpBasedCard implements Runnable {

    /**
     * default poll interval for new files (in milliseconds).
     */
    private static final int DEFAULT_POLL_INTERVAL = 333;

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
     * operation used to check is any write operation has occurred on the card.
     * We use this operation to check periodically for changes.
     */
    private static final String GET_UPDATE_STATUS = "102";

    /**
     * the logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FlashAirWiFiSD.class);

    /**
     * a list of files that we already know of, we need this to detect the newly
     * created files.
     */
    private Set<FlashAirWiFiSDFile> knownFiles = new HashSet<>();

    /**
     * the network ssid.
     */
    private String ssid;

    /**
     * the running thread that periodically checks for new files.
     */
    private Thread thread;

    /**
     * constructor for this card (needs a low level card).
     * 
     * @param potentialCard
     *            the wrapped low level card.
     */
    private FlashAirWiFiSD(ICard potentialCard) {
        super(potentialCard);
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

    @Override
    public int level() {
        return CARD_LEVEL;
    }

    @Override
    public void reconnect() {

    }

    @Override
    public void run() {
        try {
            while (thread == Thread.currentThread()) {
                try {
                    Thread.sleep(pollInterval());
                } catch (InterruptedException e) {
                    LOG.info("poll interrupted", e);
                }
                String status = executeOperation(GET_UPDATE_STATUS, true);
                if (!status.equals("0")) {
                    Set<FlashAirWiFiSDFile> newFiles = collectCurrentFiles("/DCIM", null);
                    if (newFiles != null) {
                        for (FlashAirWiFiSDFile newFile : newFiles) {
                            notifyNewFile(newFile);
                        }
                    }
                }
            }
        } finally {
            thread = null;
        }
    }

    /**
     * @return the poll interval in milliseconds. this should be configurable
     *         over the card properties.
     */
    public int pollInterval() {
        return DEFAULT_POLL_INTERVAL;
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
