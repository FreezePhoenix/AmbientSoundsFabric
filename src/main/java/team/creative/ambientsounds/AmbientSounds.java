package team.creative.ambientsounds;

import ca.weblite.objc.Client;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AmbientSounds implements ClientModInitializer {

	public static final String MODID = "ambientsounds";
	public static final Logger LOGGER = LogManager.getLogger(AmbientSounds.MODID);
	public static final AmbientSoundsConfig CONFIG = new AmbientSoundsConfig();

	public static AmbientTickHandler tickHandler;

	public static void reload() {
		if (tickHandler.engine != null)
			tickHandler.engine.stopEngine();
		if (tickHandler.enviroment != null)
			tickHandler.enviroment.reload();
		tickHandler.setEngine(AmbientEngine.loadAmbientEngine(tickHandler.soundEngine));
	}

	@Override
	public void onInitializeClient() {
		tickHandler = new AmbientTickHandler();
		ClientTickEvents.START_CLIENT_TICK.register(tickHandler::onTick);
		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			tickHandler.onRender(Minecraft.getInstance());
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(tickHandler::load);
		CommandRegistrationCallback.EVENT.register(this::commands);

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			ReloadableResourceManager reloadableResourceManager = (ReloadableResourceManager) client.getResourceManager();
			reloadableResourceManager.registerReloadListener(new SimplePreparableReloadListener() {

				@Override
				protected void apply(Object p_10793_, ResourceManager p_10794_, ProfilerFiller p_10795_) {
					AmbientSounds.reload();
				}

				@Override
				protected Object prepare(ResourceManager p_10796_, ProfilerFiller p_10797_) {
					return null;
				}
			});
		});
//        TODO: Implement
//        CreativeCoreClient.registerClientConfig(MODID);
	}

	private void commands(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
		dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("ambient-debug").executes(x -> {
			tickHandler.showDebugInfo = !tickHandler.showDebugInfo;
			return Command.SINGLE_SUCCESS;
		}));
		dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("ambient-reload").executes(x -> {
			AmbientSounds.reload();
			return Command.SINGLE_SUCCESS;
		}));
	}

}
