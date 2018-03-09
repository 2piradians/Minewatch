package twopiradians.minewatch.common.item.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.common.Minewatch;
import twopiradians.minewatch.common.entity.ability.EntityRoadhogHook;
import twopiradians.minewatch.common.entity.ability.EntityRoadhogScrap;
import twopiradians.minewatch.common.entity.projectile.EntityRoadhogBullet;
import twopiradians.minewatch.common.hero.Ability;
import twopiradians.minewatch.common.item.ModItems;
import twopiradians.minewatch.common.sound.ModSoundEvents;
import twopiradians.minewatch.common.util.EntityHelper;
import twopiradians.minewatch.common.util.TickHandler;
import twopiradians.minewatch.common.util.TickHandler.Handler;
import twopiradians.minewatch.common.util.TickHandler.Identifier;
import twopiradians.minewatch.packet.SPacketSimple;

public class ItemRoadhogWeapon extends ItemMWWeapon {

	private static final ResourceLocation CHAIN = new ResourceLocation(Minewatch.MODID, "textures/entity/roadhog_chain.png");
	public static final Handler HEALING = new Handler(Identifier.ROADHOG_HEALING, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {

			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {

			return super.onServerRemove();
		}
	};
	public static final Handler HOOKING = new Handler(Identifier.ROADHOG_HOOKING, true) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {

			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {

			return super.onServerRemove();
		}
	};
	public static final Handler HOOKED = new Handler(Identifier.ROADHOG_HOOKED, false) {
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onClientTick() {

			return super.onClientTick();
		}
		@Override
		public boolean onServerTick() {

			return super.onServerTick();
		}
		@Override
		@SideOnly(Side.CLIENT)
		public Handler onClientRemove() {

			return super.onClientRemove();
		}
		@Override
		public Handler onServerRemove() {

			return super.onServerRemove();
		}
	};

	public ItemRoadhogWeapon() {
		super(30);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityLivingBase player, EnumHand hand) { 
		// primary fire
		if (!world.isRemote && this.canUse(player, true, hand, false) && !TickHandler.hasHandler(player, Identifier.ROADHOG_HEALING)) {
			for (int i=0; i<25; ++i) {
				EntityRoadhogBullet projectile = new EntityRoadhogBullet(world, player, hand.ordinal());
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 60, 19F, hand, 10, 0);
				world.spawnEntity(projectile);
			}
			ModSoundEvents.ROADHOG_SHOOT_0.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(25) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			this.setCooldown(player, 26);
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityLivingBase player, EnumHand hand) {
		// secondary fire
		if (!world.isRemote && this.canUse(player, true, hand, false) && !TickHandler.hasHandler(player, Identifier.ROADHOG_HEALING)) {
			EntityRoadhogScrap projectile = new EntityRoadhogScrap(world, player, hand.ordinal());
			EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 60, 0F, hand, 10, 0);
			world.spawnEntity(projectile);
			ModSoundEvents.ROADHOG_SHOOT_1.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
			this.subtractFromCurrentAmmo(player, 1);
			if (world.rand.nextInt(25) == 0)
				player.getHeldItem(hand).damageItem(1, player);
			this.setCooldown(player, 26);
		}

		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		super.onUpdate(stack, world, entity, slot, isSelected);

		if (isSelected && entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHeldItemMainhand() == stack) {	
			EntityLivingBase player = (EntityLivingBase) entity;

			// hook
			if (!world.isRemote && hero.ability2.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, true)) { 
				EntityRoadhogHook projectile = new EntityRoadhogHook(world, player, EnumHand.OFF_HAND.ordinal());
				EntityHelper.setAim(projectile, player, player.rotationPitch, player.rotationYawHead, 40, 0F, EnumHand.OFF_HAND, 10, 0);
				world.spawnEntity(projectile);
				TickHandler.register(false, HOOKING.setEntity(projectile).setEntityLiving(player).setTicks(10),
						Ability.ABILITY_USING.setEntity(player).setTicks(100).setAbility(hero.ability2));
				Minewatch.network.sendToDimension(new SPacketSimple(55, player, false, projectile), world.provider.getDimension());
				ModSoundEvents.ROADHOG_HOOK_THROW.playSound(player, world.rand.nextFloat()+0.5F, world.rand.nextFloat()/3+0.8f);
			}
			// health
			else if (!world.isRemote && (player.getHeldItemOffhand() == null || player.getHeldItemOffhand().isEmpty()) && 
					hero.ability1.isSelected(player) && 
					this.canUse(player, true, EnumHand.MAIN_HAND, false) && 
					!TickHandler.hasHandler(player, Identifier.ROADHOG_HEALING)) {
				ModSoundEvents.ROADHOG_HEAL_0.playFollowingSound(player, 1, 1, false);
				ModSoundEvents.ROADHOG_HEAL_1.playFollowingSound(player, 1, 1, false);
				ModSoundEvents.ROADHOG_HEAL_2.playFollowingSound(player, 1, 1, false);
				TickHandler.register(false, HEALING.setEntity(player).setTicks(40));
				Minewatch.network.sendToDimension(new SPacketSimple(74, player, true), world.provider.getDimension());
				player.setHeldItem(EnumHand.OFF_HAND, new ItemStack(ModItems.roadhog_health));
				player.setActiveHand(EnumHand.OFF_HAND);
			}

		}
	}	
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderHand(AbstractClientPlayer player, EnumHand hand) {
		return hand == EnumHand.OFF_HAND &&
				TickHandler.hasHandler(handler -> handler.identifier == Identifier.ROADHOG_HOOKING && handler.entityLiving == Minecraft.getMinecraft().player, true);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderHookChain(RenderWorldLastEvent event) {
		/*for (Handler handler : TickHandler.getHandlers(true, null, Identifier.ROADHOG_HOOKING, null)) {
			// rope
			if (handler.entity instanceof EntityWidowmakerHook && 
					((EntityWidowmakerHook) handler.entity).getThrower() != null) {
				EntityWidowmakerHook entity = (EntityWidowmakerHook) handler.entity;
				Minecraft mc = Minecraft.getMinecraft();
				GlStateManager.pushMatrix();
				GlStateManager.enableLighting();
				mc.getTextureManager().bindTexture(ROPE);
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX);

				double width = 0.04d;
				Vec3d playerPos = EntityHelper.getEntityPartialPos(Minewatch.proxy.getRenderViewEntity());
				Vec3d throwerPos = EntityHelper.getEntityPartialPos(entity.getThrower());
				Vector2f rotations = EntityHelper.getEntityPartialRotations(entity.getThrower());
				Vec3d shooting = EntityHelper.getShootingPos(entity.getThrower(), rotations.x, rotations.y, EnumHand.OFF_HAND, 23, 0.7f).subtract(throwerPos);

				// translate to thrower
				Vec3d translate = throwerPos.subtract(playerPos);
				GlStateManager.translate(translate.x, translate.y, translate.z);
				
				Vec3d hookLook = entity.getLook(mc.getRenderPartialTicks()).scale(0.17d);
				Vec3d hookPos = EntityHelper.getEntityPartialPos(entity).addVector(0, entity.height/2f, 0).subtract(hookLook).subtract(throwerPos);
				double v = hookPos.distanceTo(shooting)*2d;

				double deg_to_rad = 0.0174532925d;
				double precision = 0.05d;
				double degrees = 360d;
				double steps = Math.round(degrees*precision);
				degrees += 21.2d;
				double angle = 0;

				for (int i=1; i<=steps; i+=2) {
					angle = degrees/steps*i;
					double circleX = Math.cos(angle*deg_to_rad);
					double circleY = Math.sin(angle*deg_to_rad);
					double circleZ = 0;//Math.cos(angle*deg_to_rad);
					Vec3d vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(hookPos);
					buffer.pos(vec.x, vec.y, vec.z).tex(i/steps, 0).endVertex();

					vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(shooting);
					buffer.pos(vec.x, vec.y, vec.z).tex(i/steps, v).endVertex();

					angle = degrees/steps*(i+1);
					circleX = Math.cos(angle*deg_to_rad);
					circleY = Math.sin(angle*deg_to_rad);
					circleZ = 0;//Math.cos(angle*deg_to_rad);
					vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(hookPos);
					buffer.pos(vec.x, vec.y, vec.z).tex((i+1)/steps, 0).endVertex();

					vec = new Vec3d(circleX, circleY, circleZ).scale(width).add(shooting);
					buffer.pos(vec.x, vec.y, vec.z).tex((i+1)/steps, v).endVertex();
				}


				tessellator.draw();
				GlStateManager.popMatrix();
			}
		}*/
	}

}