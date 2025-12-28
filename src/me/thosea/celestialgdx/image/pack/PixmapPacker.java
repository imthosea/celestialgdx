package me.thosea.celestialgdx.image.pack;

import com.badlogic.gdx.utils.Disposable;
import me.thosea.celestialgdx.image.PixelFormat;
import me.thosea.celestialgdx.image.Pixmap;
import me.thosea.celestialgdx.image.trim.PixmapTrim;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PixmapPacker<K> implements Disposable {
	public final PixelFormat format;
	public final int width;
	public final int height;
	public final int padding;
	public final int maxPages;

	private final List<Page<K>> pages = new ArrayList<>();
	private final List<Page<K>> pagesView = Collections.unmodifiableList(pages);
	private boolean disposed = false;

	private PixmapPacker(PixelFormat format, int width, int height, int padding, int maxPages) {
		this.format = format;
		this.width = width;
		this.height = height;
		this.padding = padding;
		this.maxPages = maxPages;
		this.pages.add(new Page<>(this));
	}

	public PackedEntry<K> pack(Pixmap pixmap) {
		return pack(null, pixmap, null);
	}
	public PackedEntry<K> pack(@Nullable K key, Pixmap pixmap) {
		return pack(key, pixmap, null);
	}
	public PackedEntry<K> pack(Pixmap pixmap, PixmapTrim trim) {
		return pack(null, pixmap, trim);
	}

	public PackedEntry<K> pack(@Nullable K key, Pixmap pixmap, @Nullable PixmapTrim trim) {
		pixmap.requireNotDisposed();

		int width;
		int height;
		if(trim == null) {
			width = pixmap.width;
			height = pixmap.height;
		} else {
			width = trim.width();
			height = trim.height();
			if(width < 0 || height < 0) {
				throw new IllegalArgumentException("Trim has negative width/height");
			} else if(width > pixmap.width || height > pixmap.height) {
				throw new IllegalArgumentException("Trim is too big for the pixmap");
			}
		}

		if(width + padding > this.width || height + padding > this.height) {
			throw new IllegalArgumentException("The pixmap is too big for this packer");
		}

		Page<K> page = this.pages.getLast();
		RectangleSlot slot = page.packer.pack(width + padding, height + padding);
		if(slot == null) { // out of space
			if(this.maxPages > 0 && pages.size() >= maxPages) {
				throw new IllegalStateException("Hit max page limit");
			}
			this.pages.add(page = new Page<>(this));
			slot = page.packer.pack(width + padding, height + padding);
			if(slot == null) throw new AssertionError("Couldn't pack to empty packer");
		}

		slot = slot.withSize(width, height); // remove padding
		page.pixmap.copyFrom(
				pixmap,
				/*targetX*/ slot.x(), /*targetY*/ slot.y(),
				/*srcX*/ trim != null ? trim.left() : 0,
				/*srcY*/ trim != null ? trim.top() : 0,
				width, height
		);
		PackedEntry<K> entry = new PackedEntry<>(key, slot, trim);
		page.entries.add(entry);
		return entry;
	}

	public List<Page<K>> getPages() {
		return pagesView;
	}

	@Override
	public void dispose() {
		this.requireNotDisposed();
		for(Page<K> page : pages) {
			page.pixmap.dispose();
			page.packer.dispose();
		}
		this.disposed = true;
	}

	public void requireNotDisposed() {
		if(disposed) throw new IllegalStateException("already disposed");
	}

	public record PackedEntry<K>(K key, RectangleSlot slot, @Nullable PixmapTrim trim) {}

	// intellij isn't able to figure out the type of List<PackedEntry<K>>
	// in a non-static nested class that uses the parent generic, for some reason
	public static final class Page<K> {
		private final Pixmap pixmap;
		private final RectPacker packer;
		private final List<PackedEntry<K>> entries = new ArrayList<>();
		private final List<PackedEntry<K>> entriesView = Collections.unmodifiableList(entries);

		private Page(PixmapPacker<K> packer) {
			this.pixmap = Pixmap.create(packer.format, packer.width, packer.height);
			this.packer = RectPacker.create(packer.width, packer.height);
		}

		public Pixmap getPixmap() {
			packer.requireNotDisposed();
			return pixmap;
		}
		public List<PackedEntry<K>> getPacked() {
			return entriesView;
		}
	}

	public static <K> PixmapPacker<K> create(PixelFormat format, int width, int height, int padding, int maxPages) {
		return new PixmapPacker<>(format, width, height, maxPages, padding);
	}

	public static <K> PixmapPacker<K> create(PixelFormat format, int width, int height, int padding) {
		return new PixmapPacker<>(format, width, height, padding, /*maxPages*/ -1);
	}

	public static <K> PixmapPacker<K> create(PixelFormat format, int width, int height) {
		return new PixmapPacker<>(format, width, height, /*padding*/ 0, /*maxPages*/ -1);
	}
}