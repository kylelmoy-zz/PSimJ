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
class SerializedClass implements Serializable {
	private static final long serialVersionUID = -5882189879957417026L;
	private byte[] data;
	private String name;
	
	/**
	 * Constructs a SerializedClass from the specified Class already loaded in the JVM
	 * @param type the Class to serialize
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	public SerializedClass(Class type) throws IOException, ClassNotFoundException {
		InputStream is = type.getResourceAsStream(type.getName() + ".class");
		if (is == null) {
			throw new ClassNotFoundException(type.getName() + " is not visible. Task must be public and static.");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		while (true) {
			int r = is.read(buffer);
			if (r == -1)
				break;
			out.write(buffer, 0, r);
		}
		
		this.data = out.toByteArray();
		this.name = type.getName();
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
