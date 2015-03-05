package de.bayerl.statistics.transformer;


import de.bayerl.statistics.model.Cell;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DeepCopyUtil {

    public Cell deepCopy(Cell cell) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(cell);
            oos.close();

            final ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(baos.toByteArray()));
            final Cell clone = (Cell) ois.readObject();

            return clone;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cloning failed");
        }
    }
}
