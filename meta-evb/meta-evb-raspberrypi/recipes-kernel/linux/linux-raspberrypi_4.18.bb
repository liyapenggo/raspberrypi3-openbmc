LINUX_VERSION ?= "4.18"
LINUX_RPI_BRANCH ?= "rpi-4.18.y"

SRCREV = "${AUTOREV}"
SRC_URI = " \
    git://github.com/raspberrypi/linux.git;protocol=git;branch=${LINUX_RPI_BRANCH}\
    "

require linux-raspberrypi.inc

KERNEL_VERSION_SANITY_SKIP = "1"

# COPYING file checksum has changed in 4.16+ kernels
LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"
