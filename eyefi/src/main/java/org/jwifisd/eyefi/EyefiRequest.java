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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * This is the abstract eyefi request parser. the eyefi card will send soap
 * messages to the server, every expected message type is represented by a
 * subclass. These subclasses wil parse the message for the message attributes.
 * this abstract class conains the helper methods to parse the soap xml.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class EyefiRequest {

    /**
     * parse the post data of the request using a xml parser and report every
     * detected attribute to the subclass.
     * 
     * @param postData
     *            the post data containing the soap request.
     * @throws XMLStreamException
     *             is the soap request was illegal formatted.
     */
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

    /**
     * an soap attribute was detected the subclass should get the attribute
     * name/value pair if needed.
     * 
     * @param localPart
     *            the local part of the attribute name
     * @param eventReader
     *            the xml event reader
     * @return true if the attribute was handled.
     * @throws XMLStreamException
     *             if something goes wrong
     */
    protected abstract boolean handleElement(String localPart, XMLEventReader eventReader) throws XMLStreamException;

    /**
     * get the string value of the current attribute from the event reader.
     * 
     * @param eventReader
     *            the xml event reader.
     * @return the string value og the attribute.
     * @throws XMLStreamException
     *             if something goes wrong
     */
    protected String stringValue(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent event = eventReader.nextEvent();
        String data = event.asCharacters().getData();
        return data;
    }

}
