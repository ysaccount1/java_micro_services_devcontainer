–# OAuth2 Proof of Concept

This project demonstrates a complete OAuth2 flow with three components:
1. Authorization Server (port 8080)
2. Resource Server (port 8081)
3. Client Application (port 8082)

## Components

### Authorization Server
- Handles authentication and authorization
- Issues access tokens
- Validates tokens

### Resource Server
- Provides protected resources
- Validates access tokens with the Authorization Server

### Client Application
- Requests authorization from the user
- Exchanges authorization code for access token
- Uses access token to access protected resources

## How to Run

### 1. Start the Authorization Server
```
mvn spring-boot:run -Dspring-boot.run.profiles=auth
```

### 2. Start the Resource Server
```
mvn spring-boot:run -Dspring-boot.run.profiles=resource
```

### 3. Start the Client Application
```
mvn spring-boot:run -Dspring-boot.run.profiles=client
```

## Testing the Flow

1. Open a browser and navigate to http://localhost:8082
2. Click the "Authorize" link
3. Log in with username "user" and password "password"
4. Approve the authorization request
5. The client will receive an authorization code, exchange it for an access token, and use the token to access the protected resource
6. The result page will display the access token and the response from the resource server

## OAuth2 Flow

This POC implements the Authorization Code grant type:

1. Client redirects user to Authorization Server
2. User authenticates and approves the authorization request
3. Authorization Server redirects back to Client with an authorization code
4. Client exchanges the code for an access token
5. Client uses the access token to access protected resources