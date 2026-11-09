package dev.unikon.enhancedvision;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(value = EnhancedVision.MOD_ID, dist = Dist.CLIENT)
public final class EnhancedVision {
    public static final String MOD_ID = "enhanced_vision";

    private static final KeyMapping TOGGLE = new KeyMapping(
            "key.enhanced_vision.toggle", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8, "key.categories.enhanced_vision");

    public EnhancedVision(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, VisionConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new ConfigurationScreen(mod, parent,
                (screen, type, config, title) -> new ConfigurationScreen.ConfigurationSectionScreen(screen, type, config, title) {
                    @Override
                    protected Element createSection(String key, UnmodifiableConfig spec, UnmodifiableConfig values) {
                        return "internal".equals(key) ? null : super.createSection(key, spec, values);
                    }
                }));
        modBus.addListener(this::registerKeys);
        modBus.addListener(this::registerReloadListener);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onFovModifier);
    }

    private void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE);
    }

    private void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resources -> VisionRenderer.resetAll());
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (TOGGLE.consumeClick()) {
            if (minecraft.screen != null || minecraft.level == null) {
                continue;
            }
            boolean enabled = !VisionConfig.ENABLED.get();
            VisionConfig.ENABLED.set(enabled);
            VisionConfig.SPEC.save();
            minecraft.gui.setOverlayMessage(Component.translatable(
                    enabled ? "message.enhanced_vision.enabled" : "message.enhanced_vision.disabled"), false);
        }
    }

    private void onFovModifier(ComputeFovModifierEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!VisionRenderer.isActive(minecraft) || !VisionConfig.SPRINT_FOV.get()
                || event.getPlayer() != minecraft.getCameraEntity()) {
            return;
        }
        var player = event.getPlayer();
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var sprint = speed.getModifier(ResourceLocation.withDefaultNamespace("sprinting"));
        double walkingSpeed = player.getAbilities().getWalkingSpeed();
        if (sprint == null || walkingSpeed == 0.0
                || sprint.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            return;
        }
        // Remove only vanilla's sprint contribution before other mods handle the event.
        // Flight, speed effects, bow use and the user's FOV-effects slider remain accounted for.
        double delta = VisionMath.sprintFovDelta(event.getFovModifier(), speed.getValue(),
                walkingSpeed, sprint.amount(), minecraft.options.fovEffectScale().get());
        event.setNewFovModifier((float) (event.getNewFovModifier() - delta));
    }
}
