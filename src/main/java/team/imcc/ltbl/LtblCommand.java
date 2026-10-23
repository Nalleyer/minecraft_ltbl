package team.imcc.ltbl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** /ltbl and its subcommands. Everything is per player and lives only while the overlay is on. */
public final class LtblCommand {
	private LtblCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("ltbl")
				.executes(context -> toggle(context.getSource()))
				.then(Commands.literal("on").executes(context -> enable(context.getSource())))
				.then(Commands.literal("off").executes(context -> disable(context.getSource())))
				.then(Commands.literal("status").executes(context -> status(context.getSource())))
				.then(Commands.literal("radius")
						.then(Commands.argument("blocks", IntegerArgumentType.integer(LtblConfig.MIN_RADIUS, LtblConfig.MAX_RADIUS))
								.executes(context -> setRadius(context.getSource(), IntegerArgumentType.getInteger(context, "blocks")))))
				.then(Commands.literal("light")
						.then(Commands.literal("auto").executes(context -> setLight(context.getSource(), -1)))
						.then(Commands.argument("level", IntegerArgumentType.integer(0, 15))
								.executes(context -> setLight(context.getSource(), IntegerArgumentType.getInteger(context, "level"))))));
	}

	private static int toggle(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			return notAPlayer(source);
		}
		if (OverlayManager.isEnabled(player.getUUID())) {
			OverlayManager.disable(player.getUUID());
			source.sendSuccess(() -> Component.literal("§7LTBL 已关闭"), false);
		} else {
			OverlayManager.enable(player);
			source.sendSuccess(() -> Component.literal(describe("§aLTBL 已开启", player)), false);
		}
		return 1;
	}

	private static int enable(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			return notAPlayer(source);
		}
		OverlayManager.enable(player);
		source.sendSuccess(() -> Component.literal(describe("§aLTBL 已开启", player)), false);
		return 1;
	}

	private static int disable(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			return notAPlayer(source);
		}
		OverlayManager.disable(player.getUUID());
		source.sendSuccess(() -> Component.literal("§7LTBL 已关闭"), false);
		return 1;
	}

	private static int status(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			return notAPlayer(source);
		}
		OverlayManager.Session session = OverlayManager.session(player.getUUID());
		if (session == null) {
			source.sendSuccess(() -> Component.literal("§7LTBL 未开启。输入 /ltbl 开启。"), false);
			return 1;
		}
		source.sendSuccess(() -> Component.literal(describe("§aLTBL 运行中", player)), false);
		return 1;
	}

	private static int setRadius(CommandSourceStack source, int radius) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			return notAPlayer(source);
		}
		OverlayManager.Session session = OverlayManager.session(player.getUUID());
		if (session == null) {
			return notEnabled(source);
		}
		session.setRadius(player, radius);
		source.sendSuccess(() -> Component.literal("§aLTBL 扫描半径：" + radius + " 格"), false);
		return 1;
	}

	private static int setLight(CommandSourceStack source, int level) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			return notAPlayer(source);
		}
		OverlayManager.Session session = OverlayManager.session(player.getUUID());
		if (session == null) {
			return notEnabled(source);
		}
		session.setLightLimit(level);
		source.sendSuccess(() -> Component.literal("§aLTBL 光照阈值：" + describeLight(player, session)), false);
		return 1;
	}

	public static String describe(String prefix, ServerPlayer player) {
		OverlayManager.Session session = OverlayManager.session(player.getUUID());
		if (session == null) {
			return prefix;
		}
		return prefix
				+ " §7|§r 半径 " + session.radius()
				+ " §7|§r 光照 " + describeLight(player, session)
				+ " §7|§r 粒子 " + LtblConfig.get().particle
				+ " §7|§r 一圈 " + session.cycleSeconds() + "s";
	}

	private static String describeLight(ServerPlayer player, OverlayManager.Session session) {
		if (session.lightLimit() < 0) {
			return "自动(" + SpawnRules.defaultLightLimit(player.level()) + ")";
		}
		return String.valueOf(session.lightLimit());
	}

	private static int notAPlayer(CommandSourceStack source) {
		source.sendFailure(Component.literal("LTBL：该指令只能由玩家执行"));
		return 0;
	}

	private static int notEnabled(CommandSourceStack source) {
		source.sendFailure(Component.literal("LTBL：请先用 /ltbl 开启"));
		return 0;
	}
}
