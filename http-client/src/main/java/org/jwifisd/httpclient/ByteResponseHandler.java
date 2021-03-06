package org.jwifisd.httpclient;

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
public final class ByteResponseHandler implements ResponseHandler<byte[]> {

    /**
     * the first http response code from the range that represents a ok.
     */
    protected static final int START_HTTP_OK_RESPONSE_RANGE = 200;

    /**
     * the last http response code from the range that represents a ok.
     */
    protected static final int END_HTTP_OK_RESPONSE_RANGE = 300;

    @Override
    public byte[] handleResponse(HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status >= START_HTTP_OK_RESPONSE_RANGE && status < END_HTTP_OK_RESPONSE_RANGE) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toByteArray(entity) : null;
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }
}
