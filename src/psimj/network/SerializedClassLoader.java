package psimj.network;

/**
 * A wrapper ClassLoader for loading classes from SerializedClass
 * 
 * @author Kyle Moy
 *
 */
class SerializedClassLoader extends ClassLoader {

	/**
	 * Constructs a SerializedClassLoader
	 * 
	 * @param parent
	 */
	public SerializedClassLoader(ClassLoader parent) {
		super(parent);
	}

	/**
	 * Loads a Class from a SerializedClass
	 * 
	 * @param type
	 *            the SerializedClass to load
	 * @return the loaded Class
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	public Class loadClass(SerializedClass type) throws ClassNotFoundException {
		return defineClass(type.getName(), type.getData(), 0, type.getData().length);
	}
}