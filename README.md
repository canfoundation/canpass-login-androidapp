# CANpass Login for the Android app

This is a simple example Android app written in [AppAuth](https://appauth.io/) and [Apollo GraphQL](https://www.apollographql.com/), and demonstrates how to 'Login with CANpass' for an Android app.

## Configuration

### Information You'll Need

* Authorization Endpoint
* Token Endpoint
* GraphQL Endpoint
* Client ID
* Redirect URI
* Scopes

### Configure the Example

#### In the file `Configuration.java`

```java
String AuthorizationEndpointURL = "https://canpass.me/oauth2/authorize";
String TokenEndpointURL = "https://canpass.me/oauth2/token";
String GraphQlEndpointURL = "https://api.cryptobadge.app/graphql";
String ClientID = "YOUR_CLIENT_ID";
String RedirectURI = "app.cryptobadge.oauth2:/oauth2redirect";
String Scope = "email";
```

#### How to update the redirect URI

To allow your user to be re-directed back to LoginExample, you’ll needs to associate a custom URL scheme with your app. The schema is everything before the colon (`:`). In web pages, for example, the scheme is usually http or https. Android apps can specify their own custom URL schemes. For example, if the redirect URI is `app.cryptobadge.oauth2:/oauth2redirect`, then the scheme would be `app.cryptobadge.oauth2`.

Replace the appAuthRedirectScheme manifest placeholder in build.gradle (for Module: app) with `app.cryptobadge.oauth2`


#### In the file `UserInfo.graphql`

```
query UserInfo{
  me {
    id
    name
    email
    path
    resourceUrl
  }
}
```


### GraphQL Usage

Once you create or update graphql files, Apollo will generate the appropriate Java classes that used to make requests to your GraphQL API. You can setup Apollo GraphQL by following this guide `https://github.com/apollographql/apollo-android`


#### Update the `schema.json` file

Our GraphQL service is developing so maybe this schema will out-of-date, so it may be updated manual. (TODO)

`apollo-codegen` will search for GraphQL code in the Xcode project and generate the Swift types.

```sh
npm install -g apollo-codegen
apollo-codegen download-schema http://api.cryptobadge.app/graphql --output schema
```
