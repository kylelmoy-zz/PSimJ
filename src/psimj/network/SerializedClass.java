package psimj.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Serializes Classes for network communication
 * 
 * @author Kyle Moy
 *
 */
public class SerializedClass implements Serializable {
	private static final long serialVersionUID = -5882189879957417026L;
	private byte[] data;
	private String name;

	/**
	 * Constructs a SerializedClass from the specified Class already loaded in
	 * the JVM
	 * 
	 * @param type
	 *            the Class to serialize
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	public SerializedClass(Class type) throws IOException, ClassNotFoundException {
		String resourceName = "/" + type.getName().replace(".", "/");
		InputStream is = type.getClass().getResourceAsStream(resourceName + ".class");
		if (is == null) {
			throw new ClassNotFoundException((resourceName + ".class") + " not found.");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		while (true) {
			int r = is.read(buffer);
			if (r == -1)
				break;
			out.write(buffer, 0, r);
		}
		this.name = type.getName();
		this.data = out.toByteArray();
	}

	/**
	 * @return the name of the Class
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the compiled bytecode of the Class
	 */
	public byte[] getData() {
		return data;
	}
}
