package cn.com.sina.alan.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 并发执行任务工具类
 * Created by 王鸿飞 (hongfei7@staff.sina.com.cn)  
 * 8/27/16.
 */
public class ConcurrentUtils {
    private static Logger log = LoggerFactory.getLogger(ConcurrentUtils.class);
    /**
     * 默认超时值
     */
    public static final int TIMEOUT = 1000 * 10;
    /**
     * 默认超时值时间单位
     */
    public static final TimeUnit UNIT = TimeUnit.MILLISECONDS;

    /**
     * 线程池默认大小
     */
    public static int THREAD_POOL_SIZE = 20;

    /**
     * 线程池
     */
    public static ExecutorService pool;

    private ConcurrentUtils() {}


    /**
     * 并发执行两个返回结果相同的任务
     * @param task1 并发任务1
     * @param task2 并发任务2
     * @param <T> 并发调用返回结果的类型
     * @throws Exception 并发调用发生错误
     * @return
     */
    public static <T> List<T> concurrentExecuteSame(AlanTask<T> task1, AlanTask<T> task2) throws Exception {
        return concurrentExecuteSame(task1, task2, TIMEOUT, UNIT);
    }

    /**
     * 并发执行两个任务,并调用用户自定义逻辑聚合结果
     *
     * @param task1 并发任务1
     * @param task2 并发任务2
     * @param func 聚合逻辑
     * @param <T1> 并发任务返回结果对象的类型
     * @param <T2> 聚合后的返回类型
     * @return
     * @throws Exception
     */
    public static <T1, T2> T2 concurrentExecuteSame(AlanTask<T1> task1, AlanTask<T1> task2, BiFunction<T1, T1, T2> func) throws Exception {
        return concurrentExecuteSame(task1, task2, func, TIMEOUT, UNIT);
    }


    /**
     * 并发执行两个返回结果相同的任务
     * @param task1 并发任务1
     * @param task2 并发任务2
     * @param timeout 超时时间值
     * @param unit 超时时间值的单位
     * @param <T>
     * @throws Exception 并发调用发生错误
     * @return
     */
    public static <T> List<T> concurrentExecuteSame(AlanTask<T> task1, AlanTask<T> task2, int timeout, TimeUnit unit) throws Exception {
        Future<List<T>> f = CompletableFuture.supplyAsync(
                () -> task1.execute(),
                getPool()
        ).thenCombine( CompletableFuture.supplyAsync(
                () -> task2.execute(),
                getPool()
        ), (r1, r2) -> Arrays.asList(r1, r2));

        return processFuture(f, timeout, unit);
    }

    /**
     * 并发执行两个任务,并调用用户自定义逻辑聚合结果
     * @param task1 并发任务1
     * @param task2 并发任务2
     * @param func 聚合逻辑
     * @param timeout 超时时间值
     * @param unit 超时时间值的单位
     * @param <T1> task1的返回类型
     * @param <T2> task2的返回类型
     * @return
     * @throws Exception
     */
    public static <T1, T2> T2 concurrentExecuteSame(AlanTask<T1> task1, AlanTask<T1> task2, BiFunction<T1, T1, T2> func, int timeout, TimeUnit unit) throws Exception {
        Future<T2> f = CompletableFuture.supplyAsync(
                () -> task1.execute(),
                getPool()
        ).thenCombine( CompletableFuture.supplyAsync(
                () -> task2.execute(),
                getPool()
        ), func);

        return processFuture(f, timeout, unit);
    }

    /**
     * 并发执行多个任务,取最快返回的结果
     *
     * @param tasks 并发任务
     * @param <T> 返回类型
     * @return
     * @throws Exception
     */
    public static <T> T concurrentExecuteRace(AlanTask<T>... tasks) throws Exception {
        return concurrentExecuteRace(TIMEOUT, UNIT, tasks);
    }

    /**
     * 并发执行多个任务,取最快返回的结果
     * @param timeout 超时时间值
     * @param unit 超时时间值的单位
     * @param tasks 并发任务
     * @param <T> 返回类型
     * @return
     * @throws Exception
     */
    public static <T> T concurrentExecuteRace(int timeout, TimeUnit unit, AlanTask<T>... tasks) throws Exception {
        checkTasks(tasks);

        CompletableFuture<T>[] futures = Stream.of(tasks)
                .map( task -> CompletableFuture.supplyAsync( () -> task.execute(), getPool() ) )
                .toArray( CompletableFuture[]::new );

        CompletableFuture f = CompletableFuture.anyOf(futures);
        return (T) processFuture(f, timeout, unit);
    }

    /**
     * 并发执行两个返回类型不同的任务
     * @param task1 并发任务1
     * @param task2 并发任务2
     * @param <T1> task1的返回类型
     * @param <T2> task2的返回类型
     * @return
     * @throws Exception
     */
    public static <T1, T2> List<Object> concurrentExecuteDiff(AlanTask<T1> task1, AlanTask<T2> task2) throws Exception {
        return concurrentExecuteDiff(task1, task2, TIMEOUT, UNIT);
    }

    /**
     * 并发执行两个返回类型不同的任务, 并调用用户逻辑聚合结果
     *
     * @param task1 并发任务1
     * @param task2 并发任务2
     * @param func 聚合逻辑
     * @param <T1> task1的返回类型
     * @param <T2> task2的返回类型
     * @param <T3> 聚合结果后的返回类型
     * @return
     * @throws Exception
     */
    public static <T1, T2, T3> T3 concurrentExecuteDiff(AlanTask<T1> task1, AlanTask<T2> task2, BiFunction<T1, T2, T3> func) throws Exception {
        return concurrentExecuteDiff(task1, task2, func, TIMEOUT, UNIT);
    }

    /**
     * 并发执行两个返回类型不同的任务
     * @param task1 并发任务1
     * @param task2 并发任务2
     * @param timeout 超时时间值
     * @param unit 超时时间值的单位
     * @param <T1> task1的返回类型
     * @param <T2> task2的返回类型
     * @throws Exception 并发调用发生错误
     * @return
     */
    public static <T1, T2> List<Object> concurrentExecuteDiff(AlanTask<T1> task1, AlanTask<T2> task2, int timeout, TimeUnit unit) throws Exception {
        Future<List<Object>> f = CompletableFuture.supplyAsync(
                () -> task1.execute(),
                getPool()
        ).thenCombine( CompletableFuture.supplyAsync(
                () -> task2.execute(),
                getPool()
        ), (r1, r2) -> {
            List<Object> result = new ArrayList<>(2);
            result.add(r1);
            result.add(r2);
            return result;
        } );

        return processFuture(f, timeout, unit);
    }

    /**
     * 并发执行两个返回类型不同的任务, 并调用用户逻辑聚合结果
     *
     * @param task1 并发任务1
     * @param task2 并发任务2
     * @param func 聚合逻辑
     * @param timeout 超时时间值
     * @param unit 超时时间值的单位
     * @param <T1> 并发任务1的返回结果
     * @param <T2> 并发任务2的返回结果
     * @param <T3> 聚合结果
     * @return
     * @throws Exception
     */
    public static <T1, T2, T3> T3 concurrentExecuteDiff(AlanTask<T1> task1, AlanTask<T2> task2, BiFunction<T1, T2, T3> func, int timeout, TimeUnit unit) throws Exception {
        Future<T3> f = CompletableFuture.supplyAsync(
                () -> task1.execute(),
                getPool()
        ).thenCombine( CompletableFuture.supplyAsync(
                () -> task2.execute(),
                getPool()
        ), func);

        return processFuture(f, timeout, unit);
    }

    /**
     * 支持无限多个任务并发执行
     * @param tasks 至少传2个任务
     * @return
     * @throws Exception
     */
    public static List<Object> concurrentExecuteDiffs(AlanTask<Object>... tasks) throws Exception {
        return concurrentExecuteDiffs(TIMEOUT, UNIT, tasks);
    }

    /**
     * 支持无限多个任务并发执行
     *
     * @param timeout 超时时间值
     * @param unit 超时时间值的单位
     * @param tasks 至少传2个任务
     * @return
     * @throws Exception
     */
    public static List<Object> concurrentExecuteDiffs(int timeout, TimeUnit unit, AlanTask<Object>... tasks) throws Exception {
        checkTasks(tasks);

        final int LEN = tasks.length;
        // 投递任务
        List<CompletableFuture<Object>> futures = Arrays.asList(tasks).stream()
                .map( task -> CompletableFuture.supplyAsync( () -> task.execute(), getPool() ) )
                .collect(Collectors.toList());

        // 获取结果
        List<Object> result = new ArrayList<>(LEN);
        for (CompletableFuture<Object> f : futures) {
            result.add(processFuture(f, timeout, unit));
        }

        return result;
    }

    /**
     * 从Future中取出结果并处理异常
     * @param f
     * @param timeout
     * @param unit
     * @param <T>
     * @return
     * @throws Exception
     */
    private static  <T> T processFuture(Future<T> f, int timeout, TimeUnit unit) throws Exception {
        try {
            return f.get(timeout, unit);

        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new Exception(e);

        } catch (ExecutionException e) {
            e.printStackTrace();

            // 如果是业务异常, 则重新抛出
            if (e.getCause() instanceof Exception) {
                throw (Exception)e.getCause();
            } else {
                log.error(e.getMessage());
                throw new Exception(e);
            }

        } catch (TimeoutException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new Exception(e);
        }

    }

    private static void checkTasks(AlanTask... tasks) {
        if (null == tasks) {
            throw new IllegalArgumentException("tasks cannot be null");
        }

        final int LEN = tasks.length;
        if (LEN < 2) {
            throw new IllegalArgumentException("at least 2 tasks");
        }

    }

    private static ExecutorService getPool() {
        if (null == ConcurrentUtils.pool) {
            ConcurrentUtils.pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            log.info("初始化默认线程池 {}, 大小: {}", ConcurrentUtils.pool, THREAD_POOL_SIZE);

        }

        log.debug("使用线程池: {}, 大小: {}", pool, THREAD_POOL_SIZE);
        return ConcurrentUtils.pool;
    }
    

}
