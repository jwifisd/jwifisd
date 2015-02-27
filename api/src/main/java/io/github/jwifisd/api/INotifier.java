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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

public interface INotifier {

    public static INotifier DUMMY = new INotifier() {

        @Override
        public void newFile(ICard card, byte[] file) {
        }

        @Override
        public void newCard(ICard card) {
        }

        @Override
        public String getProperty(String string) {
            return null;
        }
    };

    void newCard(ICard card);

    void newFile(ICard card, byte[] file);

    String getProperty(String string);

}
