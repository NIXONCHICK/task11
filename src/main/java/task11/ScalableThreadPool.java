package task11;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScalableThreadPool implements ThreadPool {

  private final int minThreads;
  private final int maxThreads;
  private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
  private final LinkedList<Worker> workers = new LinkedList<>();
  private final Object workersLock = new Object();
  private volatile boolean isShutdown = false;
  private final AtomicInteger activeCount = new AtomicInteger(0);

  public ScalableThreadPool(int minThreads, int maxThreads) {
    if (minThreads <= 0 || maxThreads < minThreads) {
      throw new IllegalArgumentException("Invalid thread count range");
    }
    this.minThreads = minThreads;
    this.maxThreads = maxThreads;
  }

  @Override
  public void start() {
    synchronized (workersLock) {
      for (int i = 0; i < minThreads; i++) {
        Worker worker = new Worker();
        workers.add(worker);
        worker.start();
      }
    }
  }

  @Override
  public void execute(Runnable runnable) {
    if (isShutdown) {
      throw new IllegalStateException("ThreadPool is shut down");
    }
    taskQueue.offer(runnable);
    synchronized (workersLock) {
      if (workers.size() < maxThreads && !taskQueue.isEmpty()
          && activeCount.get() == workers.size()) {
        Worker worker = new Worker();
        workers.add(worker);
        worker.start();
      }
    }
  }

  @Override
  public void shutdown() {
    isShutdown = true;
  }

  public List<Runnable> shutdownNow() {
    isShutdown = true;
    List<Runnable> notExecutedTasks = new ArrayList<>();
    taskQueue.drainTo(notExecutedTasks);
    synchronized (workersLock) {
      for (Worker worker : workers) {
        worker.interrupt();
      }
    }
    return notExecutedTasks;
  }

  private class Worker extends Thread {
    @Override
    public void run() {
      try {
        while (true) {
          if (isShutdown && taskQueue.isEmpty()) {
            break;
          }

          Runnable task;
          try {
            task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
          } catch (InterruptedException e) {
            if (isShutdown) {
              break;
            }
            continue;
          }

          if (task != null) {
            activeCount.incrementAndGet();
            try {
              task.run();
            } catch (RuntimeException e) {
              System.err.println("Ошибка выполнения задачи: " + e.getMessage());
            } finally {
              activeCount.decrementAndGet();
            }
          } else {
            synchronized (workersLock) {
              if (!isShutdown && taskQueue.isEmpty() && workers.size() > minThreads) {
                workers.remove(this);
                break;
              }
            }
          }
        }
      } finally {
        synchronized (workersLock) {
          workers.remove(this);
        }
      }
    }
  }
}
