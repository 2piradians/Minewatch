package twopiradians.minewatch.client.attachment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.minewatch.client.attachment.AttachmentManager.Type;
import twopiradians.minewatch.client.render.EntityOBJModel;

public abstract class Attachment {

	public AttachmentManager.Type type;
	public int ticksExisted;
	public int lifetime = -1;
	@SideOnly(Side.CLIENT)
	private EntityOBJModel render;
	
	public Attachment(Type type) {
		this.type = type;
	}
	
	@SideOnly(Side.CLIENT)
	public EntityOBJModel getRender() {
		if (this.render == null)
			this.render = createRender(Minecraft.getMinecraft().getRenderManager());
		return this.render;
	}

	@SideOnly(Side.CLIENT)
	public abstract EntityOBJModel createRender(RenderManager manager);

	public void onUpdate(EntityLivingBase entity) {
		if (++this.ticksExisted > this.lifetime && this.lifetime > 0)
			AttachmentManager.removeAttachments(entity, type);
	}
	
}
