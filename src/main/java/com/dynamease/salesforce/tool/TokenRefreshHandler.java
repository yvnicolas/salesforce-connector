package com.dynamease.salesforce.tool;

import java.util.Map;

/**
 * Created by Gregoire on 26/05/2015.
 * If a token refresh handler exists,
 * sales connector may refresh token automatically an notify handler (and so calling process)
 */
public interface TokenRefreshHandler {
    void handleRefresh(Map<String, String> s);
}
