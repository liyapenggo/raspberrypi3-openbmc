do_deploy_append() {
    if [ -n "${DISABLE_SPLASH}" ]; then
        sed -i '/#disable_splash=/ c\disable_splash=${DISABLE_SPLASH}' ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
    fi

    if [ -n "${DTOVERLAY}" ]; then
        echo "dtoverlay=${DTOVERLAY}" >> ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
    fi

    if [ -n "${KERNEL_FILE}" ]; then
        echo "kernel=${KERNEL_FILE}" >> ${DEPLOYDIR}/bcm2835-bootfiles/config.txt
    fi
}
