package io.github.jwifisd.transcend;

/*
 * #%L
 * jwifisd-transcend
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import io.github.jwifisd.api.ICard;
import io.github.jwifisd.api.ICardImplentation;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranscendWifiSDDetector implements ICardImplentation {

    private static final Logger LOG = LoggerFactory.getLogger(TranscendWifiSDDetector.class);

    @Override
    public ICard decreaseLevel(ICard card) {
        if (card.level() > TranscendWiFiSD.CARD_LEVEL && card.title().toLowerCase().indexOf("transcend") >= 0) {
            try {
                Socket westec = new Socket(card.ipAddress(), 5566);
                if (westec.isConnected()) {
                    return new TranscendWiFiSD(card, westec);
                } else {
                    LOG.warn("strage... the card says its a Trancend but it does not let me connect to 5566");
                    return null;
                }
            } catch (IOException e) {
                LOG.warn("strage... the card says its a Trancend but it does not fullfill the protocol", e);
                return null;
            }
        }
        return null;
    }
}
