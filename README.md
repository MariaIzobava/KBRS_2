# KBRS: lab 2
Design doc: https://docs.google.com/document/d/1DBnj9iXSVMwSpXvwBMLQng3bQVJodxtH57EpnEb96xU/edit

### Code description

1. Server source code is stored in Server.src.main.java.com.company.* package:
    * client - Client code (to run console Client).
    * server - Server code.
    * idea_cipher - IDEA + CFB code.
    * async_encryptions - code for RSA and GM generation and operation.
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

* To launch console Client run from src/ folder:

```
javac main/java/com/company/client/Client.java
java main.java.com.company.client.Client
```

### Interservice communication

1. Console Client currently supports the next commands:
```
"new RSA"  - Client will regenerate RSA keys and send Server new Public Key;
"get text" - You will be asked to provide text name and then Client will send a request to the Server to return this text in safe mode.
```

2. Android Client looks like that:
<img width="339" alt="Снимок экрана 2019-10-22 в 22 01 09" src="https://user-images.githubusercontent.com/12963610/67320882-7bb4a500-f517-11e9-9d04-aa37f6aed940.png">

