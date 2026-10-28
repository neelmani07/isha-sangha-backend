package com.ishaworld.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TrustedProxyFilterTest {

    private final TrustedProxyFilter filter = new TrustedProxyFilter("172.64.0.0/13,2606:4700::/32");

    @Test
    void usesForwardedAddressWhenCallerIsTrustedProxy() {
        assertThat(filter.clientAddress("172.71.222.36", "103.191.115.58")).isEqualTo("103.191.115.58");
    }

    @Test
    void ignoresAddressesTheVisitorAddedThemselves() {
        // A visitor sends "X-Forwarded-For: 1.2.3.4"; Cloudflare appends the real address on the right.
        assertThat(filter.clientAddress("172.71.222.36", "1.2.3.4, 103.191.115.58")).isEqualTo("103.191.115.58");
    }

    @Test
    void skipsChainedTrustedProxies() {
        assertThat(filter.clientAddress("172.71.0.1", "103.191.115.58, 172.70.1.1")).isEqualTo("103.191.115.58");
    }

    @Test
    void worksForIpv6Proxies() {
        assertThat(filter.clientAddress("2606:4700::1", "2401:4900::abcd")).isEqualTo("2401:4900::abcd");
    }

    @Test
    void ignoresHeaderFromUntrustedCaller() {
        assertThat(filter.clientAddress("8.8.8.8", "1.2.3.4")).isNull();
    }

    @Test
    void ignoresHostNamesAndGarbage() {
        assertThat(filter.clientAddress("172.71.222.36", "evil.example.com")).isNull();
        assertThat(filter.clientAddress("172.71.222.36", "")).isNull();
    }

    @Test
    void doesNothingWithoutTrustedRanges() {
        assertThat(new TrustedProxyFilter("").clientAddress("172.71.222.36", "1.2.3.4")).isNull();
    }
}
