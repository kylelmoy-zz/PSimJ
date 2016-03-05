package psimj.network;

class SerializedClassLoader extends ClassLoader {
	public SerializedClassLoader(ClassLoader parent) {
		super(parent);
	}

	@SuppressWarnings("rawtypes")
	public Class loadClass(SerializedClass type) throws ClassNotFoundException {
		return defineClass(type.getName(), type.getData(), 0, type.getData().length);
	}
}