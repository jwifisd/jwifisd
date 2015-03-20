package org.jwifisd.net.arp;

/*
 * #%L
 * jwifisd-net
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a system arp access class that will try to resolve the mac/ip address
 * combination of all known hosts.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class ARPControl {

    /**
     * the logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ARPControl.class);

    /**
     * we wait this amount of millisecond after the trigger of a tcp connection
     * to the ip address, it should be time enough to fill the arp cache.
     */
    private static final int MILLISECONDS_TO_WAIT_AFTER_THE_ARP_CACHE_TRIGGER = 100;

    /**
     * timeout to use for the arp cache trigger.
     */
    private static final int MILLISECONDS_TIMEOUT_FOR_ARP_CACHE_TRIGGER = 1000;

    /**
     * number of bytes in a ip v4 address.
     */
    private static final int NR_OF_BYTES_IN_IP_V4 = 4;

    /**
     * just an ip port to connect to, that will "force" an arp lookup on a
     * system level. that way we can resolve arp entries of addresses that did
     * not jet send tcp packets.
     */
    private static final int ECHO_PROTOKOL = 7;

    /**
     * regex pattern for ip addresses.
     */
    private static final Pattern IP_ADRESS_REGEX = Pattern
            .compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])");

    /**
     * regex pattern for one byte.
     */
    private static final Pattern IP_BYTE_REGEX = Pattern.compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])");

    /**
     * regex for a mac address.
     */
    private static final Pattern MAC_ADRESS_REGEX = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})");

    /**
     * utility class so only a not usesed private constructor.
     */
    private ARPControl() {
    }

    /**
     * convert a ip adress that is represented as a list of integers(bytes)
     * separated by dots to a 4 byte address.
     * 
     * @param hostIpString
     *            the ip address string
     * @return the 4 byte ip address
     */
    public static byte[] convertIpDottedNameToByteAdress(String hostIpString) {
        byte[] ipAdressInBytes = new byte[NR_OF_BYTES_IN_IP_V4];
        StringBuffer buffer = new StringBuffer();
        int index = 0;
        for (char character : hostIpString.toCharArray()) {
            if (Character.isDigit(character)) {
                buffer.append(character);
            } else {
                ipAdressInBytes[index++] = (byte) Integer.parseInt(buffer.toString());
                buffer.setLength(0);
            }
        }
        if (index < NR_OF_BYTES_IN_IP_V4) {
            ipAdressInBytes[index++] = (byte) Integer.parseInt(buffer.toString());
        }
        return ipAdressInBytes;
    }

    /**
     * resolve the mac address of the specified ip address.
     * 
     * @param address
     *            the ip address to resolve th mac for
     * @return the resolved mac address or null if it could not be found.
     * @throws IOException
     *             if resources could not be read.
     */
    public static String macAdressOf(final InetAddress address) throws IOException {
        Set<ARP> arps = scanArpCache(new ARPFilter() {

            @Override
            public boolean accept(ARP arp) {
                return arp.getIpAdress().equals(address);
            }
        });
        if (arps.isEmpty()) {
            // ok lets try to trigger a arp lookup
            triggerArpLookup(address);
            arps = scanArpCache(new ARPFilter() {

                @Override
                public boolean accept(ARP arp) {
                    return arp.getIpAdress().equals(address);
                }
            });
        }
        if (arps.isEmpty()) {
            return null;
        } else {
            return arps.iterator().next().getMacAdress();
        }
    }

    /**
     * main to print the arp cache to the console.
     * 
     * @param args
     *            no args nessesary
     * @throws Exception
     *             if the cache could not be read.
     */
    public static void main(String[] args) throws Exception {
        Set<ARP> arps = scanArpCache(null);
        for (ARP arp : arps) {
            System.out.println(arp.getIpAdress() + " " + arp.getMacAdress());
        }
    }

    /**
     * scan the arp cache for cache entries that pass the filter.
     * 
     * @param filter
     *            cache filter
     * @return the list of resolved arp entries that pass the filter
     * @throws IOException
     *             if the cache could not be read.
     */
    public static Set<ARP> scanArpCache(ARPFilter filter) throws IOException {
        Set<ARP> arps = new HashSet<>();
        getMacFromArpCache(filter, arps);
        callArpTool(filter, arps);
        return arps;
    }

    /**
     * lets try to connect to a port of the specified ip address, it will
     * probably fail but it will trigger a arp cache entry for the ip address.
     * 
     * @param address
     *            the address to trigger a tcp connection.
     */
    public static void triggerArpLookup(final InetAddress address) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Socket socket = null;
                try {
                    socket = new Socket(address, ECHO_PROTOKOL);
                    socket.setSoTimeout(MILLISECONDS_TIMEOUT_FOR_ARP_CACHE_TRIGGER);
                    socket.close();
                } catch (Exception e) {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (Exception e1) {
                            LOG.trace("just ignore the close exception");
                        }
                    }
                }
            }
        }, "arp lookup trigger").start();
        try {
            Thread.sleep(MILLISECONDS_TO_WAIT_AFTER_THE_ARP_CACHE_TRIGGER);
        } catch (InterruptedException e) {
            return;
        }
    }

    /**
     * call the ARP command line program, available in unix and windows and scan
     * the output for mac addresses.
     * 
     * @param filter
     *            the filter to use
     * @param arps
     *            the current list of arps to extend
     * @throws IOException
     *             if resources could no be accessed.
     */
    private static void callArpTool(ARPFilter filter, Set<ARP> arps) throws IOException {
        if (isWindows()) {
            runArp(filter, arps, "arp", "-a");
        } else {
            runArp(filter, arps, "arp", "-a", "-n");
        }
    }

    /**
     * try to read the linux arp cache file in the proc file system, also
     * available on android.
     * 
     * @param filter
     *            the filter to use
     * @param arps
     *            the current list of arps to extend
     */
    private static void getMacFromArpCache(ARPFilter filter, Set<ARP> arps) {
        File arpFileCache = new File("/proc/net/arp");
        if (arpFileCache.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(arpFileCache))) {
                String line;
                while ((line = br.readLine()) != null) {
                    scanLineForArp(filter, arps, line);
                }
            } catch (Exception e) {
                LOG.warn("the arp file existed but could not be read, ignoring the file!", e);
            }
        }
    }

    /**
     * @return true is we are running on a windows system.
     */
    private static boolean isWindows() {
        String osString = System.getProperty("os.name");
        final String os = osString.toLowerCase(Locale.ENGLISH);
        return os.contains("windows");
    }

    /**
     * call the ARP command line program, available in unix and windows and scan
     * the output for mac addresses.
     * 
     * @param filter
     *            the filter to use
     * @param arps
     *            the current list of arps to extend
     * @param strings
     *            command line parameter to use for the arp command.
     * @throws IOException
     *             if resources could no be accessed.
     */
    private static void runArp(ARPFilter filter, Set<ARP> arps, String... strings) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(strings);
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            scanLineForArp(filter, arps, line);
        }
    }

    /**
     * scan one line of arp cache, if it contains a valid ip address and a valid
     * mac address, we use the pair to create an arp object and if it passes the
     * filter it will be added to the collection.
     * 
     * @param filter
     *            the filter to use
     * @param arps
     *            the current list of arps to extend
     * @param line
     *            one line of the arp cache
     */
    private static void scanLineForArp(ARPFilter filter, Set<ARP> arps, String line) {
        ARP arp = new ARP();
        Matcher matcher = MAC_ADRESS_REGEX.matcher(line);
        if (matcher.find()) {
            arp.setMacAdress(line.substring(matcher.start(), matcher.end() + 1).trim().replace('-', ':'));
            Matcher matcherIp = IP_ADRESS_REGEX.matcher(line);
            if (matcherIp.find()) {
                String hostIpString = line.substring(matcherIp.start(), matcherIp.end() + 1).trim();
                try {
                    arp.setIpAdress(InetAddress.getByAddress(convertIpDottedNameToByteAdress(hostIpString)));
                } catch (UnknownHostException e) {
                    LOG.warn("the line from the arp looked like an ip address but it could not be resolved as one (" + hostIpString + ")", e);
                }
            }
        }
        if (arp.getIpAdress() != null && arp.getMacAdress() != null && (filter == null || filter.accept(arp))) {
            arps.add(arp);
        }
    }

}
