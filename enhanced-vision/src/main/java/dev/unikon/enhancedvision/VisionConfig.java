package dev.unikon.enhancedvision;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class VisionConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue STRENGTH;
    public static final ModConfigSpec.BooleanValue FIRST_PERSON_ONLY;
    public static final ModConfigSpec.BooleanValue FOLLOW_FOV;
    public static final ModConfigSpec.BooleanValue SPRINT_FOV;
    public static final ModConfigSpec.DoubleValue SPRINT_STRENGTH;
    public static final ModConfigSpec.DoubleValue SPRINT_TRANSITION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLED = builder.translation("enhanced_vision.config.enabled")
                .comment("Enable the visual effect. Can also be toggled with the configured key (default F8).")
                .define("enabled", true);
        STRENGTH = builder.translation("enhanced_vision.config.strength")
                .comment("Maximum peripheral expansion; actual coverage varies smoothly with distance from center.",
                        "Shared by the desktop lens and Vivecraft's per-eye lens. Default: 0.15; 0 disables both lenses.")
                .defineInRange("strength", 0.15, 0.0, 0.5);
        FIRST_PERSON_ONLY = builder.translation("enhanced_vision.config.first_person_only")
                .comment("Desktop: apply only in first person; hands, guns and HUD are not warped.",
                        "VR: only LEFT and RIGHT eye passes are processed, including their world-space hands and HUD.")
                .define("firstPersonOnly", true);
        FOLLOW_FOV = builder.translation("enhanced_vision.config.follow_fov")
                .comment("Desktop: reduce peripheral expansion below 70 degrees of world FOV (aiming/zoom).",
                        "VR keeps the headset's native eye projections; telescope/camera passes are excluded.",
                        "When disabled, peripheral strength stays constant at all FOV values.")
                .define("followFov", true);

        SPRINT_FOV = builder.translation("enhanced_vision.config.sprint_fov")
                .comment("Desktop only: replace vanilla and TACZ sprint FOV with smooth world-only widening.")
                .define("sprintFov", true);
        SPRINT_STRENGTH = builder.translation("enhanced_vision.config.sprint_strength")
                .comment("Additional world FOV while sprinting. 0.1 = 10%; hand/gun FOV stays unchanged.")
                .defineInRange("sprintStrength", 0.1, 0.0, 0.10);
        SPRINT_TRANSITION = builder.translation("enhanced_vision.config.sprint_transition")
                .comment("Seconds to reach 95% of the sprint FOV change, on both entry and recovery.")
                .defineInRange("sprintTransitionSeconds", 0.4, 0.05, 1.50);

        SPEC = builder.build();
    }

    private VisionConfig() {
    }
}
