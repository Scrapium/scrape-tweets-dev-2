package com.scrapium;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.proxy.ProxyServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class Main {
    private static final String CONSUMER_KEY = "3rJOl1ODzm9yZy63FACdg";
    private static final String CONSUMER_SECRET = "5jPoQ5kQvMJFDYRNE8bQ4rHuds4xJqhvgNJM4awaE8";
    private static final String URL = "https://api.twitter.com/graphql/TFwspiiwvDFwJDqgs6-Bcw/SearchTimeline?features=%7B%22graphql_unified_card_enabled%22%3Atrue%2C%22tweetypie_unmention_optimization_enabled%22%3Atrue%2C%22view_counts_everywhere_api_enabled%22%3Atrue%2C%22unified_cards_ad_metadata_container_dynamic_card_content_query_enabled%22%3Atrue%2C%22freedom_of_speech_not_reach_fetch_enabled%22%3Atrue%2C%22trusted_friends_api_enabled%22%3Atrue%2C%22tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled%22%3Afalse%2C%22tweet_with_visibility_results_prefer_gql_tweet_interstitials_enabled%22%3Atrue%2C%22c9s_tweet_anatomy_moderator_badge_enabled%22%3Afalse%2C%22profile_foundations_has_spaces_graphql_enabled%22%3Afalse%2C%22ios_notifications_replies_mentions_device_follow_enabled%22%3Atrue%2C%22birdwatch_consumption_enabled%22%3Afalse%2C%22rito_safety_mode_features_enabled%22%3Afalse%2C%22tweet_awards_tweet_api_enabled%22%3Afalse%2C%22tweet_with_visibility_results_prefer_gql_soft_interventions_enabled%22%3Atrue%7D&variables=%7B%22include_community_tweet_relationship%22%3Afalse%2C%22raw_query%22%3A%22test%22%2C%22product%22%3A%22Top%22%2C%22include_tweet_quick_promote_eligibility%22%3Afalse%2C%22include_professional%22%3Afalse%2C%22include_conversation_context%22%3Afalse%2C%22skip_author_community_relationship%22%3Afalse%2C%22include_is_member%22%3Afalse%2C%22include_reply_device_follow%22%3Afalse%2C%22include_unmention_info_override%22%3Afalse%2C%22include_dm_muting%22%3Afalse%2C%22query_source%22%3A%22typed_query%22%2C%22is_member_target_user_id%22%3A%220%22%7D";

    public static String quotePlus(String s) throws UnsupportedEncodingException {
        String result = URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        return result;
    }
    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, GeneralSecurityException, IOException {
        URI uri = new URI(URL);
        String baseUrl = uri.getScheme() + "://" + uri.getHost() + uri.getPath();
        Map<String, String> params = splitQuery(uri);

        //Map<String, String> headers = new HashMap<>();
        HttpHeaders header1 = new DefaultHttpHeaders();

        header1.set("User-Agent", "Twitter-iPhone/9.63 iOS/15.7.6 (Apple;iPhone8,4;;;;;1;2015)")
        //headers.put("User-Agent", "Twitter-iPhone/9.63 iOS/15.7.6 (Apple;iPhone8,4;;;;;1;2015)");

        UUID uuid = UUID.randomUUID();

        Map<String, String> oauthParams = new TreeMap<>();
        oauthParams.put("oauth_consumer_key", CONSUMER_KEY);
        oauthParams.put("oauth_nonce", uuid.toString());
        oauthParams.put("oauth_signature_method", "HMAC-SHA1");
        oauthParams.put("oauth_timestamp", String.valueOf(Instant.now().getEpochSecond()));
        oauthParams.put("oauth_version", "1.0");

        Map<String, String> allParams = new TreeMap<>();
        allParams.putAll(params);
        allParams.putAll(oauthParams);

        //String baseString = "GET&" + URLEncoder.encode(baseUrl, StandardCharsets.UTF_8).replace("+", "%20") + "&" + URLEncoder.encode(buildQuery(allParams), StandardCharsets.UTF_8).replace("+", "%20");
        String baseString = "GET&" + quotePlus(baseUrl) + "&" + quotePlus(buildQuery(allParams));



        String signingKey = CONSUMER_SECRET + "&";
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(signingKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] rawHmac = mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(rawHmac);

        oauthParams.put("oauth_signature", signature);

        String authHeader = "OAuth " + buildOAuthHeader(oauthParams);
        //headers.put("Authorization", authHeader);
        header1.set("Authorization", authHeader);

        String finalUrl = uri.getScheme() + "://" + uri.getHost() + uri.getPath() + "?" + buildQuery(params);

        /*
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                        .build())
                .build()) {
            HttpGet request = new HttpGet(finalUrl);
            headers.forEach(request::addHeader);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            System.out.println(responseString);
        }*/

        DefaultAsyncHttpClientConfig clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setConnectTimeout(8000)
                .setRequestTimeout(8000)
                .setReadTimeout(5000)
                .setMaxConnections(5000)
                .setMaxRequestRetry(1)
                .build();
        AsyncHttpClient client = asyncHttpClient(clientConfig);



        Request request1 = new RequestBuilder("GET")
                .setUrl("http://httpforever.com")
                .setHeaders(header1)
                .build();

        client.executeRequest(request1, new handler(c, proxy, task, this));

        
    }

    private static Map<String, String> splitQuery(URI uri) {
        if (uri.getQuery() == null || uri.getQuery().isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> result = new LinkedHashMap<>();
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            result.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return result;
    }

    private static String buildQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private static String buildOAuthHeader(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append("=\"");
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            sb.append('\"');
        }
        return sb.toString();
    }
}