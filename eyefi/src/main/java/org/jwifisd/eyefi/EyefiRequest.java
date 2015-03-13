package org.jwifisd.eyefi;

/*
 * #%L
 * jwifisd-eyefi
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

import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public abstract class EyefiRequest {

    public EyefiRequest(String postData) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader(postData));
        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()) {
                if (handleElement(event.asStartElement().getName().getLocalPart(), eventReader)) {
                    continue;
                }
            }
        }
    }

    protected abstract boolean handleElement(String localPart, XMLEventReader eventReader) throws XMLStreamException;

    protected String stringValue(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent event = eventReader.nextEvent();
        String data = event.asCharacters().getData();
        return data;
    }

}
