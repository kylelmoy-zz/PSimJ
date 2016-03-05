package psimj;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveBoxer {
	static Map<Class, Class> boxTypes = new HashMap<Class, Class>();
	static {
		//Map primitives to their boxes
		boxTypes.put(boolean.class, Integer.class);
		boxTypes.put(char.class, Character.class);
		boxTypes.put(byte.class, Byte.class);
		boxTypes.put(short.class, Short.class);
		boxTypes.put(int.class, Integer.class);
		boxTypes.put(long.class, Long.class);
		boxTypes.put(float.class, Float.class);
		boxTypes.put(double.class, Double.class);
		boxTypes.put(void.class, Void.class);
	}
	public static Class get(Class c) {
		return boxTypes.get(c);
	}
}
