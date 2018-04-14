package twopiradians.minewatch.client.attachment;

import net.minecraft.client.renderer.entity.RenderManager;
import twopiradians.minewatch.client.attachment.AttachmentManager.Type;
import twopiradians.minewatch.client.render.EntityOBJModel;
import twopiradians.minewatch.client.render.attachments.RenderSombraUltimateDome;

public class AttachmentSombraUltimateDome extends Attachment {

	public AttachmentSombraUltimateDome() {
		super(Type.SOMBRA_ULTIMATE_DOME);
		this.lifetime = 15;
	}

	@Override
	public EntityOBJModel createRender(RenderManager manager) {
		return new RenderSombraUltimateDome(manager);
	}

}
