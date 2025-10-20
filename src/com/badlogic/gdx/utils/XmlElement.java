package com.badlogic.gdx.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * celestialgdx - Wrapper around Java DOM API to make porting easier.
 * Unlike GDX's XmlReader.Element, however, this only represents objects
 * @param delegate the delegate
 */
// if project valhalla comes in my lifetime, this can become a value record
public record XmlElement(Element delegate) {
	private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

	static {
		try {
			FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch(ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public XmlElement {
		Objects.requireNonNull(delegate, "cannot have null delegate");
	}

	public static XmlElement parse(InputStream stream)
			throws ParserConfigurationException, IOException, SAXException {
		return new XmlElement(FACTORY.newDocumentBuilder().parse(stream).getDocumentElement());
	}

	@NotNull
	public String getName() {
		return delegate.getTagName();
	}

	public String getText() {
		return delegate.getTextContent().trim();
	}

	@NotNull
	public String expectAttribute(String key) {
		String value = getAttribute(key, null);
		if(value == null) {
			throw new IllegalArgumentException("Element " + getName() + " doesn't have key " + key);
		}
		return value;
	}

	public String getAttribute(String key) {
		return getAttribute(key, null);
	}

	@Contract("_, !null -> !null")
	public String getAttribute(String key, String def) {
		if(key == null) throw new IllegalArgumentException("missing key");
		Attr attr = delegate.getAttributeNode(key);
		return attr != null ? attr.getValue() : def;
	}

	public int getIntAttribute(String key) {
		return Integer.parseInt(expectAttribute(key));
	}

	public int getIntAttribute(String key, int def) {
		String value = getAttribute(key);
		if(value == null) return def;
		return Integer.parseInt(value);
	}

	public float getFloatAttribute(String key) {
		return Float.parseFloat(expectAttribute(key));
	}

	public float getFloatAttribute(String key, float def) {
		String value = getAttribute(key);
		if(value == null) return def;
		return Float.parseFloat(value);
	}

	@Nullable
	public XmlElement getChildByName(String name) {
		// getElementsByTagName returns for the entire document
		for(Node node = delegate.getFirstChild(); node != null; node = node.getNextSibling()) {
			if(name.equals(node.getNodeName()) && node instanceof Element element) {
				return new XmlElement(element);
			}
		}
		return null;
	}

	@NotNull
	public XmlElement expectChildByName(String name) {
		XmlElement result = getChildByName(name);
		if(result == null) {
			throw new IllegalArgumentException("Element " + getName() + " doesn't have child " + name);
		}
		return result;
	}

	public List<XmlElement> getChildrenByName(String name) {
		return convertElements(delegate.getChildNodes(), node -> name.equals(node.getNodeName()));
	}

	public List<XmlElement> getChildren() {
		return convertElements(delegate.getChildNodes(), null);
	}

	private static List<XmlElement> convertElements(NodeList list, Function<Node, Boolean> filter) {
		int length = list.getLength();
		List<XmlElement> result = new ArrayList<>(length);
		for(int i = 0; i < length; i++) {
			Node node = list.item(i);
			if(filter != null && !filter.apply(node)) continue;
			if(!(node instanceof Element element)) continue;

			result.add(new XmlElement(element));
		}
		return result;
	}
}