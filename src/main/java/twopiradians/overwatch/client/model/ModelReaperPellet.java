package twopiradians.overwatch.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelReaperPellet extends ModelBase {

	public ModelRenderer pellet;
	
	public ModelReaperPellet() {
		this.textureWidth = 16;
		this.textureHeight = 16;
		
		this.pellet = new ModelRenderer(this);
		this.pellet.addBox(0f, 0f, 0f, 1, 1, 1);
		this.pellet.setRotationPoint(0f, 0f, 0f);
	}
	
	@Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.pellet.render(scale);
    }	
}
