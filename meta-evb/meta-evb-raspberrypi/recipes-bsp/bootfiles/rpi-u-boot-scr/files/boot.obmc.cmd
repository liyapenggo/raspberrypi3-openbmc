echo "Current dtb: $fdtfile"
setenv fdtfile @@FDT_FILE@@
echo "dtb redefined: $fdtfile"
setenv fdt_load_addr @@FDT_LOAD_ADDR@@
fatload mmc 0:1 ${kernel_addr_r} @@KERNEL_IMAGETYPE@@
fatload mmc 0:1 @@FDT_LOAD_ADDR@@ ${fdtfile}
setenv bootargs @@RPI_UBOOT_CMD@@
@@KERNEL_BOOTCMD@@ ${kernel_addr_r} - @@FDT_LOAD_ADDR@@
