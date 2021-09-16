package chylex.hee.sound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import chylex.hee.system.logging.Log;
import chylex.hee.system.util.ReflectionUtils;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class MusicManager{
	public static boolean enableCustomMusic = true;
	public static boolean removeVanillaDelay = false;
	
	public static void register(){
		MinecraftForge.EVENT_BUS.register(new MusicManager());
	}
	
	private boolean hasLoaded;
	
	private MusicManager(){}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onSoundLoad(SoundLoadEvent e){
		if (hasLoaded || (!enableCustomMusic && !removeVanillaDelay))return;
		
		Minecraft mc = Minecraft.getMinecraft();
		MusicTicker mcMusicTicker = ReflectionUtils.getFieldValue(mc, "mcMusicTicker");
		
		
		if (mcMusicTicker != null){
			Class<? extends MusicTicker> tickerClass = mcMusicTicker.getClass();
			
			if (tickerClass == MusicTicker.class){
				ReflectionUtils.setFieldValue(mc, "mcMusicTicker", new CustomMusicTicker(mc,null));
				Log.info("Successfully replaced music system.");
			}
			else{
				ReflectionUtils.setFieldValue(mc, "mcMusicTicker", new CustomMusicTicker(mc,mcMusicTicker));
				Log.info("Successfully wrapped a music system replaced by another mod: $0",tickerClass.getName());
			}

			hasLoaded = true;
		}
	}
}
