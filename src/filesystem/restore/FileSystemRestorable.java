package filesystem.restore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public interface FileSystemRestorable {

  public default void restore() throws IOException {
    if (hasBackup()) {
      Map<Path, byte[]> contents = readBackupBytes(readBackup());
      for (Entry<Path, byte[]> entry : contents.entrySet()) {
        write(entry.getKey(), entry.getValue());
      }
      if (!deleteBackup()) {
        throw new IOException("can not delete backup file");
      }
    }
  }

  public default void begin() throws IOException {
    if (hasBackup()) {
      throw new IOException(
        "can not initialize transaction because a backup file exists");
    }
  }

  /**
   * @param contents
   * @throws IOException
   */
  public default void commit(Map<Path, byte[]> contents) throws IOException {
    writeBackup(writeBackupBytes(contents));
    for (Entry<Path, byte[]> entry : contents.entrySet()) {
      write(entry.getKey(), entry.getValue());
    }
    if (!deleteBackup()) {
      for (Entry<Path, byte[]> entry : contents.entrySet()) {
        write(entry.getKey(), entry.getValue());
      }
      throw new IOException("backup file can not be deleted");
    }
  }

  public default Map<Path, byte[]> readBackupBytes(byte[] backup)
    throws IOException {
    Map<Path, byte[]> map = new HashMap<>();
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(
      backup))) {
      int size = in.readInt();
      for (int n = 0; n < size; n++) {
        String paths = in.readUTF();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        int readed = in.read(bytes);
        if (readed != length) {
          throw new EOFException();
        }
        map.put(Paths.get(paths), bytes);
      }
    }
    return map;
  }

  public default byte[] writeBackupBytes(Map<Path, byte[]> contents)
    throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream out = new DataOutputStream(bytes)) {
      out.writeInt(contents.size());
      for (Path path : contents.keySet()) {
        byte[] readed = read(path);
        out.writeUTF(path.toString());
        out.writeInt(readed.length);
        out.write(readed);
      }
    }
    return bytes.toByteArray();
  }

  public byte[] read(Path path) throws IOException;

  public void write(Path path, byte[] bytes) throws IOException;

  public boolean hasBackup() throws IOException;

  public byte[] readBackup() throws IOException;

  public void writeBackup(byte[] bytes) throws IOException;

  public boolean deleteBackup();

}
