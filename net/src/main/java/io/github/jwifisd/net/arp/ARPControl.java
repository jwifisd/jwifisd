package io.github.jwifisd.net.arp;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ARPControl {

    private static final Pattern IP_ADRESS_REGEX = Pattern
            .compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])");

    private static final Pattern MAC_ADRESS_REGEX = Pattern.compile("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})");

    public static void main(String[] args) throws Exception {
        Set<ARP> arps = scanArpCache(null);
        for (ARP arp : arps) {
            System.out.println(arp.getIpAdress() + " " + arp.getMacAdress());
        }
    }

    public static String macAdressOf(final InetAddress address) throws IOException, InterruptedException {
        Set<ARP> arps = scanArpCache(new ARPFilter() {

            @Override
            public boolean accep(ARP arp) {
                return arp.getIpAdress().equals(address);
            }
        });
        if (arps.isEmpty()) {
            return null;
        } else {
            return arps.iterator().next().getMacAdress();
        }
    }

    public static Set<ARP> scanArpCache(ARPFilter filter) throws IOException, InterruptedException {
        Set<ARP> arps = new HashSet<>();
        getMacFromArpCache(filter, arps);
        callArpTool(filter, arps);
        return arps;
    }

    private static void getMacFromArpCache(ARPFilter filter, Set<ARP> arps) {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"))) {
            String line;
            while ((line = br.readLine()) != null) {
                scanLineForArp(filter, arps, line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scanLineForArp(ARPFilter filter, Set<ARP> arps, String line) {
        ARP arp = new ARP();
        Matcher matcher = MAC_ADRESS_REGEX.matcher(line);
        if (matcher.find()) {
            arp.setMacAdress(line.substring(matcher.start(), matcher.end() + 1).trim().replace('-', ':'));
            Matcher matcherIp = IP_ADRESS_REGEX.matcher(line);
            if (matcherIp.find()) {
                try {
                    arp.setIpAdress(InetAddress.getByName(line.substring(matcherIp.start(), matcherIp.end() + 1).trim()));
                } catch (UnknownHostException e) {
                    // igonre line
                }
            }
        }
        if (arp.getIpAdress() != null && arp.getMacAdress() != null && (filter == null || filter.accep(arp))) {
            arps.add(arp);
        }
    }

    private static boolean isWindows() {
        String osString = System.getProperty("os.name");
        final String os = osString.toLowerCase(Locale.ENGLISH);
        return os.contains("windows");
    }

    private static void callArpTool(ARPFilter filter, Set<ARP> arps) throws IOException, InterruptedException {
        if (isWindows()) {
            runArp(filter, arps, "arp", "-a");
        } else {
            runArp(filter, arps, "arp", "-a", "-n");
        }
    }

    private static void runArp(ARPFilter filter, Set<ARP> arps, String... strings) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(strings);
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            scanLineForArp(filter, arps, line);
        }
    }

}
