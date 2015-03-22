package org.jwifisd.api;

/*
 * #%L
 * jwifisd-api
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

import java.net.InetAddress;

/**
 * Generic api for all available wifi cards.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface ICard {

    /**
     * this level is used for cards that are just detected but have no protocol
     * support yet.
     */
    int BARE_PRIMITIVE_CARD_LEVEL = 100;

    /**
     * Add a file listener to the card that will get notified if a new file was
     * detected on the card.
     * 
     * @param fileListener
     *            the file listener to activate.
     * @return true if the listener was installed. false will happen if the card
     *         is not capable of detecting new files
     */
    boolean addListener(IFileListener fileListener);

    /**
     * @return the browser api to access the card, null if the card does not
     *         support browsing.
     */
    IBrowse browse();

    /**
     * @return the current ip address of the card.
     */
    InetAddress ipAddress();

    /**
     * @return the api level of the card, all levels below 10 are not real cards
     *         but just potential cards. a level 0 is an end implementation and
     *         can be used.
     */
    int level();

    /**
     * @return the network mac address of the card.
     */
    String mac();

    /**
     * break the connection and try to reconnect to the card.
     */
    void reconnect();

    /**
     * disables the file listener.
     * 
     * @param fileListener
     *            the listener to disable.
     * @return true if the listener was disabled.
     */
    boolean removeListener(IFileListener fileListener);

    /**
     * @return the human readable title of the card.
     */
    String title();

}
