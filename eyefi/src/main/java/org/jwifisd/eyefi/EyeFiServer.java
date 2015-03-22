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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.utils.IOUtils;
import org.jwifisd.api.INotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD;

/**
 * This is a eyefi http server that will accept requests from eyefi cards.
 * 
 * @author Richard van Nieuwenhoven
 */
public class EyeFiServer extends NanoHTTPD {

    /**
     * Class representing an uploaded soap attachment. Nanohttpd always saves
     * them as temp files to spare memory.
     * 
     * @author Richard van Nieuwenhoven
     */
    class EyeFiUploadFile {

        /**
         * the temp file with the contents.
         */
        private final File eyefiFile;

        /**
         * the digest of the file.
         * TODO: check the digest!
         */
        private final String integrityDigest;

        /**
         * the soap envelope of the request message.
         */
        private final String soapEnvelope;

        /**
         * constructor for the soap file attachment .
         * 
         * @param session
         *            the http session
         * @param files
         *            the file mappings.
         */
        public EyeFiUploadFile(IHTTPSession session, Map<String, String> files) {
            String pathname = files.get("FILENAME");
            if (pathname == null) {
                throw new IllegalArgumentException("MissingFileName");
            }
            this.eyefiFile = new File(pathname);

            this.integrityDigest = session.getParms().get("INTEGRITYDIGEST");
            this.soapEnvelope = session.getParms().get("SOAPENVELOPE");

        }

    }

    /**
     * buffer size to use when manipulating streams.
     */
    private static final int BUFFER_SIZE = 1024 * 32;

    /**
     * default sleep time when in waiting mode.
     */
    private static final int DEFAULT_SLEEP_TIME = 500;

    /**
     * the default upload key, all Mobi cards use this one.
     */
    private static final String DEFAULT_UPLOADKEY = "00000000000000000000000000000000";

    /**
     * eyefi servers must listen to this port.
     */
    private static final int EYEFI_HTTP_LISTEN_PORT = 59278;

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EyeFiServer.class);

    /**
     * map of all already detected cards.
     */
    private Map<String, EyeFiCard> cards = new HashMap<>();

    /**
     * the notifier to report newly detected cards.
     */
    private INotifier notifier;

    /**
     * status to response lookup.
     * TODO: check cleanup of responses.
     */
    private Map<Integer, PhotoStatusRequest> photoStatuses = new HashMap<>();

    /**
     * id counter to mark unique responces.
     */
    private int photoStatusId = 1;

    /**
     * create the eyefi server.
     * 
     * @param notifier
     *            the listener to report new cards.
     */
    public EyeFiServer(INotifier notifier) {
        super(EYEFI_HTTP_LISTEN_PORT);
        this.notifier = notifier != null ? notifier : INotifier.DUMMY;
    }

    /**
     * main method to start the server separately without notifiers.
     * 
     * @param args
     *            ignored
     * @throws Exception
     *             if something goes wrong
     */
    public static void main(String[] args) throws Exception {
        EyeFiServer server = new EyeFiServer(null);
        server.start();
        while (true) {
            Thread.sleep(DEFAULT_SLEEP_TIME);
        }
    }

    /**
     * calculate the md5 of all the byte arrays specified as parameters.
     * 
     * @param args
     *            the byte arrays to create the md5 over
     * @return the md5 digest.
     */
    public static byte[] md5(byte[]... args) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            for (byte[] arg : args) {
                digest.update(arg);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> files = new HashMap<String, String>();
        try {
            session.parseBody(files);
            String soapAction = session.getHeaders().get("soapaction");
            if (soapAction == null) {
                soapAction = "";
            }
            if (String.valueOf(session.getUri()).startsWith("/api/soap/eyefilm/v1")) {
                if (soapAction.indexOf("MarkLastPhotoInRoll") >= 0) {
                    return new MarkLastPhotoInRollResponse();
                } else if (String.valueOf(session.getUri()).startsWith("/api/soap/eyefilm/v1/upload")) {
                    return uploadPhoto(new EyeFiUploadFile(session, files));
                } else {
                    if (soapAction.indexOf("StartSession") >= 0) {
                        return startSession(files, session.getHeaders().get("http-client-ip"));
                    } else if (soapAction.indexOf("GetPhotoStatus") >= 0) {
                        return getPhotoStatus(files);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FaultResponse();
    }

    /**
     * eyefi sends the files tarred w will have to untar it first.
     * 
     * @param tarFile
     *            the tar file
     * @param out
     *            output stream to write the untarred contents.
     * @return true if successful
     * @throws Exception
     *             if the file could not be untarred.
     */
    private boolean extractTarFile(File tarFile, OutputStream out) throws Exception {
        boolean result = false;
        byte[] tarBytes = IOUtils.toByteArray(new FileInputStream(tarFile));
        // This is stangege but posix was not correctly detected by the apache
        // lib, so we change some bytes so the detection will work.
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes("US-ASCII"), 0, tarBytes, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);

        TarArchiveInputStream tar = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes));

        final byte[] buffer = new byte[BUFFER_SIZE];
        try {
            ArchiveEntry tarEntry = tar.getNextEntry();
            if (tarEntry != null) {
                int n = 0;
                while ((n = tar.read(buffer)) >= 0) {
                    out.write(buffer, 0, n);
                }
                result = true;
            }
        } finally {
            tar.close();
            out.close();
        }
        return result;
    }

    /**
     * handle the get photo status request and create a GetPhotoStatusResponse
     * response.
     * 
     * @param files
     *            the file attachments
     * @return the GetPhotoStatusResponse response
     * @throws Exception
     *             if something goes wrong
     */
    private Response getPhotoStatus(Map<String, String> files) throws Exception {
        PhotoStatusRequest photoStatus = new PhotoStatusRequest(files.get("postData"));
        int statusId = photoStatusId++;
        photoStatuses.put(statusId, photoStatus);
        if (LOG.isDebugEnabled()) {
            LOG.debug("unsing responce status" + photoStatus.toString());
        }
        return new GetPhotoStatusResponse(statusId);
    }

    /**
     * check if the card is new, and report it if it is to the card manager.
     * 
     * @param startSession
     *            the start session soap request
     * @throws UnknownHostException
     *             if the ip address could not be resolved (should not happen)
     */
    private void registerCart(StartSessionRequest startSession) throws UnknownHostException {
        EyeFiCard card = cards.get(startSession.getMacaddress());
        if (card == null) {
            card = new EyeFiCard(startSession);
            cards.put(startSession.getMacaddress(), card);
            notifier.newCard(card);
        }
    }

    /**
     * handle the start session request and create a StartSessionResponse for
     * it. report the card to the notifier if it is a new one.
     * 
     * @param files
     *            the file attachments
     * @param remoteIp
     *            the ip of the card.
     * @return the appropriate StartSessionResponse.
     * @throws Exception
     *             if something goes wrong
     */
    private Response startSession(Map<String, String> files, String remoteIp) throws Exception {
        StartSessionRequest startSession = new StartSessionRequest(files.get("postData"), remoteIp);
        registerCart(startSession);
        if (LOG.isDebugEnabled()) {
            LOG.debug("start session " + startSession.toString());
        }
        byte[] cnonceBytes = Hex.decodeHex(startSession.getCnonce().toCharArray());
        byte[] credential = md5(Hex.decodeHex(startSession.getMacaddress().toCharArray()), cnonceBytes, uploadKey(startSession));
        return new StartSessionResponse(Hex.encodeHexString(credential), startSession.getCnonce(), startSession.getTransfermode(), startSession.getTransfermodetimestamp());
    }

    /**
     * get the upload key for an eyefi card and use the default (for all mobi
     * cards) if there is none.
     * 
     * @param startSession
     *            the start session soap request.
     * @return the upload key
     * @throws Exception
     *             if something goes wrong (should not happen)
     */
    private byte[] uploadKey(StartSessionRequest startSession) throws Exception {
        String uploadKey = notifier.getProperty("uploadkey-" + startSession.getMacaddress());
        if (uploadKey == null || uploadKey.isEmpty()) {
            uploadKey = DEFAULT_UPLOADKEY;
        }
        try {
            return Hex.decodeHex(uploadKey.toCharArray());
        } catch (DecoderException e) {
            LOG.error("could not decode upload key, revert to using the default!", e);
            return Hex.decodeHex(DEFAULT_UPLOADKEY.toCharArray());
        }
    }

    /**
     * an upload photo request war send handle it.
     * 
     * @param context
     *            the uploaded file
     * @return the response to send back.
     * @throws Exception
     *             if something goes wrong
     */
    private Response uploadPhoto(EyeFiUploadFile context) throws Exception {
        UploadPhotoRequest uploadRequest = new UploadPhotoRequest(context.soapEnvelope);
        System.out.println(uploadRequest.toString());
        String fileName = uploadRequest.getFilename().replace(".tar", "");
        ByteArrayOutputStream fout = new ByteArrayOutputStream();
        boolean extraced = extractTarFile(context.eyefiFile, fout);
        cards.get(uploadRequest.getMacaddress()).reportNewFile(new EyeFiPhoto(fileName, fout.toByteArray()));
        return new UploadPhotoResponse(extraced);
    }

}
