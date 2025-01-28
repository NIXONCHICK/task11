package task11;

import java.util.LinkedList;
import java.util.Queue;

public class FixedThreadPool implements ThreadPool {
  private final int threadCount;
  private final Queue<Runnable> taskQueue = new LinkedList<>();
  private final Worker[] workers;
  private volatile boolean isRunning = true;

  public FixedThreadPool(int threadCount) {
    this.threadCount = threadCount;
    this.workers = new Worker[threadCount];
  }

  @Override
  public void start() {
    for (int i = 0; i < threadCount; i++) {
      workers[i] = new Worker();
      workers[i].start();
    }
  }

  @Override
  public void execute(Runnable runnable) {
    synchronized (taskQueue) {
      taskQueue.add(runnable);
      taskQueue.notify();
    }
  }

  public void shutdown() {
    isRunning = false;
    for (Worker worker : workers) {
      worker.interrupt();
    }
  }

  private class Worker extends Thread {
    @Override
    public void run() {
      while (isRunning) {
        Runnable task;
        synchronized (taskQueue) {
          while (taskQueue.isEmpty() && isRunning) {
            try {
              taskQueue.wait();
            } catch (InterruptedException e) {
              return;
            }
          }
          task = taskQueue.poll();
        }
        if (task != null) {
          try {
            task.run();
          } catch (RuntimeException e) {
            System.err.println("Ошибка выполнения задачи: " + e.getMessage());
          }
        }
      }
    }
  }
}
