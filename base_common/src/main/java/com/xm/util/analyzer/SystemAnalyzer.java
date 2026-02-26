package com.xm.util.analyzer;

import lombok.Getter;
import lombok.ToString;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;


public final class SystemAnalyzer {
    // 用于同步非线程安全操作的锁对象
    private static final Object GC_LOCK = new Object();
    // 线程安全的调用计数器
    private static final AtomicLong ANALYSIS_COUNTER = new AtomicLong(0);

    private SystemAnalyzer() {
        throw new AssertionError("SystemAnalyzer is a utility class and cannot be instantiated");
    }

    // ========== 对象内存分析 ==========

    @Getter
    @ToString
    public static final class MemoryAnalysis {
        private final long shallowSize;
        private final long deepSize;
        private final long headerSize;
        private final boolean compressedOops;
        private final String layout;
        private final String graph;

        public MemoryAnalysis(Object obj) {
            ClassLayout classLayout = ClassLayout.parseInstance(obj);
            GraphLayout graphLayout = GraphLayout.parseInstance(obj);
            this.shallowSize = classLayout.instanceSize();
            this.deepSize = graphLayout.totalSize();
            this.graph = graphLayout.toFootprint();
            this.headerSize = classLayout.headerSize();
            this.compressedOops = isCompressedOopsEnabled();
            this.layout = classLayout.toPrintable();

            // 更新调用计数器
            ANALYSIS_COUNTER.incrementAndGet();
        }

        /**
         * 检查是否启用压缩指针（无状态方法，无需同步）
         */
        public boolean isCompressedOopsEnabled() {
            // 更准确的检查方式 [1](@ref)
            return VM.current().details().contains("+UseCompressedOops");
        }
    }

    /**
     * 安全的内存分析方法
     */
    public static MemoryAnalysis analyzeMemory(Object obj) {
        return new MemoryAnalysis(obj);
    }

    /**
     * 格式化字节大小（无状态方法，无需同步）
     */
    public static String formatBytes(long bytes, boolean binaryUnits) {
        if (bytes < 0) {
            return "N/A";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }

        int base = binaryUnits ? 1024 : 1000;
        String[] units = binaryUnits
                ? new String[]{"KiB", "MiB", "GiB", "TiB", "PiB", "EiB"}
                : new String[]{"KB", "MB", "GB", "TB", "PB", "EB"};

        int exp = (int) (Math.log(bytes) / Math.log(base));
        exp = Math.min(exp, units.length);

        BigDecimal size = new BigDecimal(bytes)
                .divide(new BigDecimal(Math.pow(base, exp)), 2, RoundingMode.HALF_UP);
        String unit = units[exp - 1];

        return size.stripTrailingZeros().toPlainString() + " " + unit;
    }

    // ========== 项目内存分析 ==========

    /**
     * 项目内存汇总结果
     */
    @Getter
    @ToString
    public static final class ProjectMemorySummary {
        private final long totalUsedMemory;
        private final long totalFreeMemory;
        private final long totalMemory;
        private final long maxMemory;
        private final int objectCount;
        private final Map<String, Long> memoryByClass;
        private final long analysisCount;

        /**
         * 默认构造函数，不触发显式GC
         */
        public ProjectMemorySummary() {
            this(false); // 默认不触发显式GC
        }

        /**
         * 可选的构造函数，允许控制是否触发GC（仅供调试）
         * @param allowExplicitGC 是否允许显式GC（可能影响性能）
         */
        public ProjectMemorySummary(boolean allowExplicitGC) {
            if (allowExplicitGC) {
                // 添加警告信息，并确保线程安全 [6](@ref)
                System.err.println("Warning: Explicit GC may affect application performance. Use for debugging only.");
                performSafeGC();
            }

            Runtime runtime = Runtime.getRuntime();
            this.totalMemory = runtime.totalMemory();
            this.totalFreeMemory = runtime.freeMemory();
            this.totalUsedMemory = totalMemory - totalFreeMemory;
            this.maxMemory = runtime.maxMemory();
            this.objectCount = getApproximateObjectCount();
            this.memoryByClass = analyzeMemoryDistribution();
            this.analysisCount = ANALYSIS_COUNTER.get();
        }

        /**
         * 线程安全的GC执行
         */
        private static void performSafeGC() {
            synchronized (GC_LOCK) {
                System.gc();
                try {
                    // 短暂的等待，但不依赖其结果准确性
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        /**
         * 生成汇总报告
         */
        public String generateSummaryReport() {
            StringBuilder report = new StringBuilder();

            report.append("=== 项目内存汇总报告 ===\n");
            report.append(String.format("总内存: %s\n", formatBytes(totalMemory, true)));
            report.append(String.format("已使用: %s (%.1f%%)\n",
                    formatBytes(totalUsedMemory, true), totalUsedMemory * 100.0 / totalMemory));
            report.append(String.format("空闲内存: %s\n", formatBytes(totalFreeMemory, true)));
            report.append(String.format("最大内存: %s\n", formatBytes(maxMemory, true)));
            report.append(String.format("可用最大内存: %s\n",
                    formatBytes(maxMemory - totalMemory + totalFreeMemory, true)));
            report.append(String.format("内存分析调用次数: %d\n", analysisCount));

            // 内存池信息（懒加载实现）
            try {
                report.append("\n--- 内存池详情 ---\n");
                getMemoryPoolInfo().forEach((name, usage) -> {
                    report.append(String.format("%s: %s\n", name, usage));
                });
            } catch (Exception e) {
                report.append("内存池信息获取失败: ").append(e.getMessage()).append("\n");
            }

            return report.toString();
        }

        // 懒加载的内存池信息
        private volatile Map<String, String> memoryPoolInfoCache;

        /**
         * 获取内存池信息（懒加载）
         */
        public Map<String, String> getMemoryPoolInfo() {
            if (memoryPoolInfoCache == null) {
                synchronized (this) {
                    if (memoryPoolInfoCache == null) {
                        memoryPoolInfoCache = Collections.unmodifiableMap(generateMemoryPoolInfo());
                    }
                }
            }
            return memoryPoolInfoCache;
        }

        private Map<String, String> generateMemoryPoolInfo() {
            Map<String, String> info = new TreeMap<>();
            try {
                List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
                for (MemoryPoolMXBean pool : pools) {
                    MemoryUsage usage = pool.getUsage();
                    String usageInfo = String.format("Used: %s, Max: %s, Committed: %s",
                            formatBytes(usage.getUsed(), true),
                            formatBytes(usage.getMax(), true),
                            formatBytes(usage.getCommitted(), true));
                    info.put(pool.getName(), usageInfo);
                }
            } catch (SecurityException e) {
                info.put("Error", "Insufficient permissions to access memory pool info");
            } catch (Exception e) {
                info.put("Error", "Failed to get memory pool info: " + e.getMessage());
            }
            return info;
        }

        /**
         * 获取近似对象计数（明确说明局限性）[3](@ref)
         */
        private int getApproximateObjectCount() {
            // 诚实地说明这是估算值，实际实现需要专业工具
            try {
                // 返回加载的类数量作为近似值（这实际上不是对象实例数）
                return ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
            } catch (Exception e) {
                return -1; // 表示不可用
            }
        }

        /**
         * 分析内存分布（明确功能边界）[2](@ref)
         */
        private Map<String, Long> analyzeMemoryDistribution() {
            Map<String, Long> distribution = new TreeMap<>();

            // 明确说明当前实现只能获取内存池级别的分布
            try {
                List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
                for (MemoryPoolMXBean pool : pools) {
                    distribution.put("Pool: " + pool.getName(), pool.getUsage().getUsed());
                }
                // 添加说明信息
                distribution.put("Note", -1L); // 使用特殊值标记说明文本
            } catch (Exception e) {
                distribution.put("Analysis Failed", -1L);
            }

            return Collections.unmodifiableMap(distribution);
        }
    }


    /**
     * 获取项目内存汇总信息（调试用，可触发GC）
     */
    public static ProjectMemorySummary getProjectMemorySummary(boolean allowExplicitGC) {
        return new ProjectMemorySummary(allowExplicitGC);
    }

    /**
     * 获取总分析次数（线程安全）[6](@ref)
     */
    public static long getTotalAnalysisCount() {
        return ANALYSIS_COUNTER.get();
    }

    // 测试方法
    public static void main(String[] args) {
        System.out.println("=== 系统资源分析工具测试 ===\n");

        // 测试内存分析
        testMemoryAnalysis();

        // 测试内存汇总（不触发GC）
        System.out.println("\n=== 内存汇总测试（无显式GC）===");
        ProjectMemorySummary summary1 = SystemAnalyzer.getProjectMemorySummary(false);
        System.out.println(summary1.generateSummaryReport());

        // 测试内存汇总（触发GC，仅供调试）
        System.out.println("\n=== 内存汇总测试（有显式GC）- 调试用 ===");
        ProjectMemorySummary summary2 = SystemAnalyzer.getProjectMemorySummary(true);
        System.out.println(summary2.generateSummaryReport());

        System.out.println("总内存分析调用次数: " + getTotalAnalysisCount());
    }

    private static void testMemoryAnalysis() {
        System.out.println("=== 内存分析测试 ===");

        // 测试简单对象
        String str = "Hello, SystemAnalyzer!";
        MemoryAnalysis strAnalysis = SystemAnalyzer.analyzeMemory(str);
        System.out.println("String对象内存分析:");
        System.out.println("Shallow Size: " + formatBytes(strAnalysis.getShallowSize(), true));
        System.out.println("Deep Size: " + formatBytes(strAnalysis.getDeepSize(), true));
        System.out.println("Compressed Oops: " + strAnalysis.isCompressedOops());

        // 测试集合对象
        List<Integer> list = java.util.Arrays.asList(1, 2, 3, 4, 5);
        MemoryAnalysis listAnalysis = SystemAnalyzer.analyzeMemory(list);
        System.out.println("\nArrayList内存分析:");
        System.out.println("Shallow Size: " + formatBytes(listAnalysis.getShallowSize(), true));
        System.out.println("Deep Size: " + formatBytes(listAnalysis.getDeepSize(), true));
    }
}