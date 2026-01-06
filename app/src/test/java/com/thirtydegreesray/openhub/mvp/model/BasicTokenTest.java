package com.thirtydegreesray.openhub.mvp.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class BasicTokenTest {

    @Test
    public void testGenerateFromOauthTokenWithNullScope() {
        OauthToken oauthToken = new OauthToken();
        oauthToken.setAccessToken("test_token");
        oauthToken.setScope(null);

        BasicToken basicToken = BasicToken.generateFromOauthToken(oauthToken);

        assertNotNull(basicToken);
        assertEquals("test_token", basicToken.getToken());
        assertNotNull(basicToken.getScopes());
        assertTrue(basicToken.getScopes().isEmpty());
    }

    @Test
    public void testGenerateFromOauthTokenWithEmptyScope() {
        OauthToken oauthToken = new OauthToken();
        oauthToken.setAccessToken("test_token");
        oauthToken.setScope("");

        BasicToken basicToken = BasicToken.generateFromOauthToken(oauthToken);

        assertNotNull(basicToken);
        assertEquals("test_token", basicToken.getToken());
        assertNotNull(basicToken.getScopes());
        assertTrue(basicToken.getScopes().isEmpty());
    }

    @Test
    public void testGenerateFromOauthTokenWithValidScope() {
        OauthToken oauthToken = new OauthToken();
        oauthToken.setAccessToken("test_token");
        oauthToken.setScope("user,repo,gist");

        BasicToken basicToken = BasicToken.generateFromOauthToken(oauthToken);

        assertNotNull(basicToken);
        assertEquals("test_token", basicToken.getToken());
        assertNotNull(basicToken.getScopes());
        assertEquals(3, basicToken.getScopes().size());
        assertTrue(basicToken.getScopes().contains("user"));
        assertTrue(basicToken.getScopes().contains("repo"));
        assertTrue(basicToken.getScopes().contains("gist"));
    }
}
