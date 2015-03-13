package org.jwifisd.flashair;

/*
 * #%L
 * jwifisd-flashair
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

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

/**
 * a response handler for apache http client that receives a byte array.
 * 
 * @author Richard van Nieuwenhoven
 */
final class ByteResponseHandler implements ResponseHandler<byte[]> {

    /**
     * the first http responce code from the range that respresends a ok.
     */
    private static final int START_HTTP_OK_RESPONSE_RAGE = 200;

    /**
     * the last http responce code from the range that respresends a ok.
     */
    private static final int END_HTTP_OK_RESPONSE_RAGE = 300;

    @Override
    public byte[] handleResponse(HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= START_HTTP_OK_RESPONSE_RAGE && status < END_HTTP_OK_RESPONSE_RAGE) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toByteArray(entity) : null;
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }
}