package me.thosea.celestialgdx.graphics;

import me.thosea.celestialgdx.graphics.Shader.IntUniform;
import me.thosea.celestialgdx.graphics.Shader.Mat3x2fUniform;
import me.thosea.celestialgdx.graphics.mesh.BufferUsage;
import me.thosea.celestialgdx.graphics.mesh.Ebo;
import me.thosea.celestialgdx.graphics.mesh.Mesh;
import me.thosea.celestialgdx.graphics.mesh.Mesh.VxAttrib;
import me.thosea.celestialgdx.graphics.mesh.Vbo;
import me.thosea.celestialgdx.image.TextureRegion;
import me.thosea.celestialgdx.utils.Disposable;
import org.joml.Matrix3x2fStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.GL_FLOAT;
import static org.lwjgl.opengl.GL33.GL_TRIANGLES;

public class SpriteInstancer implements Disposable {
	private final Mesh mesh;
	private final Vbo vbo;
	private final Ebo ebo;

	private final FloatBuffer instances;
	private final int spriteLimit;

	private final Matrix3x2fStack transform = new Matrix3x2fStack();
	private final SpriteInstancerShader shader;
	private final boolean ownsShader;

	private Texture texture = null;
	private boolean drawing = false;
	private int spritesDrawn = 0;

	private boolean disposed = false;

	public SpriteInstancer() {
		this(1000, new DefaultShader(), /*ownsShader*/ true);
	}
	public SpriteInstancer(int sprites) {
		this(sprites, new DefaultShader(), /*ownsShader*/ true);
	}
	public SpriteInstancer(SpriteInstancerShader shader) {
		this(1000, shader, false);
	}
	public SpriteInstancer(int sprites, SpriteInstancerShader shader) {
		this(sprites, shader, false);
	}

	private SpriteInstancer(int sprites, SpriteInstancerShader shader, boolean ownsShader) {
		if(sprites < 1) {
			throw new IllegalArgumentException("cannot have maximum sprite count below 1");
		}

		this.vbo = Vbo.create(BufferUsage.DYNAMIC);
		this.ebo = Ebo.create(BufferUsage.STATIC);
		short[] indices = {0, 1, 2, 2, 3, 0};
		ebo.uploadIndices(indices);
		this.mesh = Mesh.create();
		this.mesh.setEbo(this.ebo);

		this.mesh.setAttributes(
				/*autoPosition*/ true,
				VxAttrib.of(vbo, 4, GL_FLOAT).divisor(1), // aScalePos
				VxAttrib.of(vbo, 4, GL_FLOAT).divisor(1) // aTexCoords
		);

		this.instances = MemoryUtil.memAllocFloat(sprites * 32);
		this.spriteLimit = sprites;

		this.shader = shader;
		this.ownsShader = ownsShader;
	}

	public void begin() {
		this.requireNotDisposed();
		if(this.drawing) throw new IllegalStateException("already in drawing mode");
		this.shader.bind();
		this.drawing = true;
	}

	public void end() {
		this.requireNotDisposed();
		if(!this.drawing) throw new IllegalStateException("not in drawing mode");
		if(this.spritesDrawn > 0) flush();
		this.texture = null;
		this.drawing = false;
	}

	public void draw(TextureRegion region, float x, float y) {
		this.draw(region, x, y, region.width, region.height);
	}

	public void draw(TextureRegion region, float x, float y, float width, float height) {
		this.requireNotDisposed();
		if(this.spritesDrawn >= this.spriteLimit) {
			this.flush();
		} else if(this.texture == null || this.texture.getHandle() != region.texture.getHandle()) {
			this.flush();
			this.texture = region.texture;
		}
		// aScalePos
		instances.put(width);
		instances.put(height);
		instances.put(x);
		instances.put(y);
		// aTexCoords
		instances.put(region.u);
		instances.put(region.v2);
		instances.put(region.u2);
		instances.put(region.v);
		this.spritesDrawn++;
	}

	public Matrix3x2fStack getTransform() {
		return this.transform;
	}

	public void updateTransform() {
		this.requireNotDisposed();
		shader.bind();
		shader.texture().set(0);
		shader.transform().set(this.transform, /*transpose*/ false);
	}

	public void flush() {
		this.requireNotDisposed();
		if(this.spritesDrawn == 0) return;

		shader.bind();
		texture.bindTexture(0);

		mesh.bind();
		vbo.bind();

		instances.flip();
		vbo.uploadVertices(instances);
		instances.clear();

		mesh.renderInstanced(GL_TRIANGLES, 6, this.spritesDrawn);

		this.spritesDrawn = 0;
	}

	@Override
	public void dispose() {
		requireNotDisposed();
		mesh.dispose();
		vbo.dispose();
		ebo.dispose();
		if(this.ownsShader) {
			((DefaultShader) this.shader).dispose();
		}
		MemoryUtil.memFree(this.instances);
		this.disposed = true;
	}
	@Override
	public boolean isDisposed() {return disposed;}

	public interface SpriteInstancerShader {
		Mat3x2fUniform transform();
		IntUniform texture();

		void bind();
	}

	public static final String VERTEX_SHADER = """
			#version 330
			precision mediump float;
			
			layout (location = 0) in vec4 aScalePos;
			layout (location = 1) in vec4 aTexCoords;
			uniform mat3x2 uTransform;
			out vec2 fTexCoord;
			
			const vec2 positions[4] = vec2[4](
				vec2(1.0, 1.0),
				vec2(1.0, 0.0),
				vec2(0.0, 0.0),
				vec2(0.0, 1.0)
			);
			
			void main() {
				vec2 basePos = positions[gl_VertexID];
				vec2 pos = basePos * aScalePos.xy + aScalePos.zw;
				gl_Position = vec4(uTransform * vec3(pos, 1.0), 1.0, 1.0);
				fTexCoord = mix(aTexCoords.xy, aTexCoords.zw, basePos);
			}""";
	public static final String FRAGMENT_SHADER = """
			#version 330
			precision mediump float;
			
			in vec2 fTexCoord;
			uniform sampler2D uTexture;
			out vec4 FragColor;
			
			void main() {
				FragColor = texture(uTexture, fTexCoord);
			}""";

	private static final class DefaultShader extends Shader implements SpriteInstancerShader {
		public final Mat3x2fUniform transform = uMat3x2f("uTransform");
		public final IntUniform texture = uInt("uTexture");

		@Override
		public Mat3x2fUniform transform() {return transform;}
		@Override
		public IntUniform texture() {return texture;}

		public DefaultShader() {
			super(VERTEX_SHADER, FRAGMENT_SHADER);
		}
	}
}