package com.badlogic.gdx.maps;

import com.badlogic.gdx.maps.loader.TiledLoaderUtils;
import com.badlogic.gdx.maps.loader.TiledLoaderUtils.ClassSupplier;
import com.badlogic.gdx.utils.Null;

import java.util.Map;
import java.util.Objects;

/**
 * celestialgdx - a tiled project file. contains class definitions and properties
 */
public class TiledProject implements ClassSupplier {
	private final Map<String, Iterable<TiledLoaderUtils.ProjectClassMember>> classes;
	private final MapProperties properties;

	public TiledProject(
			Map<String, Iterable<TiledLoaderUtils.ProjectClassMember>> classes,
			MapProperties properties
	) {
		Objects.requireNonNull(classes);
		Objects.requireNonNull(properties);
		this.classes = classes;
		this.properties = properties;
	}

	@Override
	@Null
	public Iterable<TiledLoaderUtils.ProjectClassMember> getClassMembers(String className) {
		return classes.get(className);
	}

	public MapProperties getProperties() {
		return properties;
	}
}