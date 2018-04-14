package twopiradians.minewatch.client.attachment;

import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.minewatch.common.hero.EnumHero;

@Mod.EventBusSubscriber
public class AttachmentManager {

	public enum Type {
		SOMBRA_ULTIMATE_DOME
	}

	public static HashMap<EntityLivingBase, HashSet<Attachment>> attachments = Maps.newHashMap();

	/**Called when set changed, first logging in, or hero spawned - only on CLIENT*/
	public static void onSetChanged(EntityLivingBase player, @Nullable EnumHero prevHero, @Nullable EnumHero newHero) {
		// remove all attachments if was wearing set before
		if (prevHero != null) {
			removeAttachments(player, Type.values());
		}

		// add new attachments for a hero
		if (newHero != null) {
			switch(newHero) {
			}
		}
	}

	/**Called per tick for entities wearing a full set - only on CLIENT*/
	public static void onUpdate(EntityLivingBase entity) {
		for (Attachment att : getAttachments(entity))
			att.onUpdate(entity);
	}

	public static HashSet<Attachment> getAttachments(EntityLivingBase entity) {
		HashSet<Attachment> set = attachments.get(entity);
		if (set == null)
			set = new HashSet();
		return set;
	}

	public static void addAttachments(EntityLivingBase entity, Type... types) {
		HashSet<Attachment> set = getAttachments(entity);

		for (Type type : types) {
			switch (type) {
			case SOMBRA_ULTIMATE_DOME:
				set.add(new AttachmentSombraUltimateDome());
				break;
			}
		}

		attachments.put(entity, set);
	}

	public static void removeAttachments(EntityLivingBase entity, Type... types) {
		HashSet<Attachment> set = getAttachments(entity);
		HashSet<Attachment> newSet = (HashSet<Attachment>) set.clone();

		for (Attachment att : set) 
			for (Type type : types) 
				if (type == att.type) {
					newSet.remove(att);
					break;
				}

		attachments.put(entity, newSet);
	}

	@SubscribeEvent
	public static void onRender(RenderWorldLastEvent event) {
		for (EntityLivingBase entity : attachments.keySet())
			for (Attachment att : getAttachments(entity)) {
				att.getRender().doRender(entity, -1, -1, -1, 0, event.getPartialTicks(), att);
			}
	}

}