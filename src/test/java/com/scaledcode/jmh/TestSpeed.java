package com.scaledcode.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class TestSpeed {
    private static final List<EntityWithResult<TestEntity>> targetList = TestSpeed.gatherItems(25);

    @Benchmark
    @BenchmarkMode(value = {Mode.Throughput, Mode.AverageTime})
    public void testGroupBy(Blackhole blackhole) {
        groupedBy(blackhole);
    }

    @Benchmark
    @BenchmarkMode(value = {Mode.Throughput, Mode.AverageTime})
    public void testSeparateProcessing(Blackhole blackhole) {
        separateProcessing(blackhole);
    }

    @Benchmark
    @BenchmarkMode(value = {Mode.Throughput, Mode.AverageTime})
    public void testManuallyOptimized(Blackhole blackhole) {
        optimizedProcessing(blackhole);
    }

    private void groupedBy(Blackhole blackhole) {
        var resultsGroupedByResult = targetList.stream()
                .collect(Collectors.groupingBy(EntityWithResult::getResult,
                        Collectors.mapping((infoCard) -> infoCard.getEntity().getId(), Collectors.toList())));
        blackhole.consume(resultsGroupedByResult.getOrDefault(ProcessingResult.SUCCESS, Collections.emptyList()));
        blackhole.consume(resultsGroupedByResult.getOrDefault(ProcessingResult.FAILURE, Collections.emptyList()));
    }

    private void separateProcessing(Blackhole blackhole) {
        List<String> failedItems = targetList.stream()
                .filter(infoCard -> infoCard.getResult() == ProcessingResult.FAILURE)
                .map(infoCard -> infoCard.getEntity().getId())
                .collect(Collectors.toList());

        blackhole.consume(failedItems);

        List<String> successfullyProcessedItems = targetList.stream()
                .filter(infoCard -> infoCard.getResult() == ProcessingResult.SUCCESS)
                .map(infoCard -> infoCard.getEntity().getId())
                .collect(Collectors.toList());

        blackhole.consume(successfullyProcessedItems);
    }

    private static void optimizedProcessing(Blackhole blackhole) {
        List<String> failedItems = new LinkedList<>();
        List<String> successfullyProcessedItems = new LinkedList<>();

        for(int i=0; i<targetList.size(); i++) {
            if (targetList.get(i).getResult() == ProcessingResult.SUCCESS) {
                successfullyProcessedItems.add(targetList.get(i).getEntity().getId());
            } else {
                failedItems.add(targetList.get(i).getEntity().getId());
            }
        }

        blackhole.consume(successfullyProcessedItems);
        blackhole.consume(failedItems);
    }

    public static List<EntityWithResult<TestEntity>> gatherItems(int itemsList) {
        List<EntityWithResult<TestEntity>> items = new ArrayList<>();

        for (int i=0; i<itemsList; i++) {
            EntityWithResult<TestEntity> item = new EntityWithResult<>(new TestEntity("somestring"), random.nextBoolean() ? ProcessingResult.SUCCESS : ProcessingResult.FAILURE);

            items.add(item);
        }

        return items;
    }
}
