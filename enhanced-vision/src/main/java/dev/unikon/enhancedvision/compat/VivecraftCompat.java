package dev.unikon.enhancedvision.compat;

import net.neoforged.fml.ModList;
import org.vivecraft.api.client.VRClientAPI;

/** Keeps Vivecraft's optional API isolated from clients that do not install it. */
public final class VivecraftCompat {
    private static final boolean LOADED = ModList.get().isLoaded("vivecraft");

    private VivecraftCompat() {
    }

    public static boolean isVrRunning() {
        return LOADED && VivecraftAccess.isVrActive();
    }

    private static final class VivecraftAccess {
        private static boolean isVrActive() {
            return VRClientAPI.instance().isVRActive();
        }
    }
}
