package me.thosea.celestialgdx.maps.loader;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import me.thosea.celestialgdx.assets.AssetLoader;
import me.thosea.celestialgdx.assets.AssetLoaderParameters;
import me.thosea.celestialgdx.assets.AssetLoadingContext;
import me.thosea.celestialgdx.maps.MapProperties;
import me.thosea.celestialgdx.maps.TiledProject;
import me.thosea.celestialgdx.maps.loader.element.MapElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * celestialgdx - loader for tiled projects
 */
public final class TiledProjectLoader extends AssetLoader<TiledProject, TiledProjectLoader.Parameters> {
	public TiledProjectLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	public class Parameters extends AssetLoaderParameters<TiledProject> {
		/**
		 * whether custom classes should be loaded.
		 * if false, the project will have no classes.
		 * (default: true)
		 */
		public boolean loadCustomClasses = true;

		/**
		 * whether properties should be loaded.
		 * if false, the project's properties will be blank.
		 * (default: true)
		 */
		public boolean loadProperties = true;

		// TODO celestialgdx - enum support?
	}

	@Override
	public TiledProject load(String path, Parameters parameter, AssetLoadingContext<TiledProject> ctx) throws Exception {
		char[] data = resolve(path).readString().toCharArray();
		return ctx.awaitWork(() -> {
			return load(parameter != null ? parameter : new Parameters(), data);
		});
	}

	private TiledProject load(Parameters parameter, char[] data) {
		JsonValue json = new JsonReader().parse(data, 0, data.length);

		Map<String, Iterable<TiledLoaderUtils.ProjectClassMember>> classes;
		MapProperties properties;

		if(parameter.loadCustomClasses) {
			classes = loadClasses(json.get("propertyTypes"));
		} else {
			classes = Map.of();
		}
		if(parameter.loadProperties) {
			properties = loadProperties(classes, json.get("properties"));
		} else {
			properties = new MapProperties();
		}

		return new TiledProject(classes, properties);
	}

	private Map<String, Iterable<TiledLoaderUtils.ProjectClassMember>> loadClasses(JsonValue json) {
		if(json == null) return Map.of();

		var result = new HashMap<String, Iterable<TiledLoaderUtils.ProjectClassMember>>();

		for(JsonValue type : json) {
			if(!"class".equals(type.getString("type"))) {
				continue;
			}
			String className = type.getString("name");
			JsonValue membersJson = type.get("members");
			if(membersJson.isEmpty()) {
				continue;
			}

			var members = new ArrayList<TiledLoaderUtils.ProjectClassMember>();
			for(JsonValue member : membersJson) {
				members.add(new TiledLoaderUtils.ProjectClassMember(
						member.getString("name"),
						member.getString("type"),
						member.getString("propertyType", null),
						member.get("value")
				));
			}
			result.put(className, members);
		}
		return result;
	}

	private MapProperties loadProperties(
			Map<String, Iterable<TiledLoaderUtils.ProjectClassMember>> classes,
			JsonValue json
	) {
		if(json == null) return new MapProperties();

		MapProperties result = new MapProperties();
		TiledLoaderUtils.loadProperties(
				result,
				MapElement.json(json),
				classes::get
		);
		return result;
	}
}