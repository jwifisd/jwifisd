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
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The sigleton card manager that will controll the wifisd system and all cards
 * commining in its reach.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class CardManager implements ICardManager, Runnable, IDoWithNetwork, IFileListener {

    /**
     * service loader for finding the best card implementation to access a
     * card.
     */
    private static final ServiceLoader<ICardImplentation> CARD_IMPL_SERVICE_LOADER = ServiceLoader.load(ICardImplentation.class);

    /**
     * service loader for the detector implementations.
     */
    private static final ServiceLoader<IDetector> DETECTOR_SERVICE_LOADER = ServiceLoader.load(IDetector.class);

    /**
     * empty listener array, for easy access to toArray().
     */
    private static final ICardListener[] EMPTY_CARDS = new ICardListener[0];

    /**
     * empty listener array, for easy access to toArray().
     */
    private static final IFileListener[] EMPTY_FILES = new IFileListener[0];

    /**
     * the logger to use for logging.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CardManager.class);

    /**
     * the singleton card manager.
     */
    private static final CardManager SINGLETON = new CardManager();

    /**
     * all listeners that are listenling for new cards.
     */
    private Set<ICardListener> cardListeners = Collections.synchronizedSet(new HashSet<ICardListener>());

    /**
     * all currently detected cards accessed by there mac adress.
     */
    private final HashMap<String, ICard> currentCards = new HashMap<>();

    /**
     * all listeners that are listenling for new files.
     */
    private Set<IFileListener> fileListeners = Collections.synchronizedSet(new HashSet<IFileListener>());

    /**
     * properties to use for the detectors.
     */
    private final Properties properties = new Properties();

    /**
     * the currently running detector loop.
     */
    private Thread running;

    /**
     * private constructor to prohibit instantiation other than the singleton.
     */
    private CardManager() {
        properties.put("mdns.names", "flashair,transiend");
    }

    /**
     * @return the singleton instance of the card manager.
     */
    public static ICardManager getInstance() {
        return SINGLETON;
    }

    /**
     * add a listener for new cards. The first added listener will trigger the
     * start of scanning for new cards.
     * 
     * @param cardListener
     *            the file listener to add
     */
    public void addListener(ICardListener cardListener) {
        if (cardListener != null) {
            cardListeners.add(cardListener);
            start();
        }
    }

    /**
     * add a listener for new files. The first added listener will trigger the
     * start of scanning for new cards.
     * 
     * @param fileListener
     *            the file listener to add
     */
    public void addListener(IFileListener fileListener) {
        if (fileListener != null) {
            fileListeners.add(fileListener);
            start();
        }
    }

    @Override
    public void notifyNewFile(ICard card, IWifiFile wifiFile) {
        reportNewFile(card, wifiFile);

    }

    /**
     * remove a card listener.Attention if nobody is listening anymore the
     * scanning will be stopped.
     * 
     * @param cardListener
     *            the card listener to remove
     */
    public void removeListener(ICardListener cardListener) {
        cardListeners.remove(cardListener);
        stop();
    }

    /**
     * remove a file listener. Attention if nobody is listening anymore the
     * scanning will be stopped.
     * 
     * @param fileListener
     *            the file listener to remove
     */
    public void removeListener(IFileListener fileListener) {
        fileListeners.remove(fileListener);
        stop();
    }

    /**
     * a new file was detected on one of the cards, report it to the listeners.
     * 
     * @param card
     *            the card it was detected on.
     * @param wifiFile
     *            the file that was detected.
     */
    public void reportNewFile(ICard card, IWifiFile wifiFile) {
        for (IFileListener fileListener : fileListeners.toArray(EMPTY_FILES)) {
            fileListener.notifyNewFile(card, wifiFile);
        }
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
        Iterator<IDetector> detectors = DETECTOR_SERVICE_LOADER.iterator();
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

    @Override
    public void run(LocalNetwork localNetwork) {
        Iterator<IDetector> detectors = DETECTOR_SERVICE_LOADER.iterator();
        while (detectors.hasNext()) {
            IDetector iDetector = (IDetector) detectors.next();
            try {
                if (!iDetector.isScanning()) {
                    iDetector.scan(localNetwork, new INotifier() {

                        @Override
                        public String getProperty(String key) {
                            return (String) properties.get(key);
                        }

                        @Override
                        public void newCard(ICard card) {
                            CardManager.this.newCard(card);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setProperties(Properties properties) {
        properties.putAll(properties);
    }

    /**
     * lets see if we can find a better implementation for this card? or even
     * for the found better implementation?
     * 
     * @param card
     *            the card to check.
     * @return the better implementation or the card itself.
     */
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

    /**
     * lets see if we can find a better implementation for this card?
     * 
     * @param card
     *            the card to check.
     * @return the better implementation or the card itself.
     */
    protected ICard findBetterImplementation(ICard card) {
        Iterator<ICardImplentation> cardImpl = CARD_IMPL_SERVICE_LOADER.iterator();
        while (cardImpl.hasNext()) {
            ICardImplentation cardImplentation = (ICardImplentation) cardImpl.next();
            ICard newCard = cardImplentation.decreaseLevel(card);
            if (newCard != null && newCard.level() < card.level()) {
                card = newCard;
            }
        }
        return card;
    }

    /**
     * a new card was detected, try to find the best access implementation and
     * register it. broadcast it to the listeners.
     * 
     * @param card
     *            the newly detected card.
     */
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

    /**
     * start the scanning thread, if there are listeners.
     */
    private synchronized void start() {
        if (running == null && (!fileListeners.isEmpty() || !cardListeners.isEmpty())) {
            running = new Thread(this, "Wifi SD Card Manager");
            running.start();
        }
    }

    /**
     * stop the scanning thread, if there are no more listeners.
     */
    private synchronized void stop() {
        if (fileListeners.isEmpty() && cardListeners.isEmpty()) {
            running = null;
        }
    }
}
