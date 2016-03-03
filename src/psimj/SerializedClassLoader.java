package psimj;

class SerializedClassLoader extends ClassLoader {
    public SerializedClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class loadClass(SerializedClass type) throws ClassNotFoundException {
    	return defineClass(type.getName(), type.getData(), 0, type.getData().length);
    }
}