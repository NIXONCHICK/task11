package task11;

public class Main {
  public static void main(String[] args) {
    System.out.println("Тест FixedThreadPool");
    ThreadPool fixedThreadPool = new FixedThreadPool(3);
    fixedThreadPool.start();

    for (int i = 1; i <= 10; i++) {
      int taskNumber = i;
      fixedThreadPool.execute(() -> {
        System.out.println("FixedThreadPool выполняет задачу " + taskNumber + " потоком " + Thread.currentThread().getName());
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    fixedThreadPool.shutdown();
    System.out.println("Работа пула завершена.");

    System.out.println("Тест ScalableThreadPool");
    ThreadPool scalableThreadPool = new ScalableThreadPool(2, 5);
    scalableThreadPool.start();

    for (int i = 1; i <= 12; i++) {
      int taskNumber = i;
      scalableThreadPool.execute(() -> {
        System.out.println("ScalableThreadPool выполняет задачу " + taskNumber + " потоком " + Thread.currentThread().getName());
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }

    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    scalableThreadPool.shutdown();
    System.out.println("Работа пула завершена.");
  }
}
