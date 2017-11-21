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
import java.util.Set;
import java.util.function.BiConsumer;

public class FileSystemAtomic {

  private final FileSystemAtomicModel model;

  private final BiConsumer<Path, byte[]> reload;

  public FileSystemAtomic(FileSystemAtomicModel model,
    BiConsumer<Path, byte[]> reload) {
    this.model = model;
    this.reload = reload;
  }

  public void begin() throws FileSystemAtomicException {
    try {
      if (model.hasBackup()) {
        Map<Path, byte[]> readBackupBytes = readBackupBytes(model.readBackup());
        writeContents(readBackupBytes);
        if (!model.deleteBackup()) {
          throw new FileSystemAtomicException(
            "can not initialize transaction because a backup file exists");
        }
        for (Entry<Path, byte[]> entry : readBackupBytes.entrySet()) {
          reload.accept(entry.getKey(), entry.getValue());
        }
      }
    }
    catch (IOException e) {
      throw new FileSystemAtomicException(e);
    }
  }

  /**
   * @param contents
   * @throws FileSystemAtomicException
   */
  public void commit(Map<Path, byte[]> contents)
    throws FileSystemAtomicException {
    try {
      model.writeBackup(writeBackupBytes(contents.keySet()));
      writeContents(contents);
      if (!model.deleteBackup()) {
        throw new FileSystemAtomicException("backup file can not be deleted");
      }
    }
    catch (IOException e) {
      try {
        writeContents(contents);
        model.deleteBackup();
      }
      catch (IOException ex) {
        e = new IOException(ex.getMessage(), ex);
      }
      throw new FileSystemAtomicException(e);
    }
  }

  protected void writeContents(Map<Path, byte[]> contents) throws IOException {
    for (Entry<Path, byte[]> entry : contents.entrySet()) {
      model.write(entry.getKey(), entry.getValue());
    }
  }

  protected Map<Path, byte[]> readBackupBytes(byte[] backup)
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

  protected byte[] writeBackupBytes(Set<Path> paths) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream out = new DataOutputStream(bytes)) {
      out.writeInt(paths.size());
      for (Path path : paths) {
        byte[] readed = model.read(path);
        out.writeUTF(path.toString());
        out.writeInt(readed.length);
        out.write(readed);
      }
    }
    return bytes.toByteArray();
  }

  public interface FileSystemAtomicModel {

    public byte[] read(Path path) throws IOException;

    public void write(Path path, byte[] bytes) throws IOException;

    public boolean hasBackup() throws IOException;

    public byte[] readBackup() throws IOException;

    public void writeBackup(byte[] bytes) throws IOException;

    public boolean deleteBackup();

  }

  public static class FileSystemAtomicException extends Exception {

    public FileSystemAtomicException(Exception e) {
      super(e);
    }

    public FileSystemAtomicException(String message) {
      super(message);
    }

  }

}
