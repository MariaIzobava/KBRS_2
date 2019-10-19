# Лабораторная работа №2 по КБРС
Design doc: https://docs.google.com/document/d/1DBnj9iXSVMwSpXvwBMLQng3bQVJodxtH57EpnEb96xU/edit

### Code description

1. Source code is stored in src.main.java.com.company.* package:
    * client - Client code.
    * server - Server code.
    * idea_cipher - IDEA + CFB code.
    * rsa - code for RSA generation and operation.
    * utils - classes for interservice communication support.
    * experiments - code, used for experimenting.
    
2. Text files are stored in src.main.resources:
    * atlas_shrugged.txt
    * pride_and_prejudice.txt
    * world_without_end.txt

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
