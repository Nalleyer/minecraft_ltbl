package team.imcc.ltbl;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;

/** Particle used to mark a spawnable block. All of these render emissive, i.e. visible in the dark. */
public enum ParticleStyle {
	END_ROD("end_rod", ParticleTypes.END_ROD),
	GLOW("glow", ParticleTypes.GLOW),
	FLAME("flame", ParticleTypes.FLAME),
	BARRIER("barrier", new BlockParticleOption(ParticleTypes.BLOCK_MARKER, Blocks.BARRIER.defaultBlockState()));

	private final String id;
	private final ParticleOptions particle;

	ParticleStyle(String id, ParticleOptions particle) {
		this.id = id;
		this.particle = particle;
	}

	public String id() {
		return id;
	}

	public ParticleOptions particle() {
		return particle;
	}

	public static ParticleStyle parse(String name) {
		ParticleStyle style = byId(name);
		return style != null ? style : END_ROD;
	}

	/** Strict lookup for user input; null when the name is unknown. */
	public static ParticleStyle byId(String name) {
		if (name != null) {
			for (ParticleStyle style : values()) {
				if (style.id.equalsIgnoreCase(name)) {
					return style;
				}
			}
		}
		return null;
	}
}
