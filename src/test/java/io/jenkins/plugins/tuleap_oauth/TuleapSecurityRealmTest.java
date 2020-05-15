package io.jenkins.plugins.tuleap_oauth;

import io.jenkins.plugins.tuleap_api.client.authentication.AccessTokenApi;
import io.jenkins.plugins.tuleap_api.client.authentication.OpenIDClientApi;
import io.jenkins.plugins.tuleap_oauth.checks.AccessTokenChecker;
import io.jenkins.plugins.tuleap_oauth.checks.AuthorizationCodeChecker;
import io.jenkins.plugins.tuleap_oauth.checks.IDTokenChecker;
import io.jenkins.plugins.tuleap_oauth.helper.PluginHelper;
import io.jenkins.plugins.tuleap_oauth.helper.PluginHelperImpl;
import io.jenkins.plugins.tuleap_oauth.helper.TuleapAuthorizationCodeUrlBuilder;
import io.jenkins.plugins.tuleap_server_configuration.TuleapConfiguration;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TuleapSecurityRealmTest {

    private PluginHelper pluginHelper;
    private AuthorizationCodeChecker authorizationCodeChecker;
    private AccessTokenChecker accessTokenChecker;
    private IDTokenChecker IDTokenChecker;
    private TuleapAuthorizationCodeUrlBuilder authorizationCodeUrlBuilder;
    private AccessTokenApi accessTokenApi;
    private OpenIDClientApi openIDClientApi;

    private Jenkins jenkins;

    @Before
    public void setUp() {
        this.pluginHelper = mock(PluginHelperImpl.class);
        this.authorizationCodeChecker = mock(AuthorizationCodeChecker.class);
        this.accessTokenChecker = mock(AccessTokenChecker.class);
        this.IDTokenChecker = mock(IDTokenChecker.class);
        this.authorizationCodeUrlBuilder = mock(TuleapAuthorizationCodeUrlBuilder.class);
        this.accessTokenApi = mock(AccessTokenApi.class);
        this.openIDClientApi = mock(OpenIDClientApi.class);

        this.jenkins = mock(Jenkins.class);
        when(pluginHelper.getJenkinsInstance()).thenReturn(jenkins);
    }

    private void injectMock(TuleapSecurityRealm securityRealm) {
        securityRealm.setPluginHelper(this.pluginHelper);
        securityRealm.setAuthorizationCodeChecker(this.authorizationCodeChecker);
        securityRealm.setAccessTokenChecker(this.accessTokenChecker);
        securityRealm.setIDTokenChecker(this.IDTokenChecker);
        securityRealm.setAuthorizationCodeUrlBuilder(this.authorizationCodeUrlBuilder);
        securityRealm.setAccessTokenApi(this.accessTokenApi);
        securityRealm.setOpenIDClientApi(this.openIDClientApi);
    }

    @Test
    public void testAddDashAtTheEndOfTheTuleapUriWhenItIsMissing() {
        TuleapSecurityRealm tuleapSecurityRealm = new TuleapSecurityRealm("", "");
        this.injectMock(tuleapSecurityRealm);

        TuleapConfiguration configuration = mock(TuleapConfiguration.class);
        when(this.pluginHelper.getConfiguration()).thenReturn(configuration);
        when(configuration.getDomainUrl()).thenReturn("https://jenkins.example.com");

        assertEquals("https://jenkins.example.com/", tuleapSecurityRealm.getTuleapUri());
    }

    @Test
    public void testItDoesNotAddADashAtTheOfTheUriIfTheUriAlreadyEndWithIt() {
        TuleapSecurityRealm tuleapSecurityRealm = new TuleapSecurityRealm("", "");
        this.injectMock(tuleapSecurityRealm);

        TuleapConfiguration configuration = mock(TuleapConfiguration.class);
        when(this.pluginHelper.getConfiguration()).thenReturn(configuration);
        when(configuration.getDomainUrl()).thenReturn("https://jenkins.example.com");

        assertEquals("https://jenkins.example.com/", tuleapSecurityRealm.getTuleapUri());
    }

    @Test
    public void testItShouldRedirectToClassicLogoutUrlWhenAnonymousUsersCanRead() {
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContextPath()).thenReturn("https://jenkins.example.com");

        Authentication authentication = mock(Authentication.class);

        when(this.jenkins.hasPermission(Jenkins.READ)).thenReturn(true);

        TuleapSecurityRealm tuleapSecurityRealm = new TuleapSecurityRealm( "", "");
        this.injectMock(tuleapSecurityRealm);

        assertEquals("https://jenkins.example.com/", tuleapSecurityRealm.getPostLogOutUrl(request, authentication));
    }

    @Test
    public void testItShouldRedirectToTuleapLogoutUrlWhenAnonymousUsersCannotRead() {
        StaplerRequest request = mock(StaplerRequest.class);
        when(request.getContextPath()).thenReturn("https://jenkins.example.com");

        Authentication authentication = mock(Authentication.class);

        when(this.jenkins.hasPermission(Jenkins.READ)).thenReturn(false);

        TuleapSecurityRealm tuleapSecurityRealm = new TuleapSecurityRealm( "", "");
        this.injectMock(tuleapSecurityRealm);

        assertEquals("https://jenkins.example.com/tuleapLogout", tuleapSecurityRealm.getPostLogOutUrl(request, authentication));
    }
}
