package com.dranilsaarias.nad;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


/**
 * Implementation of {@link CookieStore} for persistence cookies, uses shared
 * preference for storing cookies.
 *
 * @author Manish
 */
public class SiCookieStore2 implements CookieStore {
    private static final String LOG_TAG = "SICookieStore2";
    private static final String COOKIE_PREFS = "com.orb.net.cookieprefs";
    private static final String COOKIE_DOMAINS_STORE = "com.orb.net.CookieStore.domain";
    private static final String COOKIE_DOMAIN_PREFIX = "com.orb.net.CookieStore.domain_";
    private static final String COOKIE_NAME_PREFIX = "com.orb.net.CookieStore.cookie_";

    /*This map here will store all domain to cookies bindings*/
    private final CookieMap map;

    private final SharedPreferences cookiePrefs;

    /**
     * Construct a persistent cookie store.
     *
     * @param context Context to attach cookie store to
     */
    public SiCookieStore2(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        map = new CookieMap();

        // Load any previously stored domains into the store
        String storedCookieDomains = cookiePrefs.getString(COOKIE_DOMAINS_STORE, null);
        if (storedCookieDomains != null) {
            String[] storedCookieDomainsArray = TextUtils.split(storedCookieDomains, ",");
            //split this domains and get cookie names stored for each domain
            for (String domain : storedCookieDomainsArray) {
                String storedCookiesNames = cookiePrefs.getString(COOKIE_DOMAIN_PREFIX + domain,
                        null);
                //so now we have these cookie names
                if (storedCookiesNames != null) {
                    //split these cookie names and get serialized cookie stored
                    String[] storedCookieNamesArray = TextUtils.split(storedCookiesNames, ",");
                    if (storedCookieNamesArray != null) {
                        //in this list we store all cookies under one URI
                        List<HttpCookie> cookies = new ArrayList<>();
                        for (String cookieName : storedCookieNamesArray) {
                            String encCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + domain
                                    + cookieName, null);
                            //now we deserialize or unserialize (whatever you call it) this cookie
                            //and get HttpCookie out of it and pass it to List
                            if (encCookie != null)
                                cookies.add(decodeCookie(encCookie));
                        }
                        map.put(URI.create(domain), cookies);
                    }
                }
            }
        }
    }

    public synchronized void add(URI uri, HttpCookie cookie) {
        if (cookie == null) {
            throw new NullPointerException("cookie == null");
        }

        uri = cookiesUri(uri);
        List<HttpCookie> cookies = map.get(uri);
        if (cookies == null) {
            cookies = new ArrayList<>();
            map.put(uri, cookies);
        } else {
            cookies.remove(cookie);
        }
        cookies.add(cookie);

        // Save cookie into persistent store
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.putString(COOKIE_DOMAINS_STORE, TextUtils.join(",", map.keySet()));

        Set<String> names = new HashSet<>();
        for (HttpCookie cookie2 : cookies) {
            names.add(cookie2.getName());
            prefsWriter.putString(COOKIE_NAME_PREFIX + uri + cookie2.getName(),
                    encodeCookie(new SICookie2(cookie2)));
        }
        prefsWriter.putString(COOKIE_DOMAIN_PREFIX + uri, TextUtils.join(",", names));

        prefsWriter.apply();
    }

    public synchronized List<HttpCookie> get(URI uri) {
        if (uri == null) {
            throw new NullPointerException("uri == null");
        }

        List<HttpCookie> result = new ArrayList<>();
        // get cookies associated with given URI. If none, returns an empty list
        List<HttpCookie> cookiesForUri = map.get(uri);
        if (cookiesForUri != null) {
            for (Iterator<HttpCookie> i = cookiesForUri.iterator(); i.hasNext(); ) {
                HttpCookie cookie = i.next();
                if (cookie.hasExpired()) {
                    i.remove(); // remove expired cookies
                } else {
                    result.add(cookie);
                }
            }
        }
        // get all cookies that domain matches the URI
        for (Map.Entry<URI, List<HttpCookie>> entry : map.entrySet()) {
            if (uri.equals(entry.getKey())) {
                continue; // skip the given URI; we've already handled it
            }
            List<HttpCookie> entryCookies = entry.getValue();
            for (Iterator<HttpCookie> i = entryCookies.iterator(); i.hasNext(); ) {
                HttpCookie cookie = i.next();
                if (!HttpCookie.domainMatches(cookie.getDomain(), uri.getHost())) {
                    continue;
                }
                if (cookie.hasExpired()) {
                    i.remove(); // remove expired cookies
                } else if (!result.contains(cookie)) {
                    result.add(cookie);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<HttpCookie> getCookies() {
        List<HttpCookie> result = new ArrayList<>();
        for (List<HttpCookie> list : map.values()) {
            for (Iterator<HttpCookie> i = list.iterator(); i.hasNext(); ) {
                HttpCookie cookie = i.next();
                if (cookie.hasExpired()) {
                    i.remove(); // remove expired cookies
                } else if (!result.contains(cookie)) {
                    result.add(cookie);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<URI> getURIs() {
        List<URI> result = new ArrayList<>(map.getAllURIs());
        result.remove(null); // sigh
        return Collections.unmodifiableList(result);
    }


    public synchronized boolean remove(URI uri, HttpCookie cookie) {
        if (cookie == null) {
            throw new NullPointerException("cookie == null");
        }

        if (map.removeCookie(uri, cookie)) {
            SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
            prefsWriter.putString(COOKIE_DOMAIN_PREFIX + uri,
                    TextUtils.join(",", map.getAllCookieNames(uri)));
            prefsWriter.remove(COOKIE_NAME_PREFIX + uri + cookie.getName());
            prefsWriter.apply();
            return true;
        }
        return false;
    }

    public synchronized boolean removeAll() {
        // Clear cookies from persistent store
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.clear();
        prefsWriter.apply();

        // Clear cookies from local store
        boolean result = !map.isEmpty();
        map.clear();
        return result;
    }

    /**
     * Serializes HttpCookie object into String
     *
     * @param cookie cookie to be encoded, can be null
     * @return cookie encoded as String
     */
    private String encodeCookie(SICookie2 cookie) {
        if (cookie == null)
            return null;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException in encodeCookie", e);
            return null;
        }

        return byteArrayToHexString(os.toByteArray());
    }

    /**
     * Returns HttpCookie decoded from cookie string
     *
     * @param cookieString string of cookie as returned from http request
     * @return decoded cookie or null if exception occured
     */
    private HttpCookie decodeCookie(String cookieString) {
        byte[] bytes = hexStringToByteArray(cookieString);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        HttpCookie cookie = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SICookie2) objectInputStream.readObject()).getCookie();
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException in decodeCookie", e);
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG, "ClassNotFoundException in decodeCookie", e);
        }

        return cookie;
    }

    /**
     * Using some super basic byte array &lt;-&gt; hex conversions so we don't
     * have to rely on any large Base64 libraries. Can be overridden if you
     * like!
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * Converts hex values from strings to byte arra
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    private URI cookiesUri(URI uri) {
        if (uri == null) {
            return null;
        }
        try {
            return new URI(uri.getScheme(), uri.getHost(), null, null);
        } catch (URISyntaxException e) {
            return uri;
        }
    }

    /**
     * A implementation of {@link Map} for utility class for storing URL cookie map
     *
     * @author Manish
     */
    private class CookieMap implements Map<URI, List<HttpCookie>> {

        private final Map<URI, List<HttpCookie>> map;


        CookieMap() {
            map = new HashMap<>();
        }


        @Override
        public void clear() {
            map.clear();
        }


        @Override
        public boolean containsKey(Object key) {

            return map.containsKey(key);
        }


        @Override
        public boolean containsValue(Object value) {

            return map.containsValue(value);
        }


        @NonNull
        @Override
        public Set<java.util.Map.Entry<URI, List<HttpCookie>>> entrySet() {

            return map.entrySet();
        }


        @Override
        public List<HttpCookie> get(Object key) {

            return map.get(key);
        }


        @Override
        public boolean isEmpty() {

            return map.isEmpty();
        }


        @NonNull
        @Override
        public Set<URI> keySet() {

            return map.keySet();
        }


        @Override
        public List<HttpCookie> put(URI key, List<HttpCookie> value) {

            return map.put(key, value);
        }


        @Override
        public void putAll(@NonNull Map<? extends URI, ? extends List<HttpCookie>> map) {
            this.map.putAll(map);
        }


        @Override
        public List<HttpCookie> remove(Object key) {

            return map.remove(key);
        }


        @Override
        public int size() {

            return map.size();
        }


        @NonNull
        @Override
        public Collection<List<HttpCookie>> values() {

            return map.values();
        }


        Collection<URI> getAllURIs() {
            return map.keySet();
        }


        Collection<String> getAllCookieNames(URI uri) {
            List<HttpCookie> cookies = map.get(uri);
            Set<String> cookieNames = new HashSet<>();
            for (HttpCookie cookie : cookies) {
                cookieNames.add(cookie.getName());
            }
            return cookieNames;
        }

        boolean removeCookie(URI uri, HttpCookie httpCookie) {
            return map.containsKey(uri) && map.get(uri).remove(httpCookie);
        }

    }
}
