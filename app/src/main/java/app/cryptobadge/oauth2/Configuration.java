package app.cryptobadge.oauth2;

import junit.framework.Assert;

public class Configuration {
    static final String AuthorizationEndpointURL = "https://canpass.me/oauth2/authorize";
    static final String TokenEndpointURL = "https://canpass.me/oauth2/token";
    static final String GraphQlEndpointURL = "https://api.cryptobadge.app/graphql";
    static final String ClientID = "YOUR_CLIENT_ID";
    static final String RedirectURI = "app.cryptobadge.oauth2:/oauth2redirect";
    static final String Scope = "email";

    public static void validate() {
        Assert.assertSame("Should not change the authorization endpoint.",
                "https://canpass.me/oauth2/authorize",
                AuthorizationEndpointURL);

        Assert.assertSame("Should not change the token endpoint.",
                "https://canpass.me/oauth2/token",
                TokenEndpointURL);

        Assert.assertSame("Should not change the GraphQL endpoint.",
                "https://api.cryptobadge.app/graphql",
                GraphQlEndpointURL);

        Assert.assertNotSame("Update ClientID with your own client ID.",
                "YOUR_CLIENT_ID",
                ClientID);

        Assert.assertNotSame("Update kRedirectURI with your own redirect URI.",
                "app.cryptobadge.oauth2:/oauth2redirect",
                RedirectURI);
    }
}
