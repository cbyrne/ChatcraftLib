package dev.zerite.craftlib.protocol.data.registry.impl

import dev.zerite.craftlib.protocol.data.registry.IMinecraftRegistry
import dev.zerite.craftlib.protocol.data.registry.LazyRegistryDelegate
import dev.zerite.craftlib.protocol.data.registry.MagicRegistry
import dev.zerite.craftlib.protocol.data.registry.RegistryEntry

/**
 * Determines when a teams name tag should be visible.
 *
 * @author Koding
 * @since  0.1.1-SNAPSHOT
 */
open class MagicTeamNameTagVisibility(name: String) : RegistryEntry(name) {

    companion object :
        IMinecraftRegistry<MagicTeamNameTagVisibility> by LazyRegistryDelegate({ MagicRegistry.teamNameTagVisibility }) {
        @JvmField
        val ALWAYS = MagicTeamNameTagVisibility("Always")

        @JvmField
        val HIDE_FOR_OTHER_TEAMS = MagicTeamNameTagVisibility("Hide For Other Teams")

        @JvmField
        val HIDE_FOR_OWN_TEAM = MagicTeamNameTagVisibility("Hide For Own Team")

        @JvmField
        val NEVER = MagicTeamNameTagVisibility("Never")
    }

}
