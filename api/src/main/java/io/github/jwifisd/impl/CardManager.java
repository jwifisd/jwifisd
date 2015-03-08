package io.github.jwifisd.impl;

/*
 * #%L
 * jwifisd-api
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

import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.ICardImplentation;
import io.github.jwifisd.api.ICardManager;
import io.github.jwifisd.api.IDetector;
import io.github.jwifisd.api.IFileListener;
import io.github.jwifisd.api.INotifier;
import io.github.jwifisd.api.IWifiFile;
import io.github.jwifisd.net.IDoWithNetwork;
import io.github.jwifisd.net.LocalNetwork;
import io.github.jwifisd.net.LocalNetworkScanner;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardManager implements ICardManager, Runnable, IDoWithNetwork, IFileListener {

    private static final CardManager SINGLETON = new CardManager();

    private static final ServiceLoader<IDetector> detectorServiceLoader = ServiceLoader.load(IDetector.class);

    private static final ServiceLoader<ICardImplentation> cardImplServiceLoader = ServiceLoader.load(ICardImplentation.class);

    private Set<IFileListener> fileListeners = Collections.synchronizedSet(new HashSet<IFileListener>());

    private Set<ICardListener> cardListeners = Collections.synchronizedSet(new HashSet<ICardListener>());

    private static final Logger LOG = LoggerFactory.getLogger(CardManager.class);

    private static final ICardListener[] EMPTY_CARDS = new ICardListener[0];

    private static final IFileListener[] EMPTY_FILES = new IFileListener[0];

    private static final HashMap<String, ICard> currentCards = new HashMap<>();

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
        if (running == null && (!fileListeners.isEmpty() || !cardListeners.isEmpty())) {
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
        Iterator<IDetector> detectors = detectorServiceLoader.iterator();
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

    protected synchronized void newCard(ICard card) {
        ICard existingCard = currentCards.get(card.mac());
        if (existingCard != null && existingCard.mac() != null) {
            existingCard.reconnect();
            return;
        }
        card = deepFindBetterImplementation(card);
        for (ICardListener cardListener : cardListeners.toArray(EMPTY_CARDS)) {
            cardListener.newCard(card);
        }
        card.addListener(this);
        currentCards.put(card.mac(), card);
    }

    protected ICard deepFindBetterImplementation(ICard card) {
        if (card.level() > 0) {
            ICard newCard = findBetterImplementation(card);
            while (newCard != null && card.level() > newCard.level()) {
                card = newCard;
                newCard = findBetterImplementation(card);
            }
        }
        return card;
    }

    protected ICard findBetterImplementation(ICard card) {
        Iterator<ICardImplentation> cardImpl = cardImplServiceLoader.iterator();
        while (cardImpl.hasNext()) {
            ICardImplentation cardImplentation = (ICardImplentation) cardImpl.next();
            ICard newCard = cardImplentation.decreaseLevel(card);
            if (newCard != null && newCard.level() < card.level()) {
                card = newCard;
            }
        }
        return card;
    }

    @Override
    public void run() {
        LocalNetworkScanner scanner = new LocalNetworkScanner();
        while (running == Thread.currentThread()) {
            try {
                scanner.scan(this);
            } catch (IOException e) {
                LOG.error("scanning failed,trying again", e);
            }
        }
        Iterator<IDetector> detectors = detectorServiceLoader.iterator();
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

    public void reportNewFile(ICard card, IWifiFile wifiFile) {
        for (IFileListener fileListener : fileListeners.toArray(EMPTY_FILES)) {
            fileListener.notifyNewFile(card, wifiFile);
        }
    }

    @Override
    public void notifyNewFile(ICard card, IWifiFile wifiFile) {
        reportNewFile(card, wifiFile);

    }
}
