FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "file://boot.obmc.cmd"

do_compile() {
    sed -e 's/@@KERNEL_IMAGETYPE@@/${KERNEL_IMAGETYPE}/' \
        -e 's/@@KERNEL_BOOTCMD@@/${KERNEL_BOOTCMD}/' \
        -e 's/@@FDT_FILE@@/${FDTFILE}/' \
        -e 's/@@FDT_LOAD_ADDR@@/${FDT_LOAD_ADDR}/' \
        -e 's|@@RPI_UBOOT_CMD@@|${RPI_UBOOT_CMD}|' \
        "${WORKDIR}/boot.obmc.cmd" > "${WORKDIR}/boot.cmd"
    mkimage -A arm -T script -C none -n "Boot script" -d "${WORKDIR}/boot.cmd" boot.scr
}
