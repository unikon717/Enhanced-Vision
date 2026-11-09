package dev.unikon.enhancedvision;

/** Small, independent calculations shared by the renderer and the FOV event. */
public final class VisionMath {
    private static final double REFERENCE_TANGENT = Math.tan(Math.toRadians(35.0));

    private VisionMath() {
    }

    public static double expandedFov(double fovDegrees, double overscan) {
        return Math.toDegrees(2.0 * Math.atan(Math.tan(Math.toRadians(fovDegrees) * 0.5) * overscan));
    }

    public static double effectiveStrength(double fovDegrees, double strength, boolean followFov) {
        return followFov ? strength * Math.min(1.0, Math.tan(Math.toRadians(fovDegrees) * 0.5) / REFERENCE_TANGENT) : strength;
    }

    /** Reaches 95% of a new target in transitionSeconds, independently of frame rate. */
    public static double advanceSprint(double blend, boolean sprinting, double elapsedSeconds, double transitionSeconds) {
        double target = sprinting ? 1.0 : 0.0;
        return target + (blend - target) * Math.exp(-3.0 * elapsedSeconds / transitionSeconds);
    }

    public static double sprintFovDelta(double vanillaModifier, double speed, double walkSpeed, double sprintAmount, double effectScale) {
        double ratio = (speed / (1.0 + sprintAmount) + walkSpeed) / (speed + walkSpeed);
        return vanillaModifier * (1.0 - ratio) * effectScale;
    }
}
