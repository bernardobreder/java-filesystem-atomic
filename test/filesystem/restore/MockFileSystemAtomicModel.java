package filesystem.restore;

import java.io.IOException;
import java.nio.file.Path;

import filesystem.restore.FileSystemAtomic.FileSystemAtomicModel;

public class MockFileSystemAtomicModel implements FileSystemAtomicModel {

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] read(Path path) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(Path path, byte[] bytes) throws IOException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasBackup() throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] readBackup() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeBackup(byte[] bytes) throws IOException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean deleteBackup() {
    // TODO Auto-generated method stub
    return false;
  }

}
