package io.github.jwifisd.flashair;

import io.github.jwifisd.api.IBrowse;
import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.IFileListener;

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

    private static final String GET_UPDATE_STATUS = "102";

    private static final String GET_MAC_ADDRESS = "106";

    private static final String GET_SSID = "104";

    private static final String GET_FILE_LIST = "100";

    private static final Logger LOG = LoggerFactory.getLogger(FlashAirWiFiSD.class);

    private ICard potentialCard;

    private Thread thread;

    private CloseableHttpClient httpclient;

    private String ssid;

    private String mac;

    @Override
    public void run() {
        try {
            while (thread == Thread.currentThread()) {
            }
        } finally {

        }
    }

    @Override
    public String title() {
        return ssid;
    }

    @Override
    public String mac() {
        return potentialCard.mac();
    }

    @Override
    public InetAddress ipAddress() {
        return potentialCard.ipAddress();
    }

    @Override
    public IBrowse browse() {
        return null;
    }

    private Set<IFileListener> fileListeners = Collections.synchronizedSet(new HashSet<IFileListener>());

    @Override
    public boolean addListener(IFileListener fileListener) {
        fileListeners.add(fileListener);
        return true;
    }

    @Override
    public boolean removeListener(IFileListener fileListener) {
        return fileListeners.remove(fileListener);
    }

    @Override
    public int level() {
        return CARD_LEVEL;
    }

    @Override
    public void reconnect() {

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

    public FlashAirWiFiSD(ICard potentialCard) {
        this.potentialCard = potentialCard;
        String myMac = executeOperation(GET_MAC_ADDRESS, false);
        if (myMac != null) {
            this.ssid = executeOperation(GET_SSID, true);
            collectCurrentFiles("/DCIM");
        }
    }

    private void collectCurrentFiles(String directory) {
        String fileList = executeOperation(GET_FILE_LIST, true, "DIR", directory);
        
        System.out.println(fileList);
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

    public CloseableHttpClient getHttpClient() {
        if (httpclient == null) {
            httpclient = HttpClients.createDefault();
        }
        return httpclient;
    }
}
