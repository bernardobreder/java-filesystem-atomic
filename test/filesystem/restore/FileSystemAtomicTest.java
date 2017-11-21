package filesystem.restore;

import java.util.HashMap;

import org.junit.Test;

import filesystem.restore.FileSystemAtomic.FileSystemAtomicException;

public class FileSystemAtomicTest {

  @Test
  public void test() throws FileSystemAtomicException {
    FileSystemAtomic fs = new FileSystemAtomic(new MockFileSystemAtomicModel(),
      (a, b) -> {
      });
    fs.begin();
    fs.commit(new HashMap<>());
  }

}
