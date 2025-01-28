package task11;

import java.util.LinkedList;
import java.util.Queue;

public class ScalableThreadPool implements ThreadPool {
  private final int minThreads;
  private final int maxThreads;
  private final Queue<Runnable> taskQueue = new LinkedList<>();
  private final LinkedList<Worker> workers = new LinkedList<>();
  private volatile boolean isRunning = true;

  public ScalableThreadPool(int minThreads, int maxThreads) {
    if (minThreads <= 0 || maxThreads < minThreads) {
      throw new IllegalArgumentException("Invalid thread count range");
    }
    this.minThreads = minThreads;
    this.maxThreads = maxThreads;
  }

  @Override
  public void start() {
    synchronized (workers) {
      for (int i = 0; i < minThreads; i++) {
        Worker worker = new Worker();
        workers.add(worker);
        worker.start();
      }
    }
  }

  @Override
  public void execute(Runnable runnable) {
    synchronized (taskQueue) {
      taskQueue.add(runnable);
      taskQueue.notify();
    }

    synchronized (workers) {
      if (workers.size() < maxThreads && taskQueue.size() > workers.size()) {
        Worker worker = new Worker();
        workers.add(worker);
        worker.start();
      }
    }
  }

  public void shutdown() {
    isRunning = false;
    synchronized (workers) {
      for (Worker worker : workers) {
        worker.interrupt();
      }
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

        synchronized (workers) {
          if (taskQueue.isEmpty() && workers.size() > minThreads) {
            workers.remove(this);
            break;
          }
        }
      }
    }
  }
}
