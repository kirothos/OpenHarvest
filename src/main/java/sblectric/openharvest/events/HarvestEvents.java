package sblectric.openharvest.events;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sblectric.openharvest.config.HarvestConfig;
import sblectric.openharvest.util.HarvestUtils;

public class HarvestEvents {

	/** Harvest crops on this event */
	@SubscribeEvent
	public void onHarvestCrops(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		if(world.isRemote) return; // do nothing on client thread

		BlockPos pos = event.getPos();
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		EntityPlayerMP ply = (EntityPlayerMP)event.getEntityPlayer();

		// make sure the off-hand is active and right-clicking a crop block
		if(event.getHand() == EnumHand.OFF_HAND && block instanceof BlockCrops) {
			
			// make sure the block isn't blacklisted
			ResourceLocation name = block.getRegistryName();
			String modid = name.getResourceDomain();
			if(!HarvestConfig.modBlacklist.contains(modid) && !HarvestConfig.blockBlacklist.contains(name.toString())) {
	
				// get the age type
				PropertyInteger age = null;
				int maxAge = -1;
				for(IProperty prop : state.getPropertyNames()) {
					if(prop instanceof PropertyInteger && prop.getName().equals("age")) {
						age = (PropertyInteger)prop;
						maxAge = HarvestUtils.max(age.getAllowedValues());
						break;
					}
				}
	
				// make sure the age property is valid
				if(age != null && maxAge > -1) {
					if(state.getValue(age) == maxAge) {
						for(ItemStack s : block.getDrops(world, pos, state, 0)) {
							ply.inventory.addItemStackToInventory(s);
						}
						world.setBlockState(pos, block.getDefaultState());
					}
				}
				
			}
		}
	}
}

