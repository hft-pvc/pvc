# PVC Project - SLAM (Simultaneous localization and mapping)

This repository contains all source code for the PVC project.

Infos about SLAM can be found at [Wikipedia](http://en.wikipedia.org/wiki/Simultaneous_localization_and_mapping).

Infos about the RP6 robot system can be found on [arexx.com](http://arexx.com/).


# Connecting via Bluetooth

To enable pairing __bluez__ needs to be installed.

Check for bluetooth device:

```
lsusb | grep -i bluetooth
```

Start daemon using systemd:
```
sudo systemctl start bluetooth
```

Start __bluetoothctl__ and run the following commands:
```
power on
agent on
default-agent
scan on
pair <mac>
```

Create device:
```
sudo rfcomm bind 0 <mac> 
```

To connect to the serial port we're using __pyserial__:
```
sudo miniterm.py --exit-char=3 /dev/rfcomm0 38400
```

# Starting GUI

It is necessary to start the GUI with _sudo_ and the rxtx library.
```
sudo java -Djava.library.path=/usr/lib64/rxtx-2 -jar slam-gui.jar
```
