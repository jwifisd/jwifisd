package io.github.jwifisd.impl;

import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.ICardManager;
import io.github.jwifisd.api.IDetector;
import io.github.jwifisd.api.IFileListener;
import io.github.jwifisd.api.INotifier;
import io.github.jwifisd.net.IDoWithNetwork;
import io.github.jwifisd.net.LocalNetwork;
import io.github.jwifisd.net.LocalNetworkScanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardManager implements ICardManager, Runnable, IDoWithNetwork {

    private static final CardManager SINGLETON = new CardManager();

    private static final ServiceLoader<IDetector> serviceLoader = ServiceLoader.load(IDetector.class);

    private Set<IFileListener> fileListeners = Collections.synchronizedSet(new HashSet<IFileListener>());

    private Set<ICardListener> cardListeners = Collections.synchronizedSet(new HashSet<ICardListener>());

    private static final Logger LOG = LoggerFactory.getLogger(CardManager.class);

    private static final ICardListener[] EMPTY_CARDS = new ICardListener[0];

    private Thread running;

    private CardManager() {
    }

    public static ICardManager getInstance() {
        return SINGLETON;
    }

    public void addListener(IFileListener fileListener) {
        if (fileListener != null) {
            fileListeners.add(fileListener);
            start();
        }
    }

    public void addListener(ICardListener cardListener) {
        if (cardListener != null) {
            cardListeners.add(cardListener);
            start();
        }
    }

    public void removeListener(IFileListener fileListener) {
        fileListeners.remove(fileListener);
        stop();
    }

    public void removeListener(ICardListener cardListener) {
        cardListeners.remove(cardListener);
        stop();
    }

    private synchronized void start() {
        if (!fileListeners.isEmpty() || !cardListeners.isEmpty()) {
            running = new Thread(this, "Wifi SD Card Manager");
            running.start();
        }
    }

    private synchronized void stop() {
        if (fileListeners.isEmpty() && cardListeners.isEmpty()) {
            running = null;
        }
    }

    @Override
    public void run(LocalNetwork localNetwork) {
        Iterator<IDetector> detectors = serviceLoader.iterator();
        while (detectors.hasNext()) {
            IDetector iDetector = (IDetector) detectors.next();
            try {
                if (!iDetector.isScanning()) {
                    iDetector.scan(localNetwork, new INotifier() {

                        @Override
                        public void newFile(ICard card, byte[] file) {
                        }

                        @Override
                        public void newCard(ICard card) {
                            CardManager.this.newCard(card);
                        }

                        @Override
                        public String getProperty(String string) {
                            if (string.equalsIgnoreCase("mdns.names")) {
                                return "flashair,transiend";
                            }
                            return null;
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void newCard(ICard card) {
        for (ICardListener cardListener : cardListeners.toArray(EMPTY_CARDS)) {
            cardListener.newCard(card);
        }
    }

    @Override
    public void run() {
        LocalNetworkScanner scanner = new LocalNetworkScanner();
        while (running != Thread.currentThread()) {
            try {
                scanner.scan(this);
            } catch (IOException e) {
                LOG.error("scanning failed,trying again", e);
            }
        }
        Iterator<IDetector> detectors = serviceLoader.iterator();
        while (detectors.hasNext()) {
            IDetector iDetector = (IDetector) detectors.next();
            try {
                if (iDetector.isScanning()) {
                    iDetector.stop();
                }
            } catch (Exception e) {
                LOG.error("card detector stop failed", e);
            }
        }
    }
}
