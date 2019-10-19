# Лабораторная работа №2 по КБРС


### Launching services
* To launch Server run from src/ folder:
```
javac main/java/com/company/server/Server.java
java main.java.com.company.server.Server
```

* To launch Client run from src/ folder:

```
javac main/java/com/company/client/Client.java
java main.java.com.company.client.Client
```

### Interservice communication

Client currently supports the next commands:
```
"new RSA"  - Client will regenerate RSA keys and send Server new Public Key;
"get text" - You will be asked to provide text name and then Client will send a request to the Server to return this text in safe mode.
```
