package com.ishaworld.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Behind a CDN (Render puts Cloudflare in front of the app), every request arrives from one of a few proxy addresses.
 * When the direct caller is in app.trusted-proxies, the visitor's real address is taken from X-Forwarded-For,
 * read right to left past trusted proxies, so a visitor can't fake it by sending the header themselves.
 * Sign-in rate limits use this address.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrustedProxyFilter extends OncePerRequestFilter {

    private static final Pattern IP_LITERAL = Pattern.compile("^[0-9a-fA-F:.]{2,45}$");

    private final List<Cidr> trusted;

    public TrustedProxyFilter(@Value("${app.trusted-proxies:}") String trustedProxies) {
        List<Cidr> list = new ArrayList<>();
        Arrays.stream(trustedProxies.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(s -> list.add(Cidr.parse(s)));
        this.trusted = List.copyOf(list);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String client = trusted.isEmpty() ? null : clientAddress(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
        if (client == null) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(new HttpServletRequestWrapper(request) {
            @Override
            public String getRemoteAddr() {
                return client;
            }

            @Override
            public String getRemoteHost() {
                return client;
            }
        }, response);
    }

    /** The visitor's address, or null to keep the direct caller's address. */
    String clientAddress(String peer, String forwardedFor) {
        if (!isTrusted(peer) || forwardedFor == null || forwardedFor.isBlank()) {
            return null;
        }
        String[] hops = forwardedFor.split(",");
        for (int i = hops.length - 1; i >= 0; i--) {
            String hop = hops[i].trim();
            if (!IP_LITERAL.matcher(hop).matches()) {
                return null;
            }
            if (!isTrusted(hop)) {
                return hop;
            }
        }
        return null;
    }

    private boolean isTrusted(String address) {
        InetAddress ip = parse(address);
        return ip != null && trusted.stream().anyMatch(c -> c.contains(ip));
    }

    /** Parses an IP literal only; never does a DNS lookup. */
    private static InetAddress parse(String address) {
        if (address == null || !IP_LITERAL.matcher(address).matches()) {
            return null;
        }
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    private record Cidr(byte[] network, int prefix) {

        static Cidr parse(String cidr) {
            String[] parts = cidr.split("/");
            InetAddress address = TrustedProxyFilter.parse(parts[0]);
            if (address == null) {
                throw new IllegalArgumentException("Invalid trusted proxy range: " + cidr);
            }
            int bits = address.getAddress().length * 8;
            int prefix = parts.length > 1 ? Integer.parseInt(parts[1]) : bits;
            return new Cidr(address.getAddress(), prefix);
        }

        boolean contains(InetAddress ip) {
            byte[] candidate = ip.getAddress();
            if (candidate.length != network.length) {
                return false;
            }
            int shift = network.length * 8 - prefix;
            return new BigInteger(1, candidate).shiftRight(shift).equals(new BigInteger(1, network).shiftRight(shift));
        }
    }
}
