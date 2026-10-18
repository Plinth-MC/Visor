package org.vmstudio.visor.compatibility.nvidium.mixin;

import me.cortex.nvidium.gl.RenderDevice;
import me.cortex.nvidium.gl.buffers.IDeviceMappedBuffer;
import me.cortex.nvidium.managers.SectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.nvidium.NvidiumHelper;

@Pseudo
@Mixin(targets = "me.cortex.nvidium.RenderPipeline", remap = false)
public class RenderPipelineMixin {
    @Shadow(remap = false) @Final @Mutable
    private IDeviceMappedBuffer regionVisibility;
    @Shadow(remap = false) @Final @Mutable
    private IDeviceMappedBuffer sectionVisibility;
    @Shadow(remap = false) @Final @Mutable
    private IDeviceMappedBuffer terrainCommandBuffer;
    @Shadow(remap = false) @Final @Mutable
    private IDeviceMappedBuffer translucencyCommandBuffer;
    @Shadow(remap = false)
    private int prevRegionCount;
    @Shadow(remap = false) @Final
    private RenderDevice device;
    @Shadow(remap = false) @Final
    private SectionManager sectionManager;

    //0 = eyes, 1 = CENTER, 2 = THIRD_PERSON
    @Unique
    private final IDeviceMappedBuffer[][] visor$views = new IDeviceMappedBuffer[3][4];
    @Unique
    private final int[] visor$prevRegionCounts = new int[3];
    @Unique
    private int visor$activeSlot;

    @Inject(method = "renderFrame", at = @At("HEAD"), remap = false)
    private void visor$bindViewFrame(CallbackInfo ci) {
        visor$bind();
    }

    @Inject(method = "renderFrame", at = @At("RETURN"), remap = false)
    private void visor$unbindViewFrame(CallbackInfo ci) {
        visor$unbind();
    }

    @Inject(method = "renderTranslucent", at = @At("HEAD"), remap = false)
    private void visor$bindViewTranslucent(CallbackInfo ci) {
        visor$bind();
    }

    @Inject(method = "renderTranslucent", at = @At("RETURN"), remap = false)
    private void visor$unbindViewTranslucent(CallbackInfo ci) {
        visor$unbind();
    }

    @Inject(method = "delete", at = @At("HEAD"), remap = false)
    private void visor$deleteViewBuffers(CallbackInfo ci) {
        for (int slot = 1; slot < visor$views.length; slot++) {
            for (int i = 0; i < visor$views[slot].length; i++) {
                if (visor$views[slot][i] == null) continue;
                visor$views[slot][i].delete();
                visor$views[slot][i] = null;
            }
        }
    }

    @Unique
    private void visor$bind() {
        int slot = NvidiumHelper.getViewSlot();
        if (slot == 0) return;
        if (visor$views[slot][0] == null) {
            long regions = sectionManager.getRegionManager().maxRegions();
            visor$views[slot][0] = device.createDeviceOnlyMappedBuffer(regions);
            visor$views[slot][1] = device.createDeviceOnlyMappedBuffer(regions * 256L);
            visor$views[slot][2] = device.createDeviceOnlyMappedBuffer(regions * 8L);
            visor$views[slot][3] = device.createDeviceOnlyMappedBuffer(regions * 8L);
        }
        visor$store(0);
        visor$load(slot);
        visor$activeSlot = slot;
    }

    @Unique
    private void visor$unbind() {
        if (visor$activeSlot == 0) return;
        visor$store(visor$activeSlot);
        visor$load(0);
        visor$activeSlot = 0;
    }

    @Unique
    private void visor$store(int slot) {
        visor$views[slot][0] = regionVisibility;
        visor$views[slot][1] = sectionVisibility;
        visor$views[slot][2] = terrainCommandBuffer;
        visor$views[slot][3] = translucencyCommandBuffer;
        visor$prevRegionCounts[slot] = prevRegionCount;
    }

    @Unique
    private void visor$load(int slot) {
        regionVisibility = visor$views[slot][0];
        sectionVisibility = visor$views[slot][1];
        terrainCommandBuffer = visor$views[slot][2];
        translucencyCommandBuffer = visor$views[slot][3];
        prevRegionCount = visor$prevRegionCounts[slot];
    }
}
