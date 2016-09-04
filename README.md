# File-Upload-Plugin #

A small project to upload files to a bukkit server, while not needing FTP access.

The plugin side is somewhat finished, cleanup is still needed.
Console client has basic functionality (post, request).

The communication should go this way:

```java
# Authentication
CLIENT -> SERVER:
    PacketTokenTransmit
SERVER -> CLIENT:
    PacketAuthenticationStatus

# Requesting files
CLIENT -> SERVER:
    PacketRequestFile
SERVER -> CLIENT:
    PacketPermissionDenied | PacketOperationSuccessful | PacketReadException
    PacketTransmitFile

# Sending files
CLIENT -> SERVER:
    PacketPostFile
SERVER -> CLIENT:
    PacketPermissionDenied | PacketOperationSuccessful
CLIENT -> SERVER:
    PacketTransmitFile
SERVER -> CLIENT:
    PacketWriteException | PacketOperationSuccessful
    
# Requesting the allowed files
CLIENT -> SERVER:
    PacketRequestAvailablePaths
SERVER -> CLIENT:
    PacketListAvailablePaths
    
# If the server thinks the client may be down (30 sec inactivity)
SERVER -> CLIENT:
    PacketHeartbeatSend
CLIENT -> SERVER:
    PacketHeartbeatResponse (same id)
```
