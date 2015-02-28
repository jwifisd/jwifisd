package io.github.jwifisd.all;

/*
 * #%L
 * jwifisd-all
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

import io.github.jwifisd.api.ICard;
import io.github.jwifisd.impl.CardManager;
import io.github.jwifisd.impl.ICardListener;

import java.io.IOException;

public class ScannAll {

    public static void main(String[] args) throws IOException, InterruptedException {
        CardManager.getInstance().addListener(new ICardListener() {

            @Override
            public void newCard(ICard card) {
                System.out.println("new Card " + card.title());

            }
        });
        while (true) {
            Thread.sleep(500);
        }
    }

}
