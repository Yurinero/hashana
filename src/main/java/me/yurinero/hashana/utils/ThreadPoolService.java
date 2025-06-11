/*
 * Hashana - A desktop utility for hashing and password generation.
 * Copyright (C) 2025 Yurinero <https://github.com/Yurinero>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.yurinero.hashana.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.*;

/* Implementation of a Singleton Thread Pool Service I was recommended.
*  While the global state is said to be a con it should serve my needs well, especially in the File Hash operation.
*/

public class ThreadPoolService {
	private  static final Logger logger = LoggerFactory.getLogger(ThreadPoolService.class);
	//Singleton instance
	private static final ThreadPoolService instance = new ThreadPoolService();

	//Thread pool
	private final ExecutorService executorService;

	//Constructor
	private ThreadPoolService() {
		executorService = Executors.newFixedThreadPool(
				Math.max(2, Runtime.getRuntime().availableProcessors() - 1)
		);
	}

	//Singleton instance getter
	public static ThreadPoolService getInstance() {
		return instance;
	}

	//Executor service getter
	public ExecutorService getExecutorService() {
		return executorService;
	}

	//Shutdown method, gives services 800ms to finish their tasks before forcefully shutting them down.
	public void shutdown() {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
				List<Runnable> pendingTasks = executorService.shutdownNow();
				logger.info("Forced shutdown of {} tasks", pendingTasks.size());
			}
		}catch (InterruptedException e) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
			logger.error("Interrupted while shutting down", e);
		}
	}

	//Submits a task to the pool through executor.submit
	public <T> Future<T> submit(Callable<T> task) {
		return executorService.submit(() -> {
			try {
				return task.call();
			}catch (Exception e) {
				logger.error("Task execution failed", e);
				throw e;
			}
		});
	}
}
