# Connect to Server using a Direct Connection Method

To connect to your Music Assistant server using a Direct Connection Method, you have a few options: connect via hostname (default), via IP address, or via a reverse proxy / tunnel if your server is exposed to the internet that way.

Connecting via hostname is recommended, as it ensures you can still reach your server even if its IP address changes. If you have a static IP set, connecting via IP address works just as well.

## Fill in the fields

| Field | Description |
|---|---|
| **Server host** | The hostname or IP address of your Music Assistant server e.g. `homeassistant.local`<sup>*</sup>, `192.168.1.2` or a remote address like `ma-app.duckdns.org` or `musicassistant.mydomain.com`. |
| **Port** | The port your Music Assistant server is listening on (default: `8095`). |
| **Use TLS (wss://)** | Enable this if your server uses a secure (TLS) connection. |

\* `homeassistant.local` is the default hostname of your Home Assistant server. You can use the hostname of your Home Assistant server when Music Assistant is installed as a Home Assistant App in HA OS.

![Direct Connection via hostname](screenshots/connection-to-server-direct/hostname.jpeg)
![Direct Connection via IP address](screenshots/connection-to-server-direct/ip.jpeg)

Once your details are filled in, tap **Connect** to move on to the next step.

### Connecting over the internet without exposing the MA port

If you don't want to expose the Music Assistant port (`8095` by default) directly to the internet, you can put your server behind a reverse proxy or tunnel (for example a Cloudflare Tunnel, Nginx, Traefik, or similar) and connect through that instead. The proxy terminates HTTPS on the standard port and forwards the traffic internally to Music Assistant, so only port `443` ever needs to be reachable from outside.

In that setup, fill in the fields like this:

| Field | Value |
|---|---|
| **Server host** | Your domain, e.g. `musicassistant.mydomain.com` — **no port needed here** |
| **Port** | `443` |
| **Use TLS (wss://)** | Enabled |

> **Note:** Even though your reverse proxy address normally doesn't require you to specify a port (browsers assume `443` for HTTPS), this app still expects the port to be filled in explicitly — enter `443` rather than leaving it blank.

There are many ways to set up a reverse proxy or tunnel; this app doesn't require or favor any particular method — use whichever fits your setup.

## Authentication

After connecting, you will be asked to sign in. Choose one of the following methods:

| Authentication method | Description |
|---|---|
| **Music Assistant** | Sign in with the username and password of a Music Assistant user. |
| **Home Assistant** | Sign in using Home Assistant OAuth. The Home Assistant user must be linked to the Music Assistant server. |

![Sign in with Music Assistant credentials](screenshots/connection-to-server-direct/ma-credentials.jpeg)
![Sign in using Home Assistant](screenshots/connection-to-server-direct/sign-in-using-ha.jpeg)
![Fill in Home Assistant credentials](screenshots/connection-to-server-direct/ha-sign-in-screen.jpeg)

After signing in, you can configure the [Local Player](local-sendspin-player-settings.md) or [start using the app](home.md) right away.
