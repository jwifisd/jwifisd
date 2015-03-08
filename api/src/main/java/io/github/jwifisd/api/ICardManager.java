package io.github.jwifisd.api;

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

import io.github.jwifisd.impl.CardManager;
import io.github.jwifisd.impl.ICardListener;

import java.util.Properties;

/**
 * This is the card manager api, that is the stating point for all access to
 * wifi cards, There will be only one active instance/implementation of theis
 * interface. Access it by calling {@link CardManager#getInstance()}
 * 
 * @author Richard van Nieuwenhoven
 */
public interface ICardManager {

    /**
     * Add a listener for newly detected cards, cards that come in reach of the
     * software.
     * 
     * @param cardListener
     *            the listener to add.
     */
    void addListener(ICardListener cardListener);

    /**
     * Add a listener for new files. this listener will be called if any new
     * file is detected on any card in reach.
     * 
     * @param fileListener
     *            the listener to add.
     */
    void addListener(IFileListener fileListener);

    /**
     * Stop listening for new cards.
     * 
     * @param cardListener
     *            the listener to remove.
     */
    void removeListener(ICardListener cardListener);

    /**
     * Stop listening for new files.
     * 
     * @param fileListener
     *            the listener to remove.
     */
    void removeListener(IFileListener fileListener);

    /**
     * set properties for detetors and cards.
     * 
     * @param properties
     *            the properties to set.
     */
    void setProperties(Properties properties);

}
