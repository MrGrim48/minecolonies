package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.blocks.types.RackType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Block for the shelves of the warehouse.
 */
public class BlockMinecoloniesRack extends AbstractBlockMinecoloniesRack<BlockMinecoloniesRack>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 10.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockminecoloniesrack";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = Float.POSITIVE_INFINITY;

    /**
     * How much light goes through the block.
     */
    private static final int LIGHT_OPACITY = 0;

    public BlockMinecoloniesRack()
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(VARIANT, RackType.DEFAULT));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
    }

    @Override
    public boolean propagatesSkylightDown(final BlockState state, @NotNull final IBlockReader reader, @NotNull final BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final World worldIn = context.getWorld();
        final BlockPos pos = context.getPos();
        final BlockState state = getDefaultState();
        final TileEntity entity = worldIn.getTileEntity(pos);

        if (!(entity instanceof TileEntityRack))
        {
            return super.getStateForPlacement(context);
        }

        final AbstractTileEntityRack rack = (AbstractTileEntityRack) entity;
        if (rack.isEmpty() && (rack.getOtherChest() == null || rack.getOtherChest().isEmpty()))
        {
            if (rack.getOtherChest() != null)
            {
                if (rack.isMain())
                {
                    return state.with(AbstractBlockMinecoloniesRack.VARIANT, RackType.DEFAULTDOUBLE).with(FACING, BlockPosUtil.getFacing(rack.getNeighbor(), pos));
                }
                else
                {
                    return state.with(AbstractBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                }
            }
            else
            {
                return state.with(AbstractBlockMinecoloniesRack.VARIANT, RackType.DEFAULT);
            }
        }
        else
        {
            if (rack.getOtherChest() != null)
            {
                if (rack.isMain())
                {
                    return state.with(AbstractBlockMinecoloniesRack.VARIANT, RackType.FULLDOUBLE)
                             .with(FACING, BlockPosUtil.getFacing(rack.getNeighbor(), pos));
                }
                else
                {
                    return state.with(AbstractBlockMinecoloniesRack.VARIANT, RackType.EMPTYAIR);
                }
            }
            else
            {
                return state.with(AbstractBlockMinecoloniesRack.VARIANT, RackType.FULL);
            }
        }
    }

    /**
     * Convert the BlockState into the correct metadata value.
     *
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public BlockState rotate(@NotNull final BlockState state, final Rotation rot)
    {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    /**
     * @deprecated (Remove this as soon as minecraft offers anything better).
     */
    @NotNull
    @Override
    @Deprecated
    public BlockState mirror(@NotNull final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public boolean doesSideBlockRendering(final BlockState state, final IEnviromentBlockReader world, final BlockPos pos, final Direction face)
    {
        return false;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockState updatePostPlacement(@NotNull final BlockState stateIn, final Direction facing, final BlockState state, final IWorld worldIn, final BlockPos currentPos, final BlockPos pos)
    {
        if (state.getBlock() instanceof BlockMinecoloniesRack || stateIn.getBlock() instanceof BlockMinecoloniesRack)
        {
            final TileEntity rack = worldIn.getTileEntity(pos);
            if (rack instanceof TileEntityRack)
            {
                ((AbstractTileEntityRack) rack).neighborChanged(currentPos);
            }
            final TileEntity rack2 = worldIn.getTileEntity(currentPos);
            if (rack2 instanceof TileEntityRack)
            {
                ((AbstractTileEntityRack) rack2).neighborChanged(pos);
            }
        }
        return super.updatePostPlacement(stateIn, facing, state, worldIn, currentPos, pos);
    }


    @Override
    public void spawnAdditionalDrops(final BlockState state, final World worldIn, final BlockPos pos, final ItemStack stack)
    {
        final TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntityRack)
        {
            final IItemHandler handler = ((AbstractTileEntityRack) tileentity).getInventory();
            InventoryUtils.dropItemHandler(handler, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        super.spawnAdditionalDrops(state, worldIn, pos, stack);
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos, final PlayerEntity player, final Hand handIn, final BlockRayTraceResult hit)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(worldIn, pos);
        final TileEntity tileEntity = worldIn.getTileEntity(pos);

        if ((colony == null || colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
              && tileEntity instanceof TileEntityRack)
        {
            final TileEntityRack rack = (TileEntityRack) tileEntity;
            if (!worldIn.isRemote)
            {
                NetworkHooks.openGui((ServerPlayerEntity) player, rack, buf -> buf.writeBlockPos(rack.getPos()).writeBlockPos(rack.getOtherChest() == null ? BlockPos.ZERO : rack.getOtherChest().getPos()));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(final World worldIn, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack)
    {
        BlockState tempState = state;
        tempState = tempState.with(VARIANT, RackType.DEFAULT);
        tempState = tempState.with(FACING, placer.getHorizontalFacing().getOpposite());

        worldIn.setBlockState(pos, tempState, 2);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TileEntityRack();
    }

    @Override
    public List<ItemStack> getDrops(final BlockState state, final LootContext.Builder builder)
    {
        final List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this, 1));
        return drops;
    }
}
