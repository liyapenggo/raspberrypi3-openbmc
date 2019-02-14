# meta-evb-raspberrypi

OpenBMC on Raspberry Pi Compute Module 3 <https://www.raspberrypi.org/products/compute-module-3/>

## Description

OpenBMC distribution running on the Raspberry Pi Compute Module 3. It will also likely run on other variants with minimal changes.

## Quick start


### Prerequisites

1. Follow the base OpenBMC quick start guide to setup a working build environment. The Palmetto target is a good target. If the Palmetto build fails, all bets are off.

2. If you're too lazy to follow instructions or just excited about making this work:

```
git clone -b raspberrypi-cm3 https://github.com/truongmd/openbmc.git
```

2. Clean up the build conf directory from the Palmetto build so the new environment will be setup correctly.
```
rm -rf build/conf/
```

3. Target the Raspberry Pi CM3
```
export TEMPLATECONF=meta-evb/meta-evb-raspberrypi/meta-evb-raspberrypi-cm3/conf
```

4. Build by sourcing the openbmc-env file. It will drop you into the build directory.

```
. openbmc-env
build$ bitbake obmc-phosphor-image
```

5. Get the build onto the flash device. The following instructions will completely WIPE the CM3 eMMC flash storage. We'll use some of jumpnowtek's tools:

```
build$ git clone -b sumo https://github.com/jumpnow/meta-rpi.git jumpnowtek
```

Put the CM3 into USB Slave mode and boot with rpiboot. Assuming the flash device appears as sdb on your Linux system:

```
export OETMP=./build/tmp
export MACHINE=raspberrypi-cm3
sudo jumpnowtek/scripts/mk2parts.sh sdb
sudo jumpnowtek/scripts/copy_boot.sh sdb
sudo jumpnowtek/scripts/copy_rootfs.sh sdb obmc-phosphor my-openbmc-rpi
```

6. Attach the serial console and reboot. You should see the u-boot emitting data both on the HDMI and serial console ports.

```
U-Boot 2018.01 (Oct 20 2018 - 04:17:21 +0000)

DRAM:  948 MiB
RPI: Board rev 0xa unknown
RPI Unknown model (0xa220a0)
MMC:   sdhci@7e300000: 0
*** Warning - bad CRC, using default environment

In:    serial
Out:   vidconsole
Err:   vidconsole
Net:   No ethernet found.
starting USB...
USB0:   Core Release: 2.80a
scanning bus 0 for devices... 1 USB Device(s) found
       scanning usb for storage devices... 0 Storage Device(s) found
Hit any key to stop autoboot:  0
switch to partitions #0, OK
mmc0(part 0) is current device
Scanning mmc 0:1...
Found U-Boot script /boot.scr
reading /boot.scr
415 bytes read in 13 ms (30.3 KiB/s)
## Executing script at 02000000
Current dtb: bcm283x-rpi-other.dtb
dtb redefined: bcm2710-rpi-cm3.dtb
reading zImage
5202296 bytes read in 341 ms (14.5 MiB/s)
reading bcm2710-rpi-cm3.dtb
22940 bytes read in 20 ms (1.1 MiB/s)
Kernel image @ 0x1000000 [ 0x000000 - 0x4f6178 ]
## Flattened Device Tree blob at 02000000
   Booting using the fdt blob at 0x2000000
   reserving fdt memory region: addr=0 size=1000
   Using Device Tree in place at 02000000, end 0200899b

Starting kernel ...

[    0.000000] Booting Linux on physical CPU 0x0
[    0.000000] Linux version 4.18.14 (oe-user@oe-host) (gcc version 7.3.0 (GCC)) #1 SMP Sat Oct 20 00:33:44 UTC 2018
[    0.000000] CPU: ARMv7 Processor [410fd034] revision 4 (ARMv7), cr=10c5383d
[    0.000000] CPU: div instructions available: patching division code
[    0.000000] CPU: PIPT / VIPT nonaliasing data cache, VIPT aliasing instruction cache
[    0.000000] OF: fdt: Machine model: Raspberry Pi Compute Module 3
[    0.000000] Memory policy: Data cache writealloc
[    0.000000] cma: Reserved 8 MiB at 0x3ac00000
[    0.000000] random: get_random_bytes called from start_kernel+0xb0/0x4b4 with crng_init=0
[    0.000000] percpu: Embedded 17 pages/cpu @(ptrval) s39052 r8192 d22388 u69632
[    0.000000] Built 1 zonelists, mobility grouping on.  Total pages: 240555
[    0.000000] Kernel command line: earlyprintk console=ttyAMA0 console=tty1 root=/dev/mmcblk0p2 rootfstype=ext4 rootwait
```

7. Now get hacking.

### Other RPI boards
#### Raspberry Pi 3 Model B

Use the CM3 build if you want u-boot. The RPI3B specific u-boot doesn't seem to boot the device properly. Booting the zImage directly will work but defeats the full OpenBMC experience.

#### RPI0-Wifi

The device specific config will build a working image:
```
export TEMPLATECONF=meta-evb/meta-evb-raspberrypi/meta-evb-raspberry0-wifi/conf
```

## BMC Features unlikely to work

1. KVMoIP will NOT work. Look at the Nuvaton or ASPEED SoC's if you want remote video.
2. IPMI
