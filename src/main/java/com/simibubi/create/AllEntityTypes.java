package com.simibubi.create;

import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueRenderer;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.IFactory;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class AllEntityTypes {

	public static final EntityEntry<OrientedContraptionEntity> ORIENTED_CONTRAPTION =
		contraption("contraption", OrientedContraptionEntity::new, () -> OrientedContraptionEntityRenderer::new,
			5, 3, true);
	public static final EntityEntry<ControlledContraptionEntity> CONTROLLED_CONTRAPTION =
		contraption("stationary_contraption", ControlledContraptionEntity::new, () -> ContraptionEntityRenderer::new,
			20, 40, false);
	public static final EntityEntry<GantryContraptionEntity> GANTRY_CONTRAPTION =
		contraption("gantry_contraption", GantryContraptionEntity::new, () -> ContraptionEntityRenderer::new,
			10, 40, false);

	public static final EntityEntry<SuperGlueEntity> SUPER_GLUE =
		register("super_glue", SuperGlueEntity::new, () -> SuperGlueRenderer::new,
			EntityClassification.MISC, 10, Integer.MAX_VALUE, false, true, SuperGlueEntity::build);
	public static final EntityEntry<SeatEntity> SEAT =
		register("seat", SeatEntity::new, () -> SeatEntity.Render::new,
			EntityClassification.MISC, 0, Integer.MAX_VALUE, false, true, SeatEntity::build);

	//

	private static <T extends Entity> EntityEntry<T> contraption(String name, IFactory<T> factory,
		NonNullSupplier<IRenderFactory<? super T>> renderer, int range, int updateFrequency,
		boolean sendVelocity) {
		return register(name, factory, renderer, EntityClassification.MISC, range, updateFrequency,
			sendVelocity, true, AbstractContraptionEntity::build);
	}

	private static <T extends Entity> EntityEntry<T> register(String name, IFactory<T> factory,
		NonNullSupplier<IRenderFactory<? super T>> renderer, EntityClassification group, int range,
		int updateFrequency, boolean sendVelocity, boolean immuneToFire,
		NonNullConsumer<EntityType.Builder<T>> propertyBuilder) {
		String id = Lang.asId(name);
		return Create.registrate()
			.entity(id, factory, group)
			.properties(b -> b.setTrackingRange(range)
				.setUpdateInterval(updateFrequency)
				.setShouldReceiveVelocityUpdates(sendVelocity))
			.properties(propertyBuilder)
			.properties(b -> {
				if (immuneToFire)
					b.immuneToFire();
			})
			.renderer(renderer)
			.register();
	}

	public static void register() {}
}
