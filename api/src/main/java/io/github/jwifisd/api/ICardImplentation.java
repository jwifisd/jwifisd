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
/**
 * API to create a higher level API over a card. For example a newer version of
 * a card has a better way to do something special. All cards of jwifids will
 * never go lower than 1 so you can extent the implementation by using lower
 * numbers. You can provide implementations by using the java service loader
 * pattern.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface ICardImplentation {

    /**
     * A card was detected and this implementation is asked if it can create a
     * better (lower level number) API for the card based on the card specified.
     * 
     * @param card
     *            the card to check for a better implementation
     * @return the higher level card to use instead of the card specified asd a
     *         parameter.
     */
    ICard decreaseLevel(ICard card);
}
