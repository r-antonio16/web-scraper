#!/bin/sh

echo "Restarting Tor Proxy Service"
start-stop-daemon --stop --quiet --exec 'tor'
start-stop-daemon --start --quiet --background --exec 'tor'
until curl -f -o /dev/null -x socks5h://localhost:9050 -s https://check.torproject.org/api/ip
do
  sleep 5
done
echo "Tor Proxy Service restarted"
exit 0