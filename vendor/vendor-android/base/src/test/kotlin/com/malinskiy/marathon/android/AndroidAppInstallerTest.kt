package com.malinskiy.marathon.android

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.marathon.android.adam.TestConfigurationFactory
import com.malinskiy.marathon.android.adam.TestDeviceFactory
import com.malinskiy.marathon.android.adam.boot
import com.malinskiy.marathon.android.adam.features
import com.malinskiy.marathon.android.adam.receiveFile
import com.malinskiy.marathon.android.adam.shell
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@AdbTest
class AndroidAppInstallerTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer

    @TempDir
    lateinit var temp: File

    @Test
    fun testCleanInstallation() {
        val configuration = TestConfigurationFactory.create()
        val installer = AndroidAppInstaller(configuration)
        val device = TestDeviceFactory.create(client, configuration, mock())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()

                    shell("pm list packages", "")
                    receiveFile(temp, "/data/local/tmp/app-debug.apk", "511", "122fc3b5d69b262db9b84dfc00e8f1d4")

                    shell("pm list packages", "package:com.example")
                    receiveFile(temp, "/data/local/tmp/app-debug-androidTest.apk", "511", "8d103498247b3711817a9f18624dede7")

                }
                features("emulator-5554")
            }

            device.setup()
            installer.prepareInstallation(device)
        }
    }
}
