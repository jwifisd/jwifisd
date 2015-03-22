package org.jwifisd.api;

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
/**
 * Listener for new files becoming available on a wifisd cards. This event will
 * be used if a new file was detected on a card.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface IFileListener {

    /**
     * get notified of a new wifi file detected on a connected card. Normally
     * the file ist not yet filled with the data that will be loaded on demand.
     * But that is card specific and depends on the available api's.
     * 
     * @param card
     *            the card on with the file was detected.
     * @param wifiFile
     *            the file itself
     */
    void notifyNewFile(ICard card, IWifiFile wifiFile);
}
