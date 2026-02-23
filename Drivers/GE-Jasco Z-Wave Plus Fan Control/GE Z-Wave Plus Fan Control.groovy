
/**
 * GE Z-Wave Plus Fan Control (HA Compatible - Modernized)
 * Based on Botched1's driver, updated for modern Hubitat + Home Assistant integration.
 */

metadata {
    definition (name: "GE Z-Wave Plus Fan Control (HA Patched)", namespace: "custom", author: "ChatGPT") {
        capability "Actuator"
        capability "Switch"
        capability "SwitchLevel"
        capability "FanControl"
        capability "Refresh"
        capability "Configuration"

        attribute "speed", "enum", ["off", "low", "medium", "high"]
        attribute "fanSpeed", "enum", ["off", "low", "medium", "high"]
        command "setSpeed", ["string"]
    }
}

preferences {
    input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
}

def installed() {
    log.info "Installed"
    configure()
}

def updated() {
    log.info "Updated"
    configure()
}

def configure() {
    log.info "Configuring..."
    refresh()
}

def parse(String description) {
    if (logEnable) log.debug "parse() >> $description"
}

def on() {
    setLevel(99)
}

def off() {
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "speed", value: "off")
    sendEvent(name: "fanSpeed", value: "off")
    zwaveCommand(0)
}

def setSpeed(String speed) {
    if (logEnable) log.debug "setSpeed() called with: $speed"
    switch (speed.toLowerCase()) {
        case "low":
            setLevel(33)
            break
        case "medium":
            setLevel(65)
            break
        case "high":
        case "on":
            setLevel(99)
            break
        case "off":
            off()
            break
        default:
            log.warn "Unsupported fan speed: ${speed}"
    }
    sendEvent(name: "fanSpeed", value: speed.toLowerCase())
}

def setLevel(level) {
    if (logEnable) log.debug "setLevel($level)"
    def speed = "high"
    if (level <= 0) {
        speed = "off"
    } else if (level <= 32) {
        speed = "low"
    } else if (level <= 65) {
        speed = "medium"
    }

    sendEvent(name: "switch", value: (level > 0) ? "on" : "off")
    sendEvent(name: "level", value: level)
    sendEvent(name: "percentage", value: level)
    sendEvent(name: "speed", value: speed)
    sendEvent(name: "fanSpeed", value: speed)

    zwaveCommand(level)
}

def zwaveCommand(level) {
    def cmds = []
    cmds << zwave.switchMultilevelV3.switchMultilevelSet(value: level).format()
    cmds << zwave.switchMultilevelV3.switchMultilevelGet().format()
    cmds.each {
        sendHubCommand(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZWAVE))
    }
}

def refresh() {
    if (logEnable) log.debug "refresh()"
    def cmds = []
    cmds << zwave.switchMultilevelV3.switchMultilevelGet().format()
    cmds.each {
        sendHubCommand(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZWAVE))
    }
}
