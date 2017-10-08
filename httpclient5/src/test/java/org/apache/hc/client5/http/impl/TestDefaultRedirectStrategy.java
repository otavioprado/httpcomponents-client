/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.client5.http.impl;

import java.net.URI;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.junit.Assert;
import org.junit.Test;

public class TestDefaultRedirectStrategy {

    @Test
    public void testIsRedirectedMovedTemporary() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        Assert.assertFalse(redirectStrategy.isRedirected(httpget, response, context));
        response.setHeader(HttpHeaders.LOCATION, "http://localhost/blah");
        Assert.assertTrue(redirectStrategy.isRedirected(httpget, response, context));
    }

    @Test
    public void testIsRedirectedMovedTemporaryNoLocation() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        Assert.assertFalse(redirectStrategy.isRedirected(httpget, response, context));
    }

    @Test
    public void testIsRedirectedMovedPermanently() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_PERMANENTLY, "Redirect");
        Assert.assertFalse(redirectStrategy.isRedirected(httpget, response, context));
        response.setHeader(HttpHeaders.LOCATION, "http://localhost/blah");
        Assert.assertTrue(redirectStrategy.isRedirected(httpget, response, context));
    }

    @Test
    public void testIsRedirectedTemporaryRedirect() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_TEMPORARY_REDIRECT, "Redirect");
        Assert.assertFalse(redirectStrategy.isRedirected(httpget, response, context));
        response.setHeader(HttpHeaders.LOCATION, "http://localhost/blah");
        Assert.assertTrue(redirectStrategy.isRedirected(httpget, response, context));
    }

    @Test
    public void testIsRedirectedSeeOther() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_SEE_OTHER, "Redirect");
        Assert.assertFalse(redirectStrategy.isRedirected(httpget, response, context));
        response.setHeader(HttpHeaders.LOCATION, "http://localhost/blah");
        Assert.assertTrue(redirectStrategy.isRedirected(httpget, response, context));
    }

    @Test
    public void testIsRedirectedUnknownStatus() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(333, "Redirect");
        Assert.assertFalse(redirectStrategy.isRedirected(httpget, response, context));
        final HttpPost httppost = new HttpPost("http://localhost/");
        Assert.assertFalse(redirectStrategy.isRedirected(httppost, response, context));
    }

    @Test
    public void testIsRedirectedInvalidInput() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_SEE_OTHER, "Redirect");
        try {
            redirectStrategy.isRedirected(null, response, context);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException expected) {
        }
        try {
            redirectStrategy.isRedirected(httpget, null, context);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetLocationUri() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response.addHeader("Location", "http://localhost/stuff");
        final URI uri = redirectStrategy.getLocationURI(httpget, response, context);
        Assert.assertEquals(URI.create("http://localhost/stuff"), uri);
    }

    @Test(expected=HttpException.class)
    public void testGetLocationUriMissingHeader() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        redirectStrategy.getLocationURI(httpget, response, context);
    }

    @Test(expected=ProtocolException.class)
    public void testGetLocationUriInvalidLocation() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response.addHeader("Location", "http://localhost/not valid");
        redirectStrategy.getLocationURI(httpget, response, context);
    }

    @Test
    public void testGetLocationUriRelative() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response.addHeader("Location", "/stuff");
        final URI uri = redirectStrategy.getLocationURI(httpget, response, context);
        Assert.assertEquals(URI.create("http://localhost/stuff"), uri);
    }

    @Test
    public void testGetLocationUriRelativeWithFragment() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response.addHeader("Location", "/stuff#fragment");
        final URI uri = redirectStrategy.getLocationURI(httpget, response, context);
        Assert.assertEquals(URI.create("http://localhost/stuff#fragment"), uri);
    }

    @Test
    public void testGetLocationUriAbsoluteWithFragment() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response.addHeader("Location", "http://localhost/stuff#fragment");
        final URI uri = redirectStrategy.getLocationURI(httpget, response, context);
        Assert.assertEquals(URI.create("http://localhost/stuff#fragment"), uri);
    }

    @Test
    public void testGetLocationUriNormalized() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response.addHeader("Location", "http://localhost/././stuff/../morestuff");
        final URI uri = redirectStrategy.getLocationURI(httpget, response, context);
        Assert.assertEquals(URI.create("http://localhost/morestuff"), uri);
    }

    @Test
    public void testGetLocationUriAllowCircularRedirects() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(true).build();
        context.setRequestConfig(config);
        final URI uri1 = URI.create("http://localhost/stuff1");
        final URI uri2 = URI.create("http://localhost/stuff2");
        final URI uri3 = URI.create("http://localhost/stuff3");
        final HttpGet httpget1 = new HttpGet("http://localhost/");
        final HttpResponse response1 = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response1.addHeader("Location", uri1.toASCIIString());
        final HttpGet httpget2 = new HttpGet(uri1.toASCIIString());
        final HttpResponse response2 = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response2.addHeader("Location", uri2.toASCIIString());
        final HttpGet httpget3 = new HttpGet(uri2.toASCIIString());
        final HttpResponse response3 = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response3.addHeader("Location", uri3.toASCIIString());
        Assert.assertEquals(uri1, redirectStrategy.getLocationURI(httpget1, response1, context));
        Assert.assertEquals(uri2, redirectStrategy.getLocationURI(httpget2, response2, context));
        Assert.assertEquals(uri3, redirectStrategy.getLocationURI(httpget3, response3, context));

        final List<URI> uris = context.getRedirectLocations();
        Assert.assertNotNull(uris);
        Assert.assertTrue(uris.contains(uri1));
        Assert.assertTrue(uris.contains(uri2));
        Assert.assertTrue(uris.contains(uri3));
        Assert.assertEquals(3, uris.size());
        Assert.assertEquals(uri1, uris.get(0));
        Assert.assertEquals(uri2, uris.get(1));
        Assert.assertEquals(uri3, uris.get(2));
    }

    @Test(expected=ProtocolException.class)
    public void testGetLocationUriDisallowCircularRedirects() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/stuff");
        final RequestConfig config = RequestConfig.custom().setCircularRedirectsAllowed(false).build();
        context.setRequestConfig(config);
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response.addHeader("Location", "http://localhost/stuff");
        final URI uri = URI.create("http://localhost/stuff");
        Assert.assertEquals(uri, redirectStrategy.getLocationURI(httpget, response, context));
        redirectStrategy.getLocationURI(httpget, response, context);
    }

    @Test
    public void testGetLocationUriInvalidInput() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        final HttpClientContext context = HttpClientContext.create();
        final HttpGet httpget = new HttpGet("http://localhost/");
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_MOVED_TEMPORARILY, "Redirect");
        response.addHeader("Location", "http://localhost/stuff");
        try {
            redirectStrategy.getLocationURI(null, response, context);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException expected) {
        }
        try {
            redirectStrategy.getLocationURI(httpget, null, context);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException expected) {
        }
        try {
            redirectStrategy.getLocationURI(httpget, response, null);
            Assert.fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException expected) {
        }
    }

    @Test
    public void testCreateLocationURI() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        Assert.assertEquals("http://blahblah/",
                redirectStrategy.createLocationURI("http://BlahBlah").toASCIIString());
    }

    @Test(expected=ProtocolException.class)
    public void testCreateLocationURIInvalid() throws Exception {
        final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        redirectStrategy.createLocationURI(":::::::");
    }

}