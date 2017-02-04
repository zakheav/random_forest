package util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ThreadPool {// 需要改进，让线程池可以按批次完成任务
	private int SIZE = 100;
	private List<Thread> pool;
	private Queue<Runnable> tasks;
	private static ThreadPool instance = new ThreadPool();

	private ThreadPool() {
		pool = new ArrayList<Thread>();
		tasks = new LinkedList<Runnable>();
		for (int i = 0; i < SIZE; ++i) {
			add_labour(new Worker());
		}
	}

	public static ThreadPool get_instance() {
		return instance;
	}

	class Worker extends Thread {// 正式员工线程
		public void run() {
			while (true) {
				Runnable task;
				synchronized (tasks) {
					while (tasks.isEmpty()) {
						try {
							tasks.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					task = tasks.poll();
				}
				task.run();
			}
		}
	}

	private void add_labour(Thread t) {
		synchronized (this.pool) {
			if (pool.size() < SIZE) {
				pool.add(t);
				pool.get(pool.size() - 1).start();
			}
		}
	}

	public void add_tasks(Runnable task) {
		synchronized (tasks) {
			tasks.offer(task);
			tasks.notify();
		}
	}

}
