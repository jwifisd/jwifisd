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
/**
 * Internal card notifier interface between detectors and the card manager. To
 * report newly detected cards to the card manager.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface INotifier {

    /**
     * empty notifier implementation to avoid null checks.
     */
    INotifier DUMMY = new INotifier() {

        @Override
        public String getProperty(String string) {
            return null;
        }

        @Override
        public void newCard(ICard card) {
        }
    };

    /**
     * If a detector needs some configuration, it should use this method to get
     * the setting.
     * 
     * @param key
     *            the property key to get.
     * @return the string value of the property
     */
    String getProperty(String key);

    /**
     * report the detection of a new card.
     * 
     * @param card
     *            the newly detected card.
     */
    void newCard(ICard card);

}
