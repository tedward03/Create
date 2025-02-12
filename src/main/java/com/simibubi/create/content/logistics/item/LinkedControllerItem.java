package com.simibubi.create.content.logistics.item;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.logistics.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

public class LinkedControllerItem extends Item implements INamedContainerProvider {

	public LinkedControllerItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext ctx) {
		PlayerEntity player = ctx.getPlayer();
		if (player == null)
			return ActionResultType.PASS;
		World world = ctx.getWorld();

		if (!player.isSneaking() && player.isAllowEdit()
			&& AllBlocks.REDSTONE_LINK.has(world.getBlockState(ctx.getPos()))) {
			if (world.isRemote)
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.toggleBindMode(ctx.getPos()));
			player.getCooldownTracker()
				.setCooldown(this, 2);
			return ActionResultType.SUCCESS;
		}

		return onItemRightClick(world, player, ctx.getHand()).getType();
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack heldItem = player.getHeldItem(hand);

		if (player.isSneaking() && hand == Hand.MAIN_HAND) {
			if (!world.isRemote && player instanceof ServerPlayerEntity && player.isAllowEdit())
				NetworkHooks.openGui((ServerPlayerEntity) player, this, buf -> {
					buf.writeItemStack(heldItem);
				});
			return ActionResult.success(heldItem);
		}

		if (!player.isSneaking()) {
			if (world.isRemote)
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::toggleActive);
			player.getCooldownTracker()
				.setCooldown(this, 2);
		}

		return ActionResult.pass(heldItem);
	}

	@OnlyIn(Dist.CLIENT)
	private void toggleBindMode(BlockPos pos) {
		LinkedControllerClientHandler.toggleBindMode(pos);
	}

	@OnlyIn(Dist.CLIENT)
	private void toggleActive() {
		LinkedControllerClientHandler.toggle();
	}

	public static ItemStackHandler getFrequencyItems(ItemStack stack) {
		ItemStackHandler newInv = new ItemStackHandler(12);
		if (AllItems.LINKED_CONTROLLER.get() != stack.getItem())
			throw new IllegalArgumentException("Cannot get frequency items from non-controller: " + stack);
		CompoundNBT invNBT = stack.getOrCreateChildTag("Items");
		if (!invNBT.isEmpty())
			newInv.deserializeNBT(invNBT);
		return newInv;
	}

	public static Couple<RedstoneLinkNetworkHandler.Frequency> toFrequency(ItemStack controller, int slot) {
		ItemStackHandler frequencyItems = getFrequencyItems(controller);
		return Couple.create(Frequency.of(frequencyItems.getStackInSlot(slot * 2)),
			Frequency.of(frequencyItems.getStackInSlot(slot * 2 + 1)));
	}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		ItemStack heldItem = player.getHeldItemMainhand();
		return new LinkedControllerContainer(AllContainerTypes.LINKED_CONTROLLER.get(), id, inv, heldItem);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent(getTranslationKey());
	}

}
