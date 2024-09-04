package ee.kivikakk.ava

// Based on GenSmallAndProductiveICache.

import spinal.core._
import spinal.lib._
import vexriscv.plugin._
import vexriscv.ip.InstructionCacheConfig
import vexriscv.{VexRiscv, VexRiscvConfig, plugin}

/**
 * Created by spinalvm on 15.06.17.
 */
object GenAva extends App {
  SpinalVerilog {
    val plugins = List(
      new IBusCachedPlugin(
        resetVector = 0x00000000l,            // <-
        compressedGen = true,                 // <-
        injectorStage = true,                 // <- for compressedGen (ANTICIPATE)
        config = InstructionCacheConfig(
          cacheSize = 4096,
          bytePerLine = 32,
          wayCount = 1,
          addressWidth = 32,
          cpuDataWidth = 32,
          memDataWidth = 32,
          catchIllegalAccess = false,
          catchAccessFault = false,
          asyncTagMemory = false,
          twoCycleRam = false,
          twoCycleCache = true
        )
      ),
      new DBusSimplePlugin(
        catchAddressMisaligned = false,
        catchAccessFault = false
      ),
      new CsrPlugin(CsrPluginConfig.smallest),
      new DecoderSimplePlugin(
        catchIllegalInstruction = false
      ),
      new RegFilePlugin(
        regFileReadyKind = plugin.SYNC,
        zeroBoot = false
      ),
      new IntAluPlugin,
      new SrcPlugin(
        separatedAddSub = false,
        executeInsertion = true
      ),
      new LightShifterPlugin,
      new HazardSimplePlugin(
        bypassExecute           = true,
        bypassMemory            = true,
        bypassWriteBack         = true,
        bypassWriteBackBuffer   = true,
        pessimisticUseSrc       = false,
        pessimisticWriteRegFile = false,
        pessimisticAddressMatch = false
      ),
      new MulPlugin,                          // <- don't forget to infer DSP slices!
      new DivPlugin,                          // <- for all of "M"
      new BranchPlugin(
        earlyBranch = false,
        catchAddressMisaligned = false
      ),
      new YamlPlugin("cpu0.yaml")
    )

    val cpu = new VexRiscv(VexRiscvConfig(plugins))

    cpu.rework {
      for (plugin <- plugins) plugin match {
        case plugin: DBusSimplePlugin => {
          plugin.dBus.setAsDirectionLess()
          master(plugin.dBus.toWishbone()).setName("dBusWishbone")
        }
        case _ =>
      }
    }

    cpu
  }
}
