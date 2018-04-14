package twopiradians.minewatch.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import twopiradians.minewatch.client.render.RenderSimple;
import twopiradians.minewatch.common.entity.projectile.EntityZenyattaOrb;
import twopiradians.minewatch.common.hero.EnumHero;

public class RenderZenyattaOrb extends RenderSimple<EntityZenyattaOrb> {

	private final RenderItem itemRenderer;

	public static final ItemStack NORMAL = new ItemStack(EnumHero.ZENYATTA.weapon);
	public static final ItemStack HARMONY = new ItemStack(EnumHero.ZENYATTA.weapon);
	public static final ItemStack DISCORD = new ItemStack(EnumHero.ZENYATTA.weapon);

	public RenderZenyattaOrb(RenderManager manager) {
		super(manager, null, "", 0, 0, 0);
		this.itemRenderer = Minecraft.getMinecraft().getRenderItem();
	}

	public ItemStack getStackToRender(EntityZenyattaOrb entityIn) {
		if (entityIn.type == 0)
			return NORMAL;
		else if (entityIn.type == 1)
			return DISCORD;
		else if (entityIn.type == 2)
			return HARMONY;
		else
			return new ItemStack(EnumHero.ZENYATTA.weapon);
	}

	@Override
	public void doRender(EntityZenyattaOrb entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		if (entity.ticksExisted <= 2)
			return;

		GlStateManager.pushMatrix();
		GlStateManager.translate(x+0.15d, y+0.07d, z);

		this.itemRenderer.renderItem(this.getStackToRender(entity), ItemCameraTransforms.TransformType.FIXED);

		GlStateManager.popMatrix();
	}
}