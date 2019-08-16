package org.aoju.bus.tracer.config;

import java.util.Arrays;
import java.util.Properties;

public class PropertyChain {

	private final Iterable<Properties> propertiesChain;

	public PropertyChain(Iterable<Properties> propertiesChain) {
		this.propertiesChain = propertiesChain;
	}

	public static PropertyChain build(Properties... properties) {
		return new PropertyChain(Arrays.asList(properties));
	}


	public String getProperty(String key) {
		for (Properties properties : propertiesChain) {
			final String p = properties.getProperty(key);
			if (p != null)
				return p;
		}
		return null;
	}
}
