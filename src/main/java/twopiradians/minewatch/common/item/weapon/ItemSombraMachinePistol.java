package twopiradians.minewatch.common.item.weapon;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.key.Keys.KeyBind;
import twopiradians.minewatch.client.model.ModelMWArmor;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.EntitySombraBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.hero.EnumHero;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.tickhandler.TickHandler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Handler;
import twopiradians.minewatch.common.tickhandler.TickHandler.Identifier;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemSombraMachinePistol extends ItemMWWeapon {

	public static final Handler INVISIBLE = new Handler(Identifier.SOMBRA_INVISIBLE, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {
			if (this.ticksLeft == 14) {
				if (this.player == Minewatch.proxy.getClientPlayer() && player.world.isRemote)
					Minewatch.proxy.stopSound(player, ModSoundEvents.sombraInvisibleStart, SoundCategory.PLAYERS);
				Minewatch.proxy.playFollowingSound(entity, ModSoundEvents.sombraInvisibleStop, SoundCategory.PLAYERS, 1.0f, 1.0f, false);
				Minewatch.proxy.playFollowingSound(entity, ModSoundEvents.sombraInvisibleVoice, SoundCategory.PLAYERS, 0.7f, 1.0f, false);
			}

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {
			if (this.entityLiving != null) {
				this.entityLiving.addPotionEffect(new PotionEffect(MobEffects.SPEED, 5, 2, true, false));
				this.entityLiving.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 5, 2, true, false));
			}

			return super.onServerTick();
		}
		@Override
		public Handler onRemove() {
			EnumHero.SOMBRA.ability3.keybind.setCooldown(player, 12, false); // TODO
			Handler handler = TickHandler.getHandler(entity, Identifier.ABILITY_USING);
			if (handler != null && handler.ability == EnumHero.SOMBRA.ability3 && entity != null)
				TickHandler.unregister(entity.world.isRemote, handler);
			return super.onRemove();
		}
	};

	public ItemSombraMachinePistol() {
		super(30);
		this.savePlayerToNBT = true;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) { 
		cancelInvisibility(player);

		// shoot
		if (this.canUse(player, true, hand, false)) {
			if (!world.isRemote) {
				EntitySombraBullet bullet = new EntitySombraBullet(world, player, hand.ordinal());
				EntityHelper.setAim(bullet, player, player.rotationPitch, player.rotationYaw, -1, 1.5F, hand, 12, 0.43f);
				world.spawnEntity(bullet);
				world.playSound(null, player.posX, player.posY, player.posZ, 
						ModSoundEvents.sombraShoot, SoundCategory.PLAYERS, world.rand.nextFloat()+0.5F, 
						world.rand.nextFloat()/3+0.8f);	
				this.subtractFromCurrentAmmo(player, 1);
				if (world.rand.nextInt(25) == 0)
					player.getHeldItem(hand).damageItem(1, player);
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {	
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityPlayer) {	
			EntityPlayer player = (EntityPlayer) entity;

			// cancel invisibility
			if (hero.ability3.keybind.isKeyDown(player) || KeyBind.RMB.isKeyDown(player)) {
				Handler handler = TickHandler.getHandler(player, Identifier.SOMBRA_INVISIBLE);
				if (handler != null && handler.initialTicks-handler.ticksLeft > 30)
					cancelInvisibility(player);
			}

			// invisibility
			if (!world.isRemote && hero.ability3.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) {
				TickHandler.register(false, INVISIBLE.setEntity(player).setTicks(130),
						Ability.ABILITY_USING.setEntity(player).setTicks(130).setAbility(hero.ability3));
				Minewatch.network.sendToDimension(new SPacketSimple(27, player, true), world.provider.getDimension());
			}
		}
	}

	/**Set Invisibility handler to 14 ticks, if active*/
	public static void cancelInvisibility(EntityLivingBase entity) {
		Handler handler = TickHandler.getHandler(entity, Identifier.SOMBRA_INVISIBLE);
		if (handler != null && handler.ticksLeft > 14)
			handler.ticksLeft = 14;
	}

	@SubscribeEvent
	public void cancelInvisWhenAttacked(LivingHurtEvent event) {
		if (TickHandler.hasHandler(event.getEntityLiving(), Identifier.SOMBRA_INVISIBLE)) {
			cancelInvisibility(event.getEntityLiving());
			Minewatch.network.sendToDimension(new SPacketSimple(27, event.getEntityLiving(), false), event.getEntityLiving().world.provider.getDimension());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderArmor(EntityLivingBase entity, ModelMWArmor model) {
		// invisibility
		Handler handler = TickHandler.getHandler(entity, Identifier.SOMBRA_INVISIBLE);
		if (handler != null) {
			GlStateManager.enableCull();
			float percent = 1f;
			if (handler.ticksLeft > handler.initialTicks-14)
				percent = (handler.initialTicks-handler.ticksLeft) / 14f;
			else if (handler.ticksLeft < 14)
				percent = handler.ticksLeft / 14f;
			// full alpha if not friendly
			float alpha = EntityHelper.shouldHit(entity, Minecraft.getMinecraft().player, false) ? 1 : 0.5f;
			GlStateManager.color((255f-20f*percent)/255f, (255f-109f*percent)/255f, (255f-3f*percent)/255f, 1f-percent*alpha);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Pair<? extends IBakedModel, Matrix4f> preRenderWeapon(EntityLivingBase entity, ItemStack stack, TransformType cameraTransformType, Pair<? extends IBakedModel, Matrix4f> ret) {
		// hide gun if not friendly
		if (TickHandler.hasHandler(entity, Identifier.SOMBRA_INVISIBLE) && 
				EntityHelper.shouldHit(entity, Minecraft.getMinecraft().player, false)) {
			ret.getRight().setScale(0);
		}
		return ret;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int tintIndex) {
		EntityPlayer player = ItemMWWeapon.getPlayer(Minecraft.getMinecraft().world, stack);
		return TickHandler.hasHandler(player, Identifier.SOMBRA_INVISIBLE) ? 0xFB8AFE : -1;
	}

}