package chylex.hee.mechanics.charms.handler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import chylex.hee.mechanics.charms.CharmPouchInfo;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public final class CharmPouchHandlerClient{
	private static CharmPouchHandlerClient instance;
	
	public static void register(){
		FMLCommonHandler.instance().bus().register(instance = new CharmPouchHandlerClient());
	}
	
	public static void onActivePouchUpdate(EntityPlayer player, ItemStack is){
		if (is != instance.activePouch){
			instance.activePouch = is;
			instance.refresh = true;
		}
		
		instance.setInactiveTimer = 5;
	}
	
	public static CharmPouchInfo getActivePouch(){
		return instance.activePouchInfo;
	}
	
	private final CharmEventsClient eventsClient = new CharmEventsClient();
	private ItemStack activePouch;
	private CharmPouchInfo activePouchInfo;
	private byte setInactiveTimer;
	private boolean prevHadPouch;
	private boolean refresh;
	
	private CharmPouchHandlerClient(){}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e){
		if (e.phase != Phase.END)return;
		
		if (setInactiveTimer > 0){
			if (--setInactiveTimer == 0){
				activePouch = null;
				activePouchInfo = null;
				refresh = true;
			}
		}
		
		if (refresh){
			if (!prevHadPouch && activePouch != null){
				activePouchInfo = new CharmPouchInfo(activePouch);
				prevHadPouch = true;
				
				MinecraftForge.EVENT_BUS.register(eventsClient);
			}
			else if (prevHadPouch && activePouch == null){
				activePouchInfo = null;
				prevHadPouch = false;
				
				MinecraftForge.EVENT_BUS.unregister(eventsClient);
			}
		}
		
		refresh = false;
	}
}