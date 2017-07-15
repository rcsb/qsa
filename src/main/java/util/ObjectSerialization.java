package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ObjectSerialization<T> {

	private final Kryo kryo = new Kryo();
	private final File file;

	public ObjectSerialization(File file) {
		this.file = file;
	}

	public void serialize(T o) {
		try {
			Output output = new Output(new FileOutputStream(file, true));
			kryo.writeClassAndObject(output, o);
			output.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public T deserialize() {
		try {
			Input input = new Input(new FileInputStream(file));
			Object o = kryo.readClassAndObject(input);
			input.close();
			return (T) o;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

	}
}
