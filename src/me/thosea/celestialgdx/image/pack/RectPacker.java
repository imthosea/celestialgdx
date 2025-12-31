package me.thosea.celestialgdx.image.pack;

import me.thosea.celestialgdx.utils.Disposable;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBRPContext;
import org.lwjgl.stb.STBRPNode;
import org.lwjgl.stb.STBRPRect;
import org.lwjgl.stb.STBRectPack;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.stb.STBRectPack.stbrp_init_target;

/**
 * Packs rectangles using STBRectPack.
 * <p>
 * Create one using {@link #create}. The width and height cannot be changed afterward.
 * </p>
 * <p>
 * Pack a rectangle using {@link #pack}.
 * If packing succeeds, it'll return a {@link RectangleSlot} containing the X/Y and width/height
 * of the packed rectangle. If there's no space, null will be returned.
 * For the best results, it's recommended to sort rectangles by height before packing them.
 * </p>
 * <p>
 * Rect packer store native buffers and thus must be {@link #dispose()}d of when you're done.
 * Most methods will throw an exception if called after its called.
 * Use {@link #isDisposed()} to check.
 * </p>
 */
public final class RectPacker implements Disposable {
	public final int width;
	public final int height;

	private final STBRPContext ctx;
	private final STBRPNode.Buffer nodes;

	private boolean disposed = false;

	private RectPacker(int width, int height) {
		if(width < 1 || height < 1) {
			// a width below 1 makes stbrp crash the JVM
			// not height, but check anyway for consistency
			throw new IllegalArgumentException("cannot have width/height below 1");
		}

		this.width = width;
		this.height = height;
		this.ctx = STBRPContext.malloc();
		this.nodes = STBRPNode.malloc(width);
		stbrp_init_target(ctx, width, height, nodes);
	}

	public STBRPNode.Buffer getNodes() {
		this.requireNotDisposed();
		return nodes;
	}

	public STBRPContext getContext() {
		this.requireNotDisposed();
		return ctx;
	}

	/**
	 * Attempts to pack the specified rectangle. Returns null if it can't fit.
	 */
	@Nullable
	public RectangleSlot pack(int width, int height) {
		this.requireNotDisposed();
		if(width < 0 || height < 0) {
			/*
			 * having both width and height be negative makes stbrp crash the JVM
			 * having only one be negative doesn't crash, but that's probably not what you want
			 */
			throw new IllegalArgumentException("cannot have negative width/height");
		}
		try(MemoryStack stack = MemoryStack.stackPush()) {
			STBRPRect rect = STBRPRect.malloc(stack);
			rect.w(width);
			rect.h(height);
			STBRectPack.nstbrp_pack_rects(ctx.address(), rect.address(), 1);
			if(!rect.was_packed()) {
				return null;
			}
			return new RectangleSlot(rect.x(), rect.y(), rect.w(), rect.h());
		}
	}

	@Override
	public void dispose() {
		this.requireNotDisposed();
		this.ctx.free();
		this.nodes.free();
		this.disposed = true;
	}
	@Override
	public boolean isDisposed() {
		return disposed;
	}

	public static RectPacker create(int width, int height) {
		return new RectPacker(width, height);
	}
}