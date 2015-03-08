package io.github.jwifisd.api;

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

import io.github.jwifisd.net.LocalNetwork;

import java.io.IOException;

/**
 * The abstract card detector interface. implement this service loader interface
 * if you need a new way of detecting the presens of a wifisd card.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface IDetector {

    /**
     * @return true if this detector is currently scanning for new cards. If the
     *         detector interface does a continuous scan instead of periodically
     *         ones, returning true here will stop the periotic calls to the
     *         scan method.
     */
    boolean isScanning();

    /**
     * Periodically called method to scann the specified local network for new
     * cards.
     * 
     * @param network
     *            the network to scan
     * @param notifier
     *            the notifier to report new cards.
     * @throws IOException
     *             if something unexpected happens durring the network access.
     */
    void scan(LocalNetwork network, INotifier notifier) throws IOException;

    /**
     * stop scanning, probally because nobody is listening anymore.
     */
    void stop();
}
