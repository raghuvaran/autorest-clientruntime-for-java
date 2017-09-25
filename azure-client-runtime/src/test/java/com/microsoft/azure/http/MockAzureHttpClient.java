/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.http;

import com.google.common.io.CharStreams;
import com.microsoft.azure.AzureAsyncOperationPollStrategy;
import com.microsoft.azure.HttpBinJSON;
import com.microsoft.azure.LocationPollStrategy;
import com.microsoft.azure.MockResource;
import com.microsoft.azure.OperationResource;
import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.HttpHeader;
import com.microsoft.rest.http.HttpHeaders;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * This HttpClient attempts to mimic the behavior of http://httpbin.org without ever making a network call.
 */
public class MockAzureHttpClient extends HttpClient {
    private int pollsRemaining;

    private int getRequests;
    private int createRequests;
    private int deleteRequests;
    private int pollRequests;

    public int getRequests() {
        return getRequests;
    }

    public int createRequests() {
        return createRequests;
    }

    public int deleteRequests() {
        return deleteRequests;
    }

    public int pollRequests() {
        return pollRequests;
    }

    @Override
    public Single<HttpResponse> sendRequestInternalAsync(HttpRequest request) {
        MockAzureHttpResponse response = null;

        try {
            final URI requestUrl = new URI(request.url());
            final String requestHost = requestUrl.getHost();
            final String requestPath = requestUrl.getPath();
            final String requestPathLower = requestPath.toLowerCase();
            if (requestHost.equalsIgnoreCase("httpbin.org")) {
                if (requestPathLower.equals("/anything") || requestPathLower.startsWith("/anything/")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url()
                            // This is just to mimic the behavior we've seen with httpbin.org.
                            .replace("%20", " ");
                    json.headers = toMap(request.headers());
                    response = new MockAzureHttpResponse(200, json);
                }
                else if (requestPathLower.startsWith("/bytes/")) {
                    final String byteCountString = requestPath.substring("/bytes/".length());
                    final int byteCount = Integer.parseInt(byteCountString);
                    response = new MockAzureHttpResponse(200, new byte[byteCount]);
                }
                else if (requestPathLower.equals("/delete")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockAzureHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/get")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.url = request.url();
                    json.headers = toMap(request.headers());
                    response = new MockAzureHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/patch")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockAzureHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/post")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockAzureHttpResponse(200, json);
                }
                else if (requestPathLower.equals("/put")) {
                    final HttpBinJSON json = new HttpBinJSON();
                    json.data = bodyToString(request);
                    response = new MockAzureHttpResponse(200, json);
                }
            }
            else if (requestHost.equalsIgnoreCase("mock.azure.com")) {
                if (request.httpMethod().equalsIgnoreCase("GET")) {
                    if (requestPathLower.contains("/mockprovider/mockresources/")) {
                        ++getRequests;
                        final MockResource resource = new MockResource();
                        resource.name = requestPath.substring(requestPath.lastIndexOf('/') + 1);
                        response = new MockAzureHttpResponse(200, resource);
                    }
                    else if (requestPathLower.contains("/mockprovider/mockoperations/")) {
                        ++pollRequests;

                        final Map<String,String> requestQueryMap = queryToMap(requestUrl.getQuery());

                        final String pollType = requestQueryMap.get("PollType");

                        if (pollType.equalsIgnoreCase(AzureAsyncOperationPollStrategy.HEADER_NAME)) {
                            final OperationResource.Properties properties = new OperationResource.Properties();
                            if (pollsRemaining <= 1) {
                                properties.setProvisioningState(AzureAsyncOperationPollStrategy.SUCCEEDED);
                            }
                            else {
                                --pollsRemaining;
                                properties.setProvisioningState(AzureAsyncOperationPollStrategy.IN_PROGRESS);
                            }
                            final OperationResource operationResource = new OperationResource();
                            operationResource.setProperties(properties);
                            response = new MockAzureHttpResponse(200, operationResource);
                        }
                        else if (pollType.equalsIgnoreCase(LocationPollStrategy.HEADER_NAME)) {
                            if (pollsRemaining <= 1) {
                                final MockResource mockResource = new MockResource();
                                mockResource.name = "c";
                                response = new MockAzureHttpResponse(200, mockResource);
                            }
                            else {
                                --pollsRemaining;
                                response = new MockAzureHttpResponse(202)
                                        .withHeader(LocationPollStrategy.HEADER_NAME, request.url());
                            }
                        }
                    }
                }
                else if (request.httpMethod().equalsIgnoreCase("PUT")) {
                    ++createRequests;

                    final Map<String, String> requestQueryMap = queryToMap(requestUrl.getQuery());

                    final String pollType = requestQueryMap.get("PollType");
                    String pollsRemainingString = requestQueryMap.get("PollsRemaining");

                    if (pollType == null || "0".equals(pollsRemainingString)) {
                        final MockResource resource = new MockResource();
                        resource.name = "c";
                        response = new MockAzureHttpResponse(200, resource);
                    } else {

                        if (pollsRemainingString == null) {
                            pollsRemaining = 1;
                        }
                        else {
                            pollsRemaining = Integer.valueOf(pollsRemainingString);
                        }

                        final String initialResponseStatusCodeString = requestQueryMap.get("InitialResponseStatusCode");
                        int initialResponseStatusCode;
                        if (initialResponseStatusCodeString != null) {
                            initialResponseStatusCode = Integer.valueOf(initialResponseStatusCodeString);
                        }
                        else if (pollType.equalsIgnoreCase(LocationPollStrategy.HEADER_NAME)) {
                            initialResponseStatusCode = 202;
                        }
                        else {
                            initialResponseStatusCode = 201;
                        }

                        response = new MockAzureHttpResponse(initialResponseStatusCode);

                        final String pollUrl = "https://mock.azure.com/subscriptions/1/resourceGroups/mine/providers/mockprovider/mockoperations/1";
                        if (pollType.contains(AzureAsyncOperationPollStrategy.HEADER_NAME)) {
                            response.withHeader(AzureAsyncOperationPollStrategy.HEADER_NAME, pollUrl + "?PollType=" + AzureAsyncOperationPollStrategy.HEADER_NAME);
                        }
                        if (pollType.contains(LocationPollStrategy.HEADER_NAME)) {
                            response.withHeader(LocationPollStrategy.HEADER_NAME, pollUrl + "?PollType=" + LocationPollStrategy.HEADER_NAME);
                        }
                    }
                }
                else if (request.httpMethod().equalsIgnoreCase("DELETE")) {
                    ++deleteRequests;

                    final Map<String,String> requestQueryMap = queryToMap(requestUrl.getQuery());

                    final String pollType = requestQueryMap.get("PollType");
                    String pollsRemainingString = requestQueryMap.get("PollsRemaining");

                    if (pollType == null || "0".equals(pollsRemainingString)) {
                        response = new MockAzureHttpResponse(200);
                    }
                    else if (pollType.equals(LocationPollStrategy.HEADER_NAME)) {
                        if (pollsRemainingString == null) {
                            pollsRemaining = 1;
                        }
                        else {
                            pollsRemaining = Integer.valueOf(pollsRemainingString);
                        }

                        final String initialResponseStatusCodeString = requestQueryMap.get("InitialResponseStatusCode");
                        int initialResponseStatusCode;
                        if (initialResponseStatusCodeString != null) {
                            initialResponseStatusCode = Integer.valueOf(initialResponseStatusCodeString);
                        }
                        else if (pollType.equalsIgnoreCase(LocationPollStrategy.HEADER_NAME)) {
                            initialResponseStatusCode = 202;
                        }
                        else {
                            initialResponseStatusCode = 201;
                        }

                        response = new MockAzureHttpResponse(initialResponseStatusCode);

                        final String pollUrl = "https://mock.azure.com/subscriptions/1/resourceGroups/mine/providers/mockprovider/mockoperations/1";
                        if (pollType.contains(AzureAsyncOperationPollStrategy.HEADER_NAME)) {
                            response.withHeader(AzureAsyncOperationPollStrategy.HEADER_NAME, pollUrl + "?PollType=" + AzureAsyncOperationPollStrategy.HEADER_NAME);
                        }
                        if (pollType.contains(LocationPollStrategy.HEADER_NAME)) {
                            response.withHeader(LocationPollStrategy.HEADER_NAME, pollUrl + "?PollType=" + LocationPollStrategy.HEADER_NAME);
                        }
                    }
                }
            }
        }
        catch (Exception ignored) {
        }

        return Single.<HttpResponse>just(response);
    }

    private static Map<String,String> queryToMap(String url) {
        final Map<String,String> result = new HashMap<>();

        if (url != null) {
            final int questionMarkIndex = url.indexOf('?');
            if (questionMarkIndex >= 0) {
                url = url.substring(questionMarkIndex + 1);
            }

            for (String querySegments : url.split("&")) {
                final String[] querySegmentParts = querySegments.split("=");
                result.put(querySegmentParts[0], querySegmentParts[1]);
            }
        }

        return result;
    }

    private static String bodyToString(HttpRequest request) throws IOException {
        try (final InputStream bodyStream = request.body().createInputStream()) {
            return CharStreams.toString(new InputStreamReader(bodyStream));
        }
    }

    private static Map<String, String> toMap(HttpHeaders headers) {
        final Map<String, String> result = new HashMap<>();
        for (final HttpHeader header : headers) {
            result.put(header.name(), header.value());
        }
        return result;
    }
}