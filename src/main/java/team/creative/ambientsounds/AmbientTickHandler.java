package team.creative.ambientsounds;

import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import team.creative.ambientsounds.env.AmbientEnviroment;
import team.creative.ambientsounds.sound.AmbientSoundEngine;
import team.creative.creativecore.CreativeCore;
import team.creative.creativecore.common.config.holder.ConfigHolderDynamic;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.config.sync.ConfigSynchronization;
import team.creative.creativecore.common.util.type.list.Pair;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class AmbientTickHandler {
	public static final DecimalFormat df = new DecimalFormat("0.##");
	public AmbientSoundEngine soundEngine;
	public AmbientEnviroment enviroment = null;
	public AmbientEngine engine;
	public int timer = 0;
	public boolean showDebugInfo = false;

	public void setEngine(AmbientEngine engine) {
		this.engine = engine;
		try {
			initConfiguration();
		} catch (NoSuchFieldException e) {
			throw new Error(e);
		}
	}

	public void initConfiguration() throws NoSuchFieldException {
		CreativeConfigRegistry.ROOT.removeField(AmbientSounds.MODID);

		if (engine == null)
			return;

		ConfigHolderDynamic holder = CreativeConfigRegistry.ROOT.registerFolder(
				AmbientSounds.MODID,
				ConfigSynchronization.CLIENT
		);
		ConfigHolderDynamic sounds = holder.registerFolder("sounds");
		Field soundField = AmbientSound.class.getDeclaredField("volumeSetting");
		for (Entry<String, AmbientRegion> pair : engine.allRegions.entrySet())
			if (pair.getValue().sounds != null)
				for (AmbientSound sound : pair.getValue().sounds.values())
					sounds.registerField(pair.getKey() + "." + sound.name, soundField, sound);

		ConfigHolderDynamic dimensions = holder.registerFolder("dimensions");
		Field dimensionField = AmbientDimension.class.getDeclaredField("volumeSetting");
		for (AmbientDimension dimension : engine.dimensions.values())
			dimensions.registerField(dimension.name, dimensionField, dimension);

		holder.registerField("silent-dimensions", AmbientEngine.class.getDeclaredField("silentDimensions"), engine);
		holder.registerValue("general", AmbientSounds.CONFIG);
		CreativeCore.CONFIG_HANDLER.load(AmbientSounds.MODID, EnvType.CLIENT);
	}

	private String format(Object value) {
		if (value instanceof Double || value instanceof Float)
			return df.format(value);
		return value.toString();
	}

	private String format(List<Pair<String, Object>> details) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Pair<String, Object> pair : details) {
			if (!first)
				builder.append(",");
			else
				first = false;
			builder.append(ChatFormatting.YELLOW + pair.key + ChatFormatting.RESET + ":" + format(pair.value));
		}
		return builder.toString();
	}

	public void onRender(Minecraft client) {
		if (showDebugInfo && engine != null && !client.isPaused() && enviroment != null && client.level != null) {
			List<String> list = new ArrayList<>();

			List<Pair<String, Object>> details = new ArrayList<>();
			engine.collectDetails(details);

			details.add(new Pair<>("playing", engine.soundEngine.playingCount()));
			details.add(new Pair<>("dim-name", client.level.dimension().location()));

			list.add(format(details));
			details.clear();

			enviroment.collectLevelDetails(details);

			list.add(format(details));
			details.clear();

			enviroment.collectPlayerDetails(details, client.player);

			list.add(format(details));
			details.clear();

			enviroment.collectTerrainDetails(details);

			list.add(format(details));
			details.clear();

			enviroment.collectBiomeDetails(details);

			list.add(format(details));
			details.clear();

			for (AmbientRegion region : engine.activeRegions) {

				details.add(new Pair<>("region", ChatFormatting.DARK_GREEN + region.name + ChatFormatting.RESET));
				details.add(new Pair<>("playing", region.playing.size()));

				list.add(format(details));

				details.clear();
				for (AmbientSound sound : region.playing) {

					if (!sound.isPlaying())
						continue;

					String text = "";
					if (sound.stream1 != null) {
						details.add(new Pair<>("n", sound.stream1.location));
						details.add(new Pair<>("v", sound.stream1.volume));
						details.add(new Pair<>("i", sound.stream1.index));
						details.add(new Pair<>("p", sound.stream1.pitch));
						details.add(new Pair<>("t", sound.stream1.ticksPlayed));
						details.add(new Pair<>("d", sound.stream1.duration));

						text = "[" + format(details) + "]";

						details.clear();
					}

					if (sound.stream2 != null) {
						details.add(new Pair<>("n", sound.stream2.location));
						details.add(new Pair<>("v", sound.stream2.volume));
						details.add(new Pair<>("i", sound.stream2.index));
						details.add(new Pair<>("p", sound.stream2.pitch));
						details.add(new Pair<>("t", sound.stream2.ticksPlayed));
						details.add(new Pair<>("d", sound.stream2.duration));

						text += "[" + format(details) + "]";

						details.clear();
					}

					list.add(text);
				}
			}

			for (int i = 0; i < list.size(); ++i) {
				String s = list.get(i);

				if (!Strings.isNullOrEmpty(s)) {
					int j = client.font.lineHeight;
					int k = client.font.width(s);
					int i1 = 2 + j * i;
					PoseStack mat = new PoseStack();
					drawGradientRect(mat.last().pose(), 0, 1, i1 - 1, 2 + k + 1, i1 + j - 1, -1873784752, -1873784752);
					client.font.drawShadow(mat, s, 2, i1, 14737632);
				}
			}
		}
	}

	// Forge GuiUtils::drawGradientRect
	public static void drawGradientRect(Matrix4f mat, int zLevel, int left, int top, int right, int bottom, int startColor, int endColor)
	{
		float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
		float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
		float startGreen = (float)(startColor >>  8 & 255) / 255.0F;
		float startBlue  = (float)(startColor       & 255) / 255.0F;
		float endAlpha   = (float)(endColor   >> 24 & 255) / 255.0F;
		float endRed     = (float)(endColor   >> 16 & 255) / 255.0F;
		float endGreen   = (float)(endColor   >>  8 & 255) / 255.0F;
		float endBlue    = (float)(endColor         & 255) / 255.0F;

		RenderSystem.enableDepthTest();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(mat, right,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		buffer.vertex(mat,  left,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		buffer.vertex(mat,  left, bottom, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
		buffer.vertex(mat, right, bottom, zLevel).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
		tessellator.end();

		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	public void load(Minecraft client) {
		if (engine != null) {
			engine.onClientLoad();
		} else {
			if (soundEngine == null) {
				soundEngine = new AmbientSoundEngine(client.getSoundManager(), client.options);
				if (engine == null)
					setEngine(AmbientEngine.loadAmbientEngine(soundEngine));
				if (engine != null)
					engine.soundEngine = soundEngine;
			}
		}
	}

	public void onTick(Minecraft client) {
		if (soundEngine == null) {
			soundEngine = new AmbientSoundEngine(client.getSoundManager(), client.options);
			if (engine == null)
				setEngine(AmbientEngine.loadAmbientEngine(soundEngine));
			if (engine != null)
				engine.soundEngine = soundEngine;
		}

		if (engine == null)
			return;

		Level level = client.level;
		Player player = client.player;

		if (level != null && player != null && !client.isPaused() && client.options.getSoundSourceVolume(SoundSource.AMBIENT) > 0) {

			if (enviroment == null)
				enviroment = new AmbientEnviroment();

			AmbientDimension newDimension = engine.getDimension(level);
			if (enviroment.dimension != newDimension) {
				engine.changeDimension(enviroment, newDimension);
				enviroment.dimension = newDimension;
			}

			if (timer % engine.enviromentTickTime == 0)
				enviroment.analyzeSlow(newDimension, engine, player, level, timer);

			if (timer % engine.soundTickTime == 0) {
				enviroment.analyzeFast(newDimension, player, level, client.getDeltaFrameTime());
				engine.tick(enviroment);

				enviroment.dimension.manipulateEnviroment(enviroment);
			}

			engine.fastTick(enviroment);

			timer++;
		} else if (!engine.activeRegions.isEmpty())
			engine.stopEngine();
	}
}
