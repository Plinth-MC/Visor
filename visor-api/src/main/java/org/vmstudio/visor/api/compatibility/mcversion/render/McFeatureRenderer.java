package org.vmstudio.visor.api.compatibility.mcversion.render;

//? if >=1.21.9 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
//?}
//? if >=26.1 {
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
//?}
//? if >=26.3 {
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.util.Optional;
import java.util.OptionalDouble;
//?}

/**
 * Cross-mc-version drawing of the render nodes Visor submits
 */
public final class McFeatureRenderer {
    private McFeatureRenderer() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    //? if >=26.1 {
    private static SubmitNodeStorage storage;
    private static FeatureRenderDispatcher dispatcher;
    //?}
    //? if >=26.2 {
    private static RenderBuffers buffers;
    //?}

    //? if >=26.1 {
    private static void create() {
        Minecraft minecraft = Minecraft.getInstance();
        storage = new SubmitNodeStorage();
        //? if >=26.2 {
        buffers = new RenderBuffers(1);
        dispatcher = new FeatureRenderDispatcher(buffers, minecraft.getModelManager(), minecraft.getAtlasManager(),
                minecraft.font, minecraft.gameRenderer.gameRenderState());
        //?} else {
        /*
        RenderBuffers shared = minecraft.renderBuffers();
        dispatcher = new FeatureRenderDispatcher(storage, minecraft.getModelManager(), shared.bufferSource(),
                minecraft.getAtlasManager(), shared.outlineBufferSource(), shared.crumblingBufferSource(),
                minecraft.font, minecraft.gameRenderer.getGameRenderState());
        *///?}
    }
    //?}

    //? if >=1.21.9 {
    public static SubmitNodeCollector collector() {
        //? if >=26.1 {
        if (dispatcher == null) {
            create();
        }
        return storage;
        //?} else {
        /*return Minecraft.getInstance().gameRenderer.getSubmitNodeStorage();
        *///?}
    }

    public static void render() {
        //? if >=26.3 {
        if (dispatcher != null) {
            RenderTarget target = McRenderTarget.mainTarget();
            try (FeatureRenderDispatcher.PreparedFrame frame = dispatcher.prepareFrame(storage);
                 RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                         () -> "visor features", target.getColorTextureView(), Optional.empty(),
                         target.getDepthTextureView(), OptionalDouble.empty())) {
                RenderSystem.bindDefaultUniforms(pass);
                FeatureRenderDispatcher.renderAllFeatures(pass, frame);
            }
        }
        //?} elif >=26.2 {
        /*if (dispatcher != null) {
            dispatcher.renderAllFeatures(storage);
        }
        *///?} elif >=26.1 {
        /*if (dispatcher != null) {
            dispatcher.renderAllFeatures();
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        }
        *///?} else {
        /*Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
        minecraft.renderBuffers().bufferSource().endBatch();
        *///?}
    }
    //?}

    public static void endFrame() {
        //? if >=26.2 {
        if (buffers != null) {
            buffers.endFrame();
        }
        //?} elif >=26.1 {
        /*if (dispatcher != null) {
            storage.endFrame();
            dispatcher.endFrame();
        }
        *///?}
    }

    public static void close() {
        //? if >=26.2 {
        if (dispatcher != null) {
            dispatcher.close();
            buffers.close();
            dispatcher = null;
            buffers = null;
            storage = null;
        }
        //?} elif >=26.1 {
        /*if (dispatcher != null) {
            dispatcher.close();
            dispatcher = null;
            storage = null;
        }
        *///?}
    }
}
