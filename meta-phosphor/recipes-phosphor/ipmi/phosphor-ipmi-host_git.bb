SUMMARY = "Phosphor OpenBMC IPMI daemon"
DESCRIPTION = "Phosphor OpenBMC IPMI router and plugin libraries"
PR = "r1"
PV = "1.0+git${SRCPV}"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

RRECOMMENDS_${PN} += "packagegroup-obmc-ipmid-providers-libs"

inherit autotools pkgconfig
inherit obmc-phosphor-ipmiprovider-symlink
inherit obmc-phosphor-sdbus-service
inherit obmc-phosphor-systemd
inherit phosphor-ipmi-host
inherit pythonnative

def ipmi_whitelists(d):
    whitelists = d.getVar(
        'VIRTUAL-RUNTIME_phosphor-ipmi-providers', True) or ''
    whitelists = whitelists.split()
    whitelists = [ '{}-whitelist-native'.format(x) for x in whitelists ]
    return ' '.join(whitelists)

DEPENDS += "autoconf-archive-native"
DEPENDS += "nlohmann-json"
DEPENDS += "obmc-targets"
DEPENDS += "${@ipmi_whitelists(d)}"
DEPENDS += "phosphor-dbus-interfaces"
DEPENDS += "phosphor-logging"
DEPENDS += "phosphor-mapper"
DEPENDS += "sdbusplus"
DEPENDS += "sdbus++-native"
DEPENDS += "virtual/phosphor-ipmi-fru-merge-config"
DEPENDS += "virtual/phosphor-ipmi-sensor-inventory"
DEPENDS += "virtual/phosphor-ipmi-channel-config"
DEPENDS += "boost"
DEPENDS += "sdeventplus"

VIRTUAL-RUNTIME_ipmi-config ?= "phosphor-ipmi-config"

RDEPENDS_${PN}-dev += "phosphor-logging"
RDEPENDS_${PN}-dev += "phosphor-mapper-dev"
RDEPENDS_${PN} += "clear-once"
RDEPENDS_${PN} += "libmapper"
RDEPENDS_${PN} += "network"
RDEPENDS_${PN} += "phosphor-dbus-interfaces"
RDEPENDS_${PN} += "phosphor-mapper"
RDEPENDS_${PN} += "phosphor-time-manager"
RDEPENDS_${PN} += "sdbusplus"
RDEPENDS_${PN} += "${VIRTUAL-RUNTIME_ipmi-config}"
RDEPENDS_${PN} += "virtual/obmc-watchdog"

inherit useradd

USERADD_PACKAGES = "${PN}"
# add ipmi group
GROUPADD_PARAM_${PN} = "ipmi"
# Add root user to ipmi group
GROUPMEMS_PARAM_${PN} = "-g ipmi -a root"

SYSTEMD_SERVICE_${PN} += "xyz.openbmc_project.Ipmi.Internal.SoftPowerOff.service phosphor-ipmi-host.service"

RRECOMMENDS_${PN} += "${VIRTUAL-RUNTIME_obmc-settings-mgmt}"


require ${PN}.inc

# Setup IPMI Whitelist Conf files
WHITELIST_CONF = " \
        ${STAGING_DATADIR_NATIVE}/phosphor-ipmi-host/*.conf \
        ${S}/host-ipmid-whitelist.conf \
        "
EXTRA_OECONF = " \
        WHITELIST_CONF="${WHITELIST_CONF}" \
        SENSOR_YAML_GEN=${STAGING_DIR_NATIVE}${sensor_datadir}/sensor.yaml \
        FRU_YAML_GEN=${STAGING_DIR_NATIVE}${config_datadir}/fru_config.yaml \
        CHANNEL_YAML_GEN=${STAGING_DIR_NATIVE}${sensor_datadir}/channel.yaml \
        POWER_READING_SENSOR=${datadir}/ipmi-providers/power_reading.json\
        "

S = "${WORKDIR}/git"

SRC_URI += "file://merge_yamls.py "

HOSTIPMI_PROVIDER_LIBRARY += "libipmi20.so"
HOSTIPMI_PROVIDER_LIBRARY += "libsysintfcmds.so"
HOSTIPMI_PROVIDER_LIBRARY += "libusercmds.so"

NETIPMI_PROVIDER_LIBRARY += "libipmi20.so"
NETIPMI_PROVIDER_LIBRARY += "libusercmds.so"

FILES_${PN}_append = " ${libdir}/host-ipmid/lib*${SOLIBS}"
FILES_${PN}_append = " ${libdir}/ipmid-providers/lib*${SOLIBS}"
FILES_${PN}_append = " ${libdir}/net-ipmid/lib*${SOLIBS}"
FILES_${PN}-dev_append = " ${libdir}/ipmid-providers/lib*${SOLIBSDEV} ${libdir}/ipmid-providers/*.la"

# Soft Power Off
# install the soft power off service in the host shutdown target
SOFT_SVC = "xyz.openbmc_project.Ipmi.Internal.SoftPowerOff.service"
SOFT_TGTFMT = "obmc-host-shutdown@{0}.target"
SOFT_FMT = "../${SOFT_SVC}:${SOFT_TGTFMT}.requires/${SOFT_SVC}"
SYSTEMD_LINK_${PN} += "${@compose_list_zip(d, 'SOFT_FMT', 'OBMC_HOST_INSTANCES')}"

#Collect all hardcoded sensor yamls from different recipes and
#merge all of them with sensor.yaml.
python do_merge_sensors () {
    import subprocess

    # TODO: Perform the merge in a temporary directory?
    workdir = d.getVar('WORKDIR', True)
    nativedir = d.getVar('STAGING_DIR_NATIVE', True)
    sensorsdir = d.getVar('sensor_datadir', True)
    sensorsdir = sensorsdir[1:]
    sensorsdir = os.path.join(nativedir, sensorsdir)
    cmd = []
    cmd.append(os.path.join(workdir, 'merge_yamls.py'))
    cmd.append(os.path.join(sensorsdir, 'sensor.yaml'))

    if os.stat(os.path.join(sensorsdir, 'sensor.yaml')).st_size == 0:
        return
    fetch = bb.fetch2.Fetch([], d)
    override_urls = [url for url in fetch.urls if url.endswith('.hardcoded.yaml')]
    for url in override_urls:
        bb.debug(2, 'Overriding with source: ' + url)
        local_base = os.path.basename(fetch.localpath(url))
        filename = os.path.join(workdir, local_base)
        cmd.append(filename)

    # Invoke the script and don't catch any resulting exception.
    subprocess.check_call(cmd)
}

# python-pyyaml-native is installed by do_configure, so put this task after
addtask merge_sensors after do_configure before do_compile
